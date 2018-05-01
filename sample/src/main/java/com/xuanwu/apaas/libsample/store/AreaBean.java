package com.xuanwu.apaas.libsample.store;

import com.xuanwu.apaas.ormlib.annotation.DatabaseField;
import com.xuanwu.apaas.ormlib.core.SqliteOrmBean;

/**
 * Created by bloom on 2017/9/5.
 */

public class AreaBean extends SqliteOrmBean {

    public AreaBean(){
        super();
    }
    /**
     * 节点编号
     */
    @DatabaseField(primaryKey = true,Type= DatabaseField.FieldType.VARCHAR)
    private String regionId;
    /**
     * 节点名字
     */
    @DatabaseField(Type= DatabaseField.FieldType.VARCHAR)
    private String name;
    /**
     * 父节点编号
     */
    @DatabaseField(Type= DatabaseField.FieldType.VARCHAR)
    private String parentId;
    /**
     * 区域编号
     */
    @DatabaseField(Type= DatabaseField.FieldType.VARCHAR)
    private String regionCode;
    /**
     * 级别
     */
    @DatabaseField(Type= DatabaseField.FieldType.VARCHAR)
    private String level;
    /**
     * 路径ID
     */
    @DatabaseField(Type= DatabaseField.FieldType.VARCHAR)
    private String idpath;
    /**
     * 路径名称
     */
    @DatabaseField(Type= DatabaseField.FieldType.VARCHAR)
    private String namepath;
    /**
     * 选中状态 :状态
     */
    @DatabaseField(Type= DatabaseField.FieldType.INT)
    private int selected; // 0:不选中 1:选中

    public String getRegionId() {
        return regionId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getIdpath() {
        return idpath;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public String getNamepath() {
        return namepath;
    }

    public int getSelect() {
        return selected;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setIdpath(String idpath) {
        this.idpath = idpath;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNamepath(String namepath) {
        this.namepath = namepath;
    }

    public void setSelect(int select) {
        this.selected = select;
    }
}
