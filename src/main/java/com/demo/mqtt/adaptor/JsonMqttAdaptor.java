package com.demo.mqtt.adaptor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class JsonMqttAdaptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static void convertToMsg(SessionMsgType type, MqttMessage inbound) throws Exception {
        switch (type) {
            case POST_TELEMETRY_REQUEST:
                convertToTelemetryUploadRequest((MqttPublishMessage) inbound);
                break;
            case POST_ATTRIBUTES_REQUEST:
                convertToUpdateAttributesRequest((MqttPublishMessage) inbound);
                break;
            case SUBSCRIBE_ATTRIBUTES_REQUEST:
                System.out.println("{\"key1\":\"value1\"}");
                break;
            case GET_ATTRIBUTES_REQUEST:
                convertToGetAttributesRequest((MqttPublishMessage) inbound);
                break;
            default:
                System.err.println("unknow type:" + type);
        }
    }

    private static void convertToTelemetryUploadRequest(MqttPublishMessage inbound) throws Exception {
        String payload = validatePayload(inbound.payload());
        try {
            System.out.println("Telemetry payload is :" + payload);
            System.out.println("Telemetry msg is :" + inbound);
        } catch (IllegalStateException | JsonSyntaxException ex) {
            throw new Exception(ex);
        }
    }

    private static void convertToUpdateAttributesRequest(MqttPublishMessage inbound) throws Exception {
        String payload = validatePayload(inbound.payload());
        try {
            System.out.println("Attributes payload is :" + payload);
            System.out.println("Attributes msg is :" + inbound);
        } catch (IllegalStateException | JsonSyntaxException ex) {
            throw new Exception(ex);
        }
    }

    private static void convertToGetAttributesRequest(MqttPublishMessage inbound) throws Exception {
        try {
            String payload = inbound.payload().toString(UTF8);
            JsonElement requestBody = new JsonParser().parse(payload);
            Set<String> clientKeys = toStringSet(requestBody, "clientKeys");
            Set<String> sharedKeys = toStringSet(requestBody, "sharedKeys");
            if (clientKeys == null && sharedKeys == null) {
                System.out.println("null info");
            } else{
                if (clientKeys != null){
                    for (String clientKey : clientKeys) {
                        System.out.print("客户端属性:" + clientKey + " ");
                    }
                }
                if (sharedKeys != null){
                    for (String sharedKey : sharedKeys) {
                        System.out.print("共享设备属性:" + sharedKey + " ");
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new Exception(e);
        }
    }

    private static Set<String> toStringSet(JsonElement requestBody, String name) {
        JsonElement element = requestBody.getAsJsonObject().get(name);
        if (element != null) {
            return new HashSet<>(Arrays.asList(element.getAsString().split(",")));
        } else {
            return null;
        }
    }

    private static String validatePayload(ByteBuf payloadData) throws Exception {
        try {
            String payload = payloadData.toString(UTF8);
            if (payload == null) {
                throw new Exception(new IllegalArgumentException("Payload is empty!"));
            }
            return payload;
        } finally {
            payloadData.release();
        }
    }
}
