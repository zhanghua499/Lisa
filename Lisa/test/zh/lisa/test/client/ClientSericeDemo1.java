package zh.lisa.test.client;

import zh.lisa.RemoteCall;
import zh.lisa.test.proto.PersonProto.Person;

public interface ClientSericeDemo1 {
	@RemoteCall(remoteService ="demo1",remoteMethod = "test1")
	public void test1(int i,Person person,String str);
	
	@RemoteCall(remoteService ="demo1")
	public String hello(String name);
	
	@RemoteCall(remoteService ="demo1")
	public float add(int a,float b);
}
