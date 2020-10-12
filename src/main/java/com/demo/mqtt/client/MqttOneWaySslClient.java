package com.demo.mqtt.client;

import com.google.common.io.Resources;
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

import static com.demo.mqtt.client.SimpleMqttClient.getDevicePayload;

public class MqttOneWaySslClient {

//    private static final String MQTT_URL = "ssl://test-siot2.stc-seedland.com.cn:8883";
//    private static final String MQTT_URL = "ssl://mqttserver.com:8883";
//    private static final String MQTT_URL = "ssl://127.0.0.1:8883";
//    private static final String MQTT_URL = "ssl://test-iot-as-mqtt.stc-seedland.com.cn:28883";
    private static final String MQTT_URL = "ssl://ppe-iot-as-mqtt.stc-seedland.com.cn:8843";
//private static final String MQTT_URL = "ssl://10.22.62.202:8883";

//    private static final String token = "kZAXTQuYj5pMf7e3wxPN";//local
//    private static final String token = "bUVoaTTqsUC5qxHUtfLI";//pi
    private static final String token = "Gj6QIj1A3oKSM1EBzsn9";//ppe
    private static final String productId = "1eac74399a11b308ca4dba45ded7978";//ppe
//    private static final String token = "bfWTd8cpqhKPiiiEv1sS";//test
//    private static final String productId = "1eb0186d8d671b0ac55b387b07b3667";//test
    private static final String deviceToken = "pypD4bKxD5M5EFuIInPi";//test
    private static final String deviceId = "1eb023e0a8e2dd0969a1bcd28f2e855";//test

    private static final String CLIENT_ID = "MQTT_SSL_JAVA_CLIENT";
    private static final String KEY_STORE_FILE = "stc-seedland.com.cn.pub.pem";

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

    static void authProduct(SSLContext sslContext) throws MqttException, InterruptedException {

        MqttAsyncClient client = hmacConnect(sslContext, productId, token);
//        MqttAsyncClient client = accessTokenConnect(sslContext);

        MqttMessage message = new MqttMessage();

        String entityName = "hachi-gate-002";
        message.setPayload(getDevicePayload(entityName));
        System.out.println("create device");

        client.publish("v1/product/connect", message);
        int i = 0;
        long ts = System.currentTimeMillis();

        waitRpc(client);

        subAttributeUpdate(entityName, client);

        waitAttribure(client);

        requestAttrubute(entityName, client, 1);
        long sleepTime = 5000;
        while (i++ < 1 ){
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
//            message.setPayload(("{\""+entityName+"\":{\"groupId\":\"0011120\"}}").getBytes());
//            client.publish("v1/product/attributes", message);
            /**
             * 4、发送遥测数据
             */
            if (i == 1){
                System.out.println("publish tel");

                message.setPayload(("{\""+entityName+"\":[{\"ts\":"+ts+",\"values\":" +
                        "{\"version\":\"1.1\",\"tm_event_id\":\"STDDoorAccessReport\",\"action_type\":\"41010000100\"," +
                        "\"card_type\":\"0\",\"action_time\":1601373411000,\"card_no\":\"456\",\"use_type\":\"7\",\"unlocked\":\"0\"}}]}").getBytes());
//                message.setPayload(("{\""+entityName+"\":[{\"ts\":"+System.currentTimeMillis()+",\"values\":{\"record_id\":\"036311fb36\",\"action_type\":\"41010001000\",\"user_id\":\"05\",\"user_phone\":\"13686468905\",\"plate_number\":\"粤AV9N61\",\"vehicle_picture\":\"/ppe/parking/yue_a_v9n61.jpg?bucketName=stc-aiot-gz-1259084015&provider=cos\",\"ts\":1600854893477,\"category\":\"11110201003\",\"version\":\"1.1\",\"tm_feature_id\":\"FeatureSTDParkingDoorAccess\",\"tm_feature_version\":\"1587006242684\",\"tm_event_id\":\"STDParkingAccessReport\",\"trace_id\":\"57e5a6b3-e07d-46c7-9d0b-5c23f1f4e515\",\"trace_time\":\"1600854893548\"}}]}").getBytes());
                client.publish("v1/product/telemetry", message);
                System.out.println("pub");
            }
            Thread.sleep(sleepTime);
            System.out.println("send again" + i);
            ts += sleepTime;
        }
    }

    private static void waitRpc(MqttAsyncClient client) throws MqttException {
        String topic = "v1/product/rpc";//"v1/devices/me/rpc/request/+"
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
            }
        });
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
        String payload = "{\"id\":"+count+",\"device\":\"" +entityName+ "\",\"keys\":[\"device_in\",\"sa2\"]}";
//        String payload = "{\"id\":"+count+",\"device\":\"" +entityName+ "\"}";
//        System.out.println(payload);
        message.setPayload(payload.getBytes());
//        message.setPayload(("{\"id\":"+count+",\"device\":\"" +entityName+ "\"}").getBytes());
        client.publish("v1/product/attributes/request", message);
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