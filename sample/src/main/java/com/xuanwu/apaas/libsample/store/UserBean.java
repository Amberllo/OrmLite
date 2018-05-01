package com.xuanwu.apaas.libsample.store;

import com.xuanwu.apaas.ormlib.annotation.DatabaseField;
import com.xuanwu.apaas.ormlib.annotation.DatabaseField.FieldType;

import com.xuanwu.apaas.ormlib.core.SqliteOrmBean;

/**
 * Created by Amberllo on 2017/8/8 0008.
 */
public class UserBean extends SqliteOrmBean {


    public UserBean(){
        userStatus = 1;
    }
    @DatabaseField(primaryKey = true,Type= FieldType.VARCHAR)
    private String userId;

    @DatabaseField(Type= FieldType.VARCHAR)
    private String userName;

    @DatabaseField(Type= FieldType.VARCHAR)
    private String telephone;

    @DatabaseField(Type= FieldType.INT)
    private int userStatus;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }
}
