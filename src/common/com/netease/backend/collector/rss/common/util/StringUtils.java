package com.netease.backend.collector.rss.common.util;

/**
 * String工具类
 *
 * @author LinQ
 * @version 2012-2-7
 */
public class StringUtils {
    private StringUtils(){}

    /**
     * 去除的特殊的unicode字符
     *
     * @param original 原始字符串
     * @return 去除不存在字符后的字符串
     */
    public static String trimUnicode(String original) {
        StringBuilder builder = new StringBuilder();
        for (char c : original.toCharArray()) {
            int code = (int) c;
            if (code < 55296 || code > 63743)
                builder.append(c);
        }
        return builder.toString();
    }
}
