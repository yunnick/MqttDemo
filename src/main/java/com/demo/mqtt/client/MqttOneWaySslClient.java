package com.demo.mqtt.client;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static com.demo.mqtt.client.SimpleMqttClient.getDevicePayload;

public class MqttOneWaySslClient {

//    private static final String MQTT_URL = "ssl://test-siot2.stc-seedland.com.cn:8883";
//    private static final String MQTT_URL = "ssl://mqttserver.com:8883";
//    private static final String MQTT_URL = "ssl://127.0.0.1:8883";
//    private static final String MQTT_URL = "ssl://test-iot-as-mqtt.stc-seedland.com.cn:28883";
//    private static final String MQTT_URL = "ssl://ppe-iot-as-mqtt.stc-seedland.com.cn:8843";
      private static final String MQTT_URL = "ssl://iot-as-mqtt.stc-seedland.com.cn:8883";
//private static final String MQTT_URL = "ssl://10.22.62.202:8883";

        private static final String token = "dFX8gb6yjkext2oJprCA";//prod
private static final String productId ="1eb2899b0c722c091ef472eda095fa1";
//    private static final String token = "fkm8EoISNYcXGvuUVOe8";//local
//private static final String productId ="1eab549288ab3409514b74da6ec48fc";
    //    private static final String token = "bUVoaTTqsUC5qxHUtfLI";//pi
//    private static final String token = "Gj6QIj1A3oKSM1EBzsn9";//ppe
//    private static final String productId = "1eac74399a11b308ca4dba45ded7978";//ppe
//    private static final String token = "VRJeadxogXmjfqq9owxD";//test
//    private static final String productId = "1eae751be8313d0bdb0bb4e26fd76ba";//test
    private static final String deviceToken = "pypD4bKxD5M5EFuIInPi";//test
    private static final String deviceId = "1eb023e0a8e2dd0969a1bcd28f2e855";//test

    private static final String CLIENT_ID = "MQTT_SSL_JAVA_CLIENT";
//    private static final String KEY_STORE_FILE = "mqttserver.pub.pem";
//    private static final String KEY_STORE_FILE = "mqttserver_local.pem";
//    private static final String KEY_STORE_FILE = "mqttserver-test.pub.pem";
    private static final String KEY_STORE_FILE = "mqttserver-pro.pub.pem";

//private static final String KEY_STORE_FILE = "stc-seedland.com.cn.pub.pem";

    private static final String JKS="JKS";
    private static final String TLS="TLSV1.2";

    public static void main(String[] args) {

        try {
            // CA certificate is used to authenticate server
            CertificateFactory cAf = CertificateFactory.getInstance("X.509");
            FileInputStream caIn = new FileInputStream(new File(Resources.getResource(KEY_STORE_FILE).toURI()));
            X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn);
            KeyStore caKs = KeyStore.getInstance(JKS);
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", ca);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(caKs);

            // finally, create SSL socket factory
            SSLContext sslContext = SSLContext.getInstance(TLS);
            sslContext.init(null, tmf.getTrustManagers(), null);

            authProduct(sslContext);
//            authDevice(sslContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static MqttAsyncClient accessTokenConnect(SSLContext sslContext) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(sslContext.getSocketFactory());
        options.setUserName(token);
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, "mqttclient_"+RandomStringUtils.randomAlphabetic(5));
        client.connect(options).waitForCompletion();
        return client;
    }

    private static MqttAsyncClient hmacConnect(SSLContext sslContext, String userName, String token ) throws MqttException {
        long ts = System.currentTimeMillis() - 10;
        String algorithm = "hmacsha256";
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL,"|algorithm="+algorithm+",request-ts="+ts+"|");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(sslContext.getSocketFactory());
        options.setUserName(userName);
        String sign = Base64.getEncoder().encodeToString(HmacUtils.getInitializedMac(algorithm, token.getBytes()).doFinal((ts+"").getBytes()));
        options.setPassword(sign.toCharArray());
        client.connect(options).waitForCompletion();
        return client;
    }
