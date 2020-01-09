package com.demo.mqtt.client;

import com.google.common.io.Resources;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class MqttOneWaySslClient {

    private static final String MQTT_URL = "ssl://test-siot2.stc-seedland.com.cn:8883";
//    private static final String MQTT_URL = "ssl://mqttserver.com:8883";
//    private static final String MQTT_URL = "ssl://test-iot-as-mqtt.stc-seedland.com.cn:8883";

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

            MqttConnectOptions options = new MqttConnectOptions();
            options.setSocketFactory(sslContext.getSocketFactory());
            options.setUserName("DEV_M0kMFh5uKeQjh7MNvLuv");

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}