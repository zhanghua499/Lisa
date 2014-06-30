package zh.lisa.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RemoteServiceContext {
	private Map<String, String> actions = new HashMap<String, String>();
	private Map<String, Object> instances = new HashMap<String, Object>();
	private static RemoteServiceContext instance;
	
	private RemoteServiceContext(RemoteServiceLoader loader,String packageName){
		setAll(loader.loadLisaService(packageName));
	}
	
	public static RemoteServiceContext getContext(String packageName){
		if(instance == null){
			synchronized(RemoteServiceContext.class){	
				if(instance == null)
					instance = new RemoteServiceContext(new AnnotationRemoteServiceLoader(),packageName);
			}
		}
		return instance;
	}
	
	public static RemoteServiceContext getContext(RemoteServiceLoader loader,String packageName){
		if(instance == null){
			synchronized(RemoteServiceContext.class){	
				if(instance == null)
					instance = new RemoteServiceContext(loader,packageName);
			}
		}
		return instance;
	}
	
	public String getClassName(String serviceName) {
		Object service = actions.get(serviceName);
		if(service == null){
			throw new RuntimeException("Remote Service is not be founded!!!");
		}
		
		return (String)service;
	}

	public Object getInstance(String serviceName){
		Object instance = instances.get(serviceName);
		if(instance == null){
			throw new RuntimeException("No Instance Found");
		}
		return instance;
	}
	
	private void set(String serviceId, String serviceName) {
		actions.put(serviceId, serviceName);
		try {
			instances.put(serviceId, Class.forName(serviceName.toString()).newInstance());
		} catch (IllegalArgumentException e) {			
			e.printStackTrace();
		} catch (InstantiationException e) {			
			e.printStackTrace();
		} catch (IllegalAccessException e) {		
			e.printStackTrace();
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	
	private void setAll(Map<String, String> services) {
		Iterator<String> iter = services.keySet().iterator();
        while(iter.hasNext()) {
            String name = iter.next();
            String serviceImpl = services.get(name);
            set(name, serviceImpl);
        }
	}
}
