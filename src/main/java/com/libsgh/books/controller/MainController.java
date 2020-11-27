package com.libsgh.books.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.libsgh.books.jobs.Jobs;
import com.libsgh.books.service.MainService;

import cn.hutool.core.util.StrUtil;

@Controller
public class MainController {
	
	@Autowired
	private Jobs jobs;
	
	@Autowired
	private MainService mainService;
	
	@RequestMapping("/admin")
	public String admin(Model model) {
		model.addAttribute("books", mainService.allBooks());
		return "admin/index";
	}
	
	@RequestMapping("/api/add")
	@ResponseBody
	public String add(String name) {
		mainService.fetchOneBook(name);
		return "success";
	}
	
	@RequestMapping("/api/fetch")
	@ResponseBody
	public String fetch() {
		jobs.fetchContent();
		return "success";
	}
	
	@RequestMapping("/")
	public String index(Model model) {
		model.addAttribute("list", mainService.queryAllBooks());
		return "index";
	}
	
	@RequestMapping("/c/{cid}")
	public String read(Model model, @PathVariable String cid) {
		model.addAttribute("c", mainService.getChapterById(cid));
		return "read";
	}
	
	@RequestMapping("/c/last/{index}")
	public String last( @PathVariable Integer index, String bId) {
		String id = mainService.getChapterByIndex(bId, index, -1);
		//TODO 这里要判断一下是不是有下一章或上一章了，没有跳转新页面
		return "redirect:/c/"+id;
	}
	
	@RequestMapping("/c/next/{index}")
	public String next(@PathVariable Integer index, String bId) {
		String id = mainService.getChapterByIndex(bId, index, 1);
		//TODO 这里要判断一下是不是有下一章或上一章了，没有跳转新页面
		return "redirect:/c/"+id;
	}
	
	@RequestMapping("/b/catalog/{bid}")
	public String catalog(Model model, @PathVariable String bid, String order, Integer page) {
		if(StrUtil.isBlank(order)) {
			order = "asc";
		}
		model.addAttribute("b", mainService.getChapterListById(bid, page, order));
		return "catalog";
	}
	
	@RequestMapping("/b/detail/{bid}")
	public String detail(Model model, @PathVariable String bid) {
		model.addAttribute("b", mainService.getBookBiId(bid));
		return "detail";
	}
	
	@RequestMapping("/detail/{bookId}")
	public String detail(@PathVariable String bookId) {
		return "detail";
	}
	
}
