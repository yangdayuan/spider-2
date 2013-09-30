/**
 * 
 */
package com.netease.backend.collector.rss.common.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;

/**
 * @author wuliufu
 */
public class DcasDataOutputStream extends DataOutputStream {

	public DcasDataOutputStream(OutputStream out) {
		super(out);
	}
	
	public void writeString(String src) throws IOException {
		src = StringUtils.trimToEmpty(src);
		writeInt(src.length()); 
		writeChars(src);
	}

}
