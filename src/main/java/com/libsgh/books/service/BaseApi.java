package com.libsgh.books.service;

import java.util.List;

import com.libsgh.books.bean.Book;
import com.libsgh.books.bean.Chapter;

public interface BaseApi {
	
	public List<Book> searchByName(String name);//搜搜哦小说
	
	public Book getBookInfo(Book book);//补充查询小说信息
	
	public void chapterList(List<Chapter> list, String bookUrl, int num);//查询所有章节
	
	public Chapter chapterContent(Chapter chapter);//查询单章小说内容
	
	
}
