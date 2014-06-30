package zh.lisa.netty.client.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import org.apache.commons.pool.BasePoolableObjectFactory;

import zh.lisa.RemoteCallResultProto.RemoteCallResult;
import zh.lisa.netty.client.ClientHandler;

public class ChannelFactory extends BasePoolableObjectFactory {

	private String host;
	private int port;

	public ChannelFactory(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public Object makeObject() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						
						p.addLast(new ProtobufVarint32FrameDecoder());
						p.addLast(new ProtobufDecoder(RemoteCallResult.getDefaultInstance()));
						p.addLast(new ProtobufVarint32LengthFieldPrepender());
						p.addLast(new ProtobufEncoder());
						p.addLast(new ClientHandler());
					}
				});
		ChannelFuture f = b.connect(host, port).sync();
		f.awaitUninterruptibly();
		return f.channel();
	}

	@Override
	public void destroyObject(Object obj) throws Exception {
		if (obj instanceof Channel) {
			Channel channel = ((Channel) obj);
			EventLoop eventLoop = channel.eventLoop();
			channel.closeFuture().sync();
			eventLoop.shutdownGracefully();
		}
	}

	@Override
	public boolean validateObject(Object obj) {
		if (obj instanceof Channel) {
			Channel channel = ((Channel) obj);
			if (!channel.isActive()) {
				return false;
			}
			return true;
		}
		return false;
	}

}
