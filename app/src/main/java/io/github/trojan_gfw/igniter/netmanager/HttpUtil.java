package io.github.trojan_gfw.igniter.netmanager;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/***************
 ***** Created by fan on 2020/6/22.
 ***************/
public class HttpUtil {
    public static void sendPostOkHttpRequest(String address,String userId, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(5 * 60 * 1000, TimeUnit.MILLISECONDS)
                .writeTimeout(5 * 60 * 1000, TimeUnit.MILLISECONDS)
                .build();
        JSONObject json = new JSONObject();
        try{
            json.put("user_id",userId);
            json.put("platform",0);
        }catch (JSONException e){
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), String.valueOf(json));
        Request request = new Request.Builder()
                .method("POST",requestBody)
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public static void sendGetServerOkHttpRequest(String address,okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(5 * 60 * 1000, TimeUnit.MILLISECONDS)
                .writeTimeout(5 * 60 * 1000, TimeUnit.MILLISECONDS)
                .build();
        Request request = new Request.Builder()
                .method("GET",null)
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void createIDOkHttpRequest(String address,String admin,String user,int role, okhttp3.Callback callback) {
        Log.e("HttpUtil",address+admin+user);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(5 * 60 * 1000, TimeUnit.MILLISECONDS)
                .writeTimeout(5 * 60 * 1000, TimeUnit.MILLISECONDS)
                .build();
        JSONObject json = new JSONObject();
        JSONObject userJson = new JSONObject();
        try{
            userJson.put("id",user);
            userJson.put("role",role);
        }catch (JSONException e){
            e.printStackTrace();
        }
        try{
            json.put("admin",admin);
            json.put("user",userJson);
        }catch (JSONException e){
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), String.valueOf(json));
        Request request = new Request.Builder()
                .method("POST",requestBody)
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }

}
