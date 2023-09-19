package cn.dongrun.fomscanqrcode;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.TelephonyManager;

import org.ini4j.Ini;
import org.ini4j.Wini;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Zephyrus, 596991713@qq.com
 * @since 2023-8-15
 */
public class FimDBOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "FIM.db";
    private Context context;
    static List<Map<String, String>> PSMServerList = new ArrayList<>();
    private String hostSN = "";
    private String farmName = "";
    private String farmType = "";
    private String settingTime = "";

    private int TRRUI = -1;
    private int PCRUI = -1;
    private int PRUI = -1;
    private int FIMTRRUI = -1;
    private int FIMPCRUI = -1;
    private int FIMPRUI = -1;
    private final int PAGE_SIZE = 30;
    private int FIMMaxLPID = -1;
    private int FIMMaxSPPSID = -1;
    private int FIMMaxWPID = -1;
    private int FIMMaxRPPSID = -1;
    private int FIMMaxORIID = -1;
    private int BASEUP = -1;
    private int requestSuccessCnt = 0;

    public FimDBOpenHelper(Context context) {
        super(context, getFilePath(context), null, DATABASE_VERSION);
        this.context = context;
    }

    public static String getFilePath(Context context) {
        String filePath = context.getExternalFilesDir(null) + "/FimDbData/" + DB_NAME;
//        System.out.println(filePath);
        return filePath;
    }

    public int getRequestSuccessCnt() {
        return requestSuccessCnt;
    }

    public void setRequestSuccessCnt(int requestSuccessCnt) {
        this.requestSuccessCnt = requestSuccessCnt;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String FIMPerformanceRecord = "CREATE TABLE IF NOT EXISTS " +
                "FIMPerformanceRecord (performanceRecordID INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, hostID INTEGER, performanceTypeID INTEGER, checkTime " +
                "TEXT, result REAL, memo TEXT);";
        db.execSQL(FIMPerformanceRecord);

        String FIMProcessCheckRecord = "CREATE TABLE IF NOT EXISTS " +
                "\"FIMProcessCheckRecord\" (" +
                "\"processCheckRecordID\"INTEGER PRIMARY KEY  AUTOINCREMENT," +
                "\"express\"TEXT," +
                "\"checkTime\"TEXT," +
                "\"result\"INTEGER);";
        db.execSQL(FIMProcessCheckRecord);

        String FIMTaskRunRecord = "CREATE TABLE IF NOT EXISTS " +
                "\"FIMTaskRunRecord\" (" +
                "\"taskRunRecordID\" INTEGER  PRIMARY KEY  AUTOINCREMENT," +
                "\"express\" TEXT," +
                "\"checkTime\" TEXT," +
                "\"result\" INTEGER," +
                "\"memo\" TEXT);";
        db.execSQL(FIMTaskRunRecord);

        String LightPower = "CREATE TABLE IF NOT EXISTS \"LightPower\" ( " +
                "\"ID\" INTEGER PRIMARY KEY  AUTOINCREMENT," +
                "\"PRE_DATE\" TEXT, " +
                "\"PRE_TIME\" TEXT, " +
                "\"PRE_POWER\" REAL, " +
                "\"CORRECT_POWER\" REAL, " +
                "\"SUPPER_PRE_POWER\" REAL, " +
                "\"SUPPER_CORRECT_POWER\" REAL, " +
                "\"POWER_VALUE\"REAL, " +
                "\"POWER_WATTLESS\"REAL, " +
                "\"Difference_Value\" REAL, " +
                "\"GROUP_ID\" TEXT, " +
                "\"FILE_NAME\" TEXT, " +
                "\"SUBMIT_STATE\" TEXT, " +
                "\"STATE_TYPE\" TEXT, " +
                "\"ERROR_REASON\" TEXT, " +
                "\"JFG_ERROR\" TEXT);";
        db.execSQL(LightPower);

        String OCRSetting = "CREATE TABLE IF NOT EXISTS OCRSetting (ID INTEGER " +
                "PRIMARY KEY AUTOINCREMENT, title TEXT, top INTEGER, \"left\" " +
                "INTEGER, width " +
                "INTEGER, height INTEGER);";
        db.execSQL(OCRSetting);

        String SPPS_FZYI_MON_HIS = "CREATE TABLE IF NOT EXISTS \"SPPS_FZYI_MON_HIS\" " +
                "(\n" +
                "\t\"ID\"\tINTEGER  PRIMARY KEY  AUTOINCREMENT,\n" +
                "\t\"COLLECTION_DATE\"\tTEXT,\n" +
                "\t\"COLLECTION_TIME\"\tTEXT,\n" +
                "\t\"STOREY_H\"\tINTEGER,\n" +
                "\t\"TOTAL_IRRAD\"\tREAL,\n" +
                "\t\"DIRECT_IRRAD\"\tREAL,\n" +
                "\t\"ARI_TEM\"\tREAL,\n" +
                "\t\"WIND_DIRECTION\"\tREAL,\n" +
                "\t\"WIND_SPEED\"\tREAL,\n" +
                "\t\"HUMIDITY\"\tREAL,\n" +
                "\t\"DIFFUSE_IRRAD\"\tREAL,\n" +
                "\t\"GROUP_ID\"\tTEXT,\n" +
                "\t\"PRESSURE\"\tREAL);";
        db.execSQL(SPPS_FZYI_MON_HIS);

        String WindPower = "CREATE TABLE IF NOT EXISTS \"WindPower\" (\n" +
                "\t\"ID\"\tINTEGER PRIMARY KEY  AUTOINCREMENT,\n" +
                "\t\"foca_time\"\tTEXT,\n" +
                "\t\"original_num\"\tREAL,\n" +
                "\t\"report_num\"\tREAL,\n" +
                "\t\"super_num\"\tREAL,\n" +
                "\t\"reality_num\"\tREAL,\n" +
                "\t\"Difference_Value\"\tREAL,\n" +
                "\t\"group_id\"\tTEXT,\n" +
                "\t\"all_super_num\"\tTEXT,\n" +
                "\t\"all_report_num\"\tTEXT);";
        db.execSQL(WindPower);

        String hostSetting = "CREATE TABLE IF NOT EXISTS hostSetting (sid INTEGER " +
                "PRIMARY KEY " +
                "AUTOINCREMENT, " +
                "title TEXT, value TEXT)";
        db.execSQL(hostSetting);

        String linkCheckRecord = "CREATE TABLE IF NOT EXISTS linkCheckRecord " +
                "(linkCheckRecordID " +
                "INTEGER " +
                "PRIMARY KEY  AUTOINCREMENT, host1ID INTEGER, host2ID INTEGER, " +
                "checkTime TEXT, result INTEGER);";
        db.execSQL(linkCheckRecord);

        String performanceRecord = "CREATE TABLE IF NOT EXISTS performanceRecord " +
                "(performanceRecordID INTEGER " +
                "PRIMARY KEY  AUTOINCREMENT, hostID INTEGER, performanceTypeID " +
                "INTEGER, " +
                "checkTime TEXT, result REAL, memo TEXT);";
        db.execSQL(performanceRecord);

        String performanceType = "CREATE TABLE IF NOT EXISTS performanceType " +
                "(performanceTypeID " +
                "INTEGER PRIMARY KEY  AUTOINCREMENT, performanceTypeTitle TEXT);";
        db.execSQL(performanceType);

        String process = "CREATE TABLE process (processID INTEGER PRIMARY KEY  " +
                "AUTOINCREMENT, hostID INTEGER, processTitle TEXT, express TEXT, " +
                "execTime TEXT);";
        db.execSQL(process);

        String processCheckRecord = "CREATE TABLE IF NOT EXISTS " +
                "\"processCheckRecord\" (\n" +
                "\t\"processCheckRecordID\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"express\"\tTEXT,\n" +
                "\t\"checkTime\"\tTEXT,\n" +
                "\t\"result\"\tINTEGER);";
        db.execSQL(processCheckRecord);

        String rpps_data_calstation_avg = "CREATE TABLE IF NOT EXISTS " +
                "\"rpps_data_calstation_avg\" " +
                "(\n" +
                "\t\"ID\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"ctime\"\tTEXT,\n" +
                "\t\"total_irrad\"\tREAl,\n" +
                "\t\"direct_irrad\"\tREAl,\n" +
                "\t\"diffuse_irrad\"\tREAl,\n" +
                "\t\"speed\"\tREAl,\n" +
                "\t\"direction\"\tREAl,\n" +
                "\t\"humidity\"\tREAl,\n" +
                "\t\"layer\"\tINTEGER,\n" +
                "\t\"temperature\"\tREAl,\n" +
                "\t\"pressure\"\tREAl);";
        db.execSQL(rpps_data_calstation_avg);

        String rpps_data_report_ori = "CREATE TABLE IF NOT EXISTS " +
                "\"rpps_data_report_ori\" (\n" +
                "\t\"ID\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"dtime\"\tTEXT,\n" +
                "\t\"retype\"\tINTEGER,\n" +
                "\t\"ftype\"\tTEXT,\n" +
                "\t\"memo\"\tTEXT,\n" +
                "\t\"ttype\"\tTEXT,\n" +
                "\t\"ctype\"\tINTEGER);";
        db.execSQL(rpps_data_report_ori);

        String task = "CREATE TABLE task (taskID INTEGER PRIMARY KEY  " +
                "AUTOINCREMENT, taskTypeID INTEGER, hostID INTEGER, taskTitle TEXT," +
                " express TEXT, execTime TEXT);";
        db.execSQL(task);

        String taskRunRecord = "CREATE TABLE IF NOT EXISTS \"taskRunRecord\" (\n" +
                "\t\"taskRunRecordID\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"express\"\tTEXT,\n" +
                "\t\"checkTime\"\tTEXT,\n" +
                "\t\"result\"\tINTEGER,\n" +
                "\t\"memo\"\tTEXT);";
        db.execSQL(taskRunRecord);

        String taskType = "CREATE TABLE IF NOT EXISTS taskType (taskTypeID INTEGER " +
                "PRIMARY KEY " +
                "AUTOINCREMENT, taskTypeTitle TEXT);";
        db.execSQL(taskType);
    }

    //数据入库
    public long addNewFIMTaskRunRecord(int taskRunRecordID, String express,
                                       String checkTime, int result, String memo) {
        SQLiteDatabase db = getWritableDatabase();
        String sqlstr =
                "select count(1) from FIMTaskRunRecord where taskRunRecordID='" + taskRunRecordID + "'" + ";";
        Cursor cursor = db.rawQuery(sqlstr, null);
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                cursor.close();
                return -1;
            }
        }

        ContentValues values = new ContentValues();
        values.put("taskRunRecordID", taskRunRecordID);
        values.put("express", express);
        values.put("checkTime", checkTime);
        values.put("result", result);
        values.put("memo", memo);
        if (cursor != null) {
            cursor.close();
        }
        return db.insert("FIMTaskRunRecord", null, values);
    }


    public long addNewFIMProcessCheckRecord(int processCheckRecordID, String express,
                                            String checkTime, int result) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "select count(1) from FIMProcessCheckRecord where " +
                "processCheckRecordID=" + processCheckRecordID;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                cursor.close();
                return -1;
            }
        }

        ContentValues values = new ContentValues();
        values.put("processCheckRecordID", processCheckRecordID);
        values.put("express", express);
        values.put("checkTime", checkTime);
        values.put("result", result);
        if (cursor != null) {
            cursor.close();
        }
        return db.insert("FIMProcessCheckRecord", null, values);
    }

    public long addNewPerformanceRecord(int performanceRecordID,
                                        int performanceTypeID,
                                        String checkTime, Double result, String memo) {
        SQLiteDatabase db = getWritableDatabase();

        String sqlStr = "select count(1) from FIMPerformanceRecord where " +
                "performanceRecordID=" + performanceRecordID;
        Cursor cursor = db.rawQuery(sqlStr, null);
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                cursor.close();
                return -1;
            }
        }
        ContentValues values = new ContentValues();
        values.put("performanceRecordID", performanceRecordID);
        values.put("performanceTypeID", performanceTypeID);
        values.put("checkTime", checkTime);
        values.put("result", result);
        values.put("memo", memo);
        if (cursor != null) {
            cursor.close();
        }
        return db.insert("FIMPerformanceRecord", null, values);
    }

    public long addNewWindPower(String foca_time, double original_num,
                                double report_num, double super_num,
                                double reality_num, double Difference_Value,
                                String group_id, String all_super_num,
                                String all_report_num) {
        SQLiteDatabase db = getWritableDatabase();

        String query = "select count(1) from WindPower where foca_time=? and " +
                "group_id=?";
        String[] selectionArgs = {foca_time, group_id};
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                cursor.close();
                return -1;
            }
        }
        ContentValues values = new ContentValues();
        values.put("foca_time", foca_time);
        values.put("original_num", original_num);
        values.put("report_num", report_num);
        values.put("super_num", super_num);
        values.put("reality_num", reality_num);
        values.put("Difference_Value", Difference_Value);
        values.put("group_id", group_id);
        values.put("all_super_num", all_super_num);
        values.put("all_report_num", all_report_num);
        return db.insert("WindPower", null, values);
    }

    public long addNewRpps_data_calstation_avg(String ctime, double total_irrad,
                                               double direct_irrad,
                                               double diffuse_irrad,
                                               double speed, double direction,
                                               double humidity, int layer,
                                               double temperature, double pressure) {
        SQLiteDatabase db = getWritableDatabase();
        String sql =
                "select count(1) from rpps_data_calstation_avg where ctime = '" + ctime + "' and layer = " + layer;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                cursor.close();
                return -1;
            }
        }
        ContentValues values = new ContentValues();
        values.put("ctime", ctime);
        values.put("total_irrad", total_irrad);
        values.put("direct_irrad", direct_irrad);
        values.put("diffuse_irrad", diffuse_irrad);
        values.put("speed", speed);
        values.put("direction", direction);
        values.put("humidity", humidity);
        values.put("layer", layer);
        values.put("temperature", temperature);
        values.put("pressure", pressure);
        return db.insert("rpps_data_calstation_avg", null, values);
    }

    public long addNewRpps_data_report_ori(String dtime, int retype, String ftype,
                                           String memo, String ttype, int ctype) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "select count(1) from rpps_data_report_ori where dtime = ? and " +
                "memo = ?";
        String[] args = {dtime, memo};
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                cursor.close();
                return -1;
            }
        }
        ContentValues values = new ContentValues();
        values.put("dtime", dtime);
        values.put("retype", retype);
        values.put("ftype", ftype);
        values.put("memo", memo);
        values.put("ttype", ttype);
        values.put("ctype", ctype);
        return db.insert("rpps_data_report_ori", null, values);
    }

    public long addNewLightPower(String PRE_DATE, String PRE_TIME, double PRE_POWER,
                                 double CORRECT_POWER, double SUPPER_PRE_POWER,
                                 double SUPPER_CORRECT_POWER, double POWER_VALUE,
                                 double POWER_WATTLESS, double Difference_Value,
                                 String GROUP_ID, String FILE_NAME, String SUBMIT_STATE,
                                 String STATE_TYPE, String ERROR_REASON,
                                 String JFG_ERROR) {
        SQLiteDatabase db = getWritableDatabase();

        String sql = "select count(1) from LightPower where PRE_DATE = ? and PRE_TIME" +
                " = ? and GROUP_ID = ? and FILE_NAME = ?";
        String[] args = {PRE_DATE, PRE_TIME, GROUP_ID, FILE_NAME};
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                cursor.close();
//                System.out.println("addNewLightPower数据重复");
                return -1;
            }
        }
        ContentValues values = new ContentValues();
        values.put("PRE_DATE", PRE_DATE);
        values.put("PRE_TIME", PRE_TIME);
        values.put("PRE_POWER", PRE_POWER);
        values.put("CORRECT_POWER", CORRECT_POWER);
        values.put("SUPPER_PRE_POWER", SUPPER_PRE_POWER);
        values.put("SUPPER_CORRECT_POWER", SUPPER_CORRECT_POWER);
        values.put("POWER_VALUE", POWER_VALUE);
        values.put("POWER_WATTLESS", POWER_WATTLESS);
        values.put("Difference_Value", Difference_Value);
        values.put("GROUP_ID", GROUP_ID);
        values.put("FILE_NAME", FILE_NAME);
        values.put("SUBMIT_STATE", SUBMIT_STATE);
        values.put("STATE_TYPE", STATE_TYPE);
        values.put("ERROR_REASON", ERROR_REASON);
        values.put("JFG_ERROR", JFG_ERROR);
        return db.insert("LightPower", null, values);
    }

    public long addNewSPPS_FZYI_MON_HIS(String COLLECTION_DATE, String COLLECTION_TIME,
                                        int STOREY_H, double TOTAL_IRRAD,
                                        double DIRECT_IRRAD, double ARI_TEM,
                                        double WIND_DIRECTION, double WIND_SPEED,
                                        double HUMIDITY, double DIFFUSE_IRRAD,
                                        String GROUP_ID, double PRESSURE) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "select count(1) from SPPS_FZYI_MON_HIS where COLLECTION_DATE = " +
                "? and COLLECTION_TIME = ? and GROUP_ID = ? and STOREY_H = ?";
        String[] args = {COLLECTION_DATE, COLLECTION_TIME, GROUP_ID,
                String.valueOf(STOREY_H)};
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
//                System.out.println("addNewSPPS_FZYI_MON_HIS数据重复");
                cursor.close();
                return -1;
            }
        }

        ContentValues values = new ContentValues();
        values.put("COLLECTION_DATE", COLLECTION_DATE);
        values.put("COLLECTION_TIME", COLLECTION_TIME);
        values.put("STOREY_H", STOREY_H);
        values.put("TOTAL_IRRAD", TOTAL_IRRAD);
        values.put("DIRECT_IRRAD", DIRECT_IRRAD);
        values.put("ARI_TEM", ARI_TEM);
        values.put("WIND_DIRECTION", WIND_DIRECTION);
        values.put("WIND_SPEED", WIND_SPEED);
        values.put("HUMIDITY", HUMIDITY);
        values.put("DIFFUSE_IRRAD", DIFFUSE_IRRAD);
        values.put("GROUP_ID", GROUP_ID);
        values.put("PRESSURE", PRESSURE);
        return db.insert("SPPS_FZYI_MON_HIS", null, values);
    }

    //数据处理
    public void initSetting() { //初始化各数据
        try {
            InputStream inputStream = context.getAssets().open("farm.ini");
            Wini config = new Wini(inputStream);
            farmName = config.get("FARM", "name");
            int PSMServerCount = Integer.parseInt(config.get("PSMServerCount", "count"
            ));
            PSMServerList.clear();
            for (int i = 0; i < PSMServerCount; i++) {
                String serverIP = config.get("PSMServer" + i, "IP");
                String port = config.get("PSMServer" + i, "port");
                String path = config.get("PSMServer" + i, "path");
                String reportPath = config.get("PSMServer" + i, "reportPath");
                String sslMode = config.get("PSMServer" + i, "sslMode");

                Map<String, String> serverDetails = new HashMap<>();
                serverDetails.put("IP", serverIP);
                serverDetails.put("sslMode", sslMode);
                serverDetails.put("port", port);
                serverDetails.put("path", path);
                serverDetails.put("reportPath", reportPath);
                PSMServerList.add(serverDetails);
            }
            hostSN = readHostSetting("hostSN", getMEID(context));
            TRRUI = Integer.parseInt(readHostSetting("TaskRunRecordUploadIndex", "-1"));
            FIMTRRUI = Integer.parseInt(readHostSetting("FIMTaskRunRecordUploadIndex"
                    , "-1"));
            PCRUI = Integer.parseInt(readHostSetting("ProcessCheckRecordUploadIndex",
                    "-1"));
            FIMPCRUI = Integer.parseInt(readHostSetting(
                    "FIMProcessCheckRecordUploadIndex", "-1"));
            PRUI = Integer.parseInt(readHostSetting("PerformanceRecordUploadIndex",
                    "-1"));
            FIMPRUI = Integer.parseInt(readHostSetting(
                    "FIMPerformanceRecordUploadIndex", "-1"));
            FIMMaxLPID = Integer.parseInt(readHostSetting("FIMLightPowerIndex", "-1"));
            FIMMaxSPPSID = Integer.parseInt(readHostSetting(
                    "FIMSPPS_FZYI_MON_HISIndex", "-1"));
            FIMMaxWPID = Integer.parseInt(readHostSetting("FIMWindPowerIndex", "-1"));
            FIMMaxRPPSID = Integer.parseInt(readHostSetting(
                    "FIMRpps_data_calstation_avg_Index", "-1"));
            FIMMaxORIID = Integer.parseInt(readHostSetting(
                    "FIMRpps_data_report_ori_Index", "-1"));
            BASEUP = Integer.parseInt(readHostSetting("BASEUP", "-1"));
            settingTime = readHostSetting("settingTime", "");
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //对hostSetting表进行查询 增加操作，此表中有唯一UUID、需要上报的数据开始的各ID、回复时间等
    public String readHostSetting(String title, String defaultValue) {//上方法调用
        SQLiteDatabase db = getWritableDatabase();
        String sqlstr =
                "select value from hostSetting where title='" + title + "'" + ";";
        Cursor cursor = db.rawQuery(sqlstr, null); // 执行查询并获取结果集
        if (cursor.moveToFirst()) { // 如果结果集中有数据
            String value = cursor.getString(0); // 获取值value
            cursor.close(); // 关闭结果集
            return value; // 返回查询得到的值
        } else {
            sqlstr =
                    "insert into hostSetting(title,value) values('" + title + "','" + defaultValue + "');";
            try {
                db.execSQL(sqlstr);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.close();
            }
            return defaultValue;
        }
    }

    //返回值如何处理
    public void saveHostSetting(String title, String value) {//保存更新的上传标志
        SQLiteDatabase db = getWritableDatabase();
        String sqlstr = "select count(1) from hostSetting where title='" + title + "'";
        Cursor cursor = db.rawQuery(sqlstr, null);
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                sqlstr =
                        "update hostSetting set value='" + value + "' where title='" + title + "'";
                db.execSQL(sqlstr);
            } else {
                readHostSetting(title, value);
            }
        }
        cursor.close();
    }

    public String getMEID(Context context) {
        // 获取设备信息
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("HardwareIds") String deviceMEID = telephonyManager.getDeviceId();
        // 使用设备MEID
        return deviceMEID;
    }

    //读取本机（FOM端）任务监测到的数据      后期可以换成安卓
    public List<Object[]> readTaskRunRecords() { //可先不查 没数据
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select taskRunRecordID, express, checkTime, result, memo " +
                "from taskRunRecord " +
                "where taskRunRecordID > " + TRRUI +
                " limit 0, " + PAGE_SIZE + ";";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取本机（FOM端）进程监测到的数据
    public List<Object[]> readProcessCheckRecords() { //可不用   后期可以换成安卓
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select processCheckRecordID" +
                ",express,checkTime,result" +
                " from processCheckRecord" +
                " where processCheckRecordID > " + PCRUI +
                " limit 0," + PAGE_SIZE + ";";
        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取本机（FOM端）性能监测到的数据    后期可以换成安卓
    public List<Object[]> readPerformanceRecords() { //可不用
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select performanceRecordID" +
                ",performanceTypeID,checkTime,result,memo" +
                " from performanceRecord" +
                " where performanceRecordID > " + PRUI +
                " limit 0," + PAGE_SIZE + ";";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取对应FIM端任务监测到的数据
    public List<Object[]> readFIMTaskRunRecords() {
        SQLiteDatabase db = getReadableDatabase();

        String sqlstr = "select taskRunRecordID, express, checkTime, result, memo " +
                "from FIMTaskRunRecord " +
                "where taskRunRecordID > " + FIMTRRUI +
                " limit 0, " + PAGE_SIZE + ";";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取对应FIM端进程监测到的数据
    public List<Object[]> readFIMProcessCheckRecords() {
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select processCheckRecordID" +
                ",express,checkTime,result" +
                " from FIMProcessCheckRecord" +
                " where processCheckRecordID > " + FIMPCRUI +
                " limit 0," + PAGE_SIZE + ";";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取对应FIM端性能监测到的数据
    public List<Object[]> readFIMPerformanceRecords() {
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select performanceRecordID" +
                ",performanceTypeID,checkTime,result,memo" +
                " from FIMPerformanceRecord" +
                " where performanceRecordID > " + FIMPRUI +
                " limit 0," + PAGE_SIZE + ";";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    //
    public List<Object[]> readFIMLightPower() {
        SQLiteDatabase db = getReadableDatabase();
//        System.out.println("FIMMaxLPID:"+FIMMaxLPID);

        String sqlstr = "select ID, PRE_DATE, PRE_TIME, PRE_POWER, CORRECT_POWER, " +
                "SUPPER_PRE_POWER, SUPPER_CORRECT_POWER, POWER_VALUE" +
                ", POWER_WATTLESS, Difference_Value, GROUP_ID, FILE_NAME, " +
                "SUBMIT_STATE, STATE_TYPE" +
                ", ERROR_REASON, JFG_ERROR" +
                " from LightPower" +
                " where ID > " + FIMMaxLPID +
                " limit 0," + PAGE_SIZE + ";";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取FIMSPPS_FZYI_MON_HIS数据
    public List<Object[]> readFIMSPPS_FZYI_MON_HIS() {
//        System.out.println("FIMMaxSPPSID"+FIMMaxSPPSID);

        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select ID,COLLECTION_DATE,COLLECTION_TIME,STOREY_H," +
                "TOTAL_IRRAD,DIRECT_IRRAD,ARI_TEM,WIND_DIRECTION" +
                ",WIND_SPEED,HUMIDITY,DIFFUSE_IRRAD,GROUP_ID,PRESSURE " +
                " from SPPS_FZYI_MON_HIS" +
                " where ID > " + FIMMaxSPPSID +
                " limit 0," + PAGE_SIZE + ";";
        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }


    // 读取FIMWindPower数据
    public List<Object[]> readFIMWindPower() {
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select ID,foca_time,original_num,report_num,super_num," +
                "reality_num,Difference_Value,group_id," +
                "all_super_num,all_report_num" +
                " from WindPower" +
                " where ID > " + FIMMaxWPID +
                " limit 0," + PAGE_SIZE +
                ";";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取FIMRpps_data_report_ori数据
    public List<Object[]> readFIMRpps_data_report_ori() {
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select ID,dtime,retype,ftype,memo,ttype,ctype" +
                " from rpps_data_report_ori" +
                " where ID > " + FIMMaxORIID +
                " limit 0," + PAGE_SIZE + ";";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取FIMRpps_data_calstation_avg数据
    public List<Object[]> readFIMRpps_data_calstation_avg() {
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select ID,ctime,total_irrad,direct_irrad,diffuse_irrad," +
                "speed,direction,humidity" +
                ",layer,temperature,pressure" +
                " from rpps_data_calstation_avg" +
                " where ID > " + FIMMaxRPPSID +
                " limit 0," + PAGE_SIZE + ";";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取最新的一条任务监测数据
    public List<Object[]> readLastTaskRunRecord() {  //可以不调用
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select taskRunRecordID" +
                ",checkTime,result,memo" +
                " from taskRunRecord" +
                " where taskRunRecordID =" +
                " (select max(taskRunRecordID)" +
                " from taskRunRecord);";                      //sql有错误 缺字段

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取最后一条 processCheckRecord 数据
    public List<Object[]> readLastProcessCheckRecord() { //可不用  缺字段
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select processCheckRecordID" +
                ",checkTime,result" +
                " from processCheckRecord m " +
                " where processCheckRecordID= " +
                "(select max(processCheckRecordID) " +
                "from processCheckRecord);";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    // 读取最后一条 performanceRecord 数据
    public List<Object[]> readLastPerformanceRecord() { //可不用
        SQLiteDatabase db = getReadableDatabase();
        String sqlstr = "select performanceRecordID" +
                ",performanceTypeID,checkTime,result,memo" +
                " from performanceRecord m " +
                " where performanceRecordID= " +
                "(select max(performanceRecordID) " +
                "from performanceRecord s " +
                "where m.performanceTypeID=s.performanceTypeID);";

        Cursor cursor = db.rawQuery(sqlstr, null);
        return queryData(cursor);
    }

    public String getLasTPPID() {//用不到
        String lastTPPID = "";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(MAX(taskRunRecordID),0) FROM " +
                "taskRunRecord", null);
        if (cursor.moveToFirst()) {
            int maxTaskRunRecordID = cursor.getInt(0);
            lastTPPID += maxTaskRunRecordID;
        }
        cursor.close();
        cursor = db.rawQuery("SELECT IFNULL(MAX(processCheckRecordID),0) FROM " +
                "processCheckRecord", null);
        if (cursor.moveToFirst()) {
            int maxProcessCheckRecordID = cursor.getInt(0);
            lastTPPID += "-" + maxProcessCheckRecordID;
        }
        cursor.close();
        cursor = db.rawQuery("SELECT IFNULL(MAX(performanceRecordID),0) FROM " +
                "performanceRecord", null);
        if (cursor.moveToFirst()) {
            int maxPerformanceRecordID = cursor.getInt(0);
            lastTPPID += "-" + maxPerformanceRecordID;
        }
        cursor.close();
        db.close();
        return lastTPPID;
    }

    public boolean autoCleanDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        String sqlstr = "PRAGMA auto_vacuum = 1";
        try {
            db.execSQL(sqlstr);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    //读库拼请求JSON
    public String getDataJson() throws JSONException {
        initSetting();
        JSONObject jsonData = new JSONObject();
        if (farmType.equals("1")) {
            JSONArray nullArray = new JSONArray("[]");
            jsonData.put("data", new JSONObject()
                    .put("hostSN", hostSN)
                    .put("name", farmName)
                    .put("farmType", farmType)
                    .put("settingTime", settingTime)
                    .put("farmdb", 0)//getBASEUP()
                    .put("FOMData", new JSONObject()
                            .put("taskData", nullArray)//readTaskRunRecords()
                            .put("processData", nullArray)//readProcessCheckRecords()
                            .put("performanceData", nullArray)//readPerformanceRecords()
                    )
                    .put("FIMData", new JSONObject()
                            .put("taskData",
                                    new JSONArray(readFIMTaskRunRecords()))
                            .put("processData",
                                    new JSONArray(readFIMProcessCheckRecords()))
                            .put("performanceData",
                                    new JSONArray(readFIMPerformanceRecords()))
                            .put("LightPowerData",
                                    new JSONArray(readFIMLightPower()))
                            .put("SPPS_FZYI_MON_HIS_Data",
                                    new JSONArray(readFIMSPPS_FZYI_MON_HIS()))
                    )
            );
        } else if (farmType.equals("2")) {
            JSONArray nullArray = new JSONArray("[]");
            jsonData.put("data", new JSONObject()
                    .put("hostSN", hostSN)
                    .put("name", farmName)
                    .put("farmType", farmType)
                    .put("settingTime", settingTime)
                    .put("farmdb", 0)//getBASEUP()
                    .put("FOMData", new JSONObject()
                            .put("taskData", nullArray)//readTaskRunRecords()
                            .put("processData", nullArray)//readProcessCheckRecords()
                            .put("performanceData", nullArray)//readPerformanceRecords()
                    )
                    .put("FIMData", new JSONObject()
                            .put("taskData",
                                    new JSONArray(readFIMTaskRunRecords()))
                            .put("processData",
                                    new JSONArray(readFIMProcessCheckRecords()))
                            .put("performanceData",
                                    new JSONArray(readFIMPerformanceRecords()))
                            .put("WindPowerData",
                                    new JSONArray(readFIMWindPower()))
                            .put("rpps_data_calstation_avg_Data",
                                    new JSONArray(readFIMRpps_data_calstation_avg()))
                            .put("rpps_data_report_ori",
                                    new JSONArray(readFIMRpps_data_report_ori()))
                    )
            );
        }
        String jsonStr = jsonData.toString();
        return jsonStr;
    }

    public void uploadData() throws JSONException {
        initSetting();
        JSONObject data;
        String dataStr = getDataJson();
//        System.out.println("请求的JsonData:" + dataStr);

        for (Map<String, String> serverDetails : PSMServerList) {
            String IP = serverDetails.get("IP");
            String port = serverDetails.get("port");
            String path = serverDetails.get("path");
            String sslMode = serverDetails.get("sslMode");
            String reportPath = serverDetails.get("reportPath");
            String url;
            if (sslMode != null && sslMode.equals("1")) {
                url = "https://" + IP + ":" + port + "/" + path;
            } else {
                url = "http://" + IP + ":" + port + "/" + path;
            }
//            System.out.println("URL:" + url);
            //发送请求
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] chain,
                                                           String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] chain,
                                                           String authType) {
                            }

                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new SecureRandom());
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                HostnameVerifier hostnameVerifier = (hostname, session) -> true;
                OkHttpClient client = new OkHttpClient.Builder()
                        .sslSocketFactory(sslSocketFactory,
                                (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier(hostnameVerifier)
                        .build();

                RequestBody requestBody = RequestBody.create(MediaType.parse(
                        "application/json"), dataStr);
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
//                        System.out.println("请求码: " + response.code());

                        String responseBody = response.body().string();
                        // 处理响应数据
                        JSONObject jsonResult = new JSONObject(responseBody);
//                        System.out.println("响应数据：" + jsonResult);

                        String resultValue = jsonResult.optString("result");
                        if (resultValue.equals("setting")) {
                            clearSetting();
                            saveSettingPSMServers(jsonResult.optJSONArray("PSMServers"
                            ));
                            saveSettingTasks(jsonResult.optJSONArray("tasks"));
                            saveSettingProcesses(jsonResult.optJSONArray("processes"));
                            saveHostSetting("settingTime", jsonResult.optString(
                                    "settingTime"));
                        }
                        if (resultValue.equals("OK")) {
                            String hostId = jsonResult.optString("host_id", "");
                            /*if (!hostId.equals("")) {
                                continue;   //响应数据暂时没有hostId 这一步没有走
                            }*/
                            int psmMaster = jsonResult.optInt("psm_master");
                            if (psmMaster == 1) {
                                requestSuccessCnt++;
                                data = jsonResult;
                                saveData(data);
                            }
                        }
                    } else {
//                        System.out.println("HTTP请求失败!");
                    }
                }
            } catch (IOException | JSONException | NoSuchAlgorithmException |
                     KeyManagementException e) {
                e.printStackTrace();
//                System.out.println("上传数据失败: " + e.getMessage());
            }
        }
//        saveData(data);
    }

    public void saveData(JSONObject data) throws JSONException {//保存从响应获取的下次上传数据的ID
        saveHostSetting("TaskRunRecordUploadIndex",
                String.valueOf(data.getJSONObject("lastID").getInt("TRRID")));
        saveHostSetting("ProcessCheckRecordUploadIndex",
                String.valueOf(data.getJSONObject("lastID").getInt("PCRID")));
        saveHostSetting("PerformanceRecordUploadIndex",
                String.valueOf(data.getJSONObject("lastID").getInt("PRID")));
        saveHostSetting("FIMTaskRunRecordUploadIndex",
                String.valueOf(data.getJSONObject("lastID").getInt("FIMTRRID")));
        saveHostSetting("FIMProcessCheckRecordUploadIndex",
                String.valueOf(data.getJSONObject("lastID").getInt("FIMPCRID")));
        saveHostSetting("FIMPerformanceRecordUploadIndex",
                String.valueOf(data.getJSONObject("lastID").getInt("FIMPRID")));
        if (farmType.equals("1")) {
//            System.out.println("修改FIMLightPowerIndex");
            saveHostSetting("FIMLightPowerIndex",
                    String.valueOf(data.getJSONObject("lastID").getInt("FIMMaxLPID")));
            saveHostSetting("FIMSPPS_FZYI_MON_HISIndex",
                    String.valueOf(data.getJSONObject("lastID").getInt("FIMMaxSPPSID")));
        }
        if (farmType.equals("2")) {
            saveHostSetting("FIMWindPowerIndex",
                    String.valueOf(data.getJSONObject("lastID").getInt("FIMMaxWPID")));
            saveHostSetting("FIMRpps_data_calstation_avg_Index",
                    String.valueOf(data.getJSONObject("lastID").getInt("FIMMaxRPPSID")));
            saveHostSetting("FIMRpps_data_report_ori_Index",
                    String.valueOf(data.getJSONObject("lastID").getInt("FIMMaxORIID")));
        }
        try {
            saveHostSetting("BASEUP", String.valueOf(data.getInt("BASEUP")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFarmType(String farmType) {//根据扫码来获取的FarmType
        this.farmType = farmType;
    }

    //更改配置文件farm.ini
    public void saveSettingPSMServers(JSONArray PSMServers) throws JSONException {
        try {
            InputStream inputStream = context.getAssets().open("farm.ini");
            Ini config = new Wini(inputStream);
            int PSMServerCount = Integer.parseInt(config.get("PSMServerCount", "count"
            ));
//            逐一删除当前PSMServer配置
            for (int i = 0; i < PSMServerCount; i++) {
                String sectionName = "PSMServer" + i;
                config.remove(sectionName);
            }
//          根据参数设置新的PSM配置
            for (int i = 0; i < PSMServers.length(); i++) {
                config.put("PSMServer" + i, "IP",
                        PSMServers.getJSONArray(i).getString(0));
                config.put("PSMServer" + i, "port",
                        PSMServers.getJSONArray(i).getInt(1));
                config.put("PSMServer" + i, "path",
                        PSMServers.getJSONArray(i).getString(2));
                config.put("PSMServer" + i, "reportPath",
                        PSMServers.getJSONArray(i).getString(3));
                config.put("PSMServer" + i, "sslMode",
                        PSMServers.getJSONArray(i).getString(4));
            }
            config.store();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSettingTasks(JSONArray tasks) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject task = tasks.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put("taskID", task.getInt("taskID"));
                values.put("taskTypeID", task.getInt("taskTypeID"));
                values.put("hostID", task.getInt("hostID"));
                values.put("taskTitle", task.getString("taskTitle"));
                values.put("express", task.getString("express"));
                values.put("execTime", task.getString("execTime"));
                db.insert("task", null, values);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void saveSettingProcesses(JSONArray processes) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            for (int i = 0; i < processes.length(); i++) {
                JSONObject process = processes.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put("processID", process.getInt("processID"));
                values.put("hostID", process.getInt("hostID"));
                values.put("processTitle", process.getString("processTitle"));
                values.put("express", process.getString("express"));
                values.put("execTime", process.getString("execTime"));

                db.insert("process", null, values);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void clearSetting() {
        SQLiteDatabase db = getWritableDatabase();
        String sqlstr = "delete from task";
        db.execSQL(sqlstr);
        sqlstr = "delete from process";
        db.execSQL(sqlstr);
        db.close();
    }

    public List<Object[]> queryData(Cursor cursor) {//
        List<Object[]> rows = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Object[] row = new Object[cursor.getColumnCount()];
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_INTEGER:
                            row[i] = (cursor.getInt(i));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            row[i] = (cursor.getFloat(i));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            row[i] = (cursor.getString(i));
                            break;
                    }
                }
                rows.add(row);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return rows;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}

