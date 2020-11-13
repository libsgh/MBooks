package com.libsgh.books.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;
import com.libsgh.books.bean.Book;
import com.libsgh.books.bean.Chapter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;

@Service
public class MainService {
	
	private final static Logger logger = LoggerFactory.getLogger(MainService.class);
	
	@Autowired
	private DruidDataSource ds;
	
	@Autowired
	private BaiduApiImpl baiduApiImpl;
	
	@Autowired
	private BiQuGeImpl biQuGeImpl;
	
	public List<Book> allBooks() {
		try {
			return 	Db.use(ds).query("select * from book", Book.class);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	public Book addBook(Book book) {
		try {
			Entity record = Entity.create("book");
			record.set("id", IdUtil.randomUUID());
			record.set("source", book.getSource());
			record.set("name", book.getName());
			record.set("author", book.getAuthor());
			record.set("categoryName", book.getCategoryName());
			record.set("cover", book.getCover());
			record.set("shortSummary", book.getShortSummary());
			record.set("cpName", book.getCpName());
			record.set("lastChapterName", book.getLastChapterName());
			record.set("lastChapterUpdateTime", book.getLastChapterUpdateTime());
			record.set("createTime", DateUtil.currentSeconds());
			record.set("status", book.getStatus());
			record.set("use", 1);
			int c = Db.use(ds).insertOrUpdate(record, "id");
			if(c > 0) {
				book.setId(record.getStr("id"));
				book.setUse(1);
				return book;
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return book;
	}
	public void addChapter(Chapter chapter) {
		try {
			Entity record = Entity.create("chapter");
			record.set("id", IdUtil.randomUUID());
			record.set("bookId", chapter.getBookId());
			record.set("name", chapter.getName());
			record.set("content", chapter.getContent());
			record.set("urls", StrUtil.join(",", chapter.getUrls()));
			record.set("updateTime", chapter.getUpdateTime());
			record.set("index", chapter.getIndex());
			int c = Db.use(ds).insertOrUpdate(record, "id");
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		
	}
	@Async
	public Boolean fetchOneBook(String name) {
		List<Chapter> bdList = new ArrayList<Chapter>();
		List<Chapter> bqList = new ArrayList<Chapter>();
		Book book = baiduApiImpl.searchByName(name).get(0);
		book = baiduApiImpl.getBookInfo(book);
		baiduApiImpl.chapterList(bdList, book.getSource(), 1);
		String thirdSource = biQuGeImpl.searchByName(name).get(2).getSource();
		book.setSource(thirdSource);
		book = this.addBook(book);
		biQuGeImpl.chapterList(bqList, thirdSource, 0);
		for (Chapter bdChapter : bdList) {
			for (Chapter bqChapter : bqList) {
				if(StrUtil.subAfter(bdChapter.getName(), "章 ", false).equals(StrUtil.subAfter(bqChapter.getName(), "章 ", false))) {
					Chapter chapter = biQuGeImpl.chapterContent(bqChapter);
					chapter.setName(bdChapter.getName());
					chapter.setBookId(book.getId());
					chapter.setUpdateTime(bdChapter.getUpdateTime());
					chapter.setWordSum(bdChapter.getWordSum());
					this.addChapter(chapter);
					break;
				}
			}
		}
		return true;
	}
	public Entity getChapterById(String cid) {
		try {
			return Db.use(ds).get("chapter", "id", cid);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
