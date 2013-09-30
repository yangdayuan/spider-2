package com.netease.backend.collector.rss.common.util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * 递归读取一个多层文件夹中所有的文件
 * 每个文件的绝对路径保存在一个列表中
 * @author zhangfudong
 *
 */
public class Directory {
	private List<String> filePaths = null; 
	
	public Directory(String dirPath) {
		this.filePaths = new LinkedList<String>();
		refreshFileList(dirPath);
	}
	
    public void refreshFileList(String strPath) { 
        File dir = new File(strPath); 
        File[] files = dir.listFiles(); 
        if (files == null) 
            return; 
        for (int i = 0; i < files.length; i++) { 
            if (files[i].isDirectory()) { 
                refreshFileList(files[i].getAbsolutePath()); 
            } else { 
                String strFileName = files[i].getAbsolutePath();
                filePaths.add(strFileName);  
            } 
        }
    }

	public List<String> getFilePaths() {
		return filePaths;
	}
    
    

}
