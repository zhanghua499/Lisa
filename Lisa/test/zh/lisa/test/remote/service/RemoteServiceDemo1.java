package zh.lisa.test.remote.service;

import java.util.List;

import zh.lisa.context.RemoteService;
import zh.lisa.test.proto.AddressProto.Address;
import zh.lisa.test.proto.PersonProto.Person;
import zh.lisa.test.proto.PersonProto.Person.PhoneNumber;

@RemoteService(name = "demo1")
public class RemoteServiceDemo1 {
	
	public String hello(String name){
		System.out.println("Call from Clinet。The Name is "+name);
		return "hello "+name+"!";
	}
	
	public void test1(int i,Person person,String str){
		System.out.println("The int32 param is:"+i);
		System.out.println(person.getName() + ", " + person.getEmail());
		Address address = person.getAddress();
		System.out.println(address.getName() + ", " + address.getZip());
		List<PhoneNumber> lstPhones = person.getPhoneList();
		for (PhoneNumber phoneNumber : lstPhones) {
			System.out.println(phoneNumber.getNumber());
		}
		System.out.println("The String param is:"+str);
	}
	
	public float add(int a,float b){
		return a+b;
	}
}
