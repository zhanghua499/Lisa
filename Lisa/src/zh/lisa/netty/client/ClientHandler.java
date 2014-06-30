package zh.lisa.netty.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Parser;

import zh.lisa.RemoteCallResultProto.RemoteCallResult;
import zh.lisa.callback.Callback;
import zh.lisa.netty.client.pool.ChannelPoolSingleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

public class ClientHandler extends ChannelInboundHandlerAdapter{
	
	public static AttributeKey<Class<?>> returnTypeKey = AttributeKey.valueOf("returnType");
	public static AttributeKey<Callback> callbackKey = AttributeKey.valueOf("callback"); 
	public static AttributeKey<Class<?>> genericTypeKey = AttributeKey.valueOf("genericType"); 
	
	public ClientHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    	Channel channel = ctx.channel();
    	Class<?> returnType = channel.attr(returnTypeKey).get();
    	Callback callback = channel.attr(callbackKey).get();  	
    	RemoteCallResult result= (RemoteCallResult) msg;
    	try{
	    	if(result.getIsSuccess()){
	    		Object value = decode(result,returnType);
	    		callback.call(value);
	    	}
	    	else{
	    		System.out.println(result.getErrorMsg());
	    	}
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	finally{
    		ChannelPoolSingleton pool = ChannelPoolSingleton.getInstance("127.0.0.1", 8080);
    		pool.returnChannel(channel);
		}
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    
    private Object decode(RemoteCallResult result,Class<?> returnType) throws Exception{
    	ByteString bs = result.getResultObj();
    	byte[] bytes= bs.toByteArray();
		ByteBuf bytebuf = Unpooled.wrappedBuffer(bytes);
		Object returnValue = null;
		if(Integer.class.isAssignableFrom(returnType)||int.class.isAssignableFrom(returnType)){
			returnValue = bytebuf.readInt();
		}
		else if(Long.class.isAssignableFrom(returnType)||long.class.isAssignableFrom(returnType)){
			returnValue = bytebuf.readLong();
		}
		else if(Float.class.isAssignableFrom(returnType)||float.class.isAssignableFrom(returnType)){
			returnValue = bytebuf.readFloat();
		}
		else if(Double.class.isAssignableFrom(returnType)||double.class.isAssignableFrom(returnType)){
			returnValue = bytebuf.readDouble();
		}
		else if(String.class.isAssignableFrom(returnType)){
			returnValue = new String(bytes,"UTF-8");
		}
		else if(GeneratedMessage.class.isAssignableFrom(returnType)){
			Parser<?> parser  = (Parser<?>) returnType.getField("PARSER").get(null);
			returnValue = parser.parseFrom(bs);
		}
		else if(Collection.class.isAssignableFrom(returnType)){
			int size = result.getResultListSize();
			Class<?> cls = Class.forName(result.getResultListType());
			List<Object> list = new ArrayList<Object>(size);
			for(int i=0;i<size;i++){
				int length = result.getResultListObjLength(i);
				byte[] bytes2 = bytebuf.readBytes(length).array();
				if(Integer.class.isAssignableFrom(cls)||int.class.isAssignableFrom(cls)){
					list.add(bytebuf.readInt());
				}
				else if(Long.class.isAssignableFrom(cls)||long.class.isAssignableFrom(cls)){
					list.add(bytebuf.readLong());
				}
				else if(Float.class.isAssignableFrom(cls)||float.class.isAssignableFrom(cls)){
					list.add(bytebuf.readFloat());
				}
				else if(Double.class.isAssignableFrom(cls)||double.class.isAssignableFrom(cls)){
					list.add(bytebuf.readDouble());
				}
				else if(String.class.isAssignableFrom(cls)){
					list.add(new String(bytes2,"UTF-8"));
				}
				else if(GeneratedMessage.class.isAssignableFrom(cls)){
					Parser<?> parser  = (Parser<?>) cls.getField("PARSER").get(null);
					list.add(parser.parseFrom(bytes2));
				}
			}
			returnValue = list;
		}
		else{
			throw new RuntimeException(returnType.getName()+" is not supported By Lisa!");
		}
		return returnValue;
    }
}
