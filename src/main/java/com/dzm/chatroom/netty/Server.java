package com.dzm.chatroom.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 配置服务器功能
 * @author Administrator
 *
 */
public class Server {

	private int port;
	
	public Server(int port) {
		this.port = port;
	}
	
	public void run() throws Exception {
		EventLoopGroup loopGroup = null;
		EventLoopGroup workGroup = null;
		try {
			// 创建两个nio线程组
			// 1.专门用来网络事件处理(接受客户端的连接)
			loopGroup = new NioEventLoopGroup(5);
			// 2.进行网络通信读写
			workGroup = new NioEventLoopGroup();
			// 创建一个ServerBootstrap对象,配置netty的一系列参数
			ServerBootstrap server = new ServerBootstrap();
			server.group(loopGroup, workGroup).channel(NioServerSocketChannel.class)
			.childHandler(new ServerInitializer())
			.option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
			// 绑定端口, 执行同步阻塞方法等待服务器启动
			ChannelFuture future = server.bind(port).sync();
			future.channel().closeFuture().sync();
		} finally {
			loopGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}

	}
}
