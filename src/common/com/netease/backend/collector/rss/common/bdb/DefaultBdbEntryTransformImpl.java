package com.netease.backend.collector.rss.common.bdb;

import st.ata.util.FPGenerator;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleInputBinding;
import com.sleepycat.je.DatabaseEntry;

public class DefaultBdbEntryTransformImpl implements BdbEntryTransform {
	private static final String COLON_SLASH_SLASH = "://";
	
	/**
     * Create fingerprint.
     * Pubic access so test code can access createKey.
     * @param uri URI to fingerprint.
     * @return Fingerprint of passed <code>url</code>.
     */
    private static long createKey(CharSequence uri) {
        String url = uri.toString();
        int index = url.indexOf(COLON_SLASH_SLASH);
        if (index > 0) {
            index = url.indexOf('/', index + COLON_SLASH_SLASH.length());
        }
        CharSequence hostPlusScheme = (index == -1)? url: url.subSequence(0, index);
        long tmp = FPGenerator.std24.fp(hostPlusScheme);
        return tmp | (FPGenerator.std40.fp(url) >>> 24);
    }
    
	@Override
	public DatabaseEntry getKeyEntry(Object key) {
		String keyStr = (String) key;
		DatabaseEntry keyEntry = new DatabaseEntry();
        LongBinding.longToEntry(createKey(keyStr), keyEntry);
        
		return keyEntry;
	}

	@Override
	public DatabaseEntry getValueEntry(Object value) {
		DatabaseEntry valueEntry = new DatabaseEntry();
        TupleInput input = new TupleInput((byte[])value);
        TupleInputBinding tupleInputBinding = new TupleInputBinding();
        tupleInputBinding.objectToEntry(input, valueEntry);
        
		return valueEntry;
	}

	@Override
	public Object getValue(DatabaseEntry valueEntry) {
		TupleInputBinding tupleInputBinding = new TupleInputBinding();
		TupleInput input = tupleInputBinding.entryToObject(valueEntry);
        
		return input.getBufferBytes();
	}

	@Override
	public Object getKey(DatabaseEntry keyEntry) {
		return LongBinding.entryToLong(keyEntry);
	}

}
