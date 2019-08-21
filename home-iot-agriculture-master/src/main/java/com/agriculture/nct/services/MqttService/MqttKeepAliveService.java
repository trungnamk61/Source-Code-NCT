package com.agriculture.nct.services.MqttService;

import com.agriculture.nct.model.Actuator;
import com.agriculture.nct.security.JwtTokenProvider;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2(topic = "MQTT_KEEP_ALIVE")
public class MqttKeepAliveService extends BaseService implements MqttCallback {
    private List<Integer> activeDevices = new ArrayList<>();
    private final JwtTokenProvider jwtTokenProvider;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public MqttKeepAliveService(JwtTokenProvider jwtTokenProvider, SimpMessagingTemplate simpMessagingTemplate) {
        super(log, "SERVER_KEEP_ALIVE_SERVICE_ID", "nct_keep_alive");
        if (mqttClient.isConnected())
            log.info("Connected. Subscribing the keep alive topic ...");
        try {
            mqttClient.subscribe(subscribeTopic, getQos());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected int getQos() {
        return 2;
    }

    @Override
    public void setConnOpts() {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String msg = new String(message.getPayload());
        log.info("Received message: " + msg);
        if (!topic.equals(subscribeTopic)) {
            log.info("Message Arrived not belong to keep alive topic");
        } else {
            try {
                JSONObject jobj = new JSONObject(msg);
                String token = jobj.getString("device_token");

                Map<Integer, String> actuatorStatus = new HashMap<>();
                actuatorStatus.put(Actuator.PUMP_A, jobj.getString("pump_a_state"));
                actuatorStatus.put(Actuator.PUMP_B, jobj.getString("pump_b_state"));
                actuatorStatus.put(Actuator.PUMP_PH_UP, jobj.getString("pump_ph_up_state"));
                actuatorStatus.put(Actuator.PUMP_PH_DOWN, jobj.getString("pump_ph_down_state"));
                actuatorStatus.put(Actuator.PUMP_WATER, jobj.getString("pump_water_state"));
                actuatorStatus.put(Actuator.LED, jobj.getString("led_state"));
                actuatorStatus.put(Actuator.FAN, jobj.getString("fan_state"));

                if (jwtTokenProvider.validateToken(token)) {
                    int deviceId = jwtTokenProvider.getIdFromJWT(token);
                    log.info("Received a keep alive message for device_" + deviceId);
                    if (!activeDevices.contains(deviceId)) activeDevices.add(deviceId);
                    dbService.updateActuatorStatus(actuatorStatus, deviceId);
                    jobj.remove("device_token");
                    jobj.put("id", deviceId);
                    simpMessagingTemplate.convertAndSend("/topic/actuator/" + deviceId, jobj.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Scheduled(fixedRateString = "${scheduling.keepalive.fixedRate}", initialDelayString = "${scheduling.keepalive.fixedRate}")
    private void keepAlive() {
        List<Integer> devicesIdList = new ArrayList<>();
        if (activeDevices == null || activeDevices.isEmpty()) {
            devicesIdList.add(0);
            log.info("There are no active devices.");
        } else {
            devicesIdList = activeDevices;
            log.info("There are " + activeDevices.size() + " active devices : " + devicesIdList.toString());
        }

        // Restart list
        activeDevices = new ArrayList<>();

        try {
            dbService.updateKeepAlive(devicesIdList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

