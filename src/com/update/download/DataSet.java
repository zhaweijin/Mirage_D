package com.update.download;

import java.util.ArrayList;

/**
 * @author zhaweijin
 * @function 
 */
public class DataSet {

    private ArrayList<WebData> webDatas = new ArrayList<WebData>();

	public ArrayList<WebData> getWebDatas() {
		return webDatas;
	}

	public WebData getWebData() {
		return webDatas.get(webDatas.size() - 1);
	}

}
