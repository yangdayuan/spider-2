package com.netease.backend.collector.rss.common.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.archive.crawler.datamodel.CandidateURI;
import org.archive.httpclient.SingleHttpConnectionManager;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

import st.ata.util.FPGenerator;

import com.netease.backend.collector.rss.common.consts.Consts;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.net.TURLInfo;

public class DricUtil {
	private static final Logger logger = Logger.getLogger(DricUtil.class);
	
	/**
	 * 该jdk支持的编码集,统一大写
	 */
	private static Set<String> availableCharsets = new HashSet<String>();
	/**
	 * 优先级较高的字符集
	 */
	private static String[] priorityCharsets = new String[] {"UTF-8", "UTF8", "GBK", "GB18030", "GB2312"}; 
	
	static {
		Set<String> charsetNames = Charset.availableCharsets().keySet();  
        for (Iterator<String> it = charsetNames.iterator(); it.hasNext();)  
        {  
            String charsetName = (String) it.next(); 
            availableCharsets.add(charsetName.toUpperCase());
        }  
		availableCharsets.add("UTF8");
	}
	
	/**
	 * 查看编码是否合法
	 * @param charset 待检测编码
	 * @return 如果合法返回该编码，否则返回null
	 */
	public static String lookfor(String charset) {
		if(charset == null) {
			return null;
		}
		if(availableCharsets.contains(charset.toUpperCase())) {
			return charset;
		}
		return null;
	}

	public static URLInfo TURLInfo2URLInfo(TURLInfo turlInfo) {
		URLInfo urlInfo = new URLInfo();
		urlInfo.setUurl(turlInfo.getUurl());
		urlInfo.setPathFromSeed(turlInfo.getPathFromSeed());
		urlInfo.setVia(turlInfo.getVia());
		urlInfo.setReuseInterval(turlInfo.getReuseInterval());

		urlInfo.setModifyTime(turlInfo.getModifyTime());
		urlInfo.setDownLoadTime(turlInfo.getDownLoadTime());
		urlInfo.setSeed(turlInfo.isSeed());

		urlInfo.setAttach(turlInfo.getAttach());

		return urlInfo;
	}

	public static TURLInfo URLInfo2TURLInfo(URLInfo urlInfo) {
		TURLInfo turlInfo = new TURLInfo();
		turlInfo.setUurl(urlInfo.getUurl());
		turlInfo.setPathFromSeed(urlInfo.getPathFromSeed());
		turlInfo.setVia(urlInfo.getVia());
		turlInfo.setReuseInterval(urlInfo.getReuseInterval());

		turlInfo.setModifyTime(urlInfo.getModifyTime());
		turlInfo.setDownLoadTime(urlInfo.getDownLoadTime());
		turlInfo.setSeed(urlInfo.isSeed());

		turlInfo.setAttach(urlInfo.getAttach());

		return turlInfo;
	}

	public static CandidateURI URLInfo2CandidateURI(URLInfo urlInfo) {
		CandidateURI caUrl = null;

		try {
			UURI uuri = UURIFactory.getInstance(urlInfo.getUurl());
			UURI via = null;
			if (urlInfo.getVia() != null && urlInfo.getVia().length() > 0) {
				via = UURIFactory.getInstance(urlInfo.getVia());
			}

			caUrl = new CandidateURI(uuri, urlInfo.getPathFromSeed(), via, null);
			caUrl.setIsSeed(urlInfo.isSeed());

			caUrl.putLong(URLInfo.REUSE_INTERVAL, urlInfo.getReuseInterval());
			caUrl.putLong(URLInfo.MODIFY_TIME, urlInfo.getModifyTime());
			caUrl.putLong(URLInfo.DOWNLOAD_TIME, urlInfo.getDownLoadTime());

			caUrl.putObject(URLInfo.ATTACH, urlInfo.getAttach());
			//模拟设置cookie
//			caUrl.putString(URLInfo.COOKIE, "deffaffd-fdjfgjkda;fdsajkweuijfds");
		} catch (Exception e) {
			logger.error("URLInfo2CandidateURI url: " + urlInfo.getUurl());
			logger.error("", e);
		}

		return caUrl;
	}

	public static URLInfo CandidateURI2URLInfo(CandidateURI caUri) {
		URLInfo urlInfo = new URLInfo();

		urlInfo.setUurl(caUri.getUURI().toString());
		urlInfo.setPathFromSeed(caUri.getPathFromSeed());
		urlInfo.setVia(caUri.getVia() == null? "": caUri.getVia().toString());

		if (caUri.containsKey(URLInfo.REUSE_INTERVAL)) {
			urlInfo.setReuseInterval(caUri.getLong(URLInfo.REUSE_INTERVAL));
		}

		if (caUri.containsKey(URLInfo.MODIFY_TIME)) {
			urlInfo.setModifyTime(caUri.getLong(URLInfo.MODIFY_TIME));
		}

		if (caUri.containsKey(URLInfo.DOWNLOAD_TIME)) {
			urlInfo.setDownLoadTime(caUri.getLong(URLInfo.DOWNLOAD_TIME));
		}
		urlInfo.setSeed(caUri.isSeed());

		if (caUri.containsKey(URLInfo.ATTACH)) {
			urlInfo.setAttach((byte[])caUri.getObject(URLInfo.ATTACH));
		}

		return urlInfo;
	}

	/*public static String createKey(CandidateURI caUri) {
		return caUri.getUURI().toString();
	}*/
	
	private static final String COLON_SLASH_SLASH = "://";
	/**
     * Create fingerprint.
     * Pubic access so test code can access createKey.
     * @param uri URI to fingerprint.
     * @return Fingerprint of passed <code>url</code>.
     */
    public static long createKey(CharSequence uri) {
        String url = uri.toString();
        int index = url.indexOf(COLON_SLASH_SLASH);
        if (index > 0) {
            index = url.indexOf('/', index + COLON_SLASH_SLASH.length());
        }
        CharSequence hostPlusScheme = (index == -1)? url: url.subSequence(0, index);
        long tmp = FPGenerator.std24.fp(hostPlusScheme);
        return tmp | (FPGenerator.std40.fp(url) >>> 24);
    }

	public static String makeImgUrlTag(int pageNum, int imgNum) {
		return Consts.NEWS_IMG_TAG + Integer.toString(pageNum) + "#" + Integer.toString(imgNum) + "#";
	}

	public static String makeNextPageTag(int pageNum) {
		return Consts.NEWS_NEXTPAGE_TAG + Integer.toString(pageNum) + "#";
	}
		
	private static String getMostAvailableCharset(List<String> clist) {
		if(clist == null || clist.size() == 0) {
			return null;
		}
		for(String c : priorityCharsets) {
			for(String cc : clist) {
				if(c.equalsIgnoreCase(cc)) {
					return cc;
				}
			}
		}
		return clist.get(0);
	}
	
	/**
	 * 获取页面最可能的编码
	 * @param detectorCharset 工具检测出的编码
	 * @param responseCharset response 返回的编码
	 * @param pageCharset 页面分析出来的编码
	 * @return
	 */
	private static String getMostAvailableCharset(String detectorCharset, String responseCharset, 
			String pageCharset) {
		List<String> clist = new ArrayList<String>(3);
		if(StringUtils.isNotBlank(detectorCharset) && lookfor(detectorCharset) != null) {
			clist.add(detectorCharset);
		}
		if(StringUtils.isNotBlank(responseCharset) && lookfor(responseCharset) != null) {
			clist.add(responseCharset);
		}
		if(StringUtils.isNotBlank(pageCharset) && lookfor(pageCharset) != null) {
			clist.add(pageCharset);
		}

		if(clist.size() == 0) {
			return null;
		}
		if(clist.size() == 1) {
			return clist.get(0);
		}
		if(clist.size() == 2) {
			if(clist.get(0).equalsIgnoreCase(clist.get(1)))
				return clist.get(0);
			else {
				return getMostAvailableCharset(clist);
			}
		} else {
			if(clist.get(0).equalsIgnoreCase(clist.get(1)) ||clist.get(0).equalsIgnoreCase(clist.get(2))) {
				return clist.get(0);
			} else if(clist.get(1).equalsIgnoreCase(clist.get(2))) {
				return clist.get(1);
			} else {
				return getMostAvailableCharset(clist);
			}
		}
	}
	
	public static String getCharset(byte[] bytes) {
		return IOUtil.detectEncoding(bytes);
	}

	public static String loadHtmlFile(String path, String charset) throws IOException {
		File file = new File(path);
		InputStream is = new FileInputStream(file);
		byte[] pageData = new byte[is.available()];
		is.read(pageData);
		is.close();
		
		//工具检测编码
		String detectorCharset = getCharset(pageData);
		
		//页面检测编码
		String pageCharset = null;		
        //先假设编码为utf-8，看是否有<meta...charset标志
        String pageConvert = new String(pageData, "utf-8");
        String regex = "<meta[^>]*?charset=['\"\\s]*(.*?)['\"\\s>]+";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(pageConvert);
        if (matcher.find()) {
        	pageCharset = matcher.group(1).trim();
        }
        
        charset = getMostAvailableCharset(detectorCharset, charset, pageCharset);
        
		if (StringUtils.isBlank(charset)) {
	        if (pageData[0] == 0xef && pageData[1] == 0xbb && pageData[2] == 0xbf) {
	        	charset = "utf-8";
	        }
		}

        //如果没有charset标记，默认编码为gb2312
        if (StringUtils.isBlank(charset) || charset.equalsIgnoreCase("gb2312")) {
        	charset = "gbk";
        }

		String content = new String(pageData, charset);
		return trim(content);
	}

