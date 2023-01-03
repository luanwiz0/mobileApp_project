package ddwu.moblie.finalproject.ma01_20200962;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ClothesDBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "clothes_db";
    public final static String TABLE_NAME = "clothes_table";
    public final static String COL_ID = "_id";
    public final static String COL_NAME = "name";
    public final static String COL_CATEGORY = "category";
    public final static String COL_PHOTO = "photo";

    public ClothesDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " + COL_ID + " integer primary key autoincrement,"
                + COL_NAME + " TEXT, " + COL_CATEGORY + " TEXT, " + COL_PHOTO + " TEXT);");

//		샘플 데이터
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '옷1', '코트', '');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '옷2', '반팔', '');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '옷3', '패딩', '');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table " + TABLE_NAME);
        onCreate(db);
    }
}
