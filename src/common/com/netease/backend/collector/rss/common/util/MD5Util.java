/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author wuliufu
 */
public class MD5Util {
	private static final Logger logger = Logger.getLogger(MD5Util.class);
	private static MessageDigest md5 ;
	static{
		 try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
		}
	}
	/**
	 * 计算一个String的md5
	 * @param str
	 * @return
	 */
	public static String calcMD5(String str){
		byte[] bytes;
		synchronized (md5) {
			bytes = md5.digest(str.getBytes());
		}
		StringBuilder ret = new StringBuilder(bytes.length<<1);
		for(int i = 0; i < bytes.length; i++){
			ret.append(Character.forDigit((bytes[i]>>4)&0xf, 16));
			ret.append(Character.forDigit(bytes[i]&0xf, 16));
		}
		return ret.toString();
	}
	
	public static String calcMD5(byte[] b) {
		byte[] bytes;
		synchronized (md5) {
			bytes = md5.digest(b);
		}
		StringBuilder ret = new StringBuilder(bytes.length<<1);
		for(int i = 0; i < bytes.length; i++){
			ret.append(Character.forDigit((bytes[i]>>4)&0xf, 16));
			ret.append(Character.forDigit(bytes[i]&0xf, 16));
		}
		return ret.toString();
	}
	
	public static String calcMD5(InputStream is) {
		String md5 = "";
		try {
			md5 = DigestUtils.md5Hex(is);
		} catch (IOException e) {
		}
		return md5;
	}
	
	public static String calcMD5(File file) {
		String md5 = "";
		if(file == null || !file.exists() || !file.isFile()) {
			return md5;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			md5 = calcMD5(fis);
		} catch (FileNotFoundException e) {
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return md5;
	}
}
