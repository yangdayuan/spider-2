/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 外网ip切换器
 * @author wuliufu
 * @since 3.8.1
 */
public class IPSwitcher {
	private static final Logger logger = LoggerFactory.getLogger(IPSwitcher.class);
	private static final int LIST_SIZE = 50;
	private static List<String> outIPList = new ArrayList<String>(LIST_SIZE);
	private static CheckOuterIPThread checkOuterIPThread = null;
	private static Random random = new Random();
	
	static {
		checkOuterIPThread = new CheckOuterIPThread();
		checkOuterIPThread.checkOuterIP();
		logger.info("Find local out ips {}.", outIPList);
		System.out.println("Find local out ips " + outIPList);
		checkOuterIPThread.setDaemon(true);
		checkOuterIPThread.start();
	}
	
	/**
	 * 获取本机的一个外网ip，如果没找到则返回null
	 * @return
	 */
	public static String getLocalIP() {
		if(outIPList.isEmpty()) {
			return null;
		}
		return outIPList.get(random.nextInt(outIPList.size()));
	}
	
	public int count() {
		return outIPList.size();
	}
	
	static class CheckOuterIPThread extends Thread {
		
		private void checkOuterIP() {
			try {
				List<String> list = new ArrayList<String>(LIST_SIZE);
				Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
	            while (netInterfaces.hasMoreElements()) {
	                NetworkInterface ni = netInterfaces.nextElement();
	                Enumeration<InetAddress> ips = ni.getInetAddresses();

	                while (ips.hasMoreElements()) {
	                    InetAddress address = ips.nextElement();
	                    String ip = address.getHostAddress();
	                    if (address.isSiteLocalAddress() || address.isLoopbackAddress() || ip.contains(":")) {
	                        continue;
	                    }
	                    list.add(ip);
	                }
	            }
	            outIPList.clear();
	            outIPList.addAll(list);
			} catch(Throwable t) {
				logger.error("", t);
			}
		}
		
		public void run() {
			while(true) {
				try {
					//10分钟检查一次ip
		            Thread.sleep(10 * 60 * 1000);
		            checkOuterIP();
				} catch(Throwable t) {
					logger.error("", t);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println(getLocalIP());
		int num = 4;
		RequestTest[] rt = new RequestTest[num];
		for(int i = 0; i < num; i++) {
			rt[i] = new RequestTest();
			rt[i].start();
		}
		for(int i = 0; i < num; i++) {
			try {
				rt[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	
	public static class RequestTest extends Thread {
		private ThreadLocal<HttpClient> http = new ThreadLocal<HttpClient>();
//		private static String[] ips = new String[] {"172.31.129.66", "192.168.146.123"};
//		static Random r = new Random();
		
		public HttpClient getHttpClient() {
			HttpClient client = http.get();
			if(client == null) {
				System.out.println("Thread-" + Thread.currentThread().getId() + " create client.");
				client = new HttpClient();
				http.set(client);
			}
			return client;
		}
		public void run() {
			while(true) {
				try {
					GetMethod method = new GetMethod("http://feedback.youdao.com/iplookup/");
			            String localIP = getLocalIP();
			            HttpClient client = getHttpClient();
			            if(StringUtils.isNotBlank(localIP)) {
			                try {
			                	System.out.println(Thread.currentThread().getId() + " : set ip : " + localIP);
			                	client.getHostConfiguration().setLocalAddress(InetAddress.getByName(localIP));
			    			} catch (UnknownHostException e) {
			    				logger.error("Unkown host for " + localIP + " &uses " + 
			    						client.getHostConfiguration().getLocalAddress(), e);
			    			}
			            }
			            int code = client.executeMethod(method);   
			         String content = method.getResponseBodyAsString();
			         Pattern p = Pattern.compile("您的IP地址是\\s*:\\s*([0-9\\.]+)", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
			         Matcher m = p.matcher(content);
			         if(m.find()) {
				         System.out.println(Thread.currentThread().getId() + " : use ip : " + m.group(1));
			         }
					Thread.sleep(10);
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}

		}
	}
}
