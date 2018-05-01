package com.xuanwu.apaas.libsample.store;

import com.xuanwu.apaas.ormlib.core.SqliteOrmRepository;

/**
 * Created by LYL on 2017/9/8.
 */

public class AreaRepository extends SqliteOrmRepository<AreaBean>{
    public AreaRepository() {
        super("Common.db");
    }
}
