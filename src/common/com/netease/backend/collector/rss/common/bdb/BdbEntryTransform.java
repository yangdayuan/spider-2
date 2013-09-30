package com.netease.backend.collector.rss.common.bdb;

import com.sleepycat.je.DatabaseEntry;

public interface BdbEntryTransform {
	DatabaseEntry getKeyEntry(Object key);
	
	DatabaseEntry getValueEntry(Object value);
	
	Object getValue(DatabaseEntry valueEntry);
	
	Object getKey(DatabaseEntry keyEntry);
}
