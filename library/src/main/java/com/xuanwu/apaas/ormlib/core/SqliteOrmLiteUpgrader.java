package com.xuanwu.apaas.ormlib.core;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Amberllo on 2018/5/1 0001.
 */

public class SqliteOrmLiteUpgrader {
    private final static String TAG = SqliteOrmLiteUpgrader.class.getSimpleName();

    public static void migrate(SQLiteDatabase database) {
        migrate(database,null);
    }

    public static void migrate(SQLiteDatabase database, Class<? extends SqliteOrmRepository<?>>... daoClasses) {
        printLog("【Generate temp table】start");
        generateTempTables(database, daoClasses);
        printLog("【Generate temp table】complete");

        dropAllTables(database, daoClasses);
        createAllTables(database, daoClasses);

        printLog("【Restore data】start");
        restoreData(database, daoClasses);
        printLog("【Restore data】complete");
    }

    private static void printLog(String str) {
        Log.v(TAG,str);
    }

    private static void dropAllTables(SQLiteDatabase database, Class<? extends SqliteOrmRepository<?>>[] daoClasses) {
    }

    private static void generateTempTables(SQLiteDatabase database, Class<? extends SqliteOrmRepository<?>>[] daoClasses) {
    }

    private static void createAllTables(SQLiteDatabase database,  Class<? extends SqliteOrmRepository<?>>[] daoClasses) {
    }

    private static void restoreData(SQLiteDatabase database, Class<? extends SqliteOrmRepository<?>>[] daoClasses) {
    }
}
