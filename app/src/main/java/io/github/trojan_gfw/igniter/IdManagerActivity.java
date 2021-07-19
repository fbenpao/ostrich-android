package io.github.trojan_gfw.igniter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.github.trojan_gfw.igniter.netmanager.ApiUrl;
import io.github.trojan_gfw.igniter.netmanager.HttpUtil;
import io.github.trojan_gfw.igniter.tool.NoDoubleClickListener;
import io.github.trojan_gfw.igniter.tool.ToastUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class IdManagerActivity extends AppCompatActivity {
    Button getId;
    ImageButton copyID;
    EditText inputAdmin,inputUser;
    TextView displayID,tishi;
    private Handler mhandler;
    private LinearLayout layoutCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_manager);
        getId = findViewById(R.id.btn_getID);
        inputAdmin = findViewById(R.id.input_admin);
        inputUser = findViewById(R.id.input_user);
        copyID = findViewById(R.id.copy_id);
        displayID = findViewById(R.id.display_user_id);
        layoutCreate = findViewById(R.id.watchID);
        tishi = findViewById(R.id.tishi);
        inputUser.addTextChangedListener(textWatcher);
        mhandler = new  Handler(){
            // 通过复写handlerMessage()
            @Override
            public void handleMessage(Message msg) {
                        // 需执行UI操作
                if(msg.what==1){
                    Log.e("IdManagerActivity","msg"+msg.obj.toString());
                    Toast.makeText(getApplicationContext(),"申请ID成功,点击进行复制",Toast.LENGTH_SHORT).show();
                    layoutCreate.setVisibility(View.VISIBLE);
                    displayID.setText(msg.obj.toString());
                }else if(msg.what==2){
                    getId.setText("获取ID");
                    getId.setEnabled(true);
                }else if(msg.what==3){
                    getId.setText("获取ID");
                    getId.setEnabled(true);
                }
            }
        };

        getId.setOnClickListener(new NoDoubleClickListener(){
            @Override
            public void onNoDoubleClick(View v) {

                String adminId = inputAdmin.getText().toString();
                String userId = inputUser.getText().toString();
                if (inputAdmin.getText().toString().equals("")||inputUser.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"输入不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(userId.length()<7){
                    Toast.makeText(getApplicationContext(),"用户ID长度不能小于7位",Toast.LENGTH_SHORT).show();
                    return;
                }
                getId.setText("正在获取");
                getId.setEnabled(false);
                if (!inputAdmin.getText().toString().equals("")&&!inputUser.getText().toString().equals("")){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences sharedPreferences = getSharedPreferences("serverIP", Context.MODE_PRIVATE); //私有数据
                            String ip = sharedPreferences.getString("ip","");
                            if(ip.equals("")){
                                Looper.prepare();
                                Toast.makeText(getApplicationContext(),"ip为空,请先设置配置服务器ip!",Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                return;
                            }
                            String url = ip+"/ostrich/admin/mobile/user/create";
                            HttpUtil.createIDOkHttpRequest(url, adminId, userId, 0, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Message msg = Message.obtain(); // 实例化消息对象
                                    msg.what = 3; // 消息标识
                                    msg.obj = "获取ID"; // 消息内容存放
                                    // 步骤3：在工作线程中 通过Handler发送消息到消息队列中
                                    mhandler.sendMessage(msg);
                                    Looper.prepare();
                                    Toast.makeText(getApplicationContext(),"网络连接出现错误",Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    Message msg = Message.obtain(); // 实例化消息对象
                                    msg.what = 2; // 消息标识
                                    msg.obj = "获取ID"; // 消息内容存放
                                    // 步骤3：在工作线程中 通过Handler发送消息到消息队列中
                                    mhandler.sendMessage(msg);
                                   parsJson(response.body().string());

                                }
                            });
                        }
                    }).start();

                }else {
                    getId.setText("获取ID");
                    getId.setEnabled(true);
                    ToastUtil.showToast(getApplicationContext(),0,"输入的值不能为空");
//                    Toast.makeText(getApplicationContext(),"输入的值不能为空",Toast.LENGTH_SHORT).show();
                }

            }
        });

        copyID.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(displayID.getText());
                ToastUtil.showToast(getApplicationContext(),0,"复制成功");

            }
        });

    }


    private TextWatcher textWatcher = new TextWatcher() {
        @Override

        public void onTextChanged(CharSequence s, int start, int before,int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(inputUser.getText().toString().trim().length()<7){
                tishi.setText("请输入7到64位的字符,数字");
            }else{
                tishi.setText("");
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,int after) {

        }

    };

    private void parsJson(String data){
        try{
            JSONObject jsonObject = new JSONObject(data);
            int code = jsonObject.optInt("code");
            Log.e("IdManagerActivity",Integer.toString(code));
            switch (code){
                case 200:
                    //得到token
                    String ret = jsonObject.optString("ret");
                    JSONObject tokenObject = new JSONObject(ret);
                    String token = tokenObject.optString("token");
                    Log.e("IdManagerActivity","token---"+token);
                    Message msg = Message.obtain(); // 实例化消息对象
                    msg.what = 1; // 消息标识
                    msg.obj = token; // 消息内容存放
                    // 步骤3：在工作线程中 通过Handler发送消息到消息队列中
                    mhandler.sendMessage(msg);
                    break;
                case 400:
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"请求参数有误",Toast.LENGTH_SHORT).show();
//                    ToastUtil.showToast(getApplicationContext(),0,"请求参数有误");
                    Looper.loop();
                    break;
                case 401:
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"此为无效ID，请检查后重新输入",Toast.LENGTH_SHORT).show();
//                    ToastUtil.showToast(getApplicationContext(),0,"此为无效ID，请检查后重新输入");
                    Looper.loop();
                    break;
                case 403:
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"此ID已被禁用",Toast.LENGTH_SHORT).show();
//                    ToastUtil.showToast(getApplicationContext(),0,"此ID已被禁用");
                    Looper.loop();
                    break;
                case 404:
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"无权访问接口",Toast.LENGTH_SHORT).show();
//                    ToastUtil.showToast(getApplicationContext(),0,"无权访问接口");
                    Looper.loop();
                    break;
                case 406:
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"非法id,请重新输入",Toast.LENGTH_SHORT).show();
//                    ToastUtil.showToast(getApplicationContext(),0,"非法id,请重新输入");
                    Looper.loop();
                    break;
                case 500:
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"服务器内部错误",Toast.LENGTH_SHORT).show();
//                    ToastUtil.showToast(getApplicationContext(),0,"服务器内部错误");
                    Looper.loop();
                    break;
                default:
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"请求服务器网络发生错误",Toast.LENGTH_SHORT).show();
//                    ToastUtil.showToast(getApplicationContext(),0,"请求服务器网络发生错误");
                    Looper.loop();
                    break;

            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}