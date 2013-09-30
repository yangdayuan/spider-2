/**
 * 
 */
package com.netease.backend.collector.rss.common.util.identify.impl;

import st.ata.util.FPGenerator;

import com.netease.backend.collector.rss.common.util.identify.IIdentifyGenerator;

/**
 * @author wuliufu
 */
public abstract class IdentifyGenerator<T> implements IIdentifyGenerator<T> {
	private static final String COLON_SLASH_SLASH = "://";
	
	/**
     * Create fingerprint.
     * Pubic access so test code can access createKey.
     * @param uri URI to fingerprint.
     * @return Fingerprint of passed <code>url</code>.
     */
    public static long createKey(CharSequence uri) {
        String url = uri.toString();
        int index = url.indexOf(COLON_SLASH_SLASH);
        if (index > 0) {
            index = url.indexOf('/', index + COLON_SLASH_SLASH.length());
        }
        CharSequence hostPlusScheme = (index == -1)? url: url.subSequence(0, index);
        long tmp = FPGenerator.std24.fp(hostPlusScheme);
        return tmp | (FPGenerator.std40.fp(url) >>> 24);
    }
}
