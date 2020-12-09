package com.libsgh.books.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.libsgh.books.bean.Book;
import com.libsgh.books.bean.BookDown;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class CommonApi {
	
	public Book esouBookInfo(Book book, Integer index) {
		String body = HttpUtil.createGet(String.format("http://api.easou.com/api/bookapp/searchdzh.m?word=%s&page_id=1&count=20&cid=eef_&os=ios&appverion=1049",
				book.getName())).execute().body();
		JSONObject jo = JSONUtil.parseObj(body);
		String category = jo.getByPath("$.all_book_items["+index+"].category", String.class);
		String desc = jo.getByPath("$.all_book_items["+index+"].desc", String.class);
		String status = jo.getByPath("$.all_book_items["+index+"].status", String.class);
		String site = jo.getByPath("$.all_book_items["+index+"].site", String.class);
		String author = jo.getByPath("$.all_book_items["+index+"].author", String.class);
		String imgUrl = StrUtil.replace(jo.getByPath("$.all_book_items["+index+"].imgUrl", String.class), "http:", "https:", false);
		Long wordCount = jo.getByPath("$.all_book_items["+index+"].wordCount", Long.class);
		Long readCount = jo.getByPath("$.all_book_items["+index+"].readCount", Long.class);
		book.setAuthor(author);
		book.setShortSummary(desc);
		book.setCover(imgUrl);
		book.setCpName(site);
		book.setStatus(status);
		book.setCategoryName(category);
		book.setWordCount(wordCount);
		book.setReadCount(readCount);
		book.setDownloadUrl("");
		return book;
	}
	
	public String getDownloadUrl(Book book) {
		String body = HttpUtil.get("http://downnovel.com/search.htm?keyword="+book.getName());
		Document doc = Jsoup.parse(body);
		Elements els = doc.select(".book_textList2 li");
		List<BookDown> list = els.stream().map(element->{
			BookDown bd = new BookDown();
			String bookName = element.select("a").text();
			String dUrl = element.select("a").attr("href");
			String author = StrUtil.trim(StrUtil.subBetween(element.html(), "</a>　/", "<span"));
			String date = element.select(".time").text();
			date = DateUtil.thisYear() + "-" + date;
			bd.setName(bookName);
			bd.setAuthor(author);
			bd.setDateTime(DateUtil.parse(date,"yyyy-MM-dd HH:mm").getTime());
			bd.setDUrl("http://downnovel.com"+dUrl);
			return bd;
		}).filter(bd -> StrUtil.contains(bd.getName(), book.getName()) && StrUtil.contains(bd.getAuthor(),book.getAuthor()))
		.sorted(Comparator.comparing(BookDown::getDateTime).reversed())
		.collect(Collectors.toList());
		String url = "";
		if(list.size() > 0) {
			body = HttpUtil.get(list.get(0).getDUrl());
			doc = Jsoup.parse(body);
			url = doc.selectFirst(".btn_b").attr("href");
		}
		return url;
	}
}
