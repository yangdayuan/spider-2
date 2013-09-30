package com.netease.backend.collector.rss.manager.bdb;

public interface Compare {
	public boolean permit(Object obj, Object actualObj);
}
