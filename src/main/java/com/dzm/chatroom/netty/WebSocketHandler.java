package com.dzm.chatroom.netty;

import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.dzm.chatroom.entity.Message;
import com.dzm.chatroom.jedis.JedisDaoImpl;
import com.dzm.chatroom.utils.SpringUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

	private WebSocketServerHandshaker handshaker;

	public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	@Autowired
	private JedisDaoImpl jedis;

	WebSocketHandler() {
		jedis = (JedisDaoImpl) SpringUtil.getBeanByClass(JedisDaoImpl.class);
	}

	/**
	 * 当客户端连接成功，返回个成功信息
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		channels.add(ctx.channel());
		/*
		 * for(Channel channel : channels){ channel.writeAndFlush(new
		 * TextWebSocketFrame("[新用户] - " + ctx.channel().id() + " 加入")); }
		 * channels.add(ctx.channel());
		 * jedis.set(ctx.channel().id().asLongText(),
		 * ctx.channel().id().asLongText());
		 */
	}

	/**
	 * 当客户端断开连接
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		/*
		 * for (String key : Constant.pushCtxMap.keySet()) {
		 * 
		 * if (ctx.equals(Constant.pushCtxMap.get(key))) { // 从连接池内剔除
		 * System.out.println(Constant.pushCtxMap.size());
		 * System.out.println("剔除" + key); Constant.pushCtxMap.remove(key);
		 * System.out.println(Constant.pushCtxMap.size()); }
		 * 
		 * }
		 */
		channels.remove(ctx.channel());
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		ctx.flush();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub

		// http：//xxxx
		if (msg instanceof FullHttpRequest) {

			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			// ws://xxxx
			handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
		}

	}

	public void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

		// 关闭请求
		if (frame instanceof CloseWebSocketFrame) {

			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());

			return;
		}
		// ping请求
		if (frame instanceof PingWebSocketFrame) {

			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));

			return;
		}
		// 只支持文本格式，不支持二进制消息
		if (!(frame instanceof TextWebSocketFrame)) {

			throw new Exception("仅支持文本格式");
		}

		// 客服端发送过来的消息

		String request = ((TextWebSocketFrame) frame).text();
		Message msg = JSONObject.parseObject(request, Message.class);

		for (Channel channel : channels) {
			if (StringUtils.equals("1", msg.getType())) {
				channel.writeAndFlush(new TextWebSocketFrame("[新用户] - " + jedis.get(msg.getId()) + " 加入"));
			}else{
				channel.writeAndFlush(new TextWebSocketFrame(jedis.get(msg.getId()) + " : " + msg.getData()));
			}
		}
	}

	// 第一次请求是http请求，请求头包括ws的信息
	public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {

		if (!req.decoderResult().isSuccess()) {

			sendHttpResponse(ctx, req,
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}

		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				"ws:/" + ctx.channel() + "/websocket", null, false);
		handshaker = wsFactory.newHandshaker(req);

		if (handshaker == null) {
			// 不支持
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {

			handshaker.handshake(ctx.channel(), req);
		}

	}

	public static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {

		// 返回应答给客户端
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
		}

		// 如果是非Keep-Alive，关闭连接
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!isKeepAlive(req) || res.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}

	}

	private static boolean isKeepAlive(FullHttpRequest req) {
		return false;
	}

	// 异常处理，netty默认是关闭channel
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		// 输出日志
		cause.printStackTrace();
		ctx.close();
	}
}
