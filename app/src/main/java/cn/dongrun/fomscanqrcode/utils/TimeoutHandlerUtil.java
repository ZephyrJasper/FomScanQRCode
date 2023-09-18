package cn.dongrun.fomscanqrcode.utils;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class TimeoutHandlerUtil {
    private static Timer mTimer;
    private static boolean isRequestFunctionCalled = false; // 标记函数是否被调用
    private static final long CHECK_INTERVAL = 2* 60 * 60 * 1000;

    public static void startTimeoutChecking() {
        // 初始化计时器
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try{
                    checkRequestFunctionCall();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, CHECK_INTERVAL, CHECK_INTERVAL);
    }
    private static void checkRequestFunctionCall() {
        if (isRequestFunctionCalled) {
            // 函数被调用
            Log.d("FunctionCallChecker", "函数被调用");
//            System.out.println("函数被调用");
            isRequestFunctionCalled = false; // 重置标记
        } else {
            // 函数未被调用
            Log.d("FunctionCallChecker", "函数未被调用");
//            System.out.println("函数未被调用");
            String errMsg = "{\n" +
                    "\"fimMac\"" + ": " + "\"" + "re-qu--es-tt-00" + "\"" + ",\n" +
                    "\"trueData\"" + ": " + "请求发送失败" +
                    "\n}";
        }
    }

    public static void setRequestFunctionCalled() {
        isRequestFunctionCalled = true;
    }

    public static void cancelTimer(){
//        mTimer.cancel();
    }
}