	public static String trim(String str) {
		String newstr = str.replaceAll("　","  ");
		newstr = newstr.trim();
		newstr = newstr.replaceAll("  ","　");
		newstr = newstr .replaceAll("　", " ");
		return newstr;
	}

	public static final byte[] removeSpace(byte[]t) throws UnsupportedEncodingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < t.length; i++) {
			if (t[i] == -95 && t[i + 1] == -95) {
				out.write(32);
				i++;
			} else if (t[i] == -62 && t[i + 1] == -96) {
				out.write(32);
				i++;
			}
			else {
				out.write(t[i]);
			}
		}
		return out.toByteArray();
	}

	/**
	 * 加载xml文件
	 * @param path 文件本地路径
	 * @param charset response返回的编码
	 * @return xml字符串
	 * @throws IOException
	 */
	public static String loadXmlFile(String path, String charset) throws IOException {
		File file = new File(path);
		InputStream is = new FileInputStream(file);
		byte[] pageData = new byte[is.available()];
		is.read(pageData);
		is.close();
		if (charset == null || charset.equals("")) {
			charset = "UTF-8";
			String xml = new String(pageData, charset);
	        String regex = "<?xml[^>]*\\s*encoding\\s*=\\s*['\"](.*?)['\"][^>]*>";
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(xml);
	        if (matcher.find()) {
	        	charset = matcher.group(1);
	        }
		}
		String content = new String(pageData, charset);
		StringBuilder builder = new StringBuilder(content.length());
		for(int i = 0; i < content.length(); i++) {
			char ch = content.charAt(i);
			if(!((ch >= 0x00 && ch <= 0x08) || (ch >= 0x0b  && ch <= 0x0c) || (ch >= 0x0e   && ch <= 0x1f))) {
				builder.append(ch);
			}
		}
		return builder.toString();

	}

	public static byte[] ViaSetToBytes(HashSet<String> viaSet) {
		if (viaSet == null || viaSet.size() == 0) {
			return null;
		}
		byte[] data = null;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(byteStream);

		try {
			output.writeInt(viaSet.size());
			Iterator<String> iter = viaSet.iterator();
			while(iter.hasNext()) {
				String via = iter.next();
				data = via.getBytes();
				output.writeInt(data.length);
				output.write(data);
			}
			return byteStream.toByteArray();
		} catch (Exception e) {
			return null;
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				return null;
			}
		}
	}

	public static HashSet<String> BytesToViaSet(byte[] data) {
		HashSet<String> viaSet = new HashSet<String>();
		if (data == null) {
			return viaSet;
		}
		ByteBuffer buffer = ByteBuffer.wrap(data);
		int size = 0;
		byte[] array = null;
		size = buffer.getInt();
		for(int i = 0; i < size; i++) {
			int len = buffer.getInt();
			array = new byte[len];
			buffer.get(array);
			viaSet.add(new String(array));
		}
		return viaSet;
	}

	private static final String DEFAULT_HOST = "default-host";

	public static String getHost(URLInfo urlInfo) {
		if (urlInfo == null || urlInfo.getUurl() == null || urlInfo.getUurl().length() == 0) {
			return DEFAULT_HOST;
		}

		return getHost(urlInfo.getUurl());
	}

	public static String getHost(String url) {
		String host = DEFAULT_HOST;
		UURI uuri = null;

		try {
			uuri = UURIFactory.getInstance(url);
			host = uuri.getAuthorityMinusUserinfo();
		} catch (URIException e) {
			logger.error("", e);
		}

		return host.replace(':', '#');
	}

	private static final int CONN_TIMEOUT = 2*60000;
	private static final int WR_TIMEOUT = 2*60000;
	
	public static String fetchUrlContent(String url, String charSet) throws DricException {
		String content = null;
		HttpConnectionManager cm = new SingleHttpConnectionManager();
        HttpConnectionManagerParams hcmp = cm.getParams();
        // 设置连接超时时间(单位毫秒)
        hcmp.setConnectionTimeout(CONN_TIMEOUT);
        hcmp.setSoTimeout(WR_TIMEOUT);
		HttpClient httpClient = new HttpClient(cm);

		HttpMethod method = new GetMethod(url);
		method.getParams().setContentCharset(charSet);
		try {
			httpClient.executeMethod(method);
			content = method.getResponseBodyAsString();
		} catch (Exception e) {
			logger.error(""+url, e);
			throw new DricException(e, ErrorCode.HTTP_REQUEST_ERROR);
		}

		return content;
	}
	
	
	public static InputStream fetchUrlInputStream(String url, String charSet) throws DricException {
		InputStream content = null;
		HttpConnectionManager cm = new SingleHttpConnectionManager();
        HttpConnectionManagerParams hcmp = cm.getParams();
        // 设置连接超时时间(单位毫秒)
        hcmp.setConnectionTimeout(CONN_TIMEOUT);
        hcmp.setSoTimeout(WR_TIMEOUT);
		HttpClient httpClient = new HttpClient(cm);

		HttpMethod method = new GetMethod(url);
		method.getParams().setContentCharset(charSet);
		try {
			httpClient.executeMethod(method);
			content = method.getResponseBodyAsStream();
		} catch (Exception e) {
			logger.error(""+url, e);
			throw new DricException(e, ErrorCode.HTTP_REQUEST_ERROR);
		}

		return content;
	}
	
	
	public static String fetchUrlContent(String url,List<NameValuePair> params, String charSet) {
		String content = null;
		try {
			HttpClient httpClient = new HttpClient();
			PostMethod method = new PostMethod(url);
			method.setRequestBody(params.toArray(new NameValuePair[0]));
			method.getParams().setContentCharset(charSet);
			int statusCode = httpClient.executeMethod(method);
	        if (statusCode != HttpStatus.SC_OK) {
	        	logger.error("Method failed: " + method.getStatusLine());
	        } else {
	        	content = "";
	        }
	        content = method.getResponseBodyAsString();
		} catch(Throwable t) {
			logger.error(""+url, t);
		}
		return content;
	}
	
	
	public static InputStream fetchUrlInputStream(String url,List<NameValuePair> params , String charSet) {
		InputStream inputStream = null;
		try {
			HttpClient httpClient = new HttpClient();
			PostMethod method = new PostMethod(url);
			method.setRequestBody(params.toArray(new NameValuePair[0]));
			method.getParams().setContentCharset(charSet);
			int statusCode = httpClient.executeMethod(method);
	        if (statusCode != HttpStatus.SC_OK) {
	        	logger.error("Method failed: " + method.getStatusLine());
	        } 
	       inputStream = method.getResponseBodyAsStream();
		} catch(Throwable t) {
			logger.error("", t);
		}
		return inputStream;
	}

	public static boolean httpRequest(String url) throws Exception {
		HttpClient httpClient = new HttpClient(new HttpClientParams(),new SimpleHttpConnectionManager(true));
		GetMethod getMethod = new GetMethod(url);
		try {
			int statusCode = httpClient.executeMethod(getMethod);
            return statusCode == HttpStatus.SC_OK;
		} finally {
            getMethod.releaseConnection();
            ((SimpleHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
        }

	}

    public static String simpleRequest(String url) throws IOException {
        HttpClient httpClient = new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager(true));
        GetMethod getMethod = new GetMethod(url);
        try {
            int statusCode = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                return "";
            } else {
                return getMethod.getResponseBodyAsString();
            }
        } finally {
            getMethod.releaseConnection();
            ((SimpleHttpConnectionManager) httpClient.getHttpConnectionManager()).shutdown();
        }
    }

	public static boolean updateSourceCache(String url, String sourceUuid) throws Exception {
		if(url == null) {
			return false;
		}
		StringBuilder builder = new StringBuilder(127);
		builder.append(url).append("?sourceUuid=").append(sourceUuid);
		return httpRequest(builder.toString());
	}

	/**
	 * 判断是否是图集类文章，包含相册
	 * @param articleType 文章类型
	 * @return 是返回true，否返回false
	 */
	public static boolean isPictureView(int articleType) {
		return isAlbum(articleType) || articleType == Consts.DRIC_CONTENT_PAGE_PHOTOVIEW;
	}

	/**
	 * 判断是否是相册类文章
	 * @param articleType
	 * @return 是返回true，否返回false
	 */
	public static boolean isAlbum(int articleType) {
		return (articleType == Consts.DRIC_CONTENT_PAGE_ALBUM || articleType == Consts.DRIC_CONTENT_PAGE_PHOTO ||
				articleType == Consts.DRIC_CONTENT_PAGE_ISTYLE);
	}
	
	public static String format(int len, int data) {
		String s = String.format("%0" + len + "d", data);
		if(s.length() > len) {
			s = s.substring(0, len);
		}
		return s;
	}
}
