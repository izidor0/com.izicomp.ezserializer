package com.izicomp.ezserializer.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class Utils {

	public static Gson prepareGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				return new Date(json.getAsJsonPrimitive().getAsLong());
			}
		});
		return builder.create();
	}

	public static String streamToString(InputStream is) {
		return streamToString(is, true);
	}

	public static String streamToString(InputStream is, String charset) {
		return streamToString(is, true, charset);
	}

	public static String streamToString(InputStream is, boolean resetMark) {
		return streamToString(is, resetMark, null);
	}

	public static String streamToString(InputStream is, boolean resetMark, String charset) {
		String string = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;

			while ((length = is.read(buffer)) != -1) {
				baos.write(buffer, 0, length);
			}
			string = baos.toString(charset != null ? charset : "UTF-8");
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (resetMark) {
				try {
					is.reset();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return string;
	}
	
	public static void write(String string, String fileName) {
		try {
			FileWriter fileWriter = null;
			fileWriter = new FileWriter(fileName);
			fileWriter.write(string);
			fileWriter.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static void rename(String string, String fileName) throws Throwable {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(string.getBytes());
	}
	
	public static InputStream read(String objectName) throws Throwable {
		InputStream inputStream = new FileInputStream(new File(objectName));
		return inputStream;
	}	

}