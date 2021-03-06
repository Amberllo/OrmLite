package com.xuanwu.apaas.ormlib.annotation;

import com.xuanwu.apaas.ormlib.core.SqliteOrmBean;

import java.util.HashMap;
import java.util.Map;

public class SqliteAnnotationCache {

	private Map<String, SqliteAnnotationTable> tableCache = new HashMap<String, SqliteAnnotationTable>();

	public synchronized SqliteAnnotationTable getTable(Class clazz) {
		String tableName = ReflectionUtils.tableName(clazz);
		SqliteAnnotationTable table = tableCache.get(tableName);
		try {
			if(table==null){
				table = new SqliteAnnotationTable(tableName,clazz);
				tableCache.put(table.getTableName(), table);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}
	
	public void clear(){
		tableCache.clear();
	}
}
