package com.netease.backend.collector.rss.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;

import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;

/**
 * 该类主要用于给管理员发送通知短信和通知邮件
 * 当系统节点退出时，会自动通知管理员
 * 
 * @author solarrain
 * 
 */
public class MessageUtil {
	private static Logger logger = Logger.getLogger(MessageUtil.class);
	
	/**
	 * 发送报警邮件
	 */
	public static void sendAlertMail(AlertParam alertParam, String title, String content) {
		try {
			sendMail(title, content, alertParam.getAlertMail(), null, alertParam.getSmtpHost(),
					alertParam.getMailFrom(), alertParam.getAlias(), alertParam.getMailPassword());
		} catch (DricException e) {
			logger.error("", e);
		}
	}

	/**
	 * 发送报警短消息
	 */
	public static void sendAlertSM(AlertParam alertParam, String content) {
		for (String phone : alertParam.getAlertPhone()) {
			try {
				sendSM(alertParam.getSmsUrl(), phone, content);
				logger.info("短信发送成功");
			} catch (DricException e) {
				logger.error("", e);
			}
		}
	}

	/**
	 * 发送email
	 * 
	 * @param String subject 发送邮件标题
	 * @param String content 发送邮件内容
	 * @param String[] toList 收件人地址列表
	 * @param String[] ccList 抄送地址列表
	 * @param String smtpHost 邮件发送服务器地址
	 * @param String from 发信人地址
	 * @param String password 发送邮件认证口令
	 * @throws DricException
	 */
	synchronized static public void sendMail(String subject, String content,
			List<String> toList, List<String> ccList, String smtpHost,
			String from, String alias, String password) throws DricException {
		Properties prop = new Properties();
		prop.put("mail.transport.default", "smtp");
//		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", smtpHost);
		prop.put("mail.smtp.auth", "true");
		String account;
		int index = from.indexOf("@");
		if (index < 0) {
			throw new DricException("不合法的发信人地址：" + from + "。", ErrorCode.MAIL_ERROR);
		}
		account = from.substring(0, index);

		SMTPAuthenticator auth = new SMTPAuthenticator(account, password);

		try {
			// Get a session, with the
			// specified properties
			Session mySession = Session.getInstance(prop, auth);

			// Create a message to send,
			// specifying our session
			Message message = new MimeMessage(mySession);
			message.setSubject(subject);
			message.setContent(content, "text/html;charset=GBK");

			// Create an
			// InternetAddress, for
			// specifying recipient
			if (toList == null || toList.size() <= 0) {
				throw new DricException("收件人地址为空。", ErrorCode.MAIL_ERROR);
			}

			InternetAddress[] toAddrs = new InternetAddress[toList.size()];
			for (int i = 0; i < toAddrs.length; i++)
				toAddrs[i] = new InternetAddress(toList.get(i));
			message.setRecipients(Message.RecipientType.TO, toAddrs);

			if (ccList != null && ccList.size() > 0) {
				InternetAddress[] ccAddrs = new InternetAddress[ccList.size()];
				for (int i = 0; i < ccList.size(); i++)
					ccAddrs[i] = new InternetAddress(ccList.get(i));
				message.setRecipients(Message.RecipientType.CC, ccAddrs);
			}

			// Create an
			// InternetAddress, for
			// specifying sender address
			InternetAddress fromAddr = new InternetAddress(from, alias);
			message.setFrom(fromAddr);
			logger.debug("Sending message");

			// Send the message
			Transport.send(message);
			logger.debug("Message sent");
		} catch (Exception e) {
			logger.error("", e);
			throw new DricException(e, ErrorCode.MAIL_ERROR);
		}
		
		logger.info("邮件发送成功");

	}

	/**
	 * 发送短信通知
	 * 
	 * @param 发送手机短信的url
	 * @param String[] users 接收人的手机号码
	 * @param String content 短信息内容
	 * @throws DricException
	 */
	synchronized static public void sendSM(String smsUrl, String mobileno,
			String content) throws DricException {
		// 判断号码是否合法
		if (mobileno == null || mobileno.trim().equals("")) {
            return;
//			throw new DricException("手机号码为空", ErrorCode.PHONE_ERROR);
		}
        if (content == null || content.equals(""))
            return;

		mobileno = mobileno.trim();

		// 将消息内容中的空格转换为'+'
		content = content.replace(' ', '+');
		String stime = null;
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		stime = fmt.format(new Date());
		try {
			smsUrl = smsUrl + "&message="
					+ URLEncoder.encode(stime + "\n" + content, "GBK")
					+ "&username=" + URLEncoder.encode(mobileno, "GBK");
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
			throw new DricException(e, ErrorCode.PHONE_ERROR);
		}
		
		try {
			URL url = new URL(smsUrl);
			BufferedReader in = new BufferedReader(new InputStreamReader(url
					.openStream()));
			String hvalue = in.readLine();
			in.close();
			if (hvalue.equals("-1"))
				return;
			else {
				String errMsg;
				if (hvalue.equals("-2"))
					errMsg = "其他错误";
				else if (hvalue.equals("-3"))
					errMsg = "非法ip地址";
				else if (hvalue.equals("-4"))
					errMsg = "密码为空";
				else if (hvalue.equals("0"))
					errMsg = "发出请求的ip不合法";
				else if (hvalue.equals("1"))
					errMsg = "没有指定手机号码";
				else if (hvalue.equals(2))
					errMsg = "没有指定消息内容";
				else if (hvalue.equals("3"))
					errMsg = "没有指定发出消息的手机号码";
				else if (hvalue.equals("4"))
					errMsg = "消息内容过长，已超出70字限制";
				else if (hvalue.equals("5"))
					errMsg = "发送方号码非法 " + mobileno;
				else if (hvalue.equals("6"))
					errMsg = "接收方号码非法";
				else if (hvalue.equals("7"))
					errMsg = "没有msgprop参数";
				else
					errMsg = "无法识别的错误";
				
				throw new DricException("发送短信息失败: " + errMsg, ErrorCode.PHONE_ERROR);
			}
		} catch (IOException e) {
			logger.error("", e);
			throw new DricException(e, ErrorCode.PHONE_ERROR);
		}
	}
}
