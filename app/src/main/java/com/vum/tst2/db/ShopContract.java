package com.vum.tst2.db;

import android.provider.BaseColumns;

public class ShopContract {
    public static final String DB_NAME = "com.vum.tst2.db";
    public static final int DB_VERSION = 1;

    public class ShopEntry implements BaseColumns {
        public static final String TABLE = "shops";
        public static final String COL_SHOP_TITLE = "title";
        public static final String COL_SHOP_LAT = "lat";
        public static final String COL_SHOP_LNG = "lng";
    }
}