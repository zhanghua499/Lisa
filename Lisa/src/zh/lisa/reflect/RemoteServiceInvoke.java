package zh.lisa.reflect;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import zh.lisa.RemoteCallResultProto;
import zh.lisa.RemoteCallResultProto.RemoteCallResult;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;

public class RemoteServiceInvoke {
	private static Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();
	private static Map<MapMethodKey, Method> methodCache = new HashMap<MapMethodKey, Method>();
	
	public static RemoteCallResult invoke(String className,String methodName, Object instance,Object[] arguments,Class<?>[] types){
		RemoteCallResultProto.RemoteCallResult.Builder builder = RemoteCallResultProto.RemoteCallResult.newBuilder();
		try{
			Class<?> cls = classCache.get(className);
			if(cls == null){
				synchronized(RemoteServiceInvoke.classCache){
					if(cls == null){				 
						cls = Class.forName(className);
						classCache.put(className, cls);
					}
				}
			}	
			MapMethodKey methodKey = new MapMethodKey(cls,methodName, types);
			Method method = (Method)methodCache.get(methodKey);
			if(method == null){
				synchronized(RemoteServiceInvoke.methodCache){
					if(method == null){
						method = ClassUtil.getMethod(cls, methodName, types);
						methodCache.put(new MapMethodKey(cls,methodName, types), method);					
					}
				}
			}
			Object result = ClassUtil.invokeMethod(instance, method, arguments);
			if(result != null){
				ByteString bs = convert2ByteString(result,builder);
				builder.setResultObj(bs);
			}
			builder.setIsSuccess(true);
		}
		catch(ClassNotFoundException e){
			returnError(builder,e);
			e.printStackTrace();
		}
		catch(Exception e){
			returnError(builder,e);
			e.printStackTrace();
		}
		return builder.build();
	}
	
	private static void returnError(RemoteCallResultProto.RemoteCallResult.Builder builder,Exception e){
		builder.setErrorMsg(e.getMessage());
		builder.setIsSuccess(false);
	}
	
	private static ByteString convert2ByteString(Object arg,RemoteCallResultProto.RemoteCallResult.Builder builder) throws UnsupportedEncodingException{
		ByteBuf bytebuf = Unpooled.buffer();
		if(Integer.class.isAssignableFrom(arg.getClass())){
			bytebuf.writeInt((Integer)arg);
		}
		else if(Long.class.isAssignableFrom(arg.getClass())){
			bytebuf.writeLong((Long)arg);
		}
		else if(Float.class.isAssignableFrom(arg.getClass())){
			bytebuf.writeFloat((Float)arg);
		}
		else if(Double.class.isAssignableFrom(arg.getClass())){
			bytebuf.writeDouble((Double)arg);
		}
		else if(String.class.isAssignableFrom(arg.getClass())){
			byte[] bytes = arg.toString().getBytes("UTF-8");
			bytebuf.writeBytes(bytes);
			builder.addResultListObjLength(bytes.length);
		}
		else if(GeneratedMessage.class.isAssignableFrom(arg.getClass())){
			GeneratedMessage msg = (GeneratedMessage)arg;
			byte[] bytes = msg.toByteArray();
			bytebuf.writeBytes(bytes);
			builder.addResultListObjLength(bytes.length);
		}
		else if(Collection.class.isAssignableFrom(arg.getClass())){
			Object[] c = ((Collection<?>)arg).toArray();
			builder.setResultListSize(c.length);
			if(c.length>0){
				builder.setResultListType(c[0].getClass().getName());
			}
			for(Object obj:c){
				convert2ByteString(obj,builder);
			}
		}
		else{
			throw new RuntimeException(arg.getClass().getName()+" is not supported By Lisa!" 
					+" The Parameter is:"+arg.toString());
		}
		ByteString bs = ByteString.copyFrom(bytebuf.array());
		return bs;
	}
}
