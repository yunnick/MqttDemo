package com.demo.mqtt.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

/**
 */
public class MqttTransportServerInitializer  extends ChannelInitializer<SocketChannel> {

    private final int maxPayloadSize;

    public MqttTransportServerInitializer(int maxPayloadSize){
        this.maxPayloadSize = maxPayloadSize;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast( new MqttSslHandlerProvider().getSslHandler());
        pipeline.addLast("decoder", new MqttDecoder(maxPayloadSize));
        pipeline.addLast("encoder", MqttEncoder.INSTANCE);

        MqttTransportHandler handler = new MqttTransportHandler();

        pipeline.addLast(handler);
        socketChannel.closeFuture().addListener(handler);
        System.out.println("MqttTransportServerInitializer done");
    }
}
