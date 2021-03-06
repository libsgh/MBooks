package com.libsgh.books.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.LiteDeviceResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.libsgh.books.jobs.Jobs;
import com.libsgh.books.service.CacheService;
import com.libsgh.books.service.MainService;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import cn.hutool.log.Log;

@Controller
public class MainController {
	
	@Autowired
	private Jobs jobs;
	
	@Autowired
	private MainService mainService;
	
	@Autowired
	private CacheService cacheService;
	
	@RequestMapping("/admin")
	public String admin(Model model) {
		model.addAttribute("books", mainService.allBooks());
		return "admin/index";
	}
	
	@RequestMapping("/api/add")
	@ResponseBody
	public String add(String name, Integer index) {
		mainService.fetchOneBook(name, index);
		return "success";
	}
	
	@RequestMapping("/api/book/updateStatus")
	@ResponseBody
	public String updateStatus(String id, String status) {
		mainService.updateStatus(id, status);
		return "success";
	}
	
	@RequestMapping("/api/fetch")
	@ResponseBody
	public String fetch() {
		jobs.fetchContent();
		return "success";
	}
	
	@RequestMapping("/")
	public String index(HttpServletRequest request, Model model) {
		model.addAttribute("list", mainService.queryAllBooks());
		return "index";
	}
	
	@RequestMapping("/c/{cid}")
	public String read(HttpServletRequest request, Model model, @PathVariable String cid) {
		LiteDeviceResolver deviceResolver = new LiteDeviceResolver();
		Device device = deviceResolver.resolveDevice(request);
		model.addAttribute("isMobile", device.isMobile());
		//从缓存中获取章节
		Entity entity = cacheService.getById(cid);
		if(entity == null) {
			entity = mainService.getChapterById(cid);
		}else{
			Log.get().info("从缓存中读取章节："+entity.getStr("name"));
		}
		//异步写入缓存
		cacheService.save(entity);
		if(entity == null || entity.isEmpty()) {
			return "redirect:https://novel.noki.top/error/404";
		}else{
			model.addAttribute("c", entity);
			return "read";
		}
	}
	
	@RequestMapping("/c/last/{index}")
	public String last( @PathVariable Integer index, String bId) {
		String id = mainService.getChapterByIndex(bId, index, -1);
		if(StrUtil.isBlank(id)) {
			return "redirect:https://novel.noki.top/b/detail/"+bId;
		}else {
			return "redirect:https://novel.noki.top/c/"+id;
		}
	}
	
	@RequestMapping("/c/next/{index}")
	public String next(@PathVariable Integer index, String bId) {
		String id = mainService.getChapterByIndex(bId, index, 1);
		if(StrUtil.isBlank(id)) {
			return "redirect:https://novel.noki.top/b/detail/"+bId;
		}else {
			return "redirect:https://novel.noki.top/c/"+id;
		}
	}
	
	@RequestMapping("/b/catalog/{bid}")
	public String catalog(Model model, @PathVariable String bid, String order, Integer page) {
		if(StrUtil.isBlank(order)) {
			order = "asc";
		}
		Entity entity = mainService.getChapterListById(bid, page, order);
		if(entity == null || entity.isEmpty()) {
			return "redirect:https://novel.noki.top/error/404";
		}else{
			model.addAttribute("b", entity);
			return "catalog";
		}
	}
	
	@RequestMapping("/b/detail/{bid}")
	public String detail(Model model, @PathVariable String bid) {
		Entity entity = mainService.getBookBiId(bid);
		if(entity == null || entity.isEmpty()) {
			return "redirect:https://novel.noki.top/error/404";
		}else{
			model.addAttribute("b",entity);
			return "detail";
		}
	}
	
	@GetMapping("/error/{code}")
	public String error(@PathVariable int code, Model model) {
		String pager = "404";
		switch (code) {
        case 404:
            model.addAttribute("code", 404);
            pager = "404";
            break;
		}
		return pager;
	}
}
