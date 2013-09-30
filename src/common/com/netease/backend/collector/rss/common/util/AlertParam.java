package com.netease.backend.collector.rss.common.util;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

/**
 * @author xindingfeng
 *	该类用于定义发送报警邮件的一些参数
 */
public class AlertParam {
	private static final Logger logger = Logger.getLogger(AlertParam.class);
	
	// 报警邮件列表
	private List<String> alertMail = new LinkedList<String>();
	
	// 报警短信电话号码列表
	private List<String> alertPhone = new LinkedList<String>();
	
	// 报警邮件服务器地址
	private String smtpHost;
	
	// 报警邮件发件人id
	private String mailFrom;
	

	private String alias;
	
	// 报警邮件发件人密码
	private String mailPassword;
	
	// 报警短信url
	private String smsUrl;
	
	private static final String MAIL = "mail";
	
	private static final String PHONE = "phone";
	
	private static final String TO = "to";
	
	private static final String FROM = "from";
	
	private static final String ALIAS = "alias";
	
	private static final String PASSWORD = "password";
	
	private static final String SMTPHOST = "smtp-host";
	
	private static final String SMSURL = "sms-url";
	
	private static AlertParam instance = new AlertParam();;
	
	private AlertParam() {}
	
	public static AlertParam getInstance() {
		return instance;
	}
	
	public void init(String path) throws DricException {
		try {
			String to = null;
			Element e = null;
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(path));
			Element root = document.getRootElement();

			Element mail = root.element(MAIL);
			e = mail.element(FROM);
			mailFrom = e.getTextTrim();
			
			e = mail.element(ALIAS);
			alias = e.getTextTrim();
			
			e = mail.element(PASSWORD);
			mailPassword = e.getTextTrim();
			
			e = mail.element(SMTPHOST);
			smtpHost = e.getTextTrim();
			
			Iterator<Element> iter = mail.elementIterator(TO);
			while (iter.hasNext()) {
				e = iter.next();
				to = e.getTextTrim();
				if (to != null && to.length() > 0) {
					alertMail.add(to);
				}
			}
			
			Element phone = root.element(PHONE);
			e = phone.element(SMSURL);
			smsUrl = e.getTextTrim();
			
			iter = phone.elementIterator(TO);
			while (iter.hasNext()) {
				e = iter.next();
				to = e.getTextTrim();
				if (to != null && to.length() > 0) {
					alertPhone.add(to);
				}
			}
		} catch (ParseException e) {
			logger.error("", e);
			throw new DricException(e, ErrorCode.MESSAGE_CONF);
		} catch (DocumentException e) {
			logger.error("", e);
			throw new DricException(e, ErrorCode.MESSAGE_CONF);
		}
	}

	public List<String> getAlertMail() {
		return alertMail;
	}

	public List<String> getAlertPhone() {
		return alertPhone;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public String getAlias() {
		return alias;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public String getSmsUrl() {
		return smsUrl;
	}
	
	public static String getSmsurl() {
		return SMSURL;
	}

	public void setAlertMail(List<String> alertMail) {
		this.alertMail = alertMail;
	}

	public void setAlertPhone(List<String> alertPhone) {
		this.alertPhone = alertPhone;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public void setSmsUrl(String smsUrl) {
		this.smsUrl = smsUrl;
	}
	
	
}
