package cn.dongrun.fomscanqrcode;

import static com.google.zxing.integration.android.IntentIntegrator.parseActivityResult;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.dongrun.fomscanqrcode.bean.LogBean;
import cn.dongrun.fomscanqrcode.bean.QrData;
//import cn.dongrun.fomscanqrcode.utils.TimeHandlerUtil;
import cn.dongrun.fomscanqrcode.utils.Utils;

/**
 * @author Zephyrus, 596991713@qq.com
 * @since 2023-8-15
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";//log t生成
    protected ArrayList<QrData> qrDataList = new ArrayList<QrData>();
    private int scanCount = 1;
    private static final int MAX_SCAN_COUNT = 10000;
    private int cleanCount = 1;
    private Button scanButton;
    private IntentIntegrator integrator;
    private LogDBOpenHelper logDbOpenHelper;
    private FimDBOpenHelper fimDBOpenHelper;
    private ExecutorService mExecutor;
    private LogBean logBean;
    private QrData qrData;
    private String farmType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logDbOpenHelper = new LogDBOpenHelper(this);
        fimDBOpenHelper = new FimDBOpenHelper(this);
        integrator = new IntentIntegrator(this);
        int numThreads = Runtime.getRuntime().availableProcessors() + 2;
//        System.out.println("numThreads:" + numThreads);
        mExecutor = Executors.newFixedThreadPool(numThreads);
        logBean = new LogBean();
        scanButton = findViewById(R.id.scan_button);
        if (isRestarted()) {
            goScan();
        } else {
            scanButton.setOnClickListener(v -> goScan());
        }
    }

    private synchronized void goScan() {
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("请对准二维码");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult intentResult = parseActivityResult(requestCode, resultCode,
                intent);
        if (intentResult != null && intentResult.getContents() != null) {
            String scanResult = intentResult.getContents();
//            System.out.println("扫描的数据是:" + scanResult);
            //数据处理
            String[] splitResArr = scanResult.split(",");
            int qrAllNum = Integer.parseInt(splitResArr[1]);
//            System.out.println("qrAllNum:" + qrAllNum);

            Integer qrIndex = Integer.parseInt(splitResArr[2]);
//            System.out.println("qrIndex:" + qrIndex);

            String qrUUID = splitResArr[0].substring(2, splitResArr[0].length() - 1);
//            System.out.println("UUID:" + qrUUID);

            // 解析花括号内的数据
            int start = scanResult.indexOf("{");
            int end = scanResult.lastIndexOf("}");
            String qrDataRes = scanResult.substring(start + 1, end);

//            System.out.println("farmType:" + farmType);

            SharedPreferences uuidList = getSharedPreferences(
                    "UUID_LIST", Context.MODE_PRIVATE);
            String listUUID = uuidList.getString("UUID", "");
            if (!qrUUID.equals(listUUID)) {
                SharedPreferences.Editor editor = uuidList.edit();
                editor.putString("UUID", qrUUID);
                editor.apply();
                qrDataList.clear();
            }
            qrData = new QrData(qrIndex, qrDataRes);
            //相同二维码不加入列表
            if (!qrDataList.contains(qrData)) {
                qrDataList.add(qrData);
            }

            //数据库写入
            String scanTime = Utils.getCurrentTime();
            double rssUsage = Utils.getRSSyUsage();
            double pssUsage = getPSSUsage();
            double jvmUsage = Utils.getJVMUsage();
            logBean.setScanTime(scanTime);
            logBean.setScanCount(scanCount);
            logBean.setRequestSuccessCnt(fimDBOpenHelper.getRequestSuccessCnt());
            logBean.setQrUUID(qrUUID);
            logBean.setQrIndex(qrIndex);//测试定位漏扫 是不是没有扫到
            logBean.setQrAllNum(qrAllNum);
            logBean.setRss(rssUsage);
            logBean.setPss(pssUsage);
            logBean.setJvm(jvmUsage);

            mExecutor.execute(() -> {
                long rowID = logDbOpenHelper.insertLog(logBean);
                if (rowID != -1) {
                    Log.d(TAG, "数据插入成功");
//                        System.out.println("数据插入成功");
                } else {
                    Log.d(TAG, "数据插入失败");
//                        System.out.println("数据插入失败");
                }
            });
            Context context = getApplicationContext();
            if (cleanCount > 100) {
                Utils.cleanCache(context);
                cleanCount = 0;
            }
            cleanCount++;
            scanCount++;
            MyCaptureActivity.finishScanActivity();
            if (qrDataList.size() == qrAllNum) {
                Collections.sort(qrDataList);
                StringBuilder allQrData = new StringBuilder();
                for (QrData qr : qrDataList) {
                    allQrData.append(qr.getQrData());
                }
                String decodedData = Uri.decode(allQrData.toString());
                //数据入库
                try {
                    JSONObject jsonQrData = new JSONObject(decodedData);
                    JSONObject datas = jsonQrData.getJSONObject("data");
//                    System.out.println("datas:" + datas);
                    farmType = datas.getString("farmtype");
//                    System.out.println("farmtype:" + farmType);
                    if (farmType.equals("2")) {
                        String WindPowers = datas.getString("WindPower");
                        JSONArray WindPowersArray = new JSONArray(WindPowers);
                        for (int i = 0; i < WindPowersArray.length(); i++) {
                            JSONObject WindPower = WindPowersArray.getJSONObject(i);
                            mExecutor.execute(() -> {
                                try {
                                    fimDBOpenHelper.addNewWindPower(
                                            WindPower.getString("foca_time"),
                                            WindPower.getDouble("original_num"),
                                            WindPower.getDouble("report_num"),
                                            WindPower.getDouble("super_num"),
                                            WindPower.getDouble("reality_num"),
                                            WindPower.getDouble("Difference_Value"),
                                            WindPower.getString("group_id"),
                                            WindPower.getString("all_super_num"),
                                            WindPower.getString("all_report_num"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }

                        String rpps_data_calstation_avgs = datas.getString(
                                "rpps_data_calstation_avg");
                        JSONArray rpps_data_calstation_avgArray =
                                new JSONArray(rpps_data_calstation_avgs);
                        for (int i = 0; i < rpps_data_calstation_avgArray.length(); i++) {
                            JSONObject rpps_data_calstation_avg =
                                    rpps_data_calstation_avgArray.getJSONObject(i);
                            mExecutor.execute(() -> {
                                try {
                                    fimDBOpenHelper.addNewRpps_data_calstation_avg(
                                            rpps_data_calstation_avg.getString("ctime"),
                                            rpps_data_calstation_avg.getDouble(
                                                    "total_irrad"),
                                            rpps_data_calstation_avg.getDouble(
                                                    "direct_irrad"),
                                            rpps_data_calstation_avg.getDouble(
                                                    "diffuse_irrad"),
                                            rpps_data_calstation_avg.getDouble("speed"),
                                            rpps_data_calstation_avg.getDouble(
                                                    "direction"),
                                            rpps_data_calstation_avg.getDouble(
                                                    "humidity"),
                                            rpps_data_calstation_avg.getInt("layer"),
                                            rpps_data_calstation_avg.getDouble(
                                                    "temperature"),
                                            rpps_data_calstation_avg.getDouble(
                                                    "pressure"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }

                        String rpps_data_report_oriStr = datas.getString(
                                "report_ori");
                        JSONArray rpps_data_report_oriArray =
                                new JSONArray(rpps_data_report_oriStr);
                        for (int i = 0; i < rpps_data_report_oriArray.length(); i++) {
                            JSONObject rpps_data_report_ori =
                                    rpps_data_report_oriArray.getJSONObject(i);
                            mExecutor.execute(() -> {
                                try {
                                    fimDBOpenHelper.addNewRpps_data_report_ori(
                                            rpps_data_report_ori.getString("dtime"),
                                            rpps_data_report_ori.getInt("retype"),
                                            rpps_data_report_ori.getString("ftype"),
                                            rpps_data_report_ori.getString("memo"),
                                            rpps_data_report_ori.getString("ttype"),
                                            rpps_data_report_ori.getInt("ctype"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } else if (farmType.equals("1")) {//光
                        String LightPowerStr = datas.getString("LightPower");
                        JSONArray LightPowerArr = new JSONArray(LightPowerStr);
//                        System.out.println("LightPowerArr:"+LightPowerArr);

                        for (int i = 0; i < LightPowerArr.length(); i++) {
                            JSONObject LightPower = LightPowerArr.getJSONObject(i);
                            mExecutor.execute(() -> {
                                try {
                                    fimDBOpenHelper.addNewLightPower(
                                            LightPower.getString("PRE_DATE"),
                                            LightPower.getString("PRE_TIME"),
                                            LightPower.getDouble("PRE_POWER"),
                                            LightPower.getDouble("CORRECT_POWER"),
                                            LightPower.getDouble("SUPPER_PRE_POWER"),
                                            LightPower.getDouble(
                                                    "SUPPER_CORRECT_POWER"),
                                            LightPower.getDouble("POWER_VALUE"),
                                            LightPower.getDouble("POWER_WATTLESS"),
                                            LightPower.getDouble("Difference_Value"),
                                            LightPower.getString("GROUP_ID"),
                                            LightPower.getString("FILE_NAME"),
                                            LightPower.getString("SUBMIT_STATE"),
                                            LightPower.getString("STATE_TYPE"),
                                            LightPower.getString("ERROR_REASON"),
                                            LightPower.getString("JFG_ERROR")
                                    );
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }

                        String SPPS_FZYI_MON_HIS_Str = datas.getString(
                                "SPPS_FZYI_MON_HIS");
                        JSONArray SPPS_FZYI_MON_HIS_Arr =
                                new JSONArray(SPPS_FZYI_MON_HIS_Str);

                        for (int i = 0; i < SPPS_FZYI_MON_HIS_Arr.length(); i++) {
                            JSONObject SPPS_FZYI_MON_HIS =
                                    SPPS_FZYI_MON_HIS_Arr.getJSONObject(i);
                            mExecutor.execute(() -> {
                                try {
                                    fimDBOpenHelper.addNewSPPS_FZYI_MON_HIS(
                                            SPPS_FZYI_MON_HIS.getString(
                                                    "COLLECTION_DATE"),
                                            SPPS_FZYI_MON_HIS.getString(
                                                    "COLLECTION_TIME"),
                                            SPPS_FZYI_MON_HIS.getInt("STOREY_H"),
                                            SPPS_FZYI_MON_HIS.getDouble("TOTAL_IRRAD"),
                                            SPPS_FZYI_MON_HIS.getDouble("DIRECT_IRRAD"),
                                            SPPS_FZYI_MON_HIS.getDouble("ARI_TEM"),
                                            SPPS_FZYI_MON_HIS.getDouble(
                                                    "WIND_DIRECTION"),
                                            SPPS_FZYI_MON_HIS.getDouble("WIND_SPEED"),
                                            SPPS_FZYI_MON_HIS.getDouble("HUMIDITY"),
                                            SPPS_FZYI_MON_HIS.getDouble(
                                                    "DIFFUSE_IRRAD"),
                                            SPPS_FZYI_MON_HIS.getString("GROUP_ID"),
                                            SPPS_FZYI_MON_HIS.getDouble("PRESSURE")
                                    );
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                    //taskData  内网的性能数据  可改为安卓的性能数据
                    if (datas.has("taskData")) {
                        String FIMTaskRunRecords = datas.getString(
                                "taskData");
                        JSONArray FIMTaskRunRecordsArray =
                                new JSONArray(FIMTaskRunRecords);
//                        System.out.println("FIMTaskRunRecordsArray:"
//                        +FIMTaskRunRecordsArray);
                        for (int i = 0; i < FIMTaskRunRecordsArray.length(); i++) {
                            JSONObject FIMTaskRunRecord =
                                    FIMTaskRunRecordsArray.getJSONObject(i);
                            mExecutor.execute(() -> {
                                try {
                                    fimDBOpenHelper.addNewFIMTaskRunRecord(
                                            FIMTaskRunRecord.getInt("taskRunRecordID"),
                                            FIMTaskRunRecord.getString("express"),
                                            FIMTaskRunRecord.getString("checkTime"),
                                            FIMTaskRunRecord.getInt("result"),
                                            FIMTaskRunRecord.getString("memo"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                        String performanceDatas = datas.getString(
                                "performanceData");
                        JSONArray performanceDatasArray =
                                new JSONArray(performanceDatas);
//                        System.out.println("performanceDatasArray:"
//                        +performanceDatasArray);
                        for (int i = 0; i < performanceDatasArray.length(); i++) {
                            JSONObject performanceData =
                                    performanceDatasArray.getJSONObject(i);
                            mExecutor.execute(() -> {
                                try {
                                    fimDBOpenHelper.addNewPerformanceRecord(
                                            performanceData.getInt(
                                                    "performanceRecordID"),
                                            performanceData.getInt("performanceTypeID"),
                                            performanceData.getString("checkTime"),
                                            performanceData.getDouble("result"),
                                            performanceData.getString("memo"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                        String FIMProcessCheckRecords = datas.getString(
                                "processData");
                        JSONArray FIMProcessCheckRecordsArray =
                                new JSONArray(FIMProcessCheckRecords);
                        //只有一条
                        for (int i = 0; i < FIMProcessCheckRecordsArray.length(); i++) {
                            JSONObject FIMProcessCheckRecord =
                                    FIMProcessCheckRecordsArray.getJSONObject(i);
                            mExecutor.execute(() -> {
                                try {
                                    fimDBOpenHelper.addNewFIMProcessCheckRecord(
                                            FIMProcessCheckRecord.getInt(
                                                    "processCheckRecordID"),
                                            FIMProcessCheckRecord.getString("express"),
                                            FIMProcessCheckRecord.getString(
                                                    "checkTime"),
                                            FIMProcessCheckRecord.getInt("result"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //数据扫描入库完成
                //判断
                SharedPreferences uuidRequest = getSharedPreferences(
                        "UUID_REQUEST", Context.MODE_PRIVATE);
                String savedUUID = uuidRequest.getString("UUID", "");
                if (!qrUUID.equals(savedUUID)) {
                    SharedPreferences.Editor editor = uuidRequest.edit();
                    editor.putString("UUID", qrUUID);
                    editor.apply();
//                    System.out.println("======发送请求=========");
                    fimDBOpenHelper.setFarmType(farmType);//将获取的farmType给FimDB
                    mExecutor.execute(() -> {
                        try {
                            fimDBOpenHelper.uploadData();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });

                } else {
                    // 不发送请求，因为已经发送过相同UUID的请求
//                    System.out.println("======重复请求=========");
                }
                qrDataList.clear();
            }
            if (scanCount <= MAX_SCAN_COUNT) {
                goScan();
            } else {
                Log.i(TAG, "RESTARTED！");
                intent.putExtra("RESTARTED", false);
                qrDataList.clear();
                restartApp();
            }
        }
    }

    private boolean isRestarted() {
        return getIntent().getBooleanExtra("RESTARTED", false);
    }

    private void restartApp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // 设置重新启动标志为 true
        intent.putExtra("RESTARTED", true);
        startActivity(intent);
        finish();
        System.exit(0);
    }

    @Override
    protected void onNewIntent(Intent intent) {//重启后调用
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getBooleanExtra("RESTARTED", false)) {
            intent.putExtra("RESTARTED", false);
        }
    }

    @Override
    protected void onDestroy() {
//        System.out.println("MainActivityOnDestroy");
        super.onDestroy();
        logDbOpenHelper.close();
        fimDBOpenHelper.close();
        integrator = null;
        logBean = null;
        qrData = null;
        scanButton = null;
//        TimeoutHandlerUtil.cancelTimer();
        mExecutor.shutdown();
        System.gc();
    }

    public double getPSSUsage() {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        Debug.MemoryInfo[] memInfo =
                activityManager.getProcessMemoryInfo(new int[]{android.os.Process.myPid()});
        double totalPss = memInfo[0].getTotalPss();
        double pss = totalPss / 1024;
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String formattedPssUsage = decimalFormat.format(pss);
        return Double.parseDouble(formattedPssUsage);
    }

    @Override
    public void onBackPressed() {//手机返回主界面触发
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
