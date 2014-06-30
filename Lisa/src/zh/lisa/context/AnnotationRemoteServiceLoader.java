package zh.lisa.context;

import java.util.HashMap;
import java.util.Map;

import zh.core.search.ClassPathComponentSearch;
import zh.core.search.ComponentFilter;
import zh.core.search.ComponentInfo;
import zh.core.search.ComponentSearch;

public class AnnotationRemoteServiceLoader implements RemoteServiceLoader{
	public Map<String,String> loadLisaService(String packageName) {
		 Map<String,String> serviceMap = new HashMap<String,String>();
		 ComponentSearch cs = new ClassPathComponentSearch(packageName,null,new ComponentFilter(){			 
				public boolean accept(ComponentInfo compenent) {
					if(compenent.getComponentClass().getAnnotation(RemoteService.class) != null)
						return true;
					else
						return false;
				}
				 
			 });
		 for (Object component: cs.getComponents()) {
			 Class<?> clazz = ((ComponentInfo)component).getComponentClass();
			 String actionName = ((RemoteService) clazz.getAnnotation(RemoteService.class)).name();
			 String actionClass = clazz.getName();
			 serviceMap.put(actionName, actionClass);
		 }
		 return serviceMap;
		 
	}
}
