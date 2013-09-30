package com.netease.backend.collector.rss.common.bdb;

import com.netease.backend.collector.rss.manager.bdb.Compare;
import com.sleepycat.je.*;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class BDataBase {
	private static final Logger logger = Logger.getLogger(BDataBase.class);
	
	private transient Database db = null;
	
	private BdbEntryTransform transform = null;
	
	public BDataBase(Database db, BdbEntryTransform transform) {
		super();
		this.db = db;
		this.transform = transform;
	}
	
	public List<Object> getAllValues() {
		Cursor cursor = null;
		List<Object> values = new LinkedList<Object>();
		DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry valueEntry = new DatabaseEntry();
		
		try {
			cursor = db.openCursor(null, null);
			while (cursor.getNext(keyEntry, valueEntry, null) == OperationStatus.SUCCESS) {
				if (valueEntry.getData().length == 0) {
					continue;
				}
				
				Object value = transform.getValue(valueEntry);
				if (value == null) {
					continue;
				}
				values.add(value);
			}
		} catch (DatabaseException e) {
			logger.error("", e);
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (DatabaseException e) {
					logger.error("", e);
				}
			}
		}
		
		return values;
	}
	
	public List<Object> getAllKeys() {
		Cursor cursor = null;
		List<Object> keys = new LinkedList<Object>();
		DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry valueEntry = new DatabaseEntry();
		
		try {
			cursor = db.openCursor(null, null);
			while (cursor.getNext(keyEntry, valueEntry, null) == OperationStatus.SUCCESS) {
				Object key = transform.getKey(keyEntry);
				if (key == null) {
					continue;
				}
				keys.add(key);
			}
		} catch (DatabaseException e) {
			logger.error("", e);
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (DatabaseException e) {
					logger.error("", e);
				}
			}
		}
		
		return keys;
	}
	
	public List<Object> getKeyRange(Object key, Compare cmp) {
		List<Object> results = new LinkedList<Object>();
		DatabaseEntry keyEntry = transform.getKeyEntry(key);
        DatabaseEntry valueEntry = new DatabaseEntry();
        Cursor cursor = null;
        
        try {
			cursor = db.openCursor(null, null);
	        OperationStatus ret = cursor.getSearchKeyRange(keyEntry, valueEntry, null);
	
	        while (ret == OperationStatus.SUCCESS) {
	            Object actualKey = transform.getKey(keyEntry);
	            if (!cmp.permit(key, actualKey)) {
	                break;
	            }
	            
	            Object value = transform.getValue(valueEntry);
	            results.add(value);
	            
	            ret = cursor.getNext(keyEntry, valueEntry, null);
	        }
        } catch (DatabaseException e) {
			logger.error("", e);
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (DatabaseException e) {
					logger.error("", e);
				}
			}
		}
        
        return results;
	}

	/**
     * 放入(key,value)，如果key已经存在，不存储于数据库中，并返回false，如果key不存在，则存储于数据库中，返回true
     * @param key
     * @param value
     * @return 存储成功返回true，失败返回false
     */
	public boolean putNoOverwrite(Object key, Object value) {
		boolean ret = true;
		
		OperationStatus status = null;
		DatabaseEntry keyEntry = transform.getKeyEntry(key);
		DatabaseEntry valueEntry = transform.getValueEntry(value);
		
        try {
            status = db.putNoOverwrite(null, keyEntry, valueEntry);
        } catch (DatabaseException e) {
            logger.error("", e);
            return false;
        }
        
        if (status == OperationStatus.KEYEXIST) {
            ret = false; // not added
        } else {
            ret = true;
        }
		
		return ret;
	}
	
	/**
	 * 将(key,value)存储数据库中，如果key已经存在，将替换原有的值
	 * @param key
	 * @param value
	 * @return 存储成功，返回true，否则返回false
	 */
	public boolean put(Object key, Object value) {
		boolean ret = true;
		OperationStatus status = null;
		
		DatabaseEntry keyEntry = transform.getKeyEntry(key);
		DatabaseEntry valueEntry = transform.getValueEntry(value);
        
        try {
            status = db.put(null, keyEntry, valueEntry);
        } catch (DatabaseException e) {
            logger.error("", e);
            return false;
        }
        
        if (status == OperationStatus.SUCCESS) {
            ret = true;
        } else {
            ret = false;
        }
		
		return ret;
	}
	
	/**
	 * 根据key删除数据
	 * @param key
	 * @return 删除成功返回true，否则返回false
	 */
	public boolean remove(Object key) {
		boolean ret = true;
		
		OperationStatus status = null;
		DatabaseEntry keyEntry = transform.getKeyEntry(key);
        
        try {
            status = db.delete(null, keyEntry);
        } catch (DatabaseException e) {
            logger.error("", e);
        }
        
        if (status == OperationStatus.SUCCESS) {
            ret = true; // removed
        } else {
            ret = false; // not present
        }
        
		return ret;
	}
	
	/**
	 * 根据key获取value的值
	 * @param key
	 * @return 如果该key不存在，返回null；否则返回相对应的value值
	 */
	public Object get(Object key) {
		Object value = null;
		
		OperationStatus status = null;
		DatabaseEntry keyEntry = transform.getKeyEntry(key);
		DatabaseEntry valueEntry = new DatabaseEntry();
        
        try {
            status = db.get(null, keyEntry, valueEntry, null);
        } catch (DatabaseException e) {
            logger.error("", e);
        }
        
        if (status == OperationStatus.SUCCESS) {
            value = transform.getValue(valueEntry);
        }
		
		return value;
	}
	
	/**
	 * 检查数据库中是否包含相应的key值
	 * @param key
	 * @return 存在返回true，否则返回false
	 */
	public boolean contain(Object key) {
		Object value = get(key);
		if (value == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 同步数据
	 */
	public void sync() {
		try {
			db.sync();
		} catch (DatabaseException e) {
			logger.error("", e);
		}
	}
	
	public void close() {
		if (db != null) {
			try {
		        db.sync();
				db.close();
			} catch (DatabaseException e) {
				logger.error("", e);
			}
		    db = null;
		}
	}
}
