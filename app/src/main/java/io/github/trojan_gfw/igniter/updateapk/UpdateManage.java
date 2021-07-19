package io.github.trojan_gfw.igniter.updateapk;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.github.trojan_gfw.igniter.netmanager.ApiUrl;
import io.github.trojan_gfw.igniter.netmanager.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/***************
 ***** Created by fan on 2020/6/24.
 ***************/
public class UpdateManage extends FragmentActivity {
    /**
     * 获取应用程序的版本号
     *
     * @return
     */
    public  String getVersionName() {
        //1.包的管理者，获取应用程序中清单文件中信息
        PackageManager packageManager = getPackageManager();
        try {
            //2.根据包名获取应用程序相关信息
            //packageName : 应用程序的包名
            //flags ： 指定信息的标签，指定了标签就会获取相应标签对应的相关信息
            //PackageManager.GET_ACTIVITIES : 获取跟activity相关的信息
            //getPackageName() : 获取应用程序的包名
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            //3.获取应用程序版本号名称
            String versionName = packageInfo.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //找不到包名的异常
            e.printStackTrace();
            return null;
        }
    }

//    /**
//     * 更新版本
//     */
//    public Map<String, String> getUpdateInfo() {
//        //连接服务，联网，耗时操作，4.0不允许在主线程中执行耗时操作
//        Map<String,String> map = new HashMap<String,String>();
//      Thread thread = new Thread(new Runnable() {
//          @Override
//          public void run() {
//              //1.连接服务器
//              String url = ApiUrl.updataUrl;
//              HttpUtil.sendGetServerOkHttpRequest(url, new Callback() {
//                  @Override
//                  public void onFailure(Call call, IOException e) {
//                      Log.e("UpdateManage",e.getMessage());
//                  }
//                  @Override
//                  public void onResponse(Call call, Response response) throws IOException {
//                      try {
//                          JSONObject jsonObject = new JSONObject(response.body().string());
//                          String url = jsonObject.getString("apkurl");
//                          String version = jsonObject.getString("version");
//                          String des = jsonObject.getString("des");
//                          Log.e("打印apk版本",version);
//                          map.put("apkurl",url);
//                          map.put("version",version);
//                          map.put("des",des);
//                      }catch (JSONException e){
//                          e.printStackTrace();
//                      }
//                  }
//              });
//          };
//      });
//      thread.start();
////      try {
////          thread.join();
////      }catch (Exception e){
////          e.printStackTrace();
////      }
//      return map;
//    }

}
