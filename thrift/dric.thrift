/**
网易阅读分布式信息抓取系统网络通信thrift接口定义文件
@author wuliufu
@since 2010-07-29
*/
namespace java com.netease.backend.collector.rss.common.net

struct TURLInfo {
	1:string uurl="",
	2:string pathFromSeed ="",
	3:string via ="",
	4:i64 modifyTime =0,
	5:i64 downLoadTime =0,
	6:i64 reuseInterval =0,
	
	7:bool seed,
	8:binary attach
}

struct TUrlInfoResult {
	1:list<TURLInfo> exitDiffViaUrlInfos,
	2:list<TURLInfo> pendingURLInfos
}

/**
*适用于图集类传递图片及其描述等信息
*/
struct TImageItem {
1: string url,
2: string description
}

struct TArticle {
1: string title,
2: string content,
3: string summary,
4: i64 publishTime,
6: string originalUrl,
7: string articleFrom,
8: i32 articleType,
9: i32 topDay,
10: list<TImageItem> imageItems,
11: string articleUuid=""
}


/**
*节点信息
*/
struct TNodeInfo {
1: i32 nodeId = 0,
2: bool updateArticle,
3: list<string> duplicFilterServers,
4: string clientInfo
}


/*网络服务异常接口*/
exception DTException {
  1: string message,
  2:i32 errorCode
}

/**
*所有service的基类，定义一些通用接口
*/
service BaseService
{
	void ping(1: string server, 2:i32 port)throws (1:DTException e),
}


/**
*中心节点通信服务类
*/
service TControlService  extends BaseService{
	list<TURLInfo> requestURL() throws (1:DTException e),
	TUrlInfoResult sendURL(1:list<TURLInfo> urlInfos) throws (1:DTException e),
	bool updateTime(1:string uuri, 2:i64 modifyTime, 3:i64 downLoadTime) throws (1:DTException e),
}
