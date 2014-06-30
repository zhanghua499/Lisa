package zh.lisa.context;

import java.util.Map;

public interface RemoteServiceLoader {
	public Map<String, String> loadLisaService(String packageName);
}
