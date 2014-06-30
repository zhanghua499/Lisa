package zh.lisa.test.client;

import zh.lisa.netty.client.RemoteCallbackProxyFactory;
import zh.lisa.test.proto.AddressProto;
import zh.lisa.test.proto.PersonProto;
import zh.lisa.test.proto.PersonProto.Person;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClientSericeDemo1 cs = RemoteCallbackProxyFactory.create(ClientSericeDemo1.class);
//		cs.test1(123, getPerson(1,"xtspring2011@126.com","zhanghua","上海市普陀区同普路",200333), "This is Test1");
//		hello(cs);
		add(cs);
		
	}
	
	private static void hello(ClientSericeDemo1 cs){
		String s = cs.hello("zhanghua");
		System.out.println(s);
	}
	
	private static void add(ClientSericeDemo1 cs){
		float s = cs.add(1,12.5f);
		System.out.println(s);
	}
	
	private static Person getPerson(int id,String email,String name,String address,int zipCode){
		PersonProto.Person.Builder builder = PersonProto.Person.newBuilder();
		builder.setEmail(email);
		builder.setId(id);
		builder.setName(name);
		builder.addPhone(PersonProto.Person.PhoneNumber.newBuilder().setNumber("131111111").setType(PersonProto.Person.PhoneType.MOBILE));
		builder.addPhone(PersonProto.Person.PhoneNumber.newBuilder().setNumber("011111").setType(PersonProto.Person.PhoneType.HOME));
		
		AddressProto.Address.Builder builder2 = AddressProto.Address.newBuilder();
		builder2.setName(address);
		builder2.setZip(zipCode);
		builder.setAddress(builder2.build());
		
		Person person = builder.build();
		return person;
	}

}
