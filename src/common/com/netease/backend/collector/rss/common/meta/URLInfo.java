package com.netease.backend.collector.rss.common.meta;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * 用于在中心节点和爬虫节点进行通信的对象
 * @author XinDingfeng
 *
 */
public class URLInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 8372074605960556713L;

	private static final long DEFAULT_REUSE_INTERVAL = 0;
	
	//以下是heritrix所需要的信息
	private String uurl = "";
	
	private String pathFromSeed = "";
	
	private String via = "";
	
	private long reuseInterval = DEFAULT_REUSE_INTERVAL;
	
	//以下是做页面更新信息
	private long modifyTime = 0;
	
	private long downLoadTime = 0;
	
	private long assignTime = 0;
	
	private boolean seed = false;
	
	//以下是页面分析所需要的附加信息
	private byte[] attach = new byte[0];
	
	//url重试次数,默认为0
	private int tryTimes = 0;
	
	//以下是attribute
	public static final String REUSE_INTERVAL = "page_reuse_interval";
	
	public static final String MODIFY_TIME = "page-modify-time";
	
	public static final String DOWNLOAD_TIME = "page-download-time";
	
	public static final String ATTACH = "page-attach";
	
	public static final String COOKIE = "page-cookie";
	
	public byte[] serialize() {
		byte[] data = null;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(byteStream);
		
		try {
			data = uurl.getBytes();
			output.writeInt(data.length);
			output.write(data);
			
			data = pathFromSeed.getBytes();
			output.writeInt(data.length);
			output.write(data);
			
			data = via.getBytes();
			output.writeInt(data.length);
			output.write(data);
			
			output.writeLong(reuseInterval);
			output.writeLong(modifyTime);
			output.writeLong(downLoadTime);
			output.writeLong(assignTime);
			
			byte seedByte = (byte)(seed? 1: 0);
			output.write(seedByte);
			
			output.writeInt(attach.length);
			output.write(attach, 0, attach.length);
			
			/*
			 * 序列化tryTimes
			 */
			output.writeInt(tryTimes);
			
			return byteStream.toByteArray();
		} catch (Exception e) {
			return new byte[0];
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				return new byte[0];
			}
		}
	}
	
	public static URLInfo deserialize(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		URLInfo urlInfo = new URLInfo();
		int len = 0;
		byte[] array = null;
		
		len = buffer.getInt();
		array = new byte[len];
		buffer.get(array);
		urlInfo.setUurl(new String(array));
		
		len = buffer.getInt();
		array = new byte[len];
		buffer.get(array);
		urlInfo.setPathFromSeed(new String(array));
		
		len = buffer.getInt();
		array = new byte[len];
		buffer.get(array);
		urlInfo.setVia(new String(array));
		
		urlInfo.setReuseInterval(buffer.getLong());
		urlInfo.setModifyTime(buffer.getLong());
		urlInfo.setDownLoadTime(buffer.getLong());
		urlInfo.setAssignTime(buffer.getLong());
		if (buffer.get() == 1) {
			urlInfo.setSeed(true);
		} else {
			urlInfo.setSeed(false);
		}
		
		len = buffer.getInt();
		array = new byte[len];
		buffer.get(array);
		urlInfo.setAttach(array);
		
		/*
		 * 反序列化tryTimes
		 */
		urlInfo.setTryTimes(buffer.getInt());
		
		return urlInfo;
	}
	
    @Override
    public Object clone() {
    	URLInfo urlInfo = null;
    	try {
    		urlInfo = (URLInfo)super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return urlInfo;
    }
	
	@Override
	public boolean equals(Object obj) {
		URLInfo other = (URLInfo)obj;

		byte[] otherAttach = other.getAttach();
		if (otherAttach.length != attach.length) {
			return false;
		}
		
		for (int i = 0; i < attach.length; i++) {
			if (otherAttach[i] != attach[i]) {
				return false;
			}
		}
		
		return other.getUurl().equals(uurl) && other.getPathFromSeed().equals(pathFromSeed)
			&& other.getVia().equals(via) && other.getReuseInterval() == reuseInterval 
			&& other.getModifyTime() == modifyTime && other.getDownLoadTime() == downLoadTime 
			&& other.getAssignTime() == assignTime && other.isSeed() == seed;
	}

	public int getTryTimes() {
		return tryTimes;
	}

	public void setTryTimes(int tryTimes) {
		this.tryTimes = tryTimes;
	}

	public String getUurl() {
		return uurl;
	}

	public void setUurl(String uurl) {
		this.uurl = uurl;
	}

	public String getPathFromSeed() {
		return pathFromSeed;
	}

	public void setPathFromSeed(String pathFromSeed) {
		this.pathFromSeed = pathFromSeed;
	}

	public String getVia() {
		return via;
	}

	public void setVia(String via) {
		this.via = via;
	}

    /**
     * url间隔时间
     * @return url间隔时间
     */
	public long getReuseInterval() {
		return reuseInterval;
	}

	public void setReuseInterval(long reuseInterval) {
		this.reuseInterval = reuseInterval;
	}

	public long getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}

	public long getDownLoadTime() {
		return downLoadTime;
	}

	public void setDownLoadTime(long downLoadTime) {
		this.downLoadTime = downLoadTime;
	}

	public long getAssignTime() {
		return assignTime;
	}

	public void setAssignTime(long assignTime) {
		this.assignTime = assignTime;
	}

	public boolean isSeed() {
		return seed;
	}

	public void setSeed(boolean seed) {
		this.seed = seed;
	}

	public byte[] getAttach() {
		return attach;
	}

	public void setAttach(byte[] attach) {
		this.attach = attach;
	}
}
