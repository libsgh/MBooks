package com.libsgh.books.jobs;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;

import cn.hutool.core.io.IoUtil;
import cn.hutool.db.Db;

@Service
public class BootJob  implements  ApplicationListener<ContextRefreshedEvent> {
	
	private final static Logger logger = LoggerFactory.getLogger(BootJob.class);
	
	@Autowired
	private DruidDataSource ds;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent cre) {
		//初始化缓存数据库
		InputStream in = getClass().getClassLoader().getResourceAsStream("sql/mbooks_init.sql");
		String sql = IoUtil.read(in, Charset.forName("UTF-8"));
		try {
			int count = Db.use(ds).execute(sql);
			logger.info("上传任务sqlite初始化成功，影响行数：" + count);
		} catch (Exception e) {
		}
	}
}
