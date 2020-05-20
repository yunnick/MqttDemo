package com.demo.mqtt.client;

import com.google.common.io.Resources;
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

import static com.demo.mqtt.client.SimpleMqttClient.getDevicePayload;

public class MqttOneWaySslClient {

//    private static final String MQTT_URL = "ssl://test-siot2.stc-seedland.com.cn:8883";
//    private static final String MQTT_URL = "ssl://mqttserver.com:8883";
//    private static final String MQTT_URL = "ssl://127.0.0.1:8883";
//    private static final String MQTT_URL = "ssl://test-iot-as-mqtt.stc-seedland.com.cn:8883";
private static final String MQTT_URL = "ssl://10.22.62.202:8883";

//    private static final String token = "kZAXTQuYj5pMf7e3wxPN";//local
    private static final String token = "bUVoaTTqsUC5qxHUtfLI";//pi

    private static final String CLIENT_ID = "MQTT_SSL_JAVA_CLIENT";
    private static final String KEY_STORE_FILE = "mqttserver.pub.pem";

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static void authProduct(SSLContext sslContext) throws MqttException, InterruptedException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(sslContext.getSocketFactory());
        options.setUserName(token);
        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, "mqttclient_"+RandomStringUtils.randomAlphabetic(5));
        client.connect(options).waitForCompletion();


        MqttMessage message = new MqttMessage();

        String entityName = "ProjectX-02";
        message.setPayload(getDevicePayload(entityName));
        System.out.println("create device");

        client.publish("v1/gateway/connect", message);
        int i = 0;
        long ts = System.currentTimeMillis();



        long sleepTime = 1000;
        while (i++ < 1000 ){
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
                    "\"values\":{\"method\":\"deviceStateChangeResp2\",\"sub_type\":3}}]}").getBytes());
            client.publish("v1/gateway/telemetry", message);

            Thread.sleep(sleepTime);
            System.out.println("send again" + i);
            ts += sleepTime;
        }
    }

    static void authDevice(SSLContext sslContext) throws MqttException, InterruptedException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(sslContext.getSocketFactory());
        options.setUserName("bUVoaTTqsUC5qxHUtfLI");

        MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, CLIENT_ID, new MemoryPersistence());
        client.connect(options);
        while (!client.isConnected()){
            System.out.println("connect...");
            Thread.sleep(1000);
        }
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