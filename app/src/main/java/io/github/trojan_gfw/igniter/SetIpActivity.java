package io.github.trojan_gfw.igniter;

/***************
 ***** Created by fan on 2020/8/27.
 ***************/
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetIpActivity extends AppCompatActivity {
    Button setIp;
    TextView nowIP;
    EditText inputIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ip);
        setIp = findViewById(R.id.btn_setIP);
        nowIP = findViewById(R.id.nowIP);
        inputIp = findViewById(R.id.input_ip);
        judgeIP();
        setIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getIP = inputIp.getText().toString();
                if(getIP.equals("")){
                    Toast.makeText(getApplicationContext(),"您输入的ip为空！",Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    if(isBoolIp(getIP)){
                        SharedPreferences sharedPreferences = getSharedPreferences("serverIP", Context.MODE_PRIVATE); //私有数据
                        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
                        editor.putString("ip", getIP);
                        editor.commit();//提交修改
                        Toast.makeText(getApplicationContext(),"ip设置成功",Toast.LENGTH_SHORT).show();
                        nowIP.setText(getIP);

                    }else {
                        Toast.makeText(getApplicationContext(),"您输入的ip格式有误！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

            }
        });


    }

    private void judgeIP(){
        SharedPreferences sharedPreferences = getSharedPreferences("serverIP", Context.MODE_PRIVATE); //私有数据
        String ip = sharedPreferences.getString("ip","");
        if(ip.equals("")){
            nowIP.setText("nil");
        }else {
            nowIP.setText(ip);
        }
    }

    public boolean isBoolIp(String ipAddress) {
//        String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        String ip = "(ht|f)tp(s?)\\:\\/\\/[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:(0-9)*)*(\\/?)([a-zA-Z0-9\\-\\.\\?\\,\\'\\/\\\\\\+&amp;%\\$#_]*)?";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

}