/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;

import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 文件编码检测工具类
 * @author wuliufu
 * @since 2012-02-14
 */
public class CodeDetector {
	private static CodepageDetectorProxy detector =  CodepageDetectorProxy.getInstance();   
	static {
		detector.add(new ParsingDetector(false));
		detector.add(JChardetFacade.getInstance());
	}
	
	/**
	 * 检测字节流编码
	 * @param args
	 * @throws Exception 
	 */
	public static String detectCode(URL url) throws Exception {
		Charset charset = detector.detectCodepage(url);
		return charset.name();
	}
	

	public static void main(String[] args) throws Exception {
		String fileName = "e:/n334690246.shtml";
		FileInputStream in = new FileInputStream(fileName);
		byte[]b = new byte[in.available()];
		in.read(b);
		in.close();
		File file = new File(fileName);
		URL url= file.toURI().toURL();
		System.out.println(detector.detectCodepage(url));
		System.out.println(detector.detectCodepage(new URL("http://blog.163.com/")));
	}

}
