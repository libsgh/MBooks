package com.libsgh.books.common;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class PicBed {
	
	protected static Logger logger = LoggerFactory.getLogger(PicBed.class);
	
	public static String uploadMT(String displayUrl, String path) {
		try {
			String p = IdUtil.fastSimpleUUID();
			Long start = System.currentTimeMillis();
			FileUtil.mkdir(path + File.separator + p);
			String extName = FileUtil.extName(getFileNameFromUrl(displayUrl));
			String filename = DigestUtil.md5Hex(displayUrl.substring(displayUrl.lastIndexOf('/')+1))
					+ "." + extName;
			File file = new File(path+File.separator+p+File.separator+filename);
			HttpUtil.downloadFile(displayUrl, file);
			String body = HttpRequest.post("https://chat.dianping.com/upload").form("file", file).execute().body();
			logger.info(body+":"+(System.currentTimeMillis()-start));
			if(JSONUtil.isJson(body)) {
				JSONObject jo = JSONUtil.parseObj(body);
				if(jo.getInt("success") == 1) {
					return jo.getStr("path");
				}
			}
		} catch (HttpException e) {
			logger.error(e.getMessage(), e);
		}
		return displayUrl;
	}
	
	private static String getFileNameFromUrl(String url) {
    	try {
			String suffixes="avi|mpeg|3gp|mp3|mp4|wav|jpeg|gif|jpg|png|apk|exe|pdf|rar|zip|docx|doc";
			Pattern pat=Pattern.compile("[\\w]+[\\.]("+suffixes+")");//正则判断
			Matcher mc=pat.matcher(url);//条件匹配
			while(mc.find()){
				return mc.group();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
    	return "";
    }
	
	public static boolean isSuccess(String mediaUrl, Integer mediaType) {
		int status = HttpRequest.get(mediaUrl).execute().getStatus();
		try {
			if(mediaType == 0) {
				if(status == 404) {
					return false;
				}else if(status == 200){
					return true;
				}
			}else {
				if(status == 302) {
					return true;
				}else if(status == 200){
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
