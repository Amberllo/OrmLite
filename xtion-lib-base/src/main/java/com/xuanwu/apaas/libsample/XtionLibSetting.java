package com.xuanwu.apaas.libsample;

import android.content.Context;

import com.xuanwu.apaas.ormlib.core.SqliteOrmLite;

/**
 * Created by LYL on 2017/8/1.
 */

public class XtionLibSetting {

    public static void init(Context applicationContext){
        SqliteOrmLite.get().init(applicationContext);
    }

    public static void onDestory() {
        SqliteOrmLite.onDestory();
    }
}
