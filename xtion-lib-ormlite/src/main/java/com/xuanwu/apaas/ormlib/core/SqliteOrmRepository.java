package com.xuanwu.apaas.ormlib.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.xuanwu.apaas.ormlib.annotation.DatabaseField;
import com.xuanwu.apaas.ormlib.annotation.SqliteAnnotationCache;
import com.xuanwu.apaas.ormlib.annotation.SqliteAnnotationField;
import com.xuanwu.apaas.ormlib.annotation.SqliteAnnotationTable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.database.Cursor.FIELD_TYPE_BLOB;
import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;

/**
 * Created by Administrator on 2017/8/8 0008.
 * 一个Repository只能单表操作
 * 需要使用泛型指明表名
 */

public abstract class SqliteOrmRepository<T extends SqliteOrmBean>  {
    private String dbName;
    private Class<T> clazz;
    public SqliteOrmRepository(String dbName){

        // 使用反射技术得到T的真实类型
        //当前对象的直接超类的 Type
        Type genericSuperclass = getClass().getGenericSuperclass();
        if(genericSuperclass instanceof ParameterizedType){
            //参数化类型
            ParameterizedType parameterizedType= (ParameterizedType) genericSuperclass;
            //返回表示此类型实际类型参数的 Type 对象的数组
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            this.clazz= (Class)actualTypeArguments[0];
        }else{
            this.clazz= (Class)genericSuperclass;
        }

        this.dbName = dbName;
    }

    protected SqliteOrmDB getDB(){
        return SqliteOrmLite.get().getDB(dbName);
    }

    protected String getTableName(){
        return getDB().getTableName(clazz);
    }

