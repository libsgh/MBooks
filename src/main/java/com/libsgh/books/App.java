package com.libsgh.books;

import java.io.File;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
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
	
	@Value("${DATA_TYPE:sqlite}")
	private String dataType;
	
	@PostConstruct
    public void started() {
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	@Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error/404"));
        return factory;
    }
	
	public static void main(String[] args) {
		DbUtil.setShowSqlGlobal(true, true, true, Level.INFO);
		DbUtil.setCaseInsensitiveGlobal(false);
		SpringApplication.run(App.class, args);
	}
	
	@Bean
	@Primary
	public DruidDataSource getDataSource() {
		DruidDataSource ds = new DruidDataSource();
		if(dataType.equalsIgnoreCase("postgresql")) {
			ds.setDriverClassName("org.postgresql.Driver");
			ds.setUrl(dbUrl);
			ds.setUsername(user);
			ds.setPassword(pass);
		}else{
			ApplicationHome h = new ApplicationHome(getClass());
	        File jarF = h.getSource();
	        String path = jarF.getParentFile().toString() + "/data";
	        if(!FileUtil.exist(path)) {
	        	FileUtil.mkdir(path);
	        }
			ds.setDriverClassName("org.sqlite.JDBC");
			//ds.setUrl("jdbc:sqlite::resource:data/mbooks.db");
			ds.setUrl("jdbc:sqlite:"+path+"/mbooks.db");
		}
		return ds;
		
	}
}
