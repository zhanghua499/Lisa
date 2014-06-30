package zh.lisa.netty.client.pool;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config; 

public class ChannelPool {
private GenericObjectPool pool;
	
	public ChannelPool(String ip,int port){
		pool = new GenericObjectPool(new ChannelFactory(ip,port), defaultConfig()); 
	}
	
	public ChannelPool(Config config,String ip,int port){
		pool = new GenericObjectPool(new ChannelFactory(ip,port), config); 
	}
	
	public Channel getChannel() throws Exception{  
        return (Channel)pool.borrowObject();  
    } 
	
	public void returnChannel(Channel channel){
		try{  
            pool.returnObject(channel);  
        }catch(Exception e){  
            if(channel != null){
    			EventLoop eventLoop = channel.eventLoop();
    			try {
					channel.closeFuture().sync();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
    			eventLoop.shutdownGracefully();
            }  
        }  
	}
	
	protected Config defaultConfig(){
		 Config config = new Config(); 
		 config.maxActive = 128;
		 config.maxIdle = 12;
		 config.maxWait = 30000;
		 return config;
	}
}
