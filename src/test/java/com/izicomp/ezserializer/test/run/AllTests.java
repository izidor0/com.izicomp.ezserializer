package com.izicomp.ezserializer.test.run;

import java.util.ArrayList;

import com.izicomp.ezserializer.EzSerializer;
import com.izicomp.ezserializer.test.entity.Address;
import com.izicomp.ezserializer.test.entity.Person;

public class AllTests {
	
	public static void main(String[] args) {
		saveTest();
		recoverTest();
		cloneTest();
		toJsonTest();
	}
	
	public static void saveTest() {
		try {
			Person person = new Person();
			person.setIdentifier(99988877766L);
			person.setName("Peter Parker");

			Address houseAdress = new Address();
			houseAdress.setZipcode("09844070");
			houseAdress.setOwner(person);

			Address workAdress = new Address();
			workAdress.setZipcode("08913023");
			workAdress.setOwner(person);

			person.setAdressList(new ArrayList<Address>());
			person.getAdressList().add(houseAdress);
			person.getAdressList().add(workAdress);

			person.setMainAddress(houseAdress);

			EzSerializer ezSerializer = new EzSerializer();
			Person clonedPerson = (Person) ezSerializer.serialize(person, "peter.json");

			if (clonedPerson != null) {
				System.out.println("Serialize ok!");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void recoverTest() {
		try {
			EzSerializer ezSerializer = new EzSerializer();
			Person person = (Person) ezSerializer.recover("peter.json");

			if (person != null && person.getName().contains("Peter")) {
				System.out.println("Success recovery!");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static void cloneTest() {
		try {
			Person person = new Person();
			person.setIdentifier(99988877766L);
			person.setName("Peter Parker");

			EzSerializer ezSerializer = new EzSerializer();
			Person clonedPerson = (Person) ezSerializer.clone(person);
			System.out.println("Clone success! Person hash: " + person.hashCode() + ", Cloned Person hash: " + clonedPerson.hashCode());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static void toJsonTest() {
		try {
			Person person = new Person();
			person.setIdentifier(99988877766L);
			person.setName("Peter Parker");

			Address houseAdress = new Address();
			houseAdress.setZipcode("09844070");
			houseAdress.setOwner(person);

			Address workAdress = new Address();
			workAdress.setZipcode("08913023");
			workAdress.setOwner(person);

			person.setAdressList(new ArrayList<Address>());
			person.getAdressList().add(houseAdress);
			person.getAdressList().add(workAdress);

			person.setMainAddress(houseAdress);

			EzSerializer ezSerializer = new EzSerializer();
			String json = ezSerializer.toJson(person);

			if (json != null) {
				System.out.println("To json Success! " + json);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
