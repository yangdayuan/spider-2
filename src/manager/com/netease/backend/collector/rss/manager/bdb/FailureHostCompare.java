package com.netease.backend.collector.rss.manager.bdb;

public class FailureHostCompare implements Compare {
	private static final String HTTP = "http://";
	@Override
	public boolean permit(Object obj, Object actualObj) {
		String host = ((String)obj).toLowerCase();
		if (!host.startsWith(HTTP)) {
			host = HTTP + host;
		}
		
		String actualKey = ((String)actualObj).toLowerCase();
		if (actualKey.startsWith(host)) {
			return true;
		} else {
			return false;
		}
	}

}
