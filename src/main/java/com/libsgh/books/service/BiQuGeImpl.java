package com.libsgh.books.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.libsgh.books.bean.Book;
import com.libsgh.books.bean.Chapter;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Service
public class BiQuGeImpl implements BaseApi{
	public static void main(String[] args) {
		//Book book = new BiQuGeImpl().getBookInfo(new BiQuGeImpl().searchByName("万古神帝").get(2));
		//List<Chapter> list = new ArrayList<Chapter>();
		//new BiQuGeImpl().chapterList(list, book.getSource(), 0);
		//book.setChapters(list);
		//System.out.println(JSONUtil.toJsonPrettyStr(book));
		Chapter r = new Chapter();
		List<String> lc = new ArrayList<String>();
		lc.add("http://www.xbiquge.la/47/47784/20875607.html");
		r.setUrls(lc);
		System.out.println(new BiQuGeImpl().chapterContent(r));
	}
	@Override
	public List<Book> searchByName(String name) {
		List<Book> list = new ArrayList<Book>();
		String body = HttpRequest.post("http://www.xbiquge.la/modules/article/waps.php").form("searchkey", name).execute().body();
		Document doc = Jsoup.parse(body);
		Elements elements = doc.select(".grid").select("tbody").select("tr");
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
			int t = Integer.parseInt((DateUtil.parse(DateUtil.year(new Date())+"-"+time, DatePattern.NORM_DATE_PATTERN).getTime()/1000)+"");
			book.setLastChapterUpdateTime(t);
			list.add(book);
		}
		return list;
	}
	
	@Override
	public Book getBookInfo(Book book) {
		String body = HttpUtil.get(book.getSource());
		Document doc = Jsoup.parse(body);
		String name = doc.select("#info").select("h1").text();
		int t = Integer.parseInt((DateUtil.parse(StrUtil.subAfter(doc.select("#info").select("p").get(2).text(), "：", true)).getTime()/1000)+"");
		String chapterName = doc.select("#info").select("p").get(3).text();
		String cover = doc.select("#fmimg").select("img").attr("src");
		String shortSummary = doc.select("#intro").select("p").get(1).text();
		book.setName(name);
		book.setLastChapterName(chapterName);
		book.setLastChapterUpdateTime(t);
		book.setCover(cover);
		book.setShortSummary(shortSummary);
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
			String url = "http://www.xbiquge.la" + ele.select("a").attr("href");
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
		String content = doc.select("#content").html();
		content = content.replace("<p><a href=\"http://koubei.baidu.com/s/xbiquge.la\" target=\"_blank\">亲,点击进去,给个好评呗,分数越高更新越快,据说给新笔趣阁打满分的最后都找到了漂亮的老婆哦!</a><br>手机站全新改版升级地址：http://m.xbiquge.la，数据和书签与电脑站同步，无广告清新阅读！</p>", "");
		chapter.setContent(content);
		return chapter;
	}

}
