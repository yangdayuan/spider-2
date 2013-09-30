package com.netease.backend.collector.rss.common.contentPage;

import java.io.ByteArrayOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.netease.backend.collector.rss.common.client.ControlClient;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;
import com.netease.backend.collector.rss.common.net.TURLInfo;

public abstract class ContentPage {
	
	private static final Logger logger = Logger.getLogger(ContentPage.class);

	public int type;
	public int size;
	
	public ContentPage(int type) {
		this.type = type;
		this.size = -1;
	}
	
	public abstract boolean doAnalyze();
	
	public void deserialize(DataInputStream in) throws DricException {
		try {
			size = deseInteger(in);
			type = deseInteger(in);
		} catch (IOException e) {
			logger.error("ContentPage deserialize failed");
			throw new DricException(e, ErrorCode.CONTENTPAGE_DESERIALIZE_ERROR);
		}
	}

	public void serialize(DataOutputStream out) throws DricException {
		try {
			seInteger(out, size);
			seInteger(out, type);
		} catch (IOException e) {
			logger.error("ContentPage serialize failed");
			throw new DricException(e, ErrorCode.CONTENTPAGE_SERIALIZE_ERROR);
		}
	}
	
	public int getSerializeSize() throws DricException {
		if (size == -1) {
			ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteArrayStream);
			serialize(out);
			size = byteArrayStream.size();
			try {
				byteArrayStream.close();
				out.close();
			} catch (IOException e) {
				throw new DricException(e, ErrorCode.INVALID_CODE);
			}
		}
		return size;
	}
	
	/**
	 * 把需要重新抓取的url信息发送给manager节点
	 * @param turlInfos
	 * @return
	 */
	public boolean sendUrlInfos(List<TURLInfo> turlInfos) {
		try {
			if (turlInfos.size() != 0) {
				ControlClient.getInstance().sendURL(turlInfos);
			}
		} catch (DricException e) {
			logger.error("", e);
			return false;
		}
		return true;
	}
	
	public abstract TURLInfo recoverUrlInfo ();

	
	/**
	 * 序列化一个字节数组
	 * @param out
	 * @param bytes
	 * @throws IOException
	 */
	public void seBytes(DataOutputStream out, byte[]bytes) throws IOException {
		seInteger(out, bytes.length); 
		out.write(bytes);
	}
	/**
	 * 序列化一个字符串
	 */
	protected void seWStr(DataOutputStream out, String src) throws IOException {
		seInteger(out, src.length()); 
		out.writeChars(src);
	}
	/**
	 * 序列化一个int
	 */
	protected void seInteger(DataOutputStream out, int src) throws IOException {
		out.writeInt(src); 
	}
	
	/**
	 * 序列化一个double
	 */
	protected void seDouble(DataOutputStream out, double src) throws IOException {
		out.writeDouble(src);
	}
	
	/**
	 * 序列化一个byte
	 */
	protected void seByte(DataOutputStream out, byte src) throws IOException {
		out.writeByte(src);
	}
	
	/**
	 * 反序列化一个字符串
	 */
	protected String deseWStr(DataInputStream in) throws IOException {
		int strLen = deseInteger(in);
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < strLen; i++) {
			char c = in.readChar();
			buf.append(c);
		}
		return buf.toString();
	}
	
	/**
	 * 反序列化一个字节数组
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public byte[] deseBytes(DataInputStream in) throws IOException {
		int len = in.readInt();
		byte[] bytes = new byte[len];
		in.read(bytes);
		return bytes;
	}
	
	/**
	 * 反序列化一个int
	 */
	protected int deseInteger(DataInputStream in) throws IOException {
		return in.readInt();
	}
	
	/**
	 * 反序列化一个double
	 */
	protected double deseDouble(DataInputStream in) throws IOException {
		return in.readDouble();
	}
	
	/**
	 * 反序列化一个byte
	 */
	protected byte deseByte(DataInputStream in) throws IOException {
		return in.readByte();
	}

}
