package com.agriculture.nct.services.MqttService;

import com.agriculture.nct.model.Command;
import com.agriculture.nct.payload.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Log4j2(topic = "MQTT_CONTROL")
public class MqttControlService extends BaseService implements MqttCallback {
    private ObjectMapper mapper = new ObjectMapper();

    public MqttControlService() {
        super(log, "SERVER_CONTROL_SERVICE_ID", null);
        if (mqttClient.isConnected())
            log.info("Connected. Ready to send command to devices");
    }

    @Override
    protected int getQos() {
        return 2;
    }

    @Override
    public void setConnOpts() {
        connOpts.setCleanSession(true);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public ResponseEntity controlDevice(Command command) {
        int deviceId = command.getDeviceId();
        String commandJson;

        try {
            commandJson = mapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(command);
        }
        command.setDone(publish(deviceId, commandJson));
        if (!command.isDone())
            return ResponseEntity.ok().body(new ApiResponse(false, "Failed to send command"));
        dbService.addCommand(command);
        return ResponseEntity.ok().body(new ApiResponse(true, "Successfully send command to " + command.getActuatorName() +
                " of device_" + command.getDeviceId() + " to " + command.getAction() + " for " + command.getParam() + " seconds " + " !"));
    }

    public boolean publish(int deviceId, String commandJson) {
        boolean is_done = false;
        try {
            mqttClient.publish(String.format("nct_control_%d", deviceId), commandJson.getBytes(), getQos(), false);
            log.info("Published to the device_id " + deviceId);
            is_done = true;
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return is_done;
    }
}

