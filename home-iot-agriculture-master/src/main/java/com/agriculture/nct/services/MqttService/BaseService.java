package com.agriculture.nct.services.MqttService;

import com.agriculture.nct.database.DBServices;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;

import static com.agriculture.nct.util.AppConstants.SERVER_INSTANCE_ID;

public abstract class BaseService implements MqttCallback {
    private String broker = "tcp://broker.hivemq.com:1883";

    @Autowired
    DBServices dbService;

    MqttConnectOptions connOpts = new MqttConnectOptions();
    MqttClient mqttClient;
    private Logger log;
    String subscribeTopic;

    BaseService(Logger log, String clientId, String subscribeTopic) {
        this.log = log;
        this.subscribeTopic = subscribeTopic;
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqttClient = new MqttClient(broker, clientId + SERVER_INSTANCE_ID, persistence);
            log.info("Connecting to broker: " + broker);
            mqttClient.setCallback(this);
            setConnOpts();
            mqttClient.connect(connOpts);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    protected abstract int getQos();

    public abstract void setConnOpts();

    @Override
    public void connectionLost(Throwable arg0) {
        log.warn("Connection lost");
        while (!mqttClient.isConnected()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                log.info("Trying to connect again");
                mqttClient.connect();
                mqttClient.setCallback(this);
                mqttClient.subscribe(subscribeTopic, getQos());
            } catch (MqttException me) {
                me.printStackTrace();
            }
        }
        log.info("Connected again");
    }
}
