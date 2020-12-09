package com.libsgh.books.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Book {
	
	public String id;
	
	public String source;
	
	public String name;//书名

	public String author;//作者
	
	public String categoryName;//分类
	
	public String cover;//封面
	
	public String shortSummary;//简介
	
	public String cpName;//来源cp
	
	public String downloadUrl;//下载地址
	
	public List<Chapter> chapters;//章节列表
	
	public String lastChapterName;//最后一章 章节名称
	
	public int lastChapterUpdateTime;//最后一章 更新时间
	
	public int createTime;//创建时间
	
	public String status;//连载，完结
	
	public int use = 1;//0停用，1启用
	
	public int state = 0;//0，完成，1爬取中
	
	public Long wordCount = 0L;
	
	public Long readCount = 0L;	
	
	public int cc;	
}
