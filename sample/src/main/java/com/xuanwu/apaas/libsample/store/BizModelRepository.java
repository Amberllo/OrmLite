package com.xuanwu.apaas.libsample.store;

import com.xuanwu.apaas.ormlib.core.SqliteOrmRepository;

/**
 * Created by thomasliao on 2017/9/21.
 */

public class BizModelRepository extends SqliteOrmRepository<BizModelTable> {

    public BizModelRepository(String dbName) {
        super(dbName);
    }

}
