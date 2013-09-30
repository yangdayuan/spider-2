package com.netease.backend.collector.rss.common.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.ArrayList;
import java.util.List;

/**
 * 特殊加载的源
 *
 * @author LinQ
 * @version 2012-3-16
 */
public class SpecialSource {
    private List<String> sources = new ArrayList<String>();

    private static SpecialSource instance;

    static {
        try {
            instance = new SpecialSource();
            instance.init();
        } catch (DocumentException e) {
            instance = new SpecialSource();
            e.printStackTrace();
        }
    }

    private SpecialSource() {
    }

    public void init() throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(SpecialSource.class.getClassLoader().getResourceAsStream("special.xml"));
        Element root = document.getRootElement();
        for (Object object : root.elements()) {
            Element element = (Element) object;
            String source = element.getTextTrim();
            if (source != null && !"".equals(source)) {
                sources.add(source);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(SpecialSource.getInstance().sources.size());
        System.out.println(SpecialSource.getInstance().isSpecialSource("xx"));
    }

    public static SpecialSource getInstance() {
        return instance;
    }

    public boolean isSpecialSource(String sourceUuid) {
        return sources.contains(sourceUuid);
    }
}