package com.netease.backend.collector.rss.manager.bdb.dao;

import com.netease.backend.collector.rss.common.bdb.BdbManager;
import com.netease.backend.collector.rss.common.exception.DricException;

import java.util.ArrayList;
import java.util.List;

/**
 * sourceuuidbdb数据库反问对象
 *
 * @author LinQ
 * @version 2012-9-12
 */
public enum CacheIndexDao {
    INSTANCE;

    private static final String CACHE_SOURCE_DB_NAME = "cacheSource";
    private static final String INDEX_SOURCE_DB_NAME = "indexSource";

    private CacheIndexDao() {
        try {
            BdbManager.getInstance().open(CACHE_SOURCE_DB_NAME, null, null);
            BdbManager.getInstance().open(INDEX_SOURCE_DB_NAME, null, null);
        } catch (DricException e) {
            throw new RuntimeException("open cache source database error ...", e);
        }
    }

    /**
     * 保存缓存Uuid
     * @param sourceUuid sourceUuid
     */
    public void putCacheUuid(String sourceUuid) {
        if (sourceUuid == null)
            return;
        BdbManager.getInstance().put(CACHE_SOURCE_DB_NAME, sourceUuid, sourceUuid.getBytes());
        BdbManager.getInstance().sync(CACHE_SOURCE_DB_NAME);
    }

    /**
     * 保存索引Uuid
     * @param sourceUuid sourceUuid
     */
    public void putIndexUuid(String sourceUuid) {
        if (sourceUuid == null)
            return;
        BdbManager.getInstance().put(INDEX_SOURCE_DB_NAME, sourceUuid, sourceUuid.getBytes());
        BdbManager.getInstance().sync(INDEX_SOURCE_DB_NAME);
    }

    /**
     * 删除缓存Uuid
     * @param sourceUuid sourceUuid
     */
    public void removeCacheUuid(String sourceUuid) {
        BdbManager.getInstance().remove(CACHE_SOURCE_DB_NAME, sourceUuid);
        BdbManager.getInstance().sync(CACHE_SOURCE_DB_NAME);
    }

    /**
     * 删除缓存Uuid
     * @param sourceUuid sourceUuid
     */
    public void removeIndexUuid(String sourceUuid) {
        BdbManager.getInstance().remove(INDEX_SOURCE_DB_NAME, sourceUuid);
        BdbManager.getInstance().sync(INDEX_SOURCE_DB_NAME);
    }

    /**
     * 获取全部的sourceUuid
     * @return sourceUuid列表
     */
    public List<String> getAllCacheUuids() {
        List<String> result = new ArrayList<String>();
        List<Object> allKeys = BdbManager.getInstance().getAllValues(CACHE_SOURCE_DB_NAME);
        for (Object obj : allKeys) {
            byte[] data = (byte[]) obj;
            result.add(new String(data));
        }
        return result;
    }

    /**
     * 获取全部的sourceUuid
     * @return sourceUuid列表
     */
    public List<String> getAllIndexUuids() {
        List<String> result = new ArrayList<String>();
        List<Object> allKeys = BdbManager.getInstance().getAllValues(INDEX_SOURCE_DB_NAME);
        for (Object obj : allKeys) {
            byte[] data = (byte[]) obj;
            result.add(new String(data));
        }
        return result;
    }
}
