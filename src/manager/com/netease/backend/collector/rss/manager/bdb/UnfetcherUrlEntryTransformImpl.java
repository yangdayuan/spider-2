package com.netease.backend.collector.rss.manager.bdb;

import com.netease.backend.collector.rss.common.bdb.DefaultBdbEntryTransformImpl;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.DatabaseEntry;

public class UnfetcherUrlEntryTransformImpl extends DefaultBdbEntryTransformImpl {
	@Override
	public Object getValue(DatabaseEntry valueEntry) {
		long value = LongBinding.entryToLong(valueEntry);
		return value;
	}

	@Override
	public DatabaseEntry getValueEntry(Object value) {
		DatabaseEntry valueEntry = new DatabaseEntry();
		LongBinding.longToEntry((Long)value, valueEntry);
		return valueEntry;
	}

}
