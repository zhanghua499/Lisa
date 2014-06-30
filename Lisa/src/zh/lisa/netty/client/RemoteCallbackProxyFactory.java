package zh.lisa.netty.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class RemoteCallbackProxyFactory {
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> callInterface){
		InvocationHandler handler = new RemoteCallbackProxyHandler();
		ClassLoader loader = callInterface.getClassLoader();
		return (T) Proxy.newProxyInstance(loader,new Class[]{callInterface} , handler);
	}
}
