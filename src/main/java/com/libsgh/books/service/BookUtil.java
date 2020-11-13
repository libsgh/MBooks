package com.libsgh.books.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class BookUtil {
	public static void main(String[] args) {
		//System.out.println(queryBooks("万古神帝").toStringPretty());//万古神帝
		System.out.println(getBookInfo("4315646971").toStringPretty());//万古神帝
		//System.out.println(getBookInfo("4315646971").toStringPretty());//万古神帝
	}
	
	public static JSONObject queryBooks(String searchKey) {
		String result = HttpUtil.get("https://dushu.baidu.com/api/getSearchResultData?query="+searchKey);
		return JSONUtil.parseObj(result);
	}
	
	public static JSONObject getBookInfo(String gid) {
		String html = HttpUtil.get("https://boxnovel.baidu.com/boxnovel/detail?gid="+gid);
		String result = StrUtil.subBetween(html, "data: ", "});");
		return JSONUtil.parseObj(result);
	}
	
	public static JSONObject getBookInfo(String gid, Integer pageNum) {
		String html = HttpUtil.get("https://boxnovel.baidu.com/boxnovel/wiseapi/chapterList?bookid="+gid+"&pageNum=1&order=asc&site=");
		String result = StrUtil.subBetween(html, "data: ", "});");
		return JSONUtil.parseObj(result);
	}
	
}
