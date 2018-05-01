package com.xuanwu.apaas.libsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by LYL on 2017/8/8.
 */
open class BaseActivity : AppCompatActivity() {

    @Override override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setTitle(title: String){
        supportActionBar!!.title = title
    }
}