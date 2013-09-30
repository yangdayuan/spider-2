package com.netease.backend.collector.rss.common.contentPage;

import com.netease.backend.collector.rss.common.task.Task;

public class ContentPageFactory {
	public static ContentPage getContentPage(int type) {
		switch (type) {
		} 
		return null;
	}
	
	/**
	 * 创建ContentPage 对象
	 * @param task 该文件对应的task
	 * @param filePath 下载文件所在路径
	 * @param characterEncoding 文件编码
	 * @param realUrl 真实链接
	 * @return ContentPage 对象
	 */
	public static ContentPage newContentPage(Task task, String filePath, String characterEncoding, String realUrl) {
		ContentPage contentPage = null;
		return contentPage;
	}
}
