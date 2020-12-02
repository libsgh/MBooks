package com.libsgh.books.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.libsgh.books.service.MainService;

@Service
public class Jobs {
	
	@Autowired
	public MainService mainService;
	
	@Scheduled(cron = "0 0/30 * * * ?")
	@Async
	public void fetchContent() {
		mainService.fetchContent();
	}
	
	@Scheduled(cron = "0 0/5 * * * ?")
	public void updateNewChapter() {
		mainService.updateNewChapter();
	}
	
}
