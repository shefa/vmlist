package com.vum.tst2.db;

import android.provider.BaseColumns;

public class ShopTaskContract {
    public static final String DB_NAME = "com.vum.tst2.db";
    public static final int DB_VERSION = 1;

    public class ShopTaskEntry implements BaseColumns {
        public static final String TABLE = "shoptasks";
        public static final String COL_SHOPTASK_SHOP = "shop";
        public static final String COL_SHOPTASK_TASK = "task";
    }
}