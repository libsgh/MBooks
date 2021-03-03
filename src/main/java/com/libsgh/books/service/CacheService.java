package com.libsgh.books.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.date.DateUnit;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;

@Service
public class CacheService {
	
	public static TimedCache<String, Entity> timedCache = CacheUtil.newTimedCache(2 * DateUnit.HOUR.getMillis());
	
	@Autowired
	private DruidDataSource ds;
	
	public Entity getById(String id) {
		return timedCache.get(id);
	} 
	
	@Async
	public void save(Entity entity) {
		//清除当前的缓存
		timedCache.remove(entity.getStr("id"));
		String sql = "select * from chapter where \"bookId\"=? and index > ? order by index asc limit 5";
		try {
			List<Entity> list = Db.use(ds).query(sql, entity.getStr("bookId"), entity.getInt("index"));
			for (Entity e : list) {
				if(!timedCache.containsKey(e.getStr("id"))) {
					timedCache.put(e.getStr("id"), e);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
