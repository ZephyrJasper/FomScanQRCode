package cn.dongrun.fomscanqrcode;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.dongrun.fomscanqrcode.bean.LogBean;
/**
 * @since 2023-8-15
 * @author Zephyrus,596991713@qq.com
 */
public class LogDBOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "Fom.db";
    private static final String TABLE_NAME = "log_msg";
    private static final String CREATE_TABLE_LOG_MSG =
            "create table " + TABLE_NAME + " (id integer primary key autoincrement, " +
                    "scanTime date, scanCount integer, requestSuccess integer,qrUUID" +
                    " TEXT, qrIndex integer, qrAllNum integer,rss REAL, pss REAL, jvm" +
                    " REAL);";

    public LogDBOpenHelper(Context context) {
        super(context, getFilePath(context), null, DATABASE_VERSION);
    }

    public static String getFilePath(Context context) {
        String filePath = context.getExternalFilesDir(null) + "/FomDbData/" + DB_NAME;
//        System.out.println(filePath);
        return filePath;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOG_MSG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }

    public long insertLog(LogBean logBean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("scanTime", logBean.getScanTime());
        values.put("scanCount", logBean.getScanCount());
        values.put("requestSuccess",logBean.getRequestSuccessCnt());
        values.put("qrUUID", logBean.getQrUUID());
        values.put("qrIndex", logBean.getQrIndex());
        values.put("qrAllNum", logBean.getQrAllNum());
        values.put("rss", logBean.getRss());
        values.put("pss", logBean.getPss());
        values.put("jvm", logBean.getJvm());
        return db.insert(TABLE_NAME, null, values);
    }

    public void queryData() {
        SQLiteDatabase db = getReadableDatabase();
    }
}
