package com.xuanwu.apaas.libsample.store

import com.xuanwu.apaas.ormlib.core.SqliteOrmRepository

/**
 * Created by Administrator on 2017/8/10 0010.
 */
class AuthUserRepository : SqliteOrmRepository<AuthUserBean>("Common.db")