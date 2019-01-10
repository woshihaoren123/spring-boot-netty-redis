package com.dzm.chatroom.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ServerInitializer extends ChannelInitializer<Channel> {

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("http-codec", new HttpServerCodec());
		pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
		pipeline.addLast("http-chunked", new ChunkedWriteHandler());
		pipeline.addLast("handler", new WebSocketHandler());
	}

}
