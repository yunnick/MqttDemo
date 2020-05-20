package com.demo.mqtt.client;

import com.google.common.io.Resources;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;

public class MqttTwoWaySslClient {

//    private static final String MQTT_URL = "ssl://mqttserver.com:8883";
//    private static final String CLIENT_KEYSTORE_PASSWORD = "57tzBgVC1nE9Yk7r";
//    private static final String CLIENT_KEY_PASSWORD = "57tzBgVC1nE9Yk7r";


    private static final String MQTT_URL = "ssl://testmqtt2.stc-seedland.com.cn:8883";//siot2
//    private static final String CLIENT_KEYSTORE_PASSWORD = "@lmggTy6XNZmJwu7";
//    private static final String CLIENT_KEY_PASSWORD = "@lmggTy6XNZmJwu7";
    private static final String CLIENT_KEYSTORE_PASSWORD = "1ea68e1a8e2af20bef5978d2df6f322";
    private static final String CLIENT_KEY_PASSWORD = "1ea68e1a8e2af20bef5978d2df6f322";


    private static final String clientId = "MQTT_SSL_JAVA_CLIENT";
    private static final String keyStoreFile = "1ea68e1a8e2af20bef5978d2df6f322.JKS";

    private static final String JKS="JKS";
    private static final String TLS="TLSV1.2";

    public static void main(String[] args) {

        try {
            URL ksUrl = Resources.getResource(keyStoreFile);
            File ksFile = new File(ksUrl.toURI());
            URL tsUrl = Resources.getResource(keyStoreFile);
            File tsFile = new File(tsUrl.toURI());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            KeyStore trustStore = KeyStore.getInstance(JKS);
            trustStore.load(new FileInputStream(tsFile), CLIENT_KEYSTORE_PASSWORD.toCharArray());
            tmf.init(trustStore);
            KeyStore ks = KeyStore.getInstance(JKS);

            ks.load(new FileInputStream(ksFile), CLIENT_KEYSTORE_PASSWORD.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, CLIENT_KEY_PASSWORD.toCharArray());

            KeyManager[] km = kmf.getKeyManagers();
            TrustManager[] tm = tmf.getTrustManagers();
            SSLContext sslContext = SSLContext.getInstance(TLS);
            sslContext.init(km, tm, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setSocketFactory(sslContext.getSocketFactory());

            MqttAsyncClient client = new MqttAsyncClient(MQTT_URL, clientId, new MemoryPersistence());
            client.connect(options);
            while (!client.isConnected()){
                System.out.println("connect...");
                Thread.sleep(1000);
            }
            System.out.println("connected");
            MqttMessage message = new MqttMessage();
            message.setPayload("{\"key1\":\"value1\", \"key2\":true, \"key3\": 3.0, \"key4\": 4}".getBytes());
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