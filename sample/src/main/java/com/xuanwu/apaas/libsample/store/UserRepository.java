package com.xuanwu.apaas.libsample.store;

import com.xuanwu.apaas.ormlib.core.SqliteOrmRepository;

/**
 * Created by LYL on 2017/8/9.
 */

public class UserRepository extends SqliteOrmRepository<UserBean> {
    public UserRepository() {
        super("TestDB");
    }

    public UserBean findByName(String name){
        return findOne("select * from "+ getTableName() +" where userName = ? ",new String[]{name});
    }

}
