package com.xuanwu.apaas.libsample.store;

import com.xuanwu.apaas.ormlib.annotation.DatabaseField;
import com.xuanwu.apaas.ormlib.core.SqliteOrmBean;

import java.util.UUID;

/**
 * Created by Administrator on 2017/8/10 0010.
 */

public class AuthUserBean extends SqliteOrmBean{
    @DatabaseField(primaryKey = true,Type = DatabaseField.FieldType.VARCHAR)
    private String uid;

    @DatabaseField(Type = DatabaseField.FieldType.VARCHAR)
    private String name;


    public AuthUserBean(){
        uid = UUID.randomUUID().toString();
        name = UUID.randomUUID().toString();
    }
}
