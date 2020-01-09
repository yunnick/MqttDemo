package com.demo.mqtt.client;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.paho.client.mqttv3.*;

public class SimpleMqttClient {
//    private static final String MQTT_URL = "tcp://test-siot2.stc-seedland.com.cn:1883";
    private static final String MQTT_URL = "tcp://127.0.0.1:1883";
//    private static final String MQTT_URL = "tcp://test-iot-as-mqtt.stc-seedland.com.cn:1883";


    public static void main(String[] args) throws Exception {
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, RandomStringUtils.randomAlphanumeric(10));
        MqttConnectOptions options = new MqttConnectOptions();
//        options.setUserName("PRO_WINCBqIM22KBSMkrn9S1");//PRO_h281D8AuvH32a8ujA6qm
        options.setUserName("ObMRvIIhM3EAdiQLTGCA");


        MqttMessage connMessage = new MqttMessage();
        connMessage.setPayload("{\"connect\":true}".getBytes());
        /**
         * 1、连接设备
         */
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
        Thread.sleep(10000);
        MqttMessage message = new MqttMessage();
        /**
         * 2、创建设备
         */
        String deviceName = "cv_cameraAAA";
        System.out.println("create device");
        message.setPayload(("{\"device\":\""+deviceName+"\",\"type\":\"1\"}").getBytes());
        client.publish("v1/gateway/connect", message);
        Thread.sleep(10000);
        int i = 0;
        long ts = System.currentTimeMillis();
        while (i++ < 20){

                /**
                 * 3、发送属性数据
                 */
            System.out.println("publish attr");
            message.setPayload(("{\""+deviceName+"\":{\"groupId\":\"120\",\"entityType\":\"100\",\"category\":\"11110101010\"}}").getBytes());
            client.publish("v1/gateway/attributes", message);
            /**
             * 4、发送遥测数据
             */
            System.out.println("publish tel");

            message.setPayload(("{\""+deviceName+"\":[{\"ts\":"+ts+",\"values\":{\"ts\":"+ts+",\"version\":\"1.0\",\"count\":"+i+"}}]}").getBytes());
            client.publish("v1/gateway/telemetry", message);

            Thread.sleep(2000);
            System.out.println("send again");
            ts += 10000;
        }
        System.out.println("send finish");
        Thread.sleep(20000);
        client.disconnect();
        System.out.println("Disconnected");
        System.exit(0);
    }

    static void directSend() throws Exception{
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, RandomStringUtils.randomAlphanumeric(10));
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("PRO_ObMRvIIhM3EAdiQLTGCA");

        MqttMessage connMessage = new MqttMessage();
        connMessage.setPayload("{\"connect\":true}".getBytes());
        /**
         * 1、连接设备
         */
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
        Thread.sleep(10000);
        MqttMessage message = new MqttMessage();
                message.setPayload("{\"keyA\":\"simplevalues1\", \"keyB\":true, \"keyC\": 3.0, \"key4\": 4}".getBytes());
        client.publish("v1/devices/me/telemetry", message);
        System.out.println("publish");
        Thread.sleep(2000000);
        client.disconnect();
        System.out.println("Disconnected");
        System.exit(0);
    }
}
