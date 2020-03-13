package com.izicomp.ezserializer.test.entity;

import java.util.List;

public class Person {

	private Long identifier;
	private String name;
	private List<Address> adressList;

	private Address mainAddress;

	public Address getMainAddress() {
		return mainAddress;
	}

	public void setMainAddress(Address mainAddress) {
		this.mainAddress = mainAddress;
	}

	public Long getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Long identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Address> getAdressList() {
		return adressList;
	}

	public void setAdressList(List<Address> adressList) {
		this.adressList = adressList;
	}

}