//    static String entityName = "defaultDeviceName";
//    static String entityName = "virtual_px3";
//    static String entityName = "ProjectX-05";
    static String entityName = "Guard-12e4553f6c33";

    static void authProduct(SSLContext sslContext) throws MqttException, InterruptedException {

        MqttAsyncClient client = hmacConnect(sslContext, productId, token);
//        MqttAsyncClient client = accessTokenConnect(sslContext);

        MqttMessage message = new MqttMessage();


        message.setPayload(getDevicePayload(entityName));
        System.out.println("create device");
        client.publish("v1/gateway/connect", message);
        int i = 0;
        long ts = System.currentTimeMillis();

        waitRpc(client);

//        subAttributeUpdate(entityName, client);

//        waitAttribure(client);

//        requestAttrubute(entityName, client, 1);
        long sleepTime = 5000;
        while (i++ < 1 ){
//            client.checkPing("a", new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    System.out.println("ping success");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    System.out.println("ping fail");
//                }
//            });

            /**
             * 3、发送属性数据
             */
            System.out.println("publish attr");
            if (otaVerion != null){
                System.out.println("publish attr with version:" + otaVerion);
//                message.setPayload(("{\""+entityName+"\":{\"firm_version\":\""+otaVerion+"\"}}").getBytes());
//                pubOTAProcess(entityName, client);
            }else {
                message.setPayload(("{\""+entityName+"\":{\"groupId\":\"0011120\"}}").getBytes());
//                message.setPayload(("{\""+entityName+"\":{\"groupId\":\"0011120\", \"firm_version\":\"v1.0.2\"}}").getBytes());
            }
//            pubOTAProcess(entityName, client);

//            client.publish("v1/gateway/attributes", message);
            /**
             * 4、发送遥测数据
             */
//            if (i == 1){
                System.out.println("publish tel");

//                 message.setPayload(("{\""+entityName+"\":[{\"ts\":"+ts+",\"values\":{\"trace_id\":\""+RandomStringUtils.randomAlphabetic(10)+"\",\"version\":\"1.0\",\"flag\":\"1\",\"trace_time\":"+ts+",\"call_id\":\"198777\",\"tm_feature_id\":\"FeatureSTDMediaAlarm\",\"tm_event_id\":\"STDMediaAlarmReport\",\"keyword\":\"救命\",\"action_time\":"+(ts - 1000)+",\"action_type\":\"42010000500\",\"trigger_type\":1,\"audio_name\":\"音频路径\",\"audio_url\":\"https://test-media.stc-seedland.com.cn/tencent/api/v1/iot/media/download/beijing/1eae751be8313d0bdb0bb4e26fd76ba/2020/11/27/a3a833da783ca8c1a69ef43856b2f595-help.wav\"}}]}").getBytes());
                message.setPayload(("{\"Guard-12e4553f6c33\":[{\"ts\":"+ts+",\"values\":{\"version\":\"v1.0.12\",\"tm_feature_id\":\"FeatureSTDMediaAlarm\",\"trace_id\":1606455896,\"trace_time\":1606455896,\"tm_event_id\": \"STDMediaAlarmReport\",\"trigger_type\":1,\"action_time\":1606455899564,\"action_type\":\"42010000500\",\"keyword\":\"救命\",\"audio_url\":\"https://media.stc-seedland.com.cn/tencent/api/v1/iot/media/download/beijing/1eb2899b0c722c091ef472eda095fa1/2020/11/27/645180bce5692aa0974ba48aa1c46b6c-20201127134456_1606455896.wav\", \"audio_name\":\"./audio/20201127134456_1606455896.wav\", \"call_id\":\"181084619\", \"start_time\":\"1606455896\",\"call_time\":\"1606455899\", \"recv_time\":\"1\",\"flag\":\"1\"}}]}").getBytes());
                client.publish("v1/gateway/telemetry", message);
                System.out.println("pub");
//            }
            Thread.sleep(sleepTime);
            System.out.println("send again" + i);
            ts += sleepTime;
        }
    }

    static String otaVerion = null;

    private static void waitRpc(MqttAsyncClient client) throws MqttException {
        String topic = "v1/gateway/rpc";//"v1/devices/me/rpc/request/+"
        client.subscribe(topic, 1, null, new IMqttActionListener() {
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
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
                System.out.println(simpleDateFormat.format(new Date()) + " messageArrived topic:" + topic + ", message:" + message);
                Gson gson = new Gson();
                Map payload = gson.fromJson(message.toString(), Map.class);
                try {
                    otaVerion = (String)((Map)((Map)payload.get("data")).get("params")).get("version");
                }catch (Exception e){
                    System.out.println(e);
                }
                rpcResonse(client, ((Double) ((Map) payload.get("data")).get("id")).intValue(), entityName);
                pubOTAProcess(entityName, client);
            }
        });
    }

    private static void pubOTAProcess(String entityName, MqttAsyncClient client) throws MqttException {
        MqttMessage message = new MqttMessage();
        //            System.out.println("publish attr");
        message.setPayload(("{\""+entityName+"\":{\"step\":-1,\"desc\":\"md5校验失败\"}}").getBytes());
        client.publish("v1/product/ota/process", message);
    }

    private static void rpcResonse(MqttAsyncClient client, int reqId, String entityName) throws MqttException {
        MqttMessage message = new MqttMessage();
        String topic = "v1/gateway/rpc";
        String payload = "{\"id\":"+(reqId)+",\"device\":\"" +entityName+ "\",\"data\":false}";
        System.out.println(payload);
        message.setPayload(payload.getBytes());
        client.publish(topic, message);
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
        String payload = "{\"id\":"+count+",\"device\":\"" +entityName+ "\",\"keys\":[\"device_in\",\"sa2\"]}";
//        String payload = "{\"id\":"+count+",\"device\":\"" +entityName+ "\"}";
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

    static void authDevice(SSLContext sslContext) throws MqttException, InterruptedException {

        MqttAsyncClient client = hmacConnect(sslContext, deviceId, deviceToken);

        System.out.println("connected");
        MqttMessage message = new MqttMessage();
        message.setPayload("{\"key1\":\"svalue1\", \"key2\":true, \"key3\": 3.0, \"key4\": 4}".getBytes());
        client.publish("v1/devices/me/telemetry", message);
        System.out.println("publish");
        client.disconnectForcibly();
        System.out.println("Disconnected");
        System.exit(0);
    }
}