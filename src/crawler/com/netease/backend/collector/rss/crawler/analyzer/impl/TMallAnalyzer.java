/**
 * 
 */
package com.netease.backend.collector.rss.crawler.analyzer.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.archive.crawler.datamodel.CrawlURI;

import com.netease.backend.collector.rss.common.client.ControlClient;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.net.TURLInfo;
import com.netease.backend.collector.rss.common.task.TMallTask;
import com.netease.backend.collector.rss.common.task.Task;
import com.netease.backend.collector.rss.common.util.DricUtil;
import com.netease.backend.collector.rss.crawler.analyzer.IAnalyzer;

/**
 * 天猫单品页面分析器：标题、价格、促销价、品牌、详情页大图url
 * 示例:http://detail.tmall.com/item.htm?id=17523538425
 * @author wfp
 *
 */
public class TMallAnalyzer implements IAnalyzer {

	private static final Logger logger = Logger.getLogger(TMallAnalyzer.class);
	
	/**
	 * 用于从json页面中抓取价格等
	 */
	private static final int TMALL_PRICE_INFO_TYPE = 1;
	
	@Override
	public boolean analyze(CrawlURI curi, String filePath, String characterEncoding) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(filePath);
			String content = IOUtils.toString(inputStream, characterEncoding);
			byte[] attach = (byte[]) curi.getObject(URLInfo.ATTACH);
			TMallTask task = (TMallTask) Task.toTask(attach);
			
			// 这里taskType是自己定义页面类型,放在curi的attach中,反序列化attach即可得到
			int taskType = task.getTaskType();
			if (taskType == TMALL_PRICE_INFO_TYPE) {
				task.setSale(regexSale(content));
				task.setPromotion(regexPromotion(content));
				logger.info(task.toString());

				// to do: 转化task为特定的domain对象,进行存储
				
				
			} else {
				task.setTitle(regexTitle(content));
				task.setBrand(regexBrands(content));
				task.setImageList(regexImages(content));
				
				// 此链接为获取价格的json页面的连接,需要重新发送给manager进行调度抓取
				String uurl = regexOutLink(content);
				
				task.setUrl(uurl);
				task.setTaskType(TMALL_PRICE_INFO_TYPE);
				logger.info(task.toString());

				//把分析出来的需要再次抓取的url发送给manager重新调度
				ControlClient.getInstance().sendURL(makeUrlInfos(task, curi.getUURI().toString()));
			}

		} catch (Throwable t) {
			logger.error("", t);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return true;
	}
	
	/**
	 * 构造url信息重新发送到manager节点进行抓取调度
	 * @param uurl 需要抓取的url
	 * @param refer url的外链
	 * @return
	 * @throws DricException
	 */
	protected List<TURLInfo> makeUrlInfos(Task task, String refer) throws DricException {
		List<TURLInfo> turlInfos = new LinkedList<TURLInfo>();
		URLInfo urlInfo = new URLInfo();
		urlInfo.setSeed(false);
		urlInfo.setUurl(task.getUrl());
		urlInfo.setVia(refer);
		urlInfo.setAttach(task.toBytes());
		TURLInfo turlInfo = DricUtil.URLInfo2TURLInfo(urlInfo);
		turlInfos.add(turlInfo);
		return turlInfos;
	}
	
	/**
	 * 获取标题
	 * @param content 页面内容
	 * @return
	 * @throws IOException
	 */
	private String regexTitle(String content) throws IOException{
		String title = "";
		Pattern p = Pattern.compile("<input type=\"hidden\" name=\"title\" value=\"(.*?)\" />", Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(content);
		if (m.find()) {
			title = m.group(1).trim();
		}
		return title;
	}
	
	/**
	 * 获取商标
	 * @param content 页面内容
	 * @return
	 * @throws IOException
	 */
	private String regexBrands(String content) throws IOException{
		String brands = "";
		Pattern p = Pattern.compile(">品牌:(.*?)</li>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(content);
		if (m.find()) {
			brands= StringEscapeUtils.unescapeHtml(m.group(1)).trim();
		}
		return brands;
	} 
	
	/**
	 * 解析出价格的json页面的url
	 * @param content
	 * @return
	 * @throws IOException
	 */
	private String regexOutLink(String content) throws IOException{
		String brands = "";
		Pattern p = Pattern.compile("\"initApi\" : \"(.*?)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(content);
		if (m.find()) {
			brands= StringEscapeUtils.unescapeHtml(m.group(1)).trim();
		}
		return brands;
	} 
	
	/**
	 * 解析促销价
	 * @param content
	 * @return
	 * @throws IOException
	 */
	private String regexSale(String content) throws IOException{
		String sale = "";
		Pattern p = Pattern.compile("price\":\"(.*?)\",\"promotionList", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(content);
		if (m.find()) {
			sale = m.group(1).trim();
		}
		return sale;
	}
	
	
	/**
	 * 获取促销价格
	 * @param content
	 * @return
	 * @throws IOException
	 */
	private String regexPromotion(String content) throws IOException{
		String price = "";
		Pattern p = Pattern.compile("\"limitTime\":.*?,\"price\":\"(.*?)\",\"promText\":", Pattern.CASE_INSENSITIVE| Pattern.DOTALL);
		Matcher m = p.matcher(content);
		if (m.find()) {
			price = m.group(1).trim();
		}
		return price;
	}
	
	
	private List<String> regexImages(String content) throws IOException {
		Pattern pattern = Pattern.compile("<a href=\"#\"><img src=\"(.*?)\" /></a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);
		List<String> urlsList = new ArrayList<String>();
		int start = 0;
		while (matcher.find(start)) {
			String imgUrl = matcher.group(1).trim();
			urlsList.add(imgUrl.replaceAll("_\\d+x\\d+.\\w+", ""));
			start = matcher.end();
		}
		return urlsList;
	}
       
	public static void main(String args[]){
		try {
			//&callback=onMdskip&ip=&campaignId=&key=&abt=&cat_id=&q=&u_channel=&areaId=&ref=&brandSiteId=0
			TMallAnalyzer taAnalyzer = new TMallAnalyzer();
			String content = DricUtil.fetchUrlContent("http://detail.tmall.com/item.htm?id=17523538425 ", "gbk");
			System.out.println(taAnalyzer.regexTitle(content));
			System.out.println(taAnalyzer.regexBrands(content));
			System.out.println(taAnalyzer.regexOutLink(content));
			System.out.println(taAnalyzer.regexImages(content).size());
			List<String> imageList = taAnalyzer.regexImages(content);
			for(String image :  imageList){
				System.out.println(image);
			}
//			System.out.println(taAnalyzer.regexSale(content));
//			System.out.println(taAnalyzer.regexPromotion(content));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
