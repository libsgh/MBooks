package com.libsgh.books.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.libsgh.books.bean.Book;
import com.libsgh.books.bean.Chapter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

@Service
public class BaiduApiImpl implements BaseApi {
	
	public static void main(String[] args) {
		System.out.println(StrUtil.similar("一阶上等", "一阶上等"));
		//System.out.println(new BaiduApiImpl().getBookInfo(new BaiduApiImpl().searchByName("万古神帝").get(0)));
	}
	
	@Override
	public List<Book> searchByName(String name) {
		List<Book> list = new ArrayList<Book>();
		String result = HttpUtil.get("https://dushu.baidu.com/api/getSearchResultData?query="+name);
		JSONObject jo =  JSONUtil.parseObj(result);
		if(jo.getInt("errno") == 0) {
			JSONArray arr = jo.getByPath("$.data.novelList", JSONArray.class);
			for (Object object : arr) {
				Book book = new Book();
				String bookName = ((JSONObject)object).getStr("title");
				String author = ((JSONObject)object).getStr("author");
				String status = ((JSONObject)object).getStr("status");
				String description = ((JSONObject)object).getStr("description");
				String cover = ((JSONObject)object).getStr("cover");
				String category = ((JSONObject)object).getStr("category");
				String bookId = ((JSONObject)object).getStr("bookId");
				book.setSource(bookId);
				book.setName(bookName);
				book.setAuthor(author);
				book.setStatus(status);
				book.setCover(cover);
				book.setCategoryName(category);
				book.setShortSummary(description);
				list.add(book);
			}
			return list;
		}
		return list;
	}

	@Override
	public Book getBookInfo(Book book) {
		String html = HttpUtil.get("https://boxnovel.baidu.com/boxnovel/detail?gid=" + book.getSource());
		String result = StrUtil.subBetween(html, "data: ", "});");
		JSONObject jo = JSONUtil.parseObj(result);
		book.setCreateTime(Integer.parseInt(jo.getStr("create_time")));
		book.setCpName(jo.getStr("cp_name"));
		book.setLastChapterName(jo.getStr("last_chapter_title"));
		book.setLastChapterUpdateTime(Integer.parseInt(jo.getStr("last_chapter_update_time")));
		return book;
	}
	
	@Override
	public void chapterList(List<Chapter> list, String bookUrl, int num) {
		if(num < 1) {
			num = 1;
		}
		String result = HttpUtil.get("https://boxnovel.baidu.com/boxnovel/wiseapi/chapterList?bookid="+bookUrl+"&pageNum="+num+"&order=asc&site=");
		JSONObject jo = JSONUtil.parseObj(result);
		if(jo.getInt("errno") == 0) {
			JSONArray arr = jo.getByPath("$.data.chapter.chapterInfo", JSONArray.class);
			for (Object object : arr) {
				Chapter character = new Chapter();
				String chapterName = ((JSONObject)object).getStr("chapter_title");
				String chapterId = ((JSONObject)object).getStr("chapter_id");
				int updateTime = ((JSONObject)object).getInt("public_time");
				int index = ((JSONObject)object).getInt("chapter_index");
				int wordSum = ((JSONObject)object).getInt("word_sum");
				character.setIndex(index);
				character.setName(chapterName);;
				character.setUpdateTime(updateTime);
				character.setWordSum(wordSum);
				List<String> urls = new ArrayList<String>();
				urls.add("https://boxnovel.baidu.com/boxnovel/content?gid="+bookUrl+"&cid="+chapterId);
				character.setUrls(urls);
				list.add(character);
			}
			num++;
			chapterList(list, bookUrl, num);
		}
	}

	@Override
	public Chapter chapterContent(Chapter chapter) {
		return null;
	}

}
