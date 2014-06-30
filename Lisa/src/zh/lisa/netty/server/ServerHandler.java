package zh.lisa.netty.server;

import java.util.List;

import com.google.protobuf.Parser;

import zh.lisa.RemoteCallProtocolProto.RemoteCallProtocol;
import zh.lisa.RemoteCallResultProto.RemoteCallResult;
import zh.lisa.context.RemoteServiceContext;
import zh.lisa.reflect.RemoteServiceInvoke;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class ServerHandler extends ChannelInboundHandlerAdapter {

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	    throws Exception {
    	RemoteCallProtocol protocol= (RemoteCallProtocol) msg;
    	String serviceName = protocol.getServiceName();
		String methodName = protocol.getMethodName();
		List<String> list = protocol.getParamTypeList();
		byte[] param = protocol.getParamByte().toByteArray();
		List<Integer> lengths = protocol.getParamByteLengthList();
		ByteBuf bb = Unpooled.wrappedBuffer(param);
		Object[] params = null;
		Class<?>[] paramsType = null;
		if(list!=null){
			params = new Object[list.size()];
			paramsType = new Class<?>[list.size()];
		}
		int j=0;
		for(int i=0 ;i<list.size(); i++){
			String type = list.get(i);
			if("int".equals(type)){
				params[i] = bb.readInt();
				paramsType[i] = int.class;
			}
			else if("long".equals(type)){
				params[i] = bb.readLong();
				paramsType[i] = long.class;
			}
			else if("float".equals(type)){
				params[i] = bb.readFloat();
				paramsType[i] = float.class;
			}
			else if("double".equals(type)){
				params[i] = bb.readDouble();
				paramsType[i] = double.class;
			}
			else if("string".equals(type)){
				int length = lengths.get(j++);
				byte[] bs = bb.readBytes(length).array();
				params[i] = new String(bs,"UTF-8");
				paramsType[i] = String.class;
			}
			else{
				int length = lengths.get(j++);
				byte[] bs = bb.readBytes(length).array();
				Class<?> cls = Class.forName(type);
				Parser<?> parser  = (Parser<?>) cls.getField("PARSER").get(null);
				params[i] = parser.parseFrom(bs);
				paramsType[i]  = cls;
			}
		}
		RemoteServiceContext sc = RemoteServiceContext.getContext("remote");
		String serviceClassName = sc.getClassName(serviceName);
		Object instance = sc.getInstance(serviceName);
		RemoteCallResult result = RemoteServiceInvoke.invoke(serviceClassName,methodName,instance,params,paramsType);
		ctx.writeAndFlush(result);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();// 发生异常，关闭链路
    }
}
