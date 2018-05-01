package com.xuanwu.apaas.ormlib.core;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.xuanwu.apaas.ormlib.annotation.ReflectionUtils;
import com.xuanwu.apaas.ormlib.annotation.SqliteAnnotationCache;
import com.xuanwu.apaas.ormlib.annotation.SqliteAnnotationTable;


@SuppressLint("NewApi")
public class SqliteOrmDB extends SQLiteOpenHelper {

	public SqliteOrmDB(Context context, String dbName) {
		super(context, dbName, null, 1);
	}

    @Override
	public void onCreate(SQLiteDatabase db) {
	}

    /**
	 * 当检测与前一次创建数据库版本不一样时，先删除表再创建新表
	 */
	@SuppressLint("NewApi")
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		SqliteOrmLiteUpgrader.migrate(db);
	}

	/**
	 * 添加操作
	 *
	 * @param tableName
	 *            表名
	 * @param values
	 *            集合对象
	 * @return
	 */
	public boolean save(String tableName, ContentValues values) {
		try {
			// synchronized (lock) {
            getWritableDatabase().insert(tableName, null, values);
			// }
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 更新操作
	 *
	 * @param table
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	@SuppressLint("NewApi")
	public boolean update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		try {
			int a = getWritableDatabase().update(table, values, whereClause, whereArgs);
			return a > 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除
	 *
	 * @param deleteSql
	 *            对应跟新字段 如： "where personid=?"
	 * @param obj
	 *            [] 对应值 如： new Object[]{person.getPersonid()};
	 * @return
	 */
	public boolean delete(String table, String deleteSql, String obj[]) {
		try {
            getWritableDatabase().delete(table, deleteSql, obj);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 查询操作

	 * @param findSql
	 *            对应查询字段 如： select * from person limit ?,?
	 * @param obj
	 *            对应值 如： new
	 *            String[]{String.valueOf(fristResult),String.valueOf(
	 *            maxResult)}
	 * @return
	 */
	public Cursor find(String findSql, String obj[]) {

		try {
			// synchronized (lock) {
			Cursor cursor = getReadableDatabase().rawQuery(findSql, obj);
			return cursor;
			// }
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 创建表 xpf,并对旧的数据进行迁移
	 */
	public void creatTable(String createTableSql, String tablename)throws Exception {
		if(!isTableExits(tablename)){
			getWritableDatabase().execSQL(createTableSql);
		}
	}

	/**
	 * 删除表
	 */
	public boolean deleteTable(String tableName) {
		try {
            getWritableDatabase().execSQL("DROP TABLE IF EXISTS  " + tableName);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 判断表是否存在
	 * 
	 * @param clazz
	 * @return
	 */

	public <T> boolean isTableExits(Class<T> clazz) {
		return isTableExits(getTableName(clazz));
	}
	public boolean isTableExits(String tablename) {
		Cursor cursor = null;
		try {
			String str = "SELECT COUNT(*) FROM sqlite_master where type='table' and name='"+tablename+"'";
			cursor = getReadableDatabase().rawQuery(str, null);
			if (null != cursor && cursor.moveToFirst()) {
				return cursor.getInt(0)!=0;
			}
		} catch (Exception ex) {
			return false;
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}
		return false;
	}


	/***
	 * 关闭 获取SQLite数据库连接
	 *
	 * @return SQLiteDatabase
	 */
	public SQLiteDatabase getConnection() {
		return getWritableDatabase();
	}

	public boolean execSQL(String sql) {
		try {
            getWritableDatabase().execSQL(sql);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private void closeAllDB(){
		try {
			close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void clearCache(){
		closeAllDB();
	}


	public <T> String getTableName(Class<T> clazz) {
		return ReflectionUtils.tableName(clazz);
	}

	public <T> String getPrimaryKey(Class<T> clazz) {
		SqliteAnnotationCache cache = SqliteOrmLite.get().getSqliteAnnotationCache();
		SqliteAnnotationTable table = cache.getTable(clazz);
		return table.getPrimaryKey();
	}

	public void execByStatment(String sql,String[] args) {
		SQLiteStatement statement = getWritableDatabase().compileStatement(sql);
	}
}
