package zh.lisa.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;

import zh.lisa.RemoteCall;
import zh.lisa.RemoteCallProtocolProto;
import zh.lisa.callback.Callback;
import zh.lisa.callback.CallbackFuture;
import zh.lisa.netty.client.pool.ChannelPoolSingleton;

public class RemoteCallbackProxyHandler implements InvocationHandler{

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object returnObj = null;
		RemoteCall call = method.getAnnotation(RemoteCall.class);
		if(call!=null) {
			String remoteService = call.remoteService();
			String remoteMethod = call.remoteMethod();
			CallbackFuture<Object> future = remoteCall(remoteService,remoteMethod,args,method);
			if(method.getReturnType() == void.class){}
			else if(CallbackFuture.class.isAssignableFrom(method.getReturnType())){
				returnObj = future;
			}
			else{
				returnObj = future.get();
			}
		}
		return returnObj;
	}

	private CallbackFuture<Object> remoteCall(String service,String method,Object[] args,Method proxyMethod) throws Exception{
		RemoteCallProtocolProto.RemoteCallProtocol.Builder call 
		= RemoteCallProtocolProto.RemoteCallProtocol.newBuilder();
		final CallbackFuture<Object> future = new CallbackFuture<Object>();
		call.setServiceName(service);
		if("".equals(method))
			call.setMethodName(proxyMethod.getName());
		else
			call.setMethodName(method);
		ByteBuf bytebuf = Unpooled.buffer();
    	if(args != null){
    		for(Object arg:args){
    			putIntoBuffer(call,bytebuf,arg,proxyMethod);
    		}
    	}
    	ByteString bs = ByteString.copyFrom(bytebuf.array());
		call.setParamByte(bs);		
    	
    	ChannelPoolSingleton pool = ChannelPoolSingleton.getInstance("127.0.0.1", 8080);
    	Channel channel = pool.getChannel();
		Type type = proxyMethod.getGenericReturnType();
		if(type!=null && type instanceof ParameterizedType){
			 Type[] parameterizedType = ((ParameterizedType)type).getActualTypeArguments();
			 Class<?> entityClass = null;
			 if(parameterizedType[0] instanceof ParameterizedType){
				 entityClass = (Class<?>) ((ParameterizedType)parameterizedType[0]).getRawType();
			 }
			 else
				 entityClass = (Class<?>) parameterizedType[0];
			 channel.attr(ClientHandler.genericTypeKey).set(entityClass);
		}
		channel.attr(ClientHandler.returnTypeKey).set(proxyMethod.getReturnType());
		channel.attr(ClientHandler.callbackKey).set(new Callback(){
			@Override
			public void call(Object value) {
				future.done(value);	
			}
			
		});
		ChannelFuture cf = channel.writeAndFlush(call);
		cf.awaitUninterruptibly();
		return future;
	}
	
	private void putIntoBuffer(RemoteCallProtocolProto.RemoteCallProtocol.Builder call,ByteBuf bytebuf,Object arg,Method proxyMethod) throws Exception{
		if(Integer.class.isAssignableFrom(arg.getClass())){
			call.addParamType("int");
			bytebuf.writeInt((Integer)arg);
		}
		else if(Long.class.isAssignableFrom(arg.getClass())){
			call.addParamType("long");
			bytebuf.writeLong((Long)arg);
		}
		else if(Float.class.isAssignableFrom(arg.getClass())){
			call.addParamType("float");
			bytebuf.writeFloat((Float)arg);
		}
		else if(Double.class.isAssignableFrom(arg.getClass())){
			call.addParamType("double");
			bytebuf.writeDouble((Double)arg);
		}
		else if(String.class.isAssignableFrom(arg.getClass())){
			call.addParamType("string");
			byte[] bytes = arg.toString().getBytes("UTF-8");
			bytebuf.writeBytes(bytes);
			call.addParamByteLength(bytes.length);
		}
		else if(GeneratedMessage.class.isAssignableFrom(arg.getClass())){
			call.addParamType(arg.getClass().getName());
			GeneratedMessage msg = (GeneratedMessage)arg;
			byte[] bytes = msg.toByteArray();
			bytebuf.writeBytes(bytes);
			call.addParamByteLength(bytes.length);
		}
		else if(Collection.class.isAssignableFrom(arg.getClass())){
			call.addParamType("list");
			Object[] c = ((Collection<?>)arg).toArray();
			if(c.length>0){
				call.addListParamType(c[0].getClass().getName());
			}
			for(Object obj:c){
				putIntoBuffer(call,bytebuf,obj,proxyMethod);
			}
		}
		else{
			throw new RuntimeException(arg.getClass().getName()+" is not supported By Lisa!" 
					+" The Method is:"+proxyMethod.getName()+" The Parameter is:"+arg.toString());
		}
	}
}
