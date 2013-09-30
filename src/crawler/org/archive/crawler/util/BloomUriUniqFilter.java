package org.archive.crawler.util;

import java.io.Serializable;

import org.archive.crawler.datamodel.CandidateURI;

public class BloomUriUniqFilter extends SetBasedUriUniqFilter
implements Serializable {
	private static final long serialVersionUID = 1061526253773091309L;

	private int count = 0;

    public BloomUriUniqFilter() {
        super();
    }

    public BloomUriUniqFilter( final int n, final int d ) {
        super();
    }

    public void forget(String canonical, CandidateURI item) {
    }

    
    protected boolean setAdd(CharSequence uri) {
       count++;
       
       return true;
    }

    protected long setCount() {
        return count;
    }

    protected boolean setRemove(CharSequence uri) {
    	boolean ret = false;
    	if (count > 0) {
    		count--;
    		ret = true;
    	}
        
    	return ret;
    }
}