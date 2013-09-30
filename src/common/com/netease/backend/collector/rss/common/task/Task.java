/**
 * 
 */
package com.netease.backend.collector.rss.common.task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.netease.backend.collector.rss.common.consts.Consts;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;
import com.netease.backend.collector.rss.common.io.DcasDataInputStream;
import com.netease.backend.collector.rss.common.io.DcasDataOutputStream;

/**
 * 需要抓取任务基类。
 * @author wuliufu
 * @since 2010-12-09
 */
public abstract class Task implements Serializable {
	
	private static final long serialVersionUID = -2329403881062497618L;

	/**
	 * 链接url
	 */
	protected String url;
	
	/**
	 * url类型，商城单品对应于TmallTask, 图片url 对应于ImageTask
	 */
	protected int urlType;
	
	protected long reuseInterval;
	
	protected ContentType contentType = ContentType.TEXT_HTML_XHTML;
	
	protected byte version = 1;
	

	public Task() {
	}

	public Task(int urlType) {
		this.urlType = urlType;
	}
	

	public Task(String url, int urlType, long reuseInterval,
			ContentType contentType) {
		this.url = StringUtils.trimToEmpty(url);
		this.urlType = urlType;
		this.reuseInterval = reuseInterval;
		this.contentType = contentType;
	}

	/**
	 * 获取该task序列化后的字节数组
	 * @return
	 * @throws DricException
	 */
	public byte[] toBytes() throws DricException {
		return serialize();
	}
	
	/**
	 * 把字节数组转化成task
	 * @param bytes 待转化字节数组
	 * @return
	 * @throws DricException
	 */
	public static Task toTask(byte[] bytes) throws DricException {
		Task task = null;
		DcasDataInputStream in = new DcasDataInputStream(new ByteArrayInputStream(bytes));
		try {
			int urlType = in.readInt();
			switch(urlType){
			case Consts.URL_TMALL_PAGE_TYPE:
				task = new TMallTask();
				break;
			default :
				throw new DricException("error task url type & type = " + urlType, ErrorCode.INVALID_CODE);
			}
			task.setUrlType(urlType);
			task.deserialize(in);
		} catch (IOException e) {
			throw new DricException("", e, ErrorCode.TASK_SERIALIZE_ERROR);
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return task;
	}
	
	/**
	 * 获取url
	 * @return
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * 获取url类型
	 * 文章url 对应于ArticleTask, 图片url 对应于ImageTask , rss种子url 对应SourceTask
	 * @return
	 */
	public int getUrlType() {
		return urlType;
	}

	/**
	 * 是否是种子(rss源)
	 * @return
	 */
	public boolean isSeed(){
		return false;
	}
	
	/**
	 * task反序列化
	 * @param in
	 * @throws DricException
	 */
	protected void deserialize(DcasDataInputStream in) throws DricException {
		try {
			version = in.readByte();
			int ct = in.readByte();
			contentType = ContentType.getContentType(ct);
			url = in.readString();
			reuseInterval =in.readLong();
			doDeserialize(in);
		} catch (IOException e) {
			throw new DricException("", e, ErrorCode.TASK_SERIALIZE_ERROR);
		}
	}
	
	/**
	 * task反序列化
	 * @param in
	 * @throws DricException
	 */
	protected abstract void doDeserialize(DcasDataInputStream in) throws IOException;
	
	/**
	 * task序列化
	 * @return
	 * @throws DricException
	 */
	protected byte[] serialize() throws DricException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DcasDataOutputStream out = new DcasDataOutputStream(byteStream);
		try {
			out.writeInt(urlType);
			out.writeByte(version);
			byte ct = (byte)(contentType.getType());
			out.writeByte(ct);
			out.writeString(url);
			out.writeLong(reuseInterval);
			doSerialize(out);
			return byteStream.toByteArray();
		} catch (IOException e) {
			throw new DricException(e, ErrorCode.TASK_SERIALIZE_ERROR);
		} finally {
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * task序列化
	 * @return
	 * @throws DricException
	 */
	protected abstract void doSerialize(DcasDataOutputStream out) throws IOException;
	
	protected void setUrlType(int urlType) {
		this.urlType = urlType;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (reuseInterval != other.reuseInterval)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (urlType != other.urlType)
			return false;
		return true;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(512);
		buffer.append("url").append(" : ").append(url).append("\t");
		buffer.append("reuseInterval").append(" : ").append(reuseInterval).append("\t");
		return buffer.toString();
	}

	public long getReuseInterval() {
		return reuseInterval;
	}

	/**
	 * 获取contentType
	 * @return contentType contentType
	 */
	public ContentType getContentType() {
		return contentType;
	}

	/**
	 * 设置contentType
	 * @param contentType contentType
	 */
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * 获取version
	 * @return version version
	 */
	public byte getVersion() {
		return version;
	}

	/**
	 * 设置version
	 * @param version version
	 */
	public void setVersion(byte version) {
		this.version = version;
	}

	/**
	 * 设置reuseInterval
	 * @param reuseInterval reuseInterval
	 */
	public void setReuseInterval(long reuseInterval) {
		this.reuseInterval = reuseInterval;
	}
	
}
