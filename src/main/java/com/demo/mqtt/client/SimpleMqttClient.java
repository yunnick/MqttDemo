package com.demo.mqtt.client;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.paho.client.mqttv3.*;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class SimpleMqttClient {
    //    private static final String MQTT_URL = "tcp://test-siot2.stc-seedland.com.cn:1883";
    private static final String MQTT_URL = "tcp://127.0.0.1:1883";
//    private static final String MQTT_URL = "tcp://140.143.212.101:1883";//siot2
//    private static final String MQTT_URL = "tcp://10.22.30.3:1883";//pi

//    private static final String MQTT_URL = "tcp://test-iot-as-mqtt.stc-seedland.com.cn:1883";


    public static void main(String[] args) throws Exception {
        doMqtt();
    }

    private static MqttAsyncClient accessTokenConnect() throws MqttException {
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, RandomStringUtils.randomAlphanumeric(10));
        MqttConnectOptions options = new MqttConnectOptions();
//        options.setUserName("OFpEXYkYDqWi3EPnnT57");//PRO_h281D8AuvH32a8ujA6qm
//        options.setUserName("YYdevGZ6kaXNct0FFdy1");//test service
//        options.setUserName("TUcUIPKYbowXPFx0dB4y");//test device
//            options.setUserName("bUVoaTTqsUC5qxHUtfLI");//pi
        options.setUserName("kZAXTQuYj5pMf7e3wxPN");//local
//        options.setUserName("YYdevGZ6kaXNct0FFdy1");//local service
        options.setKeepAliveInterval(0);
        MqttMessage connMessage = new MqttMessage();
//        connMessage.setPayload("{\"connect\":true}".getBytes());

        connetc(client, options, connMessage);
        return client;
    }

    private static MqttAsyncClient hmacConnect() throws MqttException {
        long ts = System.currentTimeMillis() - 10;
        String algorithm = "hmacsha256";
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL,
                RandomStringUtils.randomAlphanumeric(10) + "|algorithm="+algorithm+",request_ts="+ts+"|");
        MqttConnectOptions options = new MqttConnectOptions();
//        options.setUserName("OFpEXYkYDqWi3EPnnT57");//PRO_h281D8AuvH32a8ujA6qm
//        options.setUserName("YYdevGZ6kaXNct0FFdy1");//test service
//        options.setUserName("TUcUIPKYbowXPFx0dB4y");//test device
//            options.setUserName("bUVoaTTqsUC5qxHUtfLI");//pi
        options.setUserName("1ea73f0bb40e8c09ca89798392184f7");//local
        String secret = "dmVonNMXf1f44XbRpaor";
        String sign = Base64.getEncoder().encodeToString(HmacUtils.getInitializedMac(algorithm, secret.getBytes()).doFinal((ts+"").getBytes()));
        options.setPassword(sign.toCharArray());
//        options.setUserName("YYdevGZ6kaXNct0FFdy1");//local service
        options.setKeepAliveInterval(0);
        MqttMessage connMessage = new MqttMessage();
//        connMessage.setPayload("{\"connect\":true}".getBytes());

        connetc(client, options, connMessage);
        return client;
    }



    private static void doMqtt() throws InterruptedException {
        try {
            /**
             * 1、连接设备
             */
//            MqttAsyncClient client = accessTokenConnect();
            MqttAsyncClient client = hmacConnect();
            String entityName = "ProjectX-01";
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println("Connect lost==========");
                    cause.printStackTrace();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
                    System.out.println(simpleDateFormat.format(new Date()) + " get from topic " + topic + ": msg:"+message);
//                    if (topic.equals("v1/product/rpc")){
//                        Gson gson = new Gson();
//                        Map payload = gson.fromJson(message.toString(), Map.class);
//                        rpcResonse(client, ((Double) ((Map) payload.get("data")).get("id")).intValue(), entityName);
//                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
//                    System.out.println("deliveryComplete");
                }
            });

            int connTime = 0;
            while (!client.isConnected()){
                connTime++;
                System.out.println("connect...");
                if (connTime == 10){
                    break;
                }
                Thread.sleep(1000);
            }
            System.out.println("connected");
            Thread.sleep(1000);
            MqttMessage message = new MqttMessage();



            /**
             * 2、创建设备
             */
//        String entityName = "Guard-12e4553f6c33";

            message.setPayload(getDevicePayload(entityName));
            System.out.println("create device");

//        String entityName ="77789";
//        message.setPayload(getServicePayload(entityName));
//        System.out.println("create service");

            client.publish("v1/product/connect", message);
            Thread.sleep(2000);
            int i = 0;

            subAttributeUpdate(entityName, client);
            long sleepTime = 5000;
            //get attribute from server
            waitAttribure(client);
