package com.xuanwu.apaas.ormlib.annotation;

import android.database.Cursor;
import android.text.TextUtils;

import com.xuanwu.apaas.ormlib.core.SqliteOrmBean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqliteAnnotationTable {
	
	private String tableName;
	private Class clazz;
	private List<SqliteAnnotationField> fields;
	private Map<String,SqliteAnnotationField> fieldMaps;
	private String primaryKey;
	
	public SqliteAnnotationTable(String tableName, Class clazz) {
		this.tableName = tableName;
		this.clazz = clazz;
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public SqliteAnnotationField getField(String name){
		if(fieldMaps==null)fieldMaps = new HashMap<String,SqliteAnnotationField>();
		return fieldMaps.get(name);
	}
	
	public synchronized List<SqliteAnnotationField> getFields(){
		
		if(fields==null){
			if(fieldMaps==null)fieldMaps = new HashMap<String,SqliteAnnotationField>();
			fields = new ArrayList<SqliteAnnotationField>();

			for(Field f:clazz.getDeclaredFields()){
				DatabaseField dbf = f.getAnnotation(DatabaseField.class);
				if(dbf!=null){
					SqliteAnnotationField saf = new SqliteAnnotationField(f,dbf);
					fields.add(saf);
					fieldMaps.put(saf.getColumnName(),saf);
					if(saf.isPrimaryKey())this.primaryKey = saf.getColumnName();
				}
			}
		}
		return fields;
	}
	
	public synchronized Map<String,Integer> getCursorIndex(Cursor cursor){
		
		Map<String,Integer> index = new HashMap<String,Integer>();
		for (int i = 0; i < cursor.getColumnCount(); i++) {
			String name = cursor.getColumnName(i);
			index.put(name, i);
		}
		return index;
	}

	public String getPrimaryKey() {
		if(TextUtils.isEmpty(primaryKey)){
			getFields();
		}
		return primaryKey;
	}
	
}