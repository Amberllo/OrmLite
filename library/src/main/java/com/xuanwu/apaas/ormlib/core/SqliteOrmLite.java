package com.xuanwu.apaas.ormlib.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xuanwu.apaas.ormlib.annotation.SqliteAnnotationCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by apple on 17/4/11.
 */

public class SqliteOrmLite {

    /**
     * SqliteOrmLite 支持多db操作
     * 1.在application初始化SqliteOrmLite组件，init传入上下文
     * 2.编写Bean直接继承SqliteOrmBean
     * 3.使用SqliteRepository中包含了增删查改方法，SqliteRepository构造函数传入数据库名
     *
     * */

    private static SqliteOrmLite instance;
    private Map<String,SqliteOrmDB> dbMap = new HashMap<>();
    private Context applicationContext;

    public static SqliteOrmLite get(){
        if(instance==null){
            instance = new SqliteOrmLite();
        }
        return instance;
    }

    public void init(Context applicationContext){
        this.applicationContext = applicationContext;
    }

    public static void migrate(SQLiteDatabase database, Class<? extends SqliteOrmRepository<?>>... daoClasses) {
        SqliteOrmLiteUpgrader.migrate(database,daoClasses);
    }

    public static void onDestory(){
        if(instance!=null){
            if(instance.sqliteAnnotationCache!=null){
                instance.sqliteAnnotationCache.clear();
            }
            for(Map.Entry<String,SqliteOrmDB> entry:instance.dbMap.entrySet()){
                SqliteOrmDB db = entry.getValue();
                db.clearCache();
            }
            instance = null;
        }

    }

    SqliteOrmDB getDB(String dbName) {
        if(!dbMap.containsKey(dbName)){
            SqliteOrmDB db = new SqliteOrmDB(applicationContext,dbName);
            dbMap.put(dbName,db);
        }
        return dbMap.get(dbName);
    }



    /** Orm Annotation中字段、Table的缓存，注入时用到 */
    private SqliteAnnotationCache sqliteAnnotationCache;

    synchronized SqliteAnnotationCache getSqliteAnnotationCache(){
        if(sqliteAnnotationCache==null){
            sqliteAnnotationCache = new SqliteAnnotationCache();
        }
        return sqliteAnnotationCache;
    }

}