//            waitRpc(client);
            while (i++ < 1){
                long ts = System.currentTimeMillis();
//                unsbuAttr(i, client);

//                ping(client);

//                sendHeartbeat(entityName, client, ts);
                /**
                 * 3、发送属性数据
                 */
//                pubAttr(i, entityName, client);
                /**
                 * 4、发送遥测数据
                 */
//                if (i ==1){
                    sendTelemetry(entityName, client, ts+12);
//                }

                /**
                 * 5、获取属性
                 */
//                requestAttrubute(entityName, client, i);

                Thread.sleep(sleepTime);
                System.out.println("send again" + i);
//                client.close(true);

            }

            System.out.println("send finish");
            Thread.sleep(2000000);
            client.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        }catch (MqttException e){
            e.printStackTrace();
//            doMqtt();
        }
    }

    private static void connetc(MqttAsyncClient client, MqttConnectOptions options, MqttMessage connMessage ) throws MqttException {
        client.connect(options, connMessage, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                System.out.println("success with msg: " + asyncActionToken.getResponse());
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                System.err.println("fail with msg: " + asyncActionToken.getResponse());
                exception.printStackTrace();
            }
        });
    }

    private static void pubAttr(int cycle, String entityName, MqttAsyncClient client) throws MqttException {
        MqttMessage message = new MqttMessage();
        //            System.out.println("publish attr");
        message.setPayload(("{\""+entityName+"\":{\"groupId\":\"0011120\",\"confs\":\"confsVa"+cycle+"\"}}").getBytes());
        client.publish("v1/product/attributes", message);
    }
    private static void ping(MqttAsyncClient client) throws MqttException {
        client.checkPing(null, new IMqttActionListener() {
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
            client.unsubscribe("v1/product/attributes/response");
        }
    }

    private static void subAttributeUpdate(String entityName, MqttAsyncClient client) throws MqttException {

        MqttMessage message = new MqttMessage();
        //get attribute from server
        client.subscribe("v1/product/attributes", 1, null, new IMqttActionListener() {
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
        client.publish("v1/product/attributes/request", message);
    }

    private static void rpcResonse(MqttAsyncClient client, int reqId, String entityName) throws MqttException {
        MqttMessage message = new MqttMessage();
        String topic = "v1/product/rpc";
        String payload = "{\"id\":"+(reqId)+",\"device\":\"" +entityName+ "\",\"data\":false}";
        System.out.println(payload);
        message.setPayload(payload.getBytes());
        client.publish(topic, message);
    }

    private static void waitRpc(MqttAsyncClient client) throws MqttException {
        client.subscribe("v1/product/rpc", 1, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                System.out.println("wait rpc command");
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

    private static void waitAttribure(MqttAsyncClient client) throws MqttException {
        client.subscribe("v1/product/attributes/response", 1, null, new IMqttActionListener() {
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

        message.setPayload(("{\""+entityName+"\":[{\"ts\":"+ts+",\"values\":{\"trace_id\":\""+RandomStringUtils.randomAlphabetic(10)+"\",\"version\":\"1.0\",\"trace_time\":"+ts+",\"call_id\":\"198777\",\"tm_feature_id\":\"FeatureSTDMediaAlarm\",\"tm_event_id\":\"STDMediaAlarmReport\",\"keyword\":\"救命\",\"action_time\":"+(ts - 1000)+",\"action_type\":\"42010000500\",\"trigger_type\":1,\"audio_name\":\"音频路径\",\"audio_url\":\"http://siothost:8080/api/v1/media/download/1ea89e07b31ead0abd14508d3aa1de2/afbf7a9c6ba98fb1111ca619576a51c1\"}}]}").getBytes());
        client.publish("v1/product/telemetry", message);
        System.out.println("publish tel");

    }

    private static void sendHeartbeat(String entityName, MqttAsyncClient client, long ts) throws MqttException {
        MqttMessage message = new MqttMessage();

        String hbm = "{\""+entityName+"\":[{\"ts\":"+ts+",\"values\":{\"trace_id\":\""+RandomStringUtils.randomAlphabetic(10)+"\",\"version\":\"1.0\",\"trace_time\":"+ts+",\"call_id\":\"198777\",\"tm_feature_id\":\"FeatureSTDMediaAlarm\",\"tm_event_id\":\"STDMediaAlarmReport\",\"keyword\":\"救命\",\"action_time\":"+(ts - 1000)+",\"action_type\":\"42010000500\",\"state\":400}}]}";

        message.setPayload(hbm.getBytes());
        client.publish("v1/product/heartbeat", message);
        System.out.println("publish tel");

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
