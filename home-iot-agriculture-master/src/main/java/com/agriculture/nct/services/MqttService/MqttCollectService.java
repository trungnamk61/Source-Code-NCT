package com.agriculture.nct.services.MqttService;

import com.agriculture.nct.security.JwtTokenProvider;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2(topic = "MQTT_COLLECT")
public class MqttCollectService extends BaseService implements MqttCallback {
    private final JwtTokenProvider jwtTokenProvider;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public MqttCollectService(JwtTokenProvider jwtTokenProvider, SimpMessagingTemplate simpMessagingTemplate) {
        super(log, "SERVER_COLLECT_SERVICE_ID", "nct_collect");
        if (mqttClient.isConnected())
            log.info("Connected. Subscribing the collect data from devices ...");
        try {
            mqttClient.subscribe(subscribeTopic, getQos());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        this.jwtTokenProvider = jwtTokenProvider;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    protected int getQos() {
        return 0;
    }

    @Override
    public void setConnOpts() {
        connOpts.setCleanSession(true);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String msg = new String(message.getPayload());
        log.info("Received message: " + msg);
        if (!topic.equals(subscribeTopic)) {
            log.info("Arrived message do not belong to subscribed collect topic");
            return;
        } else {
            int packet_no;
            double tempVal, humidVal, ecVal, phVal, lightVal;
            String secretKey;
            System.out.println("Received a collect data packet");
            JSONObject jobj;
            //Parse json data packet
            try {
                jobj = new JSONObject(msg);
                packet_no = jobj.getInt("packet_no");
                System.out.println("PACKET_NO: " + packet_no);
                secretKey = jobj.getString("device_token");
                tempVal = jobj.getDouble("temperature");
                System.out.println("TEMPERATURE: " + tempVal);
                humidVal = jobj.getDouble("humidity");
                System.out.println("HUMIDITY: " + humidVal);
                ecVal = jobj.getDouble("EC");
                System.out.println("EC: " + ecVal);
                phVal = jobj.getDouble("pH");
                System.out.println("PH: " + phVal);
                lightVal = jobj.getDouble("light_intensity");
                System.out.println("Light: " + lightVal);

                if (jwtTokenProvider.validateToken(secretKey)) {
                    int deviceId = jwtTokenProvider.getIdFromJWT(secretKey);
                    try {
                        dbService.addSensorData(deviceId, packet_no, tempVal, humidVal, phVal, ecVal, lightVal);
                        log.info("Device " + deviceId + " Sensor data successfully inserted to database");
                    } catch (DataAccessException e) {
                        e.printStackTrace();
                    }
                    jobj.remove("device_token");
                    jobj.put("id", deviceId);
                    simpMessagingTemplate.convertAndSend("/topic/sensor/" + deviceId, jobj.toString());
                }
            } catch (JSONException e) {
                log.error("Collect data packet is wrong format");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}