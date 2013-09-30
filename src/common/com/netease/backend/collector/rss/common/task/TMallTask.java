/**
 * 
 */
package com.netease.backend.collector.rss.common.task;

import com.netease.backend.collector.rss.common.consts.Consts;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.io.DcasDataInputStream;
import com.netease.backend.collector.rss.common.io.DcasDataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 正文抓取任务类
 * @author wuliufu
 * 标题、价格、促销价、品牌、详情页大图url
 * @since 2010-12-08
 */
public class TMallTask extends Task {
	
	private static final long serialVersionUID = 3926286399895109782L;
	
	//文章类型
	private int taskType;
	
	private String title;
	
	private String sale;
	
	private String promotion;
	
	private String brand;
	
	private List<String> imageList = new ArrayList<String>();

	protected TMallTask() {
		
	}
	
	public TMallTask(String url) {
		super(url, Consts.URL_TMALL_PAGE_TYPE , -1,ContentType.TEXT_HTML_XHTML);
	}
	
	public TMallTask(int taskType, String url, String title,  String sale, String promotion, String brand, List<String> imageList) {
		super(url, Consts.URL_TMALL_PAGE_TYPE , -1,ContentType.TEXT_HTML_XHTML);
		this.taskType = taskType;
		this.title = title;
		this.sale = sale;
		this.promotion = promotion;
		this.brand = brand;
		this.imageList = imageList;
		this.reuseInterval = Consts.PHOTOVIEW_REUSE_INTERVAL;
	}
	
	@Override
	protected void doDeserialize(DcasDataInputStream in) throws IOException {
		taskType = in.readInt();
		title = in.readString();
		sale =in.readString();
		promotion = in.readString();
		brand = in.readString();
		int imageSize = in.readInt();
		imageList.clear();
		for (int i = 0; i < imageSize; i++) {
		   imageList.add(in.readString());
		}
		
    }
	
	/**
	 * task序列化
	 * @return
	 * @throws DricException
	 */
	protected void doSerialize(DcasDataOutputStream out) throws IOException {
		out.writeInt(taskType);
		out.writeString(title);
		out.writeString(sale);
		out.writeString(promotion);
		out.writeString(brand);
		out.writeInt(imageList.size());
		for (String image : imageList) {
			out.writeString(image);
		}
      
	}


	public String toString() {
		StringBuffer buffer = new StringBuffer(1024);
		buffer.append(getClass().getSimpleName()).append("(");
		buffer.append(super.toString());
		Class<?> classType = getClass();
		Field[] fields = classType.getDeclaredFields();
		for(Field field : fields){
			try {
				Object val = field.get(this);
				buffer.append(field.getName()).append(" : ").append(val.toString()).append("\t");
			} catch (Exception e) {
			}
		}
		buffer.append(")");
		return buffer.toString();
	}

	public int getTaskType() {
		return taskType;
	}

	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSale() {
		return sale;
	}

	public void setSale(String sale) {
		this.sale = sale;
	}

	public String getPromotion() {
		return promotion;
	}

	public void setPromotion(String promotion) {
		this.promotion = promotion;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public List<String> getImageList() {
		return imageList;
	}

	public void setImageList(List<String> imageList) {
		this.imageList = imageList;
	}
	
	

}
