package com.netease.backend.collector.rss.common.consts;


public class Consts {
	public static final String CRAWLER_ALREADY = ".already";
	public static final String ANALYZER_DOING = ".doing";
	public static final String ANALYZER_INCORRECT = ".incorrect";
	
	public static final long ONE_WEEK = 7L* 24 * 60 * 60 * 1000L;
	
	/*
	 * 下载数据类型
	 */
	public static final int DRIC_CONTENT_PAGE_UNKNOWN = -1;
	public static final int DRIC_CONTENT_PAGE_SOURCE = 0;
	//新闻
	public static final int DRIC_CONTENT_PAGE_NEWS = 1;
	//博客
	public static final int DRIC_CONTENT_PAGE_BLOG = 2;
	//需要模板是配的相册图集类型(相册)
	public static final int DRIC_CONTENT_PAGE_ALBUM = 3;
	//论坛
	public static final int DRIC_CONTENT_PAGE_FORUM = 4;
	//图集
	public static final int DRIC_CONTENT_PAGE_PHOTOVIEW = 5;
	// 微博
	public static final int DRIC_CONTENT_PAGE_TWITTER = 6;
	//由接口完成的相册类型
	public static final int DRIC_CONTENT_PAGE_PHOTO = 7;
	//爱搭配
	public static final int DRIC_CONTENT_PAGE_ISTYLE = 8;
    // 网易应用
	public static final int DRIC_CONTENT_PAGE_NETEASE_APP = 9;
	//视频
	public static final int DRIC_CONTENT_PAGE_VIDEO = 10;
	//音频
	public static final int DRIC_CONTENT_PAGE_AUDIO = 11;
	
	//音视频中间页面
	public static final int DRIC_CONTENT_PAGE_TRAN_AUDIO = 12;
	//报纸文章类型
	public static final int DRIC_CONTENT_PAGE_NEWSPAPER = 13;
	

    public static final int DRIC_CONTENT_PAGE_SPECIAL = 99;
    public static final int DRIC_CONTENT_PAGE_NEWSPAPER_SECTION = 98;
    public static final int DRIC_VIDEO_PAGE_SPECIAL = 100;
	

	//24*60*60*1000
	public static final long MILLIS_PER_DAY = 86400000;
	
	/**
	 * 文章已经全部抓取完毕
	 */
	public static final int ARTICLE_FETCHED_FINISHED = 1;
	
	/**
	 * 文章还未抓取完毕
	 */
	public static final int ARTICLE_FETCHED_UNFINISHED = 0;
	
	//文章第一页
	public static final int FIRST_PAGE = 1;
	
	/*
	 * 	最初生成的图片比较将被替换成"#$#IMGTAG#1","#$#IMGTAG#2"这个样式，
	 *  在图片被实际下载后再重新替换成html标记
	 *  
	 *  类似，多页的新闻在第一页被下载后会在正文后加上"#$#NEXTTAG#1", "#$#NEXTTAG#2"这样的标记
	 *  在后续页面实际下载后将这些标记替换成正文
	 *  
	 *  在这两类标记被完全替换掉之前，这篇新闻不会被推送给用户
	 */

	public static final String NEWS_IMG_TAG = "###IMGTAG#";
	public static final String NEWS_NEXTPAGE_TAG = "###NEXTTAG#";
	
	//摘要程序自动截取
	public static final int SUMMARY_AUTO_GENERATE = 0;
	
	//摘要由编辑获得
	public static final int SUMMARY_EDIT_GENERATE = 1;
	
	/**
	 * UrlInfo中urlType 类型
	 */
	public static final int URL_SOURCE_TYPE = 0;
	
	public static final int URL_ARTICLE_TYPE = 1;
	
	public static final int URL_IMAGE_TYPE = 2;
	
	public static final int URL_PHOTO_TYPE = 3;
	
	public static final int URL_ICON_TYPE = 4;
	
	public static final int URL_PDF_TYPE = 5;
	
	public static final int URL_VIDEO_PAGE_TYPE = 6;
	
	public static final int URL_TRANSITION_VIDEO_PAGE_TYPE = 7;
	
	//淘宝单品页面类型
	public static final int URL_TMALL_PAGE_TYPE = 8;
	
	public static final int URL_VIDEO_COVER_TYPE = 998;

	/**
	 * 源的类型，包括rss,html,ebook,video等
	 */
	public static final int SOURCE_RSS_TYPE = 1;

	public static final int SOURCE_HTML_TYPE = 2;

    public static final int SOURCE_JSON_TYPE = 3;

    public static final int SOURCE_TWITTER_TYPE = 4;
    
    public static final int SOURCE_PODCAST_TYPE = 5;
    
    public static final int SOURCE_JSON_PATH_TYPE = 7;

	public static final int SOURCE_VIDEO_TYPE = 9;


	
	//源的状态
	public static final int SOURCE_STATE_DEL = -1;
	
	public static final int SOURCE_STATE_VALID = 0;
	
	public static final int SOURCE_STATE_TEST = 1;
	
	public static final int SOURCE_STATE_INVALID = 2;
	/**
	 * 不可再订阅
	 */
	public static final int SOURCE_STATE_UNSUBSCRIBE = 3;
	
	//版式类型
	/**
	 * 普通新闻版式
	 */
	public static final int LAYOUT_TYPE_COMMENT = 0;
	
	/**
	 * 相册/图片版式
	 */
	public static final int LAYOUT_TYPE_PHOTO = 1;
	
