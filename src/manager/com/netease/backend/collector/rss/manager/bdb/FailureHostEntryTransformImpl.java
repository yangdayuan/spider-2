package com.netease.backend.collector.rss.manager.bdb;

import com.netease.backend.collector.rss.common.bdb.BdbEntryTransform;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.DatabaseEntry;

public class FailureHostEntryTransformImpl implements BdbEntryTransform {
	
	private DatabaseEntry transByString(String str) {
		DatabaseEntry entry = new DatabaseEntry();
        StringBinding.stringToEntry(str, entry);
        return entry;
	}

	@Override
	public DatabaseEntry getKeyEntry(Object key) {
		String keyStr = (String) key;
        
		return transByString(keyStr);
	}

	@Override
	public Object getValue(DatabaseEntry valueEntry) {
        Long value = LongBinding.entryToLong(valueEntry);
        
		return value;
	}

	@Override
	public DatabaseEntry getValueEntry(Object value) {
		DatabaseEntry entry = new DatabaseEntry();
        LongBinding.longToEntry((Long)value, entry);
		return entry;
	}

	@Override
	public Object getKey(DatabaseEntry keyEntry) {
		String key = StringBinding.entryToString(keyEntry);
		return key;
	}

	
}
