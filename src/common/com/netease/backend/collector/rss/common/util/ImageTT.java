package com.netease.backend.collector.rss.common.util;

import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

public class ImageTT {

	/**
	 * @param args
	 * @throws com.netease.backend.collector.rss.common.exception.DricException
	 * @throws Exception 
	 */
	public static void main(String[] args) throws DricException, Exception {
		String srcPath = "../logo.png";
		String desPath = "result.jpg";
		IMOperation op = new IMOperation();
		op.addImage();
		op.background("white");
        op.flatten();
        op.p_profile("*");
        op.resize(null, 250);
		op.addImage();
        System.out.println(op.toString());
		ConvertCmd convert = new ForWinConvertCmd(true);
		try {
			convert.run(op, srcPath, desPath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DricException("crop Image args error", ErrorCode.CROP_IMAGE_ERROR);  
		}
	}

}