	/**
	 * 图片新闻版式
	 */
	public static final int LAYOUT_TYPE_PICTURENEWS = 2;
	
	/** 
	 * 微博版式
	 */
	public static final int LAYOUT_TYPE_TWITTER = 3;
	
	/**
	 * 爱搭配版式
	 */
	public static final int LAYOUT_TYPE_ISTYLE = 4;
	
	/**
	 * 论坛版式
	 */
	public static final int LAYOUT_TYPE_FORUM = 5;
	
	/**
	 * 英文版式
	 */
	public static final int LAYOUT_TYPE_ENGLISH = 6;
	
	/**
	 * 视频/公开课版式
	 */
	public static final int LAYOUT_TYPE_VIDEO = 7;
	
	/**
	 * 电子书版式
	 */
	public static final int LAYOUT_TYPE_BOOK = 8;
	
	/**
	 * 电子杂志版式
	 */
	public static final int LAYOUT_TYPE_MAGAZINE = 9;
	
	/**
	 * 自定义RSS版式
	 */
	public static final int LAYOUT_TYPE_RSS = 10;
	
	/**
	 * 音频版式
	 */
	public static final int LAYOUT_TYPE_AUDIO = 11;

    public static final int LAYOUT_TYPE_PDF = 12;
	
	
	public static final long NEWS_REUSE_INTERVAL = -1;
	public static final long VIDEO_PAGE_REUSE_INTERVAL = -1;
	public static final long BLOG_REUSE_INTERVAL = 7*24*60*60*1000;
	public static final long PHOTOVIEW_REUSE_INTERVAL = -1;
	public static final long IMAGE_REUSE_INTERVAL = -1;
	public static final long AVATARS_REUSE_INTERVAL = 7*24*60*60*1000;
	
	public static final int NEED_FETCH_ICON_YES = 1;
	public static final int NEED_FETCH_ICON_NO = 0;
	
	public static final int UNFIXED_YES = 1;
	
	/**
	 * 正文页面版式类型
	 */
	public static final int PAGE_LAYOUT_MULTI_FIRST = 1;

    /**
     * 添加rss原文链接
     */
	public static final int PAGE_LAYOUT_NORMAL = 0;

	/**抓取方案*/
	//全部
	public static final int FETCH_PRIORITY_ALL = -1;
	/**
	 * 正常
	 */
	public static final int FETCH_PRIORITY_NORMAL = 0;
	
	//多图
	public static final int FETCH_PRIORITY_MUCH_PICTURE = 1;
	
	//新闻实时
	public static final int FETCH_PRIORITY_REALTIME = 2;

    /**
     * 抓取类型，相对于ArticleSource的FetchType字段
     */
    public static class FetchType {
        public static final int CRAWLER = 0;
        public static final int LOFTER = 1;
        public static final int TWITTER = 2;
        public static final int TOWN = 3;
        public static final int ISTYLE = 4;
        public static final int NETEAES_APP = 5;
    }

    public static class Reserved {
        public static final int SPECIAL = 3;
    }

    /**
     * 链接状态,0去除链接，1保留链接
     */
    public static class LinkStatus {
        public static final int REMOVE_LINK = 0;
        public static final int REPLACE_LINK = 1;
        public static final int WEIBO_LINK = 2;
    }

    public static class Blacklist {
        public static final int BLACK = 1;
    }

    public static class SpecialType {
        public static final int SPECIAL = 1;
        public static final int NEWSPAPER = 2;
        public static final int NEWSPAPER_SECTION = -1;
    }

    public static final String SpecialName = "专题";

    public static class ArticleStatus {
        public static final int BLACK = -2;
    }
    
    public static final String UTF8 = "UTF-8";
    
    /**
     * 书籍性质描述
     */
    public static final String PAY_DESC_PAY = "VIP作品";
    public static final String PAY_DESC_FREEREAD = "免费全本";
    public static final String PAY_DESC_TRYREAD = "免费节选";
    
    /**
     * 是否客戶端处理的书
     */
    
    public static final int  NOT_CLIENT_HANDLER=0;
    public static final int  IS_CLIENT_HANDLER=1;

    // 是否需要支付，0：否，1：是
    public static final class NeedPay {
        public static final int YES = 1;
        public static final int NO = 0;
    }

    /**
     * 文章版式，0：新闻普通版式、1：新闻分页分栏版式、2：相册普通版式、3：相册版式化、4：微博、5：论坛、6：视频
     */
    public static final class ArticleStyle {
        public static final int NORMAL_NEWS = 0;
        public static final int MULTI_NEWS = 1;
        public static final int NORMAL_PHOTO = 2;
        public static final int STYLE_PHOTO = 3;
        public static final int TWITTER = 4;
        public static final int FORUM = 5;
        public static final int VIDEO = 6;
    }
    
    /**
     * 快速生成epub类型,增加或修改章节
     */
    public static final String RAPID_ADD_TO_EPUB="RAPID_ADD_TO_EPUB";
    
    public static final String RAPID_UPDATE_TO_EPUB="RAPID_UPDATE_TO_EPUB";
    
    /**
     * 基本数据类型所占字节数
     */
    public static final int INT_SZIE = 4;
    public static final int LONG_SZIE = 8;
    public static final int BYTE_SZIE = 1;
    public static final int CHAR_SZIE = 2;
    
    public static final long MILLIS_PER_HOUR = 60 * 60 * 1000;

    public static class SpecialCaseKey {
        public static final String CALLBACK_URL = "callbackUrl";
    }
}
