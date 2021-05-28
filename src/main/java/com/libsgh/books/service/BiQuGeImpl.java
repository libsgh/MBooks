package com.libsgh.books.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.libsgh.books.bean.Book;
import com.libsgh.books.bean.Chapter;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;

@Service
public class BiQuGeImpl extends CommonApi implements BaseApi{
	
	public static String bqugeUrl = "http://www.xbiquge.la";
	
	private final static Logger logger = LoggerFactory.getLogger(BiQuGeImpl.class);
	
	public static void main(String[] args) {
		String body = HttpUtil.get("https://www.xbiquge.la/13/13959/6244136.html");
		Document doc = Jsoup.parse(body);
		System.out.println(body);
		doc.select("#content").select("p").last().remove();
		String content = doc.select("#content").html();
		System.out.println(content);
		//Book b = new Book();
		//b.setSource("http://www.xbiquge.la/7/7552/");
		//b = new BiQuGeImpl().getBookInfo(b);
		//System.out.println(b);
	//	Book book = new BiQuGeImpl().getBookInfo(new BiQuGeImpl().searchByName("圣墟").get(2));
	//	System.out.println(book);
		//List<Chapter> list = new ArrayList<Chapter>();
		//new BiQuGeImpl().chapterList(list, book.getSource(), 0);
		//book.setChapters(list);
		//System.out.println(JSONUtil.toJsonPrettyStr(book));
		//Chapter r = new Chapter();
		//List<String> lc = new ArrayList<String>();
		//lc.add("http://www.xbiquge.la/47/47784/20875607.html");
		//r.setUrls(lc);
		//System.out.println(new BiQuGeImpl().chapterContent(r));
	}
	
	@Override
	public List<Book> searchByName(String name) {
		List<Book> list = new ArrayList<Book>();
		String body = HttpRequest.post(bqugeUrl+"/modules/article/waps.php").form("searchkey", name).execute().body();
		Document doc = Jsoup.parse(body);
		Elements elements = doc.select(".grid").select("tbody").select("tr");
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Calendar cal = Calendar.getInstance() ;
	    Long now = cal.getTimeInMillis();
		for (int i = 1; i < elements.size(); i++) {
			Book book = new Book();
			Element ele = elements.get(i);
			String bookName = ele.select(".even").get(0).select("a").text().trim();
			String source = ele.select(".even").get(0).select("a").attr("href").trim();
			String lastChapterName = ele.select(".odd").get(0).select("a").text().trim();
			String author = ele.select(".even").get(1).html().trim();
			String time = ele.select(".odd").get(1).html().trim();
			book.setName(bookName);
			book.setSource(source);
			book.setLastChapterName(lastChapterName);
			book.setAuthor(author);
			int t = Integer.parseInt((DateUtil.parse(DateUtil.year(new Date(now))+"-"+time, DatePattern.NORM_DATE_PATTERN).getTime()/1000)+"");
			book.setLastChapterUpdateTime(t);
			list.add(book);
		}
		return list;
	}
	
	@Override
	public Book getBookInfo(Book book) {
		String body = HttpUtil.get(book.getSource());
		try {
			Document doc = Jsoup.parse(body);
			String name = doc.select("#info").select("h1").text();
			int t = Integer.parseInt((DateUtil.parse(StrUtil.subAfter(doc.select("#info").select("p").get(2).text(), "：", true)).getTime()/1000)+"");
			String chapterName = doc.select("#info").select("p").get(3).text();
			String cover = doc.select("#fmimg").select("img").attr("src");
			String shortSummary = doc.select("#intro").select("p").get(1).text();
			String categoryName = doc.select("meta[property=og:novel:category]").attr("content");
			book.setName(name);
			book.setLastChapterName(chapterName);
			book.setLastChapterUpdateTime(t);
			book.setCover(cover);
			book.setShortSummary(shortSummary);
			book.setCategoryName(categoryName);
			book.setCpName("笔趣阁");
			book.setStatus("连载");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			logger.info(body);
		}
		return book;
	}

	@Override
	public void chapterList(List<Chapter> list, String bookUrl, int num) {
		String body = HttpUtil.get(bookUrl);
		Document doc = Jsoup.parse(body);
		Elements els = doc.select("#list").select("dl").select("dd");
		int index = 0;
		for (Element ele : els) {
			index++;
			Chapter c = new Chapter();
			String cName = ele.select("a").text();
			String url = bqugeUrl + ele.select("a").attr("href");
			List<String> urls = new ArrayList<String>();
			urls.add(url);
			c.setName(cName);
			c.setIndex(index);
			c.setUrls(urls);
			list.add(c);
		}
	}

	@Override
	public Chapter chapterContent(Chapter chapter) {
		String body = HttpUtil.get(chapter.getUrls().get(0));
		Document doc = Jsoup.parse(body);
		doc.select("#content").select("p").last().remove();
		String content = doc.select("#content").html();
		chapter.setContent(content);
		return chapter;
	}

}
