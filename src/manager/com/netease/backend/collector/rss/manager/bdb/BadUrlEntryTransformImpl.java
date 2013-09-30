package com.netease.backend.collector.rss.manager.bdb;

import com.netease.backend.collector.rss.common.bdb.DefaultBdbEntryTransformImpl;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.DatabaseEntry;

public class BadUrlEntryTransformImpl extends DefaultBdbEntryTransformImpl {
	@Override
	public Object getValue(DatabaseEntry valueEntry) {
		Integer value = IntegerBinding.entryToInt(valueEntry);
		return value;
	}

	@Override
	public DatabaseEntry getValueEntry(Object value) {
		DatabaseEntry valueEntry = new DatabaseEntry();
		IntegerBinding.intToEntry((Integer)value, valueEntry);
		return valueEntry;
	}

}
