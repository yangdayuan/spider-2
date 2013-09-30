/**
 * 
 */
package com.netease.backend.collector.rss.common.exception;

public class ErrorCode {
	/**
	 * 默认值，没意义
	 */
	public static final int INVALID_CODE = 0;
	/**
	 * 其他异常
	 */
	public static final int OTHER_ERROR = -1;
	
	
	/*用户传入参数发生错误代码*/
	/**
	 * 验证参数是否为null
	 */
	public static final int ARGUMENT_INVALID = 100;
	
	
	//crawl初始化错误
	public static final int CRAWL_INIT = 201;
	
	//网络通信相关(thrift)
	public static final int T_TRANSPORT_ERROR = 301;
	
	//存储相关
	/**
	 * dfs接口初始化错误
	 */
	public static final int S_DFS_INIT_FAILED = 401;
	
	/**
	 * dfs存储异常
	 */
	public static final int S_DFS_STORE_EXC = 402;
	
	/**
	 * 存储失败
	 */
	public static final int S_STORE_FAILED = 403;
	
	public static final int S_NOT_FOUND_PARENT = 404;
	
	//读取配置文件出错
	public static final int CRAWLER_CONF = 501;
	
	public static final int PHOTO_CONF = 502;
	
	public static final int OPEN_CONF = 503;
	
	public static final int MESSAGE_CONF = 504;
	
	public static final int LOG_MONITOR_CONF = 504;
	
	//本地io相关
	public static final int FILE_HEADER_ERROR = 700;
	
	public static final int FILE_SYSTEM_ERROR = 701;
	
	public static final int FILE_READ_ERROR = 702;
	
	public static final int FILE_SIZE_ERROR = 703;
	
	public static final int FILE_WRITE_ERROR = 704;
	
	//manager节点读取配置文件失败
	public static final int MANAGER_CONF = 801;
	//manager节点seed文件失败
	public static final int MANAGER_LOAD_SEED = 802;
	
	//创建db失败
	public static final int BDB_INIT = 901;
	
	public static final int BDB_OPEN = 902;
	//analyzer相关
	public static final int ANALYZER_XPATH_BLOG_PASS_ERROR = 1000;
	public static final int ANALYZER_XPATH_NEWS_PASS_ERROR = 1001;
	
	public static final int ANALYZER_REGEX_BLOG_PASS_ERROR = 1010;
	public static final int ANALYZER_REGEX_NEWS_PASS_ERROR = 1011;
	
	public static final int ANALYZER_TEMPLATE_INIT_FAILED = 1060;
	
	public static final int ANALYZER_CETR_CLUSTER = 1070;
	
	public static final int ANALYZER_ABSOLUTE_URL = 1070;
	
	/**
	 * task序列化出错
	 */
	public static final int TASK_SERIALIZE_ERROR = 1101;
	
	/**
	 * task类型出错
	 */
	public static final int TASK_TYPE_ERROR = 1102;
	
	//写入中间文件序列化出错
	public static final int CONTENTPAGE_SERIALIZE_ERROR = 1201;
	public static final int CONTENTPAGE_DESERIALIZE_ERROR = 1202;
	public static final int CONTENTPAGE_TYPE_ERROR = 1203;
	
	public static final int DB_LOAD_DRIVER = 1301;
	public static final int DB_CONNECTION = 1302;
	public static final int DB_EXECUTE = 1303;
	public static final int DB_CLOSE = 1304;
	
	public static final int CROP_IMAGE_ERROR = 1404;
	
	public static final int HTTP_REQUEST_ERROR = 1501;
	
	//发送邮件失败
	public static final int MAIL_ERROR = 1601;
	//发送短消息失败
	public static final int PHONE_ERROR = 1602;
	
	//自定义插入文章错误
	public static final int CUSTOM_ARTICLE_ARG_NULL = 1701;
	public static final int CUSTOM_ARTICLE_IMAGE_ERROR = 1702;
	public static final int CUSTOM_ARTICLE_NOT_EXISTS = 1700;
}
