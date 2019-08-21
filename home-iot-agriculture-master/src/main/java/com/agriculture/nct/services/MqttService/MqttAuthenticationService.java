package com.agriculture.nct.services.MqttService;

import com.agriculture.nct.model.Device;
import com.agriculture.nct.exception.ResourceNotFoundException;
import com.agriculture.nct.security.JwtTokenProvider;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2(topic = "MQTT_AUTHEN")
public class MqttAuthenticationService extends BaseService {
    private final JwtTokenProvider jwtTokenProvider;

    private String authen_pub_topic = "nct_authentication_result_%d";

    @Autowired
    public MqttAuthenticationService(JwtTokenProvider jwtTokenProvider) {
        super(log, "SERVER_AUTHEN_SERVICE_ID", "nct_authentication");
        if (mqttClient.isConnected())
            log.info("Connected. Subscribing the authentication request from devices...");
        try {
            mqttClient.subscribe(subscribeTopic, getQos());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String msg = new String(message.getPayload());
        log.info("Received message: " + msg);
        if (!topic.equals(subscribeTopic)) {
            log.info("Message Arrived not subscribed authentication topic");
            return;
        } else {
            int devid = -1;
            String token = "";
            log.info("Received an authentication request");
            try {
                JSONObject jobj = new JSONObject(msg);
                devid = jobj.getInt("id");
                log.info("DEVICE ID: " + devid);
                token = jobj.getString("user_token");
                log.info("TOKEN: " + token);

            } catch (JSONException e) {
                log.error("Authentication request is not required format");
                e.printStackTrace();
            }

            String status;
            String resultMessage;
            if (jwtTokenProvider.validateToken(token)) {
                int userId = jwtTokenProvider.getIdFromJWT(token);
                int finalDeviceId = devid;
                try {
                    Device device = dbService.getDeviceById(devid).orElseThrow(() -> new ResourceNotFoundException("Device", "id", finalDeviceId));

                    if (device.isAlive()) {
                        // device is collecting. Deny authentication
                        status = "ERROR";
                        resultMessage = "This device is collecting. Stop device's crop for re-authentication.";
                    } else if (device.getCurrentCrop() != 0) {
                        status = "ERROR";
                        resultMessage = "This device has another crop. Stop device's crop for re-authentication.";
                    } else if (dbService.registerDevice(userId, devid)) {
                        log.info("Authentication for device " + devid + " success.");
                        status = "OK";
                        resultMessage = "Token authentication successfully.";
                    } else {
                        // error
                        status = "ERROR";
                        resultMessage = "There is an internal server error.";
                    }
                } catch (ResourceNotFoundException e) {
                    e.printStackTrace();
                    status = "ERROR";
                    resultMessage = "Device not exist.";
                }
            } else {
                log.error("Authentication for device " + devid + " failed");
                status = "ERROR";
                resultMessage = "Token authentication failed.";
            }

            String authen_pub_msg = "{\"status\":\"" + status + "\",\"message\":\"" + resultMessage + "\"}";
            try {
                String authen_pub_topic_id = String.format(authen_pub_topic, devid);
                MqttTopic mqttTopicAuthen = mqttClient.getTopic(authen_pub_topic_id);
                mqttTopicAuthen.publish(authen_pub_msg.getBytes(), 0, false);

                log.info("Published authentication result to " + authen_pub_topic_id);
            } catch (MqttException me) {
                log.error("Send authentication result failed");
                me.printStackTrace();
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    protected int getQos() {
        return 2;
    }

    @Override
    public void setConnOpts() {

    }
}
