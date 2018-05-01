package com.xuanwu.apaas.ormlib.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.xuanwu.apaas.ormlib.annotation.DatabaseField;
import com.xuanwu.apaas.ormlib.annotation.ReflectionUtils;
import com.xuanwu.apaas.ormlib.annotation.SqliteAnnotationCache;
import com.xuanwu.apaas.ormlib.annotation.SqliteAnnotationField;
import com.xuanwu.apaas.ormlib.annotation.SqliteAnnotationTable;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SqliteOrmBean implements Serializable,Cloneable {

	private String TABLE_NAME = "";
	private String SQL_CREATETABLE = "";
	private int indexId;

	 public String getTableName(){
		if(TextUtils.isEmpty(TABLE_NAME)){
			TABLE_NAME = ReflectionUtils.tableName(this.getClass());
		}
		return TABLE_NAME;
	}

	/**
	 * 得到构建表的sql语句
	 */
	String getCreateTableSql(){

		if(TextUtils.isEmpty(TABLE_NAME)) TABLE_NAME = getTableName();
		if(!TextUtils.isEmpty(SQL_CREATETABLE))return SQL_CREATETABLE;

		//遍历带注解的字段
		List<SqliteAnnotationField> safs =  getSqliteAnnotationField();
        List<String> fieldsStr = new ArrayList<String>();
        fieldsStr.add("`_id` integer primary key autoincrement");

		for(SqliteAnnotationField saf:safs){
            fieldsStr.add("`"+ saf.getColumnName() +"`" +" "+saf.getType()+(saf.isPrimaryKey()?" COLLATE NOCASE ":""));
		}
        //拼接初始化表的语句
        SQL_CREATETABLE = String.format("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( %s )", TextUtils.join(",",fieldsStr));
		return SQL_CREATETABLE;
		
	}

	/**
	 * 扩展字段控件用的时候，把注解的值取出，并转换成字符串
	 */
	Map<String,String> getAnnotationFieldValue(){
		Map<String,String> values = new HashMap<String,String>();
		
		for(SqliteAnnotationField saf:getSqliteAnnotationField()){
			Field f = saf.getField();
			try {
				f.setAccessible(true);
				Object value = f.get(this);
				if (value != null)
					values.put(f.getName(), value.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return values;
	}
	
	/**
	 * 获取对象中所有注解的值
	 */
	Map<String,Object> getAFValue(){
		Map<String,Object> values = new HashMap<String,Object>();
		
		for(SqliteAnnotationField saf:getSqliteAnnotationField()){
			Field f = saf.getField();
			try {
				f.setAccessible(true);
				Object value = f.get(this);
				if (value != null)
					values.put(f.getName(), value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return values;
	}
	
	/**
	 * 获取注解值并转换成JSON对象
	 */
	JSONObject getJsonAnnotationFieldValue(){
		JSONObject jb = new JSONObject();
		for(SqliteAnnotationField saf:getSqliteAnnotationField()){
			Field f = saf.getField();
			try {
				f.setAccessible(true);
				Object value = f.get(this);
				if (value != null)
					jb.put(f.getName(), value.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return jb;
	}
	
	/**
	 * 把map中的值填充到字段中
	 */
	void setAnnotationField(Map<String,String> values){
		
		for(SqliteAnnotationField af:getSqliteAnnotationField()){
			try {
				Field f = af.getField();
				DatabaseField.FieldType type = af.getType();

				f.setAccessible(true);
				if (!values.containsKey(f.getName())) {
					continue;
				}
				String value = values.get(f.getName());
				if (value == null)
					value = "";

				if (type == DatabaseField.FieldType.INT) {
					if (value.equals("")) {
						f.set(this, 0);
					} else {
						f.set(this, Integer.valueOf(value));
					}
				} else if (type == DatabaseField.FieldType.VARCHAR) {
					f.set(this, value);
				} else if (type == DatabaseField.FieldType.REAL) {
					if (value.equals("")) {
						f.set(this, 0);
					} else {
						f.set(this, Float.valueOf(value));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    void onSetCursorValueComplete(Cursor cursor){}

    /**
     * 功能描述：通过游标赋值
     */
	final void setAnnotationField(Cursor cursor){
		setAnnotationField(cursor,null);
	}

    /**
     * 功能描述：通过游标赋值
     */
    final void setAnnotationField(Cursor cursor, Map<String, Integer> cursorIndex){
    	try {
    		SqliteAnnotationCache cache = SqliteOrmLite.get().getSqliteAnnotationCache();
    		SqliteAnnotationTable table = cache.getTable(this.getClass());

            int indexId_InCursor;
            if(cursorIndex!=null){
                indexId_InCursor = cursorIndex.get("_id");
            }else{
                indexId_InCursor = cursor.getColumnIndex("_id");
            }

            if(indexId_InCursor!= -1 ){
                this.indexId = cursor.getInt(indexId_InCursor);
            }
	    	for(SqliteAnnotationField saf:table.getFields()){
				Field f = saf.getField();
				try {
					f.setAccessible(true);
					
					int index;
					if(cursorIndex!=null){
						index = cursorIndex.get(saf.getColumnName());
					}else{
						index = cursor.getColumnIndex(saf.getColumnName());
					}
					
					DatabaseField.FieldType t = saf.getType();
					if (t == DatabaseField.FieldType.INT) {
						f.set(this, cursor.getInt(index));
					} else if (t == DatabaseField.FieldType.VARCHAR) {
						f.set(this, cursor.getString(index));
					} else if (t == DatabaseField.FieldType.REAL) {
						f.set(this, cursor.getFloat(index));
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(f.getName() +" 未能正常赋值 ");
				}
				
			}
            onSetCursorValueComplete(cursor);

	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
    }



    /**
     * 功能描述：把用户对象的属性转换成键值对
     */
    ContentValues tranform2Values() {
		
        ContentValues values = new ContentValues();
		for(SqliteAnnotationField saf:getSqliteAnnotationField()){
			try {
				Field f = saf.getField();
				f.setAccessible(true);
				Object v = f.get(this);
				if (v != null){
					String value = v.toString();
					values.put(saf.getColumnName(), value);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
        return values;
    }

	void tranformSqliteStatement(SQLiteStatement statement){
		List<SqliteAnnotationField> fields = getSqliteAnnotationField();
		for(int i=0;i<fields.size();i++){
			try {
				SqliteAnnotationField saf = fields.get(i);
				Field f = saf.getField();
				f.setAccessible(true);
				Object v = f.get(this);
				if (v != null){
					if(saf.getType() == DatabaseField.FieldType.VARCHAR){
						String value = v.toString();
						statement.bindString(i+1,value);
					}else if(saf.getType() == DatabaseField.FieldType.INT){
						long value;
						if(TextUtils.isEmpty(""+ v)){
							value = 0;
						}else{
							value =  Long.valueOf(""+ v);
						}
						statement.bindLong(i+1,value);
					}else if(saf.getType() == DatabaseField.FieldType.REAL){
						double value;
						if(TextUtils.isEmpty(""+ v)){
							value = 0;
						}else{
							value = Double.valueOf(""+ v);
						}
						statement.bindDouble(i+1,value);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	/**
	 * 获取注解字段
	 */
	List<SqliteAnnotationField> getSqliteAnnotationField(){

		SqliteAnnotationCache cache = SqliteOrmLite.get().getSqliteAnnotationCache();
		SqliteAnnotationTable table = cache.getTable(this.getClass());
		return table.getFields();
		
	}

	/**
	 * 获取表主键
	 */
	String getPrimaryKey(){
		SqliteAnnotationCache cache = SqliteOrmLite.get().getSqliteAnnotationCache();
		SqliteAnnotationTable table = cache.getTable(this.getClass());
		return table.getPrimaryKey();
	}
	


    public int getIndexId() {
        return indexId;
    }

	/**
	 * 获取该对象的id值
	 */
	String getPrimaryId(){
		String result = "";
		String key = getPrimaryKey();
		if(TextUtils.isEmpty(key))return null;
		SqliteAnnotationCache cache = SqliteOrmLite.get().getSqliteAnnotationCache();
		SqliteAnnotationTable table = cache.getTable(this.getClass());
		SqliteAnnotationField field = table.getField(key);
		
		Field f = field.getField();
		f.setAccessible(true);
		try {
			result = (String) f.get(this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	SqliteOrmBean cloneDALEx() throws CloneNotSupportedException {
		return (SqliteOrmBean) clone();
	}

}