package com.netease.backend.collector.rss.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

public class CharConvertor {
    
    private static final Logger logger = Logger.getLogger(CharConvertor.class);
    
    private static HashMap<String, String> m_wordMap = new HashMap<String, String>();
    
    static {
        try{
            InputStream input = new ClassPathResource("t2s.properties").getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            String tmp = reader.readLine();
            while (tmp != null) {
                if ((tmp.trim().length() == 2) && ((tmp.charAt(0) != '乾'))) {
                    m_wordMap.put(tmp.substring(0, 1), tmp.substring(1));
                }
                tmp = reader.readLine();
            }
            reader.close();
        } catch(IOException e){
            logger.error("", e);
        }
    }

	static public String convert(String original) {
		if (original == null) {
			return null;
		}
		if (original.trim().length() == 0) {
			return "";
		}

		if (m_wordMap.size() == 0) {
			return original;
		}
		original = original.trim();
		int len = original.length();
		String tempStr = null;
		String origStr = null;
		StringBuffer result = new StringBuffer(original.length());
		for (int i = 0; i < len; i++) {
			origStr = original.substring(i, i + 1);
			tempStr = m_wordMap.get(origStr);
			if ((tempStr != null) && (tempStr.length() == 1)) {
				result.append(tempStr);
			} else {
				result.append(origStr);
			}
		}
		return result.toString();
	}
	
}
