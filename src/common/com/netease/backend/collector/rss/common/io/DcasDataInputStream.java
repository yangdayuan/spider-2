/**
 * 
 */
package com.netease.backend.collector.rss.common.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author wuliufu
 */
public class DcasDataInputStream extends DataInputStream {

	public DcasDataInputStream(InputStream in) {
		super(in);
	}
	
	public String readString() throws IOException {
		int strLen = readInt();
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < strLen; i++) {
			char c = readChar();
			buf.append(c);
		}
		return buf.toString();
	}

}
