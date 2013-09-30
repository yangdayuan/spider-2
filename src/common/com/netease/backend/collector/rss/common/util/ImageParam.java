package com.netease.backend.collector.rss.common.util;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

/**
 * 图片转换参数
 *
 * @author LinQ
 * @version 2012-3-12
 */
public class ImageParam {
    private static final JsonConfig config = new JsonConfig();

    static {
        config.setJsonPropertyFilter(new PropertyFilter() {
            @Override
            public boolean apply(Object o, String s, Object o1) {
                return s.equals("background");
            }
        });
    }

    private double quality = 75;
    private double sharpen;
    /**
     * 是否设置背景色
     */
    private boolean background = false;
    /**
     * 图片url
     */
    private String url;

    /**
     * 从json转换一个ImageConvertOption对象
     * @param json 数据库中保存的json
     * @return ImageConvertOption对象
     */
    public static ImageParam fromJson(String json) {
        JSONObject jsonObject = JSONObject.fromObject(json, config);
        return (ImageParam) JSONObject.toBean(jsonObject, ImageParam.class);
    }

    public String toJSON() {
        JSONObject jsonObject = JSONObject.fromObject(this);
        return JSONSerializer.toJSON(jsonObject, config).toString();
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public double getSharpen() {
        return sharpen;
    }

    public void setSharpen(double sharpen) {
        this.sharpen = sharpen;
    }

    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageParam that = (ImageParam) o;

        if (Double.compare(that.quality, quality) != 0) return false;
        if (Double.compare(that.sharpen, sharpen) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = quality != +0.0d ? Double.doubleToLongBits(quality) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = sharpen != +0.0d ? Double.doubleToLongBits(sharpen) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
