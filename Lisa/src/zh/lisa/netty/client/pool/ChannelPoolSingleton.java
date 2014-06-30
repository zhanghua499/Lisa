package zh.lisa.netty.client.pool;

import io.netty.channel.Channel;

public class ChannelPoolSingleton {
	
	private static ChannelPoolSingleton instance;
	private ChannelPool pool;
	
	private ChannelPoolSingleton(String host,int port){
		this.pool = new ChannelPool(host,port);
	}
	
	public static ChannelPoolSingleton getInstance(String host,int port){
		if(instance == null){
			synchronized(ChannelPoolSingleton.class){
				if(instance == null){
					instance = new ChannelPoolSingleton(host,port);
				}
			}
		}
		return instance;
	}
	
	public Channel getChannel() throws Exception{
		return pool.getChannel();
	}
	
	public void returnChannel(Channel channel){
		pool.returnChannel(channel);
	}
}