    /**
     * 单个保存
     * */
    public void saveOrUpdate(T bean){
        String id = bean.getPrimaryId();
        if(TextUtils.isEmpty(id))return;
        try {
            saveOrUpdate(bean,bean.getPrimaryKey()+"=?", new String[]{id});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveOrUpdate(T bean, String whereClause, String[] whereArgs) throws Exception {

        SqliteOrmDB db = getDB();
        ContentValues values = bean.tranform2Values();
        boolean isExist = isExist(bean,whereClause,whereArgs);
        if(isExist){
            boolean result = db.update(bean.getTableName(), values, whereClause,whereArgs);
            if(!result)throw  new Exception("update failed");
        }else{
            boolean result = db.save(bean.getTableName(), values);
            if(!result)throw  new Exception("save failed");
        }

    }

    /**
     * 批量保存
     * 1.改造前，直接暴力遍历：  1w条数据 共8800ms，checkExist就用了2500ms了
     * 2.使用Statement后，checkExist 只用statement: 450ms，rawQueryWithFactory: 1897ms
     * 2.1 使用transaction 包裹
     * 2.2 在插入前后，整个过程，只申请一次数据库链接，finally再关闭
     * 2.3 只是使用3条sql语句，使用3个statement，每次操作只是清除对应statement的bind值
     * 2.4 statement也只是打开，关闭一次而已，最大程度降低性能损耗
     * 3.优化以后，插入需要6122ms，但更新则需要更多16892ms，性能损耗更为严重，
     * 3.1 不同机器执行的性能不一致 海马玩 inesert 8000ms，update 25456ms
     * 3.1 uodate比起insert多消耗2~3倍的的性能
     * 3.2 不进行字段操作,插入4504ms 更新 9809ms
     * 4.使用旧时orm 插入8295ms ，update 20474ms
     * */
    public void saveOrUpdate(final List<T> beans){
        operatorWithTransaction(new OnTransactionListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTransaction(SqliteOrmDB db) throws Exception {
                if(beans.size()==0)return;

                SQLiteDatabase sqliteDatabase = getDB().getConnection();
                sqliteDatabase.acquireReference();

                SQLiteStatement updateStatement = null;
                try {

                    long start = System.currentTimeMillis();
                    //自动建表
                    T templateBean = beans.get(0);
                    checkTable(templateBean);
                    long t1 = System.currentTimeMillis();
                    System.out.println(" checkTable = "+(t1-start));


                    List<String> columns = new ArrayList<>();
                    List<String> mark = new ArrayList<>();
                    List<String> updates = new ArrayList<>();

                    List<SqliteAnnotationField> fields =  templateBean.getSqliteAnnotationField();
                    for(SqliteAnnotationField field:fields){
                        String column = field.getColumnName();
                        columns.add(column);
                        mark.add("?");
                        updates.add(column+"=?");
                    }

                    String updateSql = String.format(" UPDATE %s SET %s WHERE %s ",
                            getTableName(),
                            TextUtils.join(",",updates),
                            getPrimaryKey()+"=?");
                    updateStatement = sqliteDatabase.compileStatement(updateSql);

                    long t2 = System.currentTimeMillis();
                    System.out.println(" makeSQL = "+(t2-t1));

                    List<String> exists = isExist(beans);
                    List<T> insertBeans = new ArrayList<>();
                    for(T bean:beans){

                        boolean isExist = exists.contains(bean.getPrimaryId());
                        if(isExist){
                            bean.tranformSqliteStatement(updateStatement);
                            updateStatement.executeUpdateDelete();
                        }else{
                            insertBeans.add(bean);
                        }
                    }
                    insert(insertBeans);

                    long t3 = System.currentTimeMillis();
                    System.out.println(" saveOrUpdate = "+(t3-t2));

                }catch (SQLiteException e){
                    e.printStackTrace();
                }finally{

                    if(updateStatement!=null){
                        updateStatement.close();
                    }
                    sqliteDatabase.releaseReference();
                }

            }
        });
    }


    /**
     * 批量插入
     * 由于update比起insert，更消耗性能2-3倍的性能
     * 大批量数据插数据库时用
     * */
    private void insert(final List<T> beans){
        if(beans.size()==0)return;
        SQLiteDatabase sqliteDatabase = getDB().getConnection();
        try {
            long start = System.currentTimeMillis();
            //自动建表
            T templateBean = beans.get(0);
            checkTable(templateBean);

            List<SqliteAnnotationField> fields = templateBean.getSqliteAnnotationField();
            List<String> columns = new ArrayList<>();

            for(SqliteAnnotationField field:fields){
                String column = field.getColumnName();
                columns.add(column);
            }
            String insertSql = String.format(" INSERT INTO %s %s ",
                    getTableName(),
                    " ("+TextUtils.join(",",columns)+") ");

            List<String> exists = isExist(beans);

            List<String> tmp = new ArrayList<>();
            for(int i=0;i<beans.size();i++){
                T bean = beans.get(i);
                if(exists.contains(bean.getPrimaryId())){
                    continue;
                }
                List<String> union = new ArrayList<>();
                Map<String,Object> afValue = bean.getAFValue();
                for(SqliteAnnotationField saf:bean.getSqliteAnnotationField()){

                    try {
                        Object valueObj = afValue.get(saf.getColumnName());
                        if(saf.getType() == DatabaseField.FieldType.VARCHAR){
                            union.add(" '"+ (valueObj==null?"":valueObj.toString()) +"' ");
                        }else if(saf.getType() == DatabaseField.FieldType.INT){
                            union.add(""+(valueObj==null?0:valueObj));
                        }else if(saf.getType() == DatabaseField.FieldType.REAL){
                            union.add(""+(valueObj==null?0:valueObj));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(union.size()!=0){//union == 0 就一个字段都没有
                    String str = " SELECT "+TextUtils.join(",",union);
                    tmp.add(str);
                    if(tmp.size()==500 || i == beans.size()-1){
                        String value = TextUtils.join(" UNION  ",tmp);
                        //tmp full
                        tmp.clear();
                        String sql = insertSql+value;
                        sqliteDatabase.execSQL(sql);
                    }

                }
            }
            long t4 = System.currentTimeMillis();
            System.out.println(" insert  = "+(t4-start));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean isExist(T bean, String whereCase, String[] whereArgs){
        boolean result = false;
        Cursor cursor = null;
        try {
            checkTable(bean);
            SqliteOrmDB db = getDB();
            String where = TextUtils.isEmpty(whereCase)?"":" where "+whereCase;
            cursor = db.find("select * from "+ bean.getTableName()  +where,whereArgs);
            if (cursor != null && cursor.moveToNext()) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    private List<String> isExist(List<T> beans){
        if(beans==null || beans.size()==0){
            return new ArrayList<>();
        }
        T templateBean = beans.get(0);
        String checkExistSql = String.format(" SELECT %s from %s WHERE %s in ",
                templateBean.getPrimaryKey(),
                getTableName(),
                templateBean.getPrimaryKey());
        List<String> tmpIds = new ArrayList<>();
        List<String> existIds = new ArrayList<>();
        for(int i=0;i<beans.size();i++){
            T bean = beans.get(i);
            tmpIds.add(bean.getPrimaryId());
            if(tmpIds.size()==100 || i == beans.size()-1){

                String sql = checkExistSql+"('"+ TextUtils.join("','",tmpIds) +"')";
                Cursor cursor = getDB().find(sql,new String[]{});
                while (cursor.moveToNext()){
                    String id = cursor.getString(0);
                    existIds.add(id);
                }
                cursor.close();
                tmpIds.clear();
            }
        }

        return existIds;
    }

    private void checkTable(T bean) throws Exception{
        SqliteOrmDB db = getDB();
        boolean isExist = db.isTableExits(bean.getTableName());
        if(!isExist){
            db.creatTable(bean.getCreateTableSql(),bean.getTableName());
        }
    }

    /**
     * 通过事务操作，效率高
     */
    private void operatorWithTransaction(OnTransactionListener listener){
        SqliteOrmDB db = null;
        try {
            db = getDB();
            if(db.getConnection().isOpen()){
                db.getConnection().beginTransaction();
                listener.onTransaction(db);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.getConnection().isOpen()) {
                db.getConnection().setTransactionSuccessful();
                db.getConnection().endTransaction();
            }
        }
    }

    protected interface OnTransactionListener{
        void onTransaction(SqliteOrmDB db) throws Exception;
    }


    private T newDALExInstance(){
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    /** ------------------------------- 基本查询方法 -------------------------------*/

    public T findOne(String sql, String[] params){
        SqliteOrmBean dalex;
        Cursor cursor = null;
        try {
            SqliteOrmDB db = getDB();

            if(db.isTableExits(clazz)){
                cursor = db.find(sql,params);
                if (cursor != null && cursor.moveToNext()) {
                    dalex = newDALExInstance();
                    dalex.setAnnotationField(cursor);
                    return (T)dalex;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    public  T findById(String id){
        String table = getTableName();
        String sql = "select * from "+ table +" where "+ getPrimaryKey()+" =? ";
        return findOne(sql, new String[]{id});
    }

    private String getPrimaryKey() {
        return getDB().getPrimaryKey(clazz);
    }


    public List<T> findAll(){
        String table = getDB().getTableName(clazz);
        return findList("select * from "+ table, new String[]{});
    }

    public  List<T> findList(String sql, String[] params){
        List<SqliteOrmBean> list = new ArrayList<>();
        Cursor cursor = null;
        SqliteOrmBean baseDalex = null;
        try {
            SqliteOrmDB db = getDB();
            if (db.isTableExits(clazz)) {
                cursor = db.find(sql,params);
                while (cursor != null && cursor.moveToNext()) {
                    if(baseDalex==null){
                        baseDalex = newDALExInstance();
                    }
                    SqliteAnnotationCache cache = SqliteOrmLite.get().getSqliteAnnotationCache();
                    SqliteAnnotationTable table = cache.getTable(baseDalex.getClass());
                    Map<String, Integer> cursorIndex = table.getCursorIndex(cursor);

                    SqliteOrmBean dalex = baseDalex.cloneDALEx();
                    dalex.setAnnotationField(cursor,cursorIndex);
                    list.add(dalex);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return (List<T>)list;
    }

    public Map<String,Object> findOneBySql(String sql,String[] params){
        Map<String,Object> result = null;
        Cursor cursor = null;
        try {
            SqliteOrmDB db = getDB();
            if (db.isTableExits(clazz)) {
                cursor = db.find(sql,params);
                if (cursor != null && cursor.moveToNext()) {
                    result = new HashMap<>();
                    for(int i=0;i<cursor.getColumnCount();i++){
                        String name = cursor.getColumnName(i);
                        switch (cursor.getType(i)){
                            case FIELD_TYPE_NULL:
                                result.put(name,null);
                                break;
                            case FIELD_TYPE_INTEGER:
                                result.put(name,cursor.getInt(i));
                                break;
                            case FIELD_TYPE_STRING:
                                result.put(name,cursor.getString(i));
                                break;
                            case FIELD_TYPE_FLOAT:
                                result.put(name,cursor.getFloat(i));
                                break;
                            case FIELD_TYPE_BLOB:
                                result.put(name,cursor.getBlob(i));
                                break;
                        }
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    public List<Map<String,Object>> findListBySql(String sql,String[] params){
        List<Map<String,Object>> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            SqliteOrmDB db = getDB();
            if (db.isTableExits(clazz)) {
                cursor = db.find(sql,params);
                while (cursor != null && cursor.moveToNext()) {
                    Map<String,Object> map = new HashMap<>();
                    for(int i=0;i<cursor.getColumnCount();i++){
                        String name = cursor.getColumnName(i);
                        switch (cursor.getType(i)){
                            case FIELD_TYPE_NULL:
                                map.put(name,null);
                                break;
                            case FIELD_TYPE_INTEGER:
                                map.put(name,cursor.getInt(i));
                                break;
                            case FIELD_TYPE_STRING:
                                map.put(name,cursor.getString(i));
                                break;
                            case FIELD_TYPE_FLOAT:
                                map.put(name,cursor.getFloat(i));
                                break;
                            case FIELD_TYPE_BLOB:
                                map.put(name,cursor.getBlob(i));
                                break;
                        }
                    }
                    list.add(map);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return list;
    }

    public void deleteById(String id){
        SqliteOrmDB db;
        try {
            db = getDB();
            db.delete(getDB().getTableName(clazz), getDB().getPrimaryKey(clazz) +"=?", new String[]{id});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //sql语句要写对啊
    public int count(String sql, String[] params){
        int result = 0;
        Cursor cursor = null;
        try {
            SqliteOrmDB db = getDB();
            if (db.isTableExits(clazz)) {
                cursor = db.find(sql,params);
                if (cursor != null && cursor.moveToNext()) {
                    result = cursor.getInt(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    public void update(ContentValues condition,String where,String[] whereArgs){
        SqliteOrmDB db = null;
        try {
            db = getDB();
            String table = getTableName();
            db.getConnection().beginTransaction();
            db.update(table,condition, where , whereArgs);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.getConnection().setTransactionSuccessful();
                db.getConnection().endTransaction();
            }
        }
    }

}
