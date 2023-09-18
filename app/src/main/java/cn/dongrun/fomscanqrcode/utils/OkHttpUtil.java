package cn.dongrun.fomscanqrcode.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.dongrun.fomscanqrcode.FimDBOpenHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @since 2023-8-15
 * @author Zephyrus,596991713@qq.com
 */
public class OkHttpUtil {
    private static final String TAG = "OkHttpUtil";
    private  static Context context;
    private static FimDBOpenHelper fimDBOpenHelper;

    public static void sendAsyncRequest(String url, String jsonData) {
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, jsonData);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i(TAG, "okHttpPost enqueue: \n onFailure:" + call.request() + "\n" +
                        " body:" + call.request().body().contentType()
                        + "\n IOException:" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                Log.i(TAG, "okHttpPost enqueue: \n onResponse:"+ response +"\n
//                body:" +response.body().string());
                System.out.println("响应的数据：" + response.body().string());
            }
        });
    }

    public static void sendSyncRequest(String url, String jsonData) {
        JSONObject data;
        fimDBOpenHelper = new FimDBOpenHelper(context);
        try {
            // 创建信任所有证书的TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }
            };
            // 创建SSL上下文
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            HostnameVerifier hostnameVerifier = (hostname, session) -> true;
            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(hostnameVerifier)
                    .build();

            // 构建请求体
            MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
            RequestBody requestBody = RequestBody.create(mediaType, jsonData);
            // 创建请求
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            // 发送请求并获取响应
            try (Response response = client.newCall(request).execute()) {
                // 处理响应
                if (response.isSuccessful()) {
                    System.out.println("请求码: " + response.code());
                    String responseBody = response.body().string();
                    // 处理响应数据
                    JSONObject jsonResult = new JSONObject(responseBody);
                    System.out.println("响应数据：" + jsonResult);
                    String resultValue = jsonResult.optString("result");
                    if (resultValue.equals("setting")) {
//                            clearSetting();
                        fimDBOpenHelper.saveSettingPSMServers(jsonResult.optJSONArray("PSMServers"
                        ));
//                            saveSettingTasks(jsonResult.optJSONArray("tasks"));
//                            saveSettingProcesses(jsonResult.optJSONArray
//                            ("processes"));
                        fimDBOpenHelper.saveHostSetting("settingTime",
                                jsonResult.optString(
                                "settingTime"));
                    }
                    if (resultValue.equals("OK")) {
                        String hostId = jsonResult.optString("host_id", "");
                        System.out.println("hostID:"+hostId);
                        if (!hostId.equals("")) {
//                            continue;
                        }else {
                            int psmMaster = jsonResult.optInt("psm_master");
                            if (psmMaster == 1) {
                                data = jsonResult;
                                System.out.println("数据data:" + data);
                                fimDBOpenHelper.saveData(data);
                            }
                        }

                    }
                } else {
                    System.out.println("Request Failed");
                }

            }
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException |
                 JSONException e) {
            e.printStackTrace();
        }

        /*OkHttpClient okHttpClient = new OkHttpClient(); //最初的请求
        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, jsonData);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
//            Log.i(TAG, response.body() != null ? response.body().string() : "null");
            System.out.println("response" + response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

}
