package com.izicomp.ezserializer;

import java.io.InputStream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.izicomp.ezserializer.util.ReflectionUtils;
import com.izicomp.ezserializer.util.Utils;

public class EzSerializer {

	private final Gson gson = Utils.prepareGson();
	private boolean keepSaved = true;
	private String serializedJson = null;

	public Object serialize(Object object, String objectName) {
		try {
			ReflectionUtils.loadLazyFields(object);
			
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("objectName", objectName);
			jsonObject.addProperty("className", object.getClass().getName());
			jsonObject.addProperty("xmlObject", ReflectionUtils.toXStreamXml(object));

			this.serializedJson = gson.toJson(jsonObject);
			Object serializedObject = this.recover(objectName);
			
			if (keepSaved) {
				Utils.write(this.serializedJson, objectName);
			}
			return serializedObject;

		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Object recover(String objectName) {
		try {
			if (keepSaved) {
				InputStream inputStream = Utils.read(objectName);
				this.serializedJson = Utils.streamToString(inputStream, false);
			}
			JsonObject jsonObject = gson.fromJson(this.serializedJson, JsonObject.class);
			Class<?> clazz = Class.forName(jsonObject.get("className").getAsString());
			Object object = clazz.newInstance();

			String xml = jsonObject.get("xmlObject").getAsString();
			return ReflectionUtils.fromXStreamXml(xml, object);

		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Object clone(Object object) {
		this.keepSaved = false;
		Object clonedObject = this.serialize(object, object.getClass().getSimpleName());
		this.keepSaved = true;
		return clonedObject;
	}
	
	public String toJson(Object object) {
		try {
			this.keepSaved = false;
			Object clonedObject = this.serialize(object, object.getClass().getSimpleName());
			this.keepSaved = true;
			
			ReflectionUtils.clearCylicReferences(clonedObject);
			return gson.toJson(clonedObject);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
