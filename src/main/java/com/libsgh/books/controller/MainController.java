package com.libsgh.books.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.libsgh.books.service.MainService;

@Controller
public class MainController {
	
	@Autowired
	private MainService mainService;
	
	@RequestMapping("/admin")
	public String admin(Model model) {
		model.addAttribute("books", mainService.allBooks());
		return "admin/index";
	}
	@RequestMapping("/api/add")
	@ResponseBody
	public String add() {
		mainService.fetchOneBook("万古神帝");
		return "success";
	}
	
	@RequestMapping("/index")
	public String index() {
		return "index";
	}
	
	@RequestMapping("/read/{cid}")
	public String read(Model model, @PathVariable String cid) {
		model.addAttribute("c", mainService.getChapterById(cid));
		return "read";
	}
	
	@RequestMapping("/detail/{bookId}")
	public String detail(@PathVariable String bookId) {
		return "detail";
	}
	
}
