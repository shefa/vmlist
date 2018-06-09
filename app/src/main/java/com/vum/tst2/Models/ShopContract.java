package com.vum.tst2.Models;

import android.provider.BaseColumns;

public class ShopContract {
    public class ShopEntry implements BaseColumns {
        public static final String TABLE = "shops";
        public static final String COL_SHOP_TITLE = "title";
        public static final String COL_SHOP_LAT = "lat";
        public static final String COL_SHOP_LNG = "lng";
    }
}