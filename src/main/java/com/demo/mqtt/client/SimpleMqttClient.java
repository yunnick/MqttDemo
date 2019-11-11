package com.demo.mqtt.client;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.paho.client.mqttv3.*;

public class SimpleMqttClient {
    private static final String MQTT_URL = "tcp://mqttserver.com:8883";


    public static void main(String[] args) throws Exception {
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, RandomStringUtils.randomAlphanumeric(10));
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("YHtxAM9wI7y64jjIYa5y");

        MqttMessage connMessage = new MqttMessage();
        connMessage.setPayload("{\"userName\":\"1YHtxAM9wI7y64jjIYa5y\"}".getBytes());
        client.connect(options, connMessage, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                System.out.println("success with msg: " + asyncActionToken.getResponse());
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                System.out.println("fail with msg: " + asyncActionToken.getResponse());
            }
        });
        while (!client.isConnected()){
            System.out.println("connect...");
            Thread.sleep(1000);
        }
        System.out.println("connected");
        MqttMessage message = new MqttMessage();
        message.setPayload("{\"key1\":\"values1\", \"key2\":true, \"key3\": 3.0, \"key4\": 4}".getBytes());
        client.publish("v1/devices/me/telemetry", message);
        System.out.println("publish");
        Thread.sleep(2000);
        client.disconnect();
        System.out.println("Disconnected");
        System.exit(0);
    }
}
