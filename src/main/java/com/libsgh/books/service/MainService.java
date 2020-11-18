package com.libsgh.books.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import cn.hutool.core.util.PageUtil;
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
			record.set("id", IdUtil.objectId());
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
			record.set("id", IdUtil.objectId());
			record.set("bookId", chapter.getBookId());
			record.set("name", chapter.getName());
			record.set("content", chapter.getContent());
			record.set("urls", StrUtil.join(",", chapter.getUrls()));
			record.set("updateTime", chapter.getUpdateTime());
			record.set("index", chapter.getIndex());
			Db.use(ds).insertOrUpdate(record, "id");
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
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
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
			return Db.use(ds).queryOne("SELECT a\n" + 
					"	.*,\n" + 
					"	b.NAME AS bookName,\n" + 
					"	b.author AS author \n" + 
					"FROM\n" + 
					"	chapter a\n" + 
					"	LEFT JOIN book b ON b.id = a.\"bookId\"\n" + 
					"WHERE\n" + 
					"	a.id = ?", cid);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public static void main(String[] args) {
		
	}
	
	public Entity getChapterListById(String bid, Integer page, String order) {
		try {
			if(page == null || page < 1) {
				page = 1;
			}
			Entity entity = Db.use(ds).get("book", "id", bid);
			int totalCount = Db.use(ds).queryNumber("select count(*) from chapter where \"bookId\"=?", bid).intValue();
			int tatalPage = PageUtil.totalPage(totalCount, 50);
			if(page > tatalPage) {
				page = tatalPage;
			}
			int[] startEnd = PageUtil.transToStartEnd(page - 1, 50);
			List<Entity> list =  Db.use(ds).query("SELECT" + 
					" a.name, a.id" + 
					" FROM " + 
					"	chapter a" + 
					"	WHERE a.\"bookId\"=?" + 
					"	ORDER BY a.index "+order+" limit 50 offset "+startEnd[0], bid);
			entity.put("chapterList", list);
			entity.put("pages", page);
			entity.put("order", order);
			List<Map<String, Object>> pageList = new ArrayList<Map<String, Object>>();
			for (int i = 1; i <= tatalPage; i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				int pageBegin =  (i-1)*50+1;
				int pageEnd = i * 50 > totalCount ? totalCount: pageBegin + 50 - 1;
				map.put("pageBegin", pageBegin);
				map.put("pageEnd", pageEnd);
				pageList.add(map);
			}
			if(order.equals("desc")) {
				Collections.reverse(pageList);
			}
			for (int i = 0; i < pageList.size(); i++) {
				if(page == (i+1)) {
					pageList.get(i).put("checked", true);
					entity.put("pageBegin", pageList.get(i).get("pageBegin"));
					entity.put("pageEnd", pageList.get(i).get("pageEnd"));
				}else{
					pageList.get(i).put("checked", false);
				}
			}
			entity.put("pageList", pageList);
			return entity;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public Entity getBookBiId(String bid) {
		try {
			Entity entity = Db.use(ds).get("book", "id", bid);
			if(entity.getStr("shortSummary").length() > 50) {
				entity.put("shortSummarySub", StrUtil.sub(entity.getStr("shortSummary"), 0, 50)+"......");
			}else {
				entity.put("shortSummarySub", entity.getStr("shortSummary"));
			}
			List<Entity> list = Db.use(ds).query("select name,id from chapter where \"bookId\"=? order by index desc limit 5", bid);
			entity.put("chapters", list);
			return entity;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public List<Entity> queryAllBooks() {
		try {
			List<Entity> list = Db.use(ds).query("select * from book order by \"lastChapterUpdateTime\" desc");
			return list;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
}
