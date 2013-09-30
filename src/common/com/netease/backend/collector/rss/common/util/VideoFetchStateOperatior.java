/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

/**
 * 视频抓取状态操作
 * @author wuliufu
 */
public class VideoFetchStateOperatior {
	
	/**
	 * 更新抓取状态值
	 * @param fetchState 抓取状态值
	 * @param videoType 待设置格式类型
	 * @param state   待设置状态值
	 * @return 更新后的抓取状态值
	 */
	public static int updateVideoState(int fetchState, VideoType videoType, VideoFetchState state) {
		fetchState = clearZero(fetchState, videoType);
		int st = state.getState() << (videoType.getType() * 4);
		fetchState = fetchState | st;
		return fetchState;
	}
	
	/**
	 * 封面抓取状态使用末2bit表示
	 * @param videoFetchState
	 * @param state
	 * @return
	 */
	public static int updateVideoCoverState(int videoImageFetchState, VideoFetchState state) {
		for(int i = 0; i < 2; i++) {
			videoImageFetchState = BitUtil.clear(videoImageFetchState, i);
		}
		videoImageFetchState = videoImageFetchState | state.getState();
		return videoImageFetchState;
	}
	
	/**
	 * 检查是否已经正常抓到封面
	 * @param videoImageFetchState
	 * @return
	 */
	public static boolean checkCoverFetched(int videoImageFetchState) {
		int state = videoImageFetchState & 0x3;
		if(state != 0x1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 检查音视频是否抓取完成
	 * @param fetchState
	 * @return
	 */
	public static boolean checkFetchedEnd(int fetchState) {
		for(int i = 0; i <  VideoType.size(); i++) {
			int state = fetchState & 0xf;
			if(state == 1 || state == 2) {
				return false;
			}
			fetchState = fetchState >>> 4;
		}
		return true;
	}
	
	/**
	 * 把指定格式类型的状态值清零
	 * @param fetchState 抓取状态值
	 * @param videoType 待设置格式类型
	 * @return 指定格式类型的状态值清零
	 */
	private static int clearZero(int fetchState, VideoType videoType) {
		for(int i = videoType.getType() * 4; i < (videoType.getType() + 1) * 4; i++) {
			fetchState = BitUtil.clear(fetchState, i);
		}
		return fetchState;
	}
	
	public static void main(String[] args) {
		int fetchState = 0;
		fetchState = updateVideoState(fetchState, VideoType.M3U8_OR_AUDIO, VideoFetchState.CHECKED);
		fetchState = updateVideoState(fetchState, VideoType.FLASH, VideoFetchState.CHECKED);
		System.out.println(Integer.toBinaryString(fetchState));
		System.out.println(checkFetchedEnd(fetchState));
	}
	
	public static enum VideoType {
		M3U8_OR_AUDIO(0),
		MP4(1),
		FLASH(2);
		
		private int type;

		private VideoType(int type) {
			this.type = type;
		}

		/**
		 * 获取type
		 * @return type type
		 */
		public int getType() {
			return type;
		}
		
		/**
		 * 获取一共有几个枚举
		 * @return
		 */
		public static int size() {
			return 3;
		}
		
	}
	
	public static enum VideoFetchState {
		/**
		 * 无此格式音视频
		 */
		NONE(0),
		/**
		 * 等待抓取
		 */
		FETCHING(1),
		/**
		 * 抓取完毕
		 */
		FETCHED(2),
		/**
		 * 检查完毕，如果检查失败把值设置成NONE
		 */
		CHECKED(3);
		
		private int state;

		private VideoFetchState(int state) {
			this.state = state;
		}

		/**
		 * 获取state
		 * @return state state
		 */
		public int getState() {
			return state;
		}	
	}
}
