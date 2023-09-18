package cn.dongrun.fomscanqrcode.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
/**
 * @since 2023-8-15
 * @author Zephyrus,596991713@qq.com
 */
public class Utils {
    public static String getCurrentDate() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(now);
    }

    public static String getCurrentTime() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(now);
    }

    public static void cleanCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getCacheDir());
            deleteFilesByDirectory(context.getCodeCacheDir());
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }

    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : Objects.requireNonNull(directory.listFiles())) {
                item.delete();
            }
        }
    }

    public  static double getJVMUsage() {
        long totalRuntimeMem = Runtime.getRuntime().totalMemory();
        long freeRuntimeMem = Runtime.getRuntime().freeMemory();
        long useRuntimeMem = totalRuntimeMem - freeRuntimeMem;
        double jvm = useRuntimeMem / 1024.0 / 1024;
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String formattedJVMUsage = decimalFormat.format(jvm);
        return Double.parseDouble(formattedJVMUsage);
    }

    public static double getRSSyUsage() {
        String memFilePath = "/proc/" + android.os.Process.myPid() + "/status";
        BufferedReader bufferedReader = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(memFilePath);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d("TAG", " read line : " + line);
                if (!TextUtils.isEmpty(line) && line.contains("VmRSS")) {
                    String rss = line.split(":")[1].trim().split(" ")[0];
                    double v = Double.parseDouble(rss) / 1024.0;
                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
                    String formattedRssUsage = decimalFormat.format(v);
                    return Double.parseDouble(formattedRssUsage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }
}
