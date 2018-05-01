package com.xuanwu.apaas.libsample

import android.os.Bundle
import com.xuanwu.apaas.libsample.store.UserBean
import com.xuanwu.apaas.libsample.store.UserRepository
import com.xuanwu.apaas.ormlib.core.SqliteOrmRepository
import kotlinx.android.synthetic.main.activity_user.*

/**
 * Created by LYL on 2017/8/9.
 */
class UserActivity :BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        var userId = intent.getStringExtra("userId")
        var user = UserRepository().findById(userId)
        tv_userName.text = user.userName
        tv_userTel.text = user.telephone
        tv_userid.text = user.userId
    }
}