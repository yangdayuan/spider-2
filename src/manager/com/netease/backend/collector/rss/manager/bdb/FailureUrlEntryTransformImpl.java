package com.netease.backend.collector.rss.manager.bdb;

import com.netease.backend.collector.rss.common.bdb.DefaultBdbEntryTransformImpl;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.DatabaseEntry;

public class FailureUrlEntryTransformImpl extends DefaultBdbEntryTransformImpl {
	@Override
	public DatabaseEntry getKeyEntry(Object key) {
		String keyStr = (String) key;
		DatabaseEntry keyEntry = new DatabaseEntry();
        StringBinding.stringToEntry(keyStr, keyEntry);
        
		return keyEntry;
	}
	
	@Override
	public Object getKey(DatabaseEntry keyEntry) {
		String key = StringBinding.entryToString(keyEntry);
		
		return key;
	}
}
