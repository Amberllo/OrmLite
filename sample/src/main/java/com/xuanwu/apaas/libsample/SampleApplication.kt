package com.xuanwu.apaas.libsample

import android.app.Application
import android.content.Context

/**
 * Created by LYL on 2017/8/1.
 */
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        XtionLibSetting.init(this)
    }
//    override fun onTerminate() {
//        super.onTerminate()
//        XtionLibSetting.onDestory()
//    }

}