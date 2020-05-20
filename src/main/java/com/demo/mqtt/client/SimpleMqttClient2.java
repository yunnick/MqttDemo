package com.demo.mqtt.client;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.paho.client.mqttv3.*;

public class SimpleMqttClient2 {
//    private static final String MQTT_URL = "tcp://test-siot2.stc-seedland.com.cn:1883";
    private static final String MQTT_URL = "tcp://127.0.0.1:1883";
//    private static final String MQTT_URL = "tcp://140.143.212.101:1883";//siot2
//    private static final String MQTT_URL = "tcp://10.22.62.248:1883";//pi

//    private static final String MQTT_URL = "tcp://test-iot-as-mqtt.stc-seedland.com.cn:1883";


    public static void main(String[] args) throws Exception {
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, RandomStringUtils.randomAlphanumeric(10));
        MqttConnectOptions options = new MqttConnectOptions();
//        options.setUserName("OFpEXYkYDqWi3EPnnT57");//PRO_h281D8AuvH32a8ujA6qm
//        options.setUserName("YYdevGZ6kaXNct0FFdy1");//test service
//        options.setUserName("TUcUIPKYbowXPFx0dB4y");//test device
//        options.setUserName("bUVoaTTqsUC5qxHUtfLI");//pi
        options.setUserName("kZAXTQuYj5pMf7e3wxPN");//local

        MqttMessage connMessage = new MqttMessage();
//        connMessage.setPayload("{\"connect\":true}".getBytes());
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
                exception.printStackTrace();
            }
        });
        while (!client.isConnected()){
            System.out.println("connect...");
            Thread.sleep(1000);
        }
        System.out.println("connected");
        Thread.sleep(1000);
        MqttMessage message = new MqttMessage();
        /**
         * 2、创建设备
         */
        String entityName = "hachi-indoor-infrared-alarm-zg_gdgzlgq6";
        message.setPayload(getDevicePayload(entityName));
        System.out.println("create device");

//        String entityName ="77789";
//        message.setPayload(getServicePayload(entityName));
//        System.out.println("create service");

        client.publish("v1/gateway/connect", message);
        Thread.sleep(2000);
        int i = 0;
        long ts = System.currentTimeMillis();



        long sleepTime = 10000;
        while (i++ < Integer.MAX_VALUE -100 ){
            client.checkPing("a", new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("ping success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("ping fail");
                }
            });

                /**
                 * 3、发送属性数据
                 */
//            System.out.println("publish attr");
            message.setPayload(("{\""+entityName+"\":{\"groupId\":\"0011120\"}}").getBytes());
            client.publish("v1/gateway/attributes", message);
            /**
             * 4、发送遥测数据
             */
//            System.out.println("publish tel");

            message.setPayload(("{\""+ entityName +"\":[{\"ts\":"+ts+"," +
                    "\"values\":{\"method\":\"deviceStateChangeResp\",\"sub_type\":2,\"alarm_severity\":\"1\",\"sub_id\":0," +
                    "\"alarm_category\":\"510100302\",\"main_type\":\"slave\",\"home_id\":\"EDF694079366C4538B85EC705261EB29\"," +
                    "\"action_type\":\"41010004110\",\"category\":\"11510031001\",\"version\":\"1.0\"," +
                    "\"entity_id\":\"34D9B16C1139F04363C6337A60C7260F-slave-2-0\",\"device_name\":\""+ entityName +"\"," +
                    "\"trace_id\":\"958f8940-a890-4d09-8d7e-4a770f739661\",\"trace_time\":\""+ts+"\"}}]}").getBytes());
            client.publish("v1/gateway/telemetry", message);

            Thread.sleep(sleepTime);
            System.out.println("send again" + i);
            ts += sleepTime;


        }
        System.out.println("send finish");
        Thread.sleep(20000);
        client.disconnect();
        System.out.println("Disconnected");
        System.exit(0);
    }

    static byte[] getDevicePayload(String name){
        return ("{\"device\":\""+name+"\",\"type\":\"1\"}").getBytes();
    }

    private static byte[] getServicePayload(String name){
       return  ("{\"service\":\""+name+"\",\"type\":\"1\"}").getBytes();
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
