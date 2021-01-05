package com.demo.mqtt.client;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleMultiMqttClient {
    //    private static final String MQTT_URL = "tcp://test-siot2.stc-seedland.com.cn:1883";
    private static final String MQTT_URL = "tcp://10.22.30.36:1883";
//    private static final String MQTT_URL = "tcp://140.143.212.101:1883";//siot2
//    private static final String MQTT_URL = "tcp://10.22.62.202:1883";//pi

//    private static final String MQTT_URL = "tcp://test-iot-as-mqtt.stc-seedland.com.cn:1883";
    static ScheduledExecutorService executorService = null;//Executors.newScheduledThreadPool(6000);
    static ExecutorService es = Executors.newFixedThreadPool(100);

    public static void main(String[] args) throws Exception {
        AtomicInteger connCount = new AtomicInteger(0);
        for (int i = 0; i < 2000; i++){
            final int  idx = i;
            Future f = es.submit(() -> {
                try {
                    doMqtt(idx);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connCount.incrementAndGet();
                return "";
            });
        }
    }

    private static void doMqtt(int i) throws InterruptedException {
        try {
            MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, "mqtclient1-"+RandomStringUtils.randomAlphanumeric(10),
                    new MqttDefaultFilePersistence(),
                    new TimerPingSender(), executorService);
            MqttConnectOptions options = new MqttConnectOptions();
//        options.setUserName("OFpEXYkYDqWi3EPnnT57");//PRO_h281D8AuvH32a8ujA6qm
//        options.setUserName("YYdevGZ6kaXNct0FFdy1");//test service
//        options.setUserName("TUcUIPKYbowXPFx0dB4y");//test device
//            options.setUserName("bUVoaTTqsUC5qxHUtfLI");//pi
        options.setUserName("fkm8EoISNYcXGvuUVOe8");//local
//        options.setUserName("YYdevGZ6kaXNct0FFdy1");//local service


            MqttMessage connMessage = new MqttMessage();
//        connMessage.setPayload("{\"connect\":true}".getBytes());
            /**
             * 1、连接设备
             */
            System.out.println(i+" try to connect");
            client.connect(options, connMessage, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println(i + "success with msg: " + asyncActionToken.getResponse());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.err.println(i + "fail with msg: " + asyncActionToken.getResponse());
                    exception.printStackTrace();
                }
            }).waitForCompletion();
            System.out.println(i+" connected");
            Thread.sleep(1000);
            MqttMessage message = new MqttMessage();
            /**
             * 2、创建设备
             */
//        String entityName = "Guard-32e06bd9a009";
//            String entityName = "ProjectX-02";
//            message.setPayload(getDevicePayload(entityName));
//            System.out.println("create device");

//        String entityName ="77789";
//        message.setPayload(getServicePayload(entityName));
//        System.out.println("create service");
//        Thread.sleep(RandomUtils.nextInt(1000, 100000));
//        client.publish("v1/gateway/connect", message);


//            subAttributeUpdate(entityName, client);
        //get attribute from server
//            waitAttribure(client);

            long ts = System.currentTimeMillis();
//                unsbuAttr(i, client);

//                ping(client);

            /**
             * 3、发送属性数据
             */
//            pubAttr(i, entityName, client);
            /**
             * 4、发送遥测数据
             */
//            Thread.sleep(RandomUtils.nextInt(1000, 100000));
//            sendTelemetry(entityName, client, ts);

            /**
             * 5、获取属性
             */
//                requestAttrubute(entityName, client, i);

//            System.out.println("send finish");
//            Thread.sleep(10000);
//            client.disconnect();
//            System.out.println(i+"Disconnected");
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    private static void pubAttr(int cycle, String entityName, MqttAsyncClient client) throws MqttException {
        MqttMessage message = new MqttMessage();
        //            System.out.println("publish attr");
        message.setPayload(("{\""+entityName+"\":{\"groupId\":\"0011120\",\"confs\":\"confsVa"+cycle+"\"}}").getBytes());
        client.publish("v1/gateway/attributes", message);
    }
    private static void ping(MqttAsyncClient client) throws MqttException {
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
    }

    private static void unsbuAttr(int cycle, MqttAsyncClient client) throws MqttException {
        if (cycle == 3){
            System.out.println("===unsubscribe response");
            client.unsubscribe("v1/gateway/attributes/response");
        }
    }

    private static void subAttributeUpdate(String entityName, MqttAsyncClient client) throws MqttException {

        MqttMessage message = new MqttMessage();
        //get attribute from server
        client.subscribe("v1/gateway/attributes", 1, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                System.out.println("subscribe attribute response");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

            }
        }, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("subscribe messageArrived topic:" + topic + ", message:" + message);
            }
        });
    }

    private static void requestAttrubute(String entityName, MqttAsyncClient client, int count) throws MqttException {
        MqttMessage message = new MqttMessage();

//        message.setPayload(("{\"id\":"+count+",\"device\":\"" +entityName+ "\",\"client\":false,\"keys\":[\"sa2\",\"confs\"]}").getBytes());
//        String payload = "{\"id\":"+count+",\"device\":\"" +entityName+ "\",\"keys\":[\"device_in\",\"sa2\"]}";
        String payload = "{\"id\":"+count+",\"device\":\"" +entityName+ "\"}";
//        System.out.println(payload);
        message.setPayload(payload.getBytes());
//        message.setPayload(("{\"id\":"+count+",\"device\":\"" +entityName+ "\"}").getBytes());
        client.publish("v1/gateway/attributes/request", message);
    }

    private static void waitAttribure(MqttAsyncClient client) throws MqttException {
        client.subscribe("v1/gateway/attributes/response", 1, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                System.out.println("get attribute response");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

            }
        }, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("messageArrived topic:" + topic + ", message:" + message);
            }
        });
    }

    private static void sendTelemetry(String entityName, MqttAsyncClient client, long ts) throws MqttException {
        MqttMessage message = new MqttMessage();
        System.out.println("publish tel");

        message.setPayload(("{\""+entityName+"\":[{\"ts\":"+ts+",\"values\":{\"version\":\"1.0\",\"feature_id\":\"FeatureSTDMediaAlarm\",\"event_id\":\"STDMediaAlarmReport\",\"action_time\":"+(ts - 1000)+",\"action_type\":\"42010000500\",\"media_url\":\"http://siothost/static/media/warn.mp3\"}}]}").getBytes());
        client.publish("v1/gateway/telemetry", message);

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
        Thread.sleep(2000);
        client.disconnect();
        System.out.println("Disconnected");
        System.exit(0);
    }
}
