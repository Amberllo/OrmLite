package com.xuanwu.apaas.libsample.store;

import com.xuanwu.apaas.ormlib.annotation.DatabaseField;
import com.xuanwu.apaas.ormlib.core.SqliteOrmBean;

/**
 * Created by thomasliao on 2017/9/21.
 */

public class BizModelTable extends SqliteOrmBean {

    @DatabaseField(Type = DatabaseField.FieldType.VARCHAR)
    private String modellogicname;

    @DatabaseField(Type = DatabaseField.FieldType.VARCHAR)
    private String confjson;

    @DatabaseField(Type = DatabaseField.FieldType.VARCHAR)
    private String ref;

    @DatabaseField(primaryKey = true, Type = DatabaseField.FieldType.VARCHAR)
    private String modelcode;

    @DatabaseField(Type = DatabaseField.FieldType.VARCHAR)
    private String modellogiccode;

    @DatabaseField(Type = DatabaseField.FieldType.VARCHAR)
    private String status;

    public BizModelTable(String modellogicname, String confjson, String ref, String modelcode,
                         String modellogiccode, String status) {
        super();
        this.modellogicname = modellogicname;
//        this.confjson = confjson;
        this.ref = ref;
        this.modelcode = modelcode;
        this.modellogiccode = modellogiccode;
        this.status = status;
    }
}
//
//            {
//                    "modellogicname": "终端检查",
//                    "confjson": "",
//                    "ref": "",
//                    "modelcode": "896206671490191441",
//                    "modellogiccode": "910049676986814551",
//                    "status": "1"
//                    },