package zh.lisa.reflect;

public class InvokeCommand {
	private String className;
	private String methodName;
	private Object instance;
	private Object[] arguments;
	private Class<?>[] argumentTypes;

	public InvokeCommand(String className,String methodName, Object instance,Object[] arguments,Class<?>[] types){
		this.className = className;
		this.methodName = methodName;
		this.arguments = arguments;
		this.instance = instance;
		this.argumentTypes = types;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public String getMethodName() {
		return this.methodName;
	}

	public Object[] getArguments() {
		return this.arguments;
	}
	
	public Class<?>[] getArgumentTypes() {
		return argumentTypes;
	}
	
	public Object getInstance(){
		return instance;
	}
}
