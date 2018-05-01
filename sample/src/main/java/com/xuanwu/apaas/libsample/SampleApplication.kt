package com.xuanwu.apaas.libsample

import android.app.Application
import android.content.Context
import com.xuanwu.apaas.ormlib.core.SqliteOrmLite

/**
 * Created by LYL on 2017/8/1.
 */
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SqliteOrmLite.get().init(this)
    }
//    override fun onTerminate() {
//        super.onTerminate()
//        XtionLibSetting.onDestory()
//    }

}