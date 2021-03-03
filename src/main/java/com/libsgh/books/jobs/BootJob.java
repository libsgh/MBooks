package com.libsgh.books.jobs;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${DATA_TYPE:sqlite}")
	private String dataType;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent cre) {
		//初始化缓存数据库
		String sql = "";
		int count = 0;
		if(dataType.equalsIgnoreCase("postgresql")) {
			InputStream in = getClass().getClassLoader().getResourceAsStream("sql/mbooks_init_postgres.sql");
			sql = IoUtil.read(in, Charset.forName("UTF-8"));
			try {
				count += Db.use(ds).execute(sql);
			} catch (Exception e) {
			}
		}else {
			InputStream in = getClass().getClassLoader().getResourceAsStream("sql/mbooks_init_sqlite_0.sql");
			sql = IoUtil.read(in, Charset.forName("UTF-8"));
			try {
				count += Db.use(ds).execute(sql);
			} catch (Exception e) {
			}
			in = getClass().getClassLoader().getResourceAsStream("sql/mbooks_init_sqlite_1.sql");
			sql = IoUtil.read(in, Charset.forName("UTF-8"));
			try {
				count += Db.use(ds).execute(sql);
			} catch (Exception e) {
			}
		}
		try {
			
		} catch (Exception e) {
		}
		logger.info("sql初始化成功，影响行数：" + count);
	}
}
