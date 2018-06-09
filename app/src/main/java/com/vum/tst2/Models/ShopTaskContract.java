package com.vum.tst2.Models;

import android.provider.BaseColumns;

public class ShopTaskContract {
    public class ShopTaskEntry implements BaseColumns {
        public static final String TABLE = "shoptasks";
        public static final String COL_SHOPTASK_SHOP = "shop";
        public static final String COL_SHOPTASK_TASK = "task";
    }
}