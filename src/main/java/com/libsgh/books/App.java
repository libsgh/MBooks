package com.libsgh.books;

import java.io.File;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.alibaba.druid.pool.DruidDataSource;

import cn.hutool.core.io.FileUtil;
import cn.hutool.db.DbUtil;
import cn.hutool.log.level.Level;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class App {

	@Value("${JDBC_DATABASE_URL}")
	private String dbUrl;
	
	@Value("${JDBC_USER}")
	private String user;
	
	@Value("${JDBC_PASS}")
	private String pass;
	
	@PostConstruct
    public void started() {
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	public static void main(String[] args) {
		DbUtil.setShowSqlGlobal(true, true, true, Level.INFO);
		SpringApplication.run(App.class, args);
	}
	
	@Bean
	@Primary
	public DruidDataSource getDataSource() {
		DruidDataSource ds = new DruidDataSource();
		ds.setDriverClassName("org.postgresql.Driver");
		ds.setUrl(dbUrl);
		ds.setUsername(user);
		ds.setPassword(pass);
		return ds;
	}
}
