package com.libsgh.books;

import java.io.File;

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

	public static void main(String[] args) {
		DbUtil.setShowSqlGlobal(true, true, true, Level.INFO);
		SpringApplication.run(App.class, args);
	}
	
	@Bean
	@Primary
	public DruidDataSource getDataSource() {
		DruidDataSource ds = new DruidDataSource();
		ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String path = jarF.getParentFile().toString() + "/data";
        if(!FileUtil.exist(path)) {
        	FileUtil.mkdir(path);
        }
		ds.setDriverClassName("org.sqlite.JDBC");
		//ds.setUrl("jdbc:sqlite:/home/single/eclipse-workspace/MBooks/src/main/resources/data/mbooks.db");
		ds.setUrl("jdbc:sqlite:"+path+"/mbooks.db");
		return ds;
	}
}
