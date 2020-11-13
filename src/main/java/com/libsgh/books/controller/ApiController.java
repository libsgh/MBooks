package com.libsgh.books.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.libsgh.books.bean.Book;
import com.libsgh.books.service.BaiduApiImpl;

@RestController
@RequestMapping("/api")
public class ApiController {
	
	@Autowired
	private BaiduApiImpl baiduApiImpl;
	
	@RequestMapping("/bd/searchByName")
	@ResponseBody
	public List<Book> searchByName(String name){
		return baiduApiImpl.searchByName(name);
	}
	
	@RequestMapping("/bd/getBookInfo")
	@ResponseBody
	public Book getBookInfo(Book book){
		return baiduApiImpl.getBookInfo(book);
	}
	
}
