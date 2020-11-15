package com.libsgh.books.jobs;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.db.Db;
import cn.hutool.http.HttpRequest;

@Service
public class BootJob  implements  ApplicationListener<ContextRefreshedEvent> {
	
	private final static Logger logger = LoggerFactory.getLogger(BootJob.class);
	
	@Autowired
	private DruidDataSource ds;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent cre) {
		//初始化缓存数据库
		InputStream stream = getClass().getClassLoader().getResourceAsStream("sql/mbooks_init.sql");
		List<String> list = new ArrayList<String>();
		IoUtil.readLines(stream, Charset.forName("UTF-8"), new LineHandler() {
			@Override
			public void handle(String line) {
				list.add(line);
			}
		});
		int count = 0;
		for (String sql : list) {
			try {
				count += Db.use(ds).execute(sql);
			} catch (Exception e) {
			}
		}
		logger.info("上传任务sqlite初始化成功，影响行数：" + count);
	}
}
