package com.libsgh.books.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	public Boolean fetchOneBook(String name, Integer index) {
		List<Chapter> bqList = new ArrayList<Chapter>();
		Book book = biQuGeImpl.searchByName(name).get(index);
		Book baiduBook = baiduApiImpl.searchByName(name).get(index);
		book.setCategoryName(baiduBook.getCategoryName());
		book.setCpName(baiduBook.getCpName());
		book.setStatus(baiduBook.getStatus());
		book.setCover(baiduBook.getCover());
		book.setShortSummary(baiduBook.getShortSummary());
		System.out.println(book.toString());
		biQuGeImpl.chapterList(bqList, book.getSource(), 0);
		book = this.addBook(book);
		for (Chapter bqChapter : bqList) {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
			Chapter chapter = biQuGeImpl.chapterContent(bqChapter);
			chapter.setName(bqChapter.getName());
			chapter.setBookId(book.getId());
			chapter.setUpdateTime(bqChapter.getUpdateTime());
			chapter.setWordSum(bqChapter.getWordSum());
			this.addChapter(chapter);
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
			entity.put("lastId", list.get(0).getStr("id"));
			return entity;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public List<Entity> queryAllBooks() {
		try {
			List<Entity> list = Db.use(ds).query("select * from book order by \"lastChapterUpdateTime\" desc");
			return list.stream().map(r->{
				r.set("timeF", formateTimestamp(r.getLong("lastChapterUpdateTime")*1000));
				return r;
			}).collect(Collectors.toList());
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	public static String formateTimestamp(Long timestamp) {
		String result = "";
		int minute = 1000 * 60;
		int hour = minute * 60;
		int day = hour * 24;
		int week = day * 7;
		int month = day * 30;
	    Long now = new Date().getTime();
	    Long diffValue = now - timestamp;
	    if(diffValue < 0){
	        return result;
	    }
	    long minC = diffValue/minute;
	    long hourC = diffValue/hour;
	    long dayC = diffValue/day;
	    long weekC = diffValue/week;
	    long monthC = diffValue/month;
	    if(monthC >= 1 && monthC <= 3){
	        result = monthC + "月前";
	    }else if(weekC >= 1 && weekC <= 3){
	        result = weekC + "周前";
	    }else if(dayC >= 1 && dayC <= 6){
	        result = dayC + "天前";
	    }else if(hourC >= 1 && hourC <= 23){
	        result = hourC + "小时前";
	    }else if(minC >= 1 && minC <= 59){
	        result = minC + "分钟前";
	    }else if(diffValue >= 0 && diffValue <= minute){
	        result = "刚刚";
	    }else {
	    	if(DateUtil.year(new Date(timestamp)) == DateUtil.year(new Date())) {
	    		result = DateUtil.format(new Date(timestamp), "MM月dd日");
	    	}else{
	    		result = DateUtil.format(new Date(timestamp), "yyyy年MM月dd日");
	    	}
	    }
	    return result;
	}
	public String getChapterByIndex(String bId, Integer index, int i) {
		try {
			String sql = "";
			if(i == 1) {
				sql = "select id from chapter where \"bookId\"=? and index > ? order by index asc limit 1";
			}else {
				sql = "select id from chapter where \"bookId\"=? and index < ? order by index desc limit 1";
			}
			String id = Db.use(ds).queryString(sql, bId, index);
			if(StrUtil.isBlank(id)) {
				sql = "select id from chapter where \"bookId\"=? and index = ? limit 1";
				id = Db.use(ds).queryString(sql, bId, index);
			}
			return id;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return "";
		
	}
	public void fetchContent() {
		try {
			List<Entity> list = Db.use(ds).query("select * from chapter where content = ''");
			for (Entity entity : list) {
				List<String> urls = StrUtil.splitTrim(entity.getStr("urls"), ",");
				Chapter chapter = new Chapter();
				chapter.setUrls(urls);
				Chapter c = biQuGeImpl.chapterContent(chapter);
				if(StrUtil.isNotBlank(c.getContent())) {
					Db.use(ds).execute("update chapter set content = ? where id=?", c.getContent(), entity.getStr("id"));
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void updateNewChapter() {
		try {
			//查询连载中的书籍
			List<Entity> books = Db.use(ds).query("select * from book where status = '连载'");
			for (Entity book : books) {
				List<Chapter> cNewList = new ArrayList<Chapter>();
				Book b = new Book();
				b.setSource(book.getStr("source"));
				b = biQuGeImpl.getBookInfo(b);
				Db.use(ds).execute("update book set \"lastChapterName\"=?,\"lastChapterUpdateTime\"=? where id=?", b.getLastChapterName(), 
						b.getLastChapterUpdateTime(), book.getStr("id"));
				biQuGeImpl.chapterList(cNewList, book.getStr("source"), 1);
				for (int i = cNewList.size() - 1; i >= 0; i--) {
					Chapter cn = cNewList.get(i);
					Entity c = Db.use(ds).queryOne("select * from chapter where \"bookId\" = ? and name=?", book.getStr("id"), cn.getName());
					if(c == null || c.isEmpty()) {
						try {
							Thread.sleep(1000L);
						} catch (InterruptedException e) {
							logger.error(e.getMessage(), e);
						}
						Chapter chapter = biQuGeImpl.chapterContent(cn);
						chapter.setName(cn.getName());
						chapter.setBookId(book.getStr("id"));
						chapter.setUpdateTime(cn.getUpdateTime());
						chapter.setWordSum(cn.getWordSum());
						this.addChapter(chapter);
					}else {
						break;
					}
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
