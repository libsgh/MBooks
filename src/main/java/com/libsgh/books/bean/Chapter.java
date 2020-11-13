package com.libsgh.books.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Chapter {
	
	public String id;
	
	public String bookId;
	
	public String name;//章节名称
	
	public String content;//内容
	
	public List<String> urls;//三方章节地址
	
	public int updateTime;//更新时间
	
	public int index;//序号
	
	public int wordSum;//字数

}
