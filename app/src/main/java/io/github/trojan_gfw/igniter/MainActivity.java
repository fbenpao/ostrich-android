package io.github.trojan_gfw.igniter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import io.github.trojan_gfw.igniter.bean.ServerIp;
import io.github.trojan_gfw.igniter.common.constants.Constants;
import io.github.trojan_gfw.igniter.common.os.Task;
import io.github.trojan_gfw.igniter.common.os.Threads;
import io.github.trojan_gfw.igniter.common.utils.PreferenceUtils;
import io.github.trojan_gfw.igniter.common.utils.SnackbarUtils;
import io.github.trojan_gfw.igniter.connection.TrojanConnection;
import io.github.trojan_gfw.igniter.exempt.activity.ExemptAppActivity;
import io.github.trojan_gfw.igniter.netmanager.ApiUrl;
import io.github.trojan_gfw.igniter.netmanager.HttpUtil;
import io.github.trojan_gfw.igniter.proxy.aidl.ITrojanService;
import io.github.trojan_gfw.igniter.servers.activity.ServerListActivity;
import io.github.trojan_gfw.igniter.servers.data.ServerListDataManager;
import io.github.trojan_gfw.igniter.servers.data.ServerListDataSource;
import io.github.trojan_gfw.igniter.tile.ProxyHelper;
import io.github.trojan_gfw.igniter.tool.NoDoubleClickListener;
import io.github.trojan_gfw.igniter.tool.NoFastDoubleClickListener;
import io.github.trojan_gfw.igniter.tool.ToastUtil;
import io.github.trojan_gfw.igniter.updateapk.UpdateManage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements TrojanConnection.Callback {
    
    private static final String TAG = "MainActivity";
    private static final int READ_WRITE_EXT_STORAGE_PERMISSION_REQUEST = 514;
    private static final int VPN_REQUEST_CODE = 233;
    private static final int SERVER_LIST_CHOOSE_REQUEST_CODE = 1024;
    private static final int EXEMPT_APP_CONFIGURE_REQUEST_CODE = 2077;
    private static final String CONNECTION_TEST_URL = "https://www.google.com";

    private String shareLink;
    private ViewGroup rootViewGroup;
    private Switch verifySwitch;
    private Switch clashSwitch;
    private TextView clashLink;
    private Button startStopButton;
    private Button btnAbout;
    private EditText trojanURLText;
    private EditText id;
    private CheckBox idCheckBox;
    DrawerLayout mainDrawerLayout;
    RelativeLayout leftdrawOut;
    private ActionBarDrawerToggle drawerbar;
    private @ProxyService.ProxyState
    int proxyState = ProxyService.STATE_NONE;
    private final TrojanConnection connection = new TrojanConnection(false);
    private ITrojanService trojanService;
    private ServerListDataSource serverListDataManager;
    private AlertDialog linkDialog;
    public ArrayList<ServerIp> serverIps = new ArrayList<>();
    private String getVersion = "";
    private Handler mhandler;


    private void copyRawResourceToDir(int resId, String destPathName, boolean override) {
        File file = new File(destPathName);
        if (override || !file.exists()) {
            try {
                try (InputStream is = getResources().openRawResource(resId);
                     FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateViews(int state) {
        proxyState = state;
        boolean inputEnabled;
        switch (state) {
            case ProxyService.STARTING: {
                inputEnabled = false;
                startStopButton.setText(R.string.button_service__starting);
                startStopButton.setBackgroundColor(Color.parseColor("#FF96E1DA"));
                startStopButton.setEnabled(false);
                break;
            }
            case ProxyService.STARTED: {
                inputEnabled = false;
                startStopButton.setText(R.string.button_service__stop);
                startStopButton.setBackgroundColor(Color.parseColor("#E3544A"));
                startStopButton.setEnabled(true);
                break;
            }
            case ProxyService.STOPPING: {
                inputEnabled = false;
                startStopButton.setText(R.string.button_service__stopping);
                startStopButton.setBackgroundColor(Color.parseColor("#E3544A"));
                startStopButton.setEnabled(false);
                break;
            }
            default: {
                inputEnabled = true;
                startStopButton.setText(R.string.button_service__start);
                startStopButton.setBackgroundColor(Color.parseColor("#FF96E1DA"));
                startStopButton.setEnabled(true);
                break;
            }
        }
        clashSwitch.setEnabled(inputEnabled);
        clashLink.setEnabled(inputEnabled);
        id.setEnabled(inputEnabled);
        idCheckBox.setEnabled(inputEnabled);
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21) {
//            View decorView = getWindow().getDecorView();
            //??????????????????????????????
//            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(getResources().getColor(R.color.statusBar));
        }
        setContentView(R.layout.activity_main);
        //ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setNavigationIcon(R.drawable.more);
        //??????ActionBar??????,??????ActionBar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar !=  null) {
            //????????????
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.hide();
            //????????????????????????????????????????????????
        }
        rootViewGroup = findViewById(R.id.rootScrollView);
        verifySwitch = findViewById(R.id.verifySwitch);
        clashSwitch = findViewById(R.id.clashSwitch);
        clashLink = findViewById(R.id.clashLink);
        clashLink.setMovementMethod(LinkMovementMethod.getInstance());
        startStopButton = findViewById(R.id.startStopButton);
        id = findViewById(R.id.user_id);
        idCheckBox = findViewById(R.id.checkBox);
        mainDrawerLayout= findViewById(R.id.main_drawer_layout);
        leftdrawOut = findViewById(R.id.drawer_out);
        btnAbout = findViewById(R.id.btn_two);
        copyRawResourceToDir(R.raw.cacert, Globals.getCaCertPath(), true);
        copyRawResourceToDir(R.raw.country, Globals.getCountryMmdbPath(), true);
        copyRawResourceToDir(R.raw.clash_config, Globals.getClashConfigPath(), false);

        boolean enableClash = PreferenceUtils.getBooleanPreference(getContentResolver(),
                Uri.parse(Constants.PREFERENCE_URI), Constants.PREFERENCE_KEY_ENABLE_CLASH, true);
        clashSwitch.setChecked(enableClash);
        clashSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Generally speaking, it's better to insert content into ContentProvider in background
                // thread, but that may cause data inconsistency when user starts proxy right after
                // switching.
                PreferenceUtils.putBooleanPreference(getContentResolver(),
                        Uri.parse(Constants.PREFERENCE_URI), Constants.PREFERENCE_KEY_ENABLE_CLASH,
                        isChecked);
            }
        });
        mhandler = new  Handler(){
            // ????????????handlerMessage()
            @Override
            public void handleMessage(Message msg) {
                // ?????????UI??????
                if(msg.what==100){
                    startStopButton.setEnabled(true);
                    startStopButton.setText(R.string.button_service__start);

                }else if(msg.what==200){
                    startStopButton.setEnabled(true);
                    startStopButton.setText(R.string.button_service__start);
                }
            }
        };
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AboutActivity.class);
                startActivity(intent);
            }
        });

        startStopButton.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if(id.getText().toString().trim().length()==0){
                    Toast.makeText(getApplicationContext(),"?????????ID????????????",Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences sharedPreferences = getSharedPreferences("serverIP", Context.MODE_PRIVATE); //????????????
                String ip = sharedPreferences.getString("ip","");
                if(ip.equals("")){
                    Toast.makeText(getApplicationContext(),"ip??????,????????????ip!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(startStopButton.getText().toString().trim().equals("??????")||startStopButton.getText().toString().trim().equals("Start")){
//                    startStopButton.setText(R.string.button_service__starting);
                    startStopButton.setEnabled(false);
                }
                listenUserID();
                //???????????????,????????????
                if (proxyState == ProxyService.STARTED) {
                    // stop ProxyService
                    ProxyHelper.stopProxyService(getApplicationContext());
                    return;
                }
                //?????????????????????????????????????????????
                getServerData();
            }
        });

//        startStopButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
////                if (!Globals.getTrojanConfigInstance().isValidRunningConfig()) {
////                    Toast.makeText(MainActivity.this,
////                            R.string.invalid_configuration,
////                            Toast.LENGTH_LONG).show();
////                    return;
////                }
//                if(id.getText().toString().trim().length()==0){
//                    Toast.makeText(getApplicationContext(),"?????????ID????????????",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                listenUserID();
//                //???????????????,????????????
//                if (proxyState == ProxyService.STARTED) {
//                    // stop ProxyService
//                    ProxyHelper.stopProxyService(getApplicationContext());
//                    return;
//                }
//                //?????????????????????????????????????????????
//                getServerData();
//            }
//        });

        toolbar.setOnMenuItemClickListener(new NoFastDoubleClickListener(){
            @Override
            public void noFastDoubleClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_test_connection:
                        testConnection();
                        break;
                    case R.id.action_show_develop_info_logcat:
                        // log of this process
                        LogHelper.showDevelopInfoInLogcat();
                        // log of other processes
                        showDevelopInfoInLogcat();
                        break;
                    case R.id.action_save_profile:
                        if (!Globals.getTrojanConfigInstance().isValidRunningConfig()) {
                            Toast.makeText(MainActivity.this, R.string.invalid_configuration, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Threads.instance().runOnWorkThread(new Task() {
                            @Override
                            public void onRun() {
                                TrojanConfig config = Globals.getTrojanConfigInstance();
                                TrojanHelper.WriteTrojanConfig(config, Globals.getTrojanConfigPath());
                                serverListDataManager.saveServerConfig(config);
                                showSaveConfigResult(true);
                            }
                        });
                        break;
                    case R.id.action_view_server_list:
                        clearEditTextFocus();
                        startActivityForResult(ServerListActivity.create(MainActivity.this), SERVER_LIST_CHOOSE_REQUEST_CODE);
                        break;
                    case R.id.action_about:
                        clearEditTextFocus();
                        startActivity(AboutActivity.create(MainActivity.this));
                        break;
                    case R.id.action_share_link:
                        trojanURLText.setText(shareLink);
                        linkDialog.show();
                        trojanURLText.selectAll();
                        break;
                    case R.id.action_exempt_app:
                        startActivityForResult(ExemptAppActivity.create(MainActivity.this), EXEMPT_APP_CONFIGURE_REQUEST_CODE);
                        break;
                    case R.id.action_getID:
                        Intent intent = new Intent(MainActivity.this,IdManagerActivity.class);
                        startActivity(intent);
                        //??????ID
                        break;
                    case R.id.action_setIP:
                        Intent intent2 = new Intent(MainActivity.this,SetIpActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.action_checkUpdate:
                        checkUpdate();
                        break;
                    default:
                        break;
                }
            }
        });

        //toolbar?????????????????????drawerlayout?????????
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mainDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mainDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        serverListDataManager = new ServerListDataManager(Globals.getTrojanConfigListPath());
        connection.connect(this, this);
        Threads.instance().runOnWorkThread(new Task() {
            @Override
            public void onRun() {
                PreferenceUtils.putBooleanPreference(getContentResolver(),
                        Uri.parse(Constants.PREFERENCE_URI),
                        Constants.PREFERENCE_KEY_FIRST_START, false);
            }
        });

    }



    private void listenUserID(){
        if(idCheckBox.isChecked()){
            Log.e("MainActivity","isCheck???true");
            if(id.getText().toString().trim().length()!=0){
                Log.e("MainActivity","??????id?????????");
                //??????1???????????????SharedPreferences??????
                SharedPreferences sharedPreferences= getSharedPreferences("data", Context.MODE_PRIVATE);
                //??????2??? ?????????SharedPreferences.Editor??????
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //??????3????????????????????????????????????
                editor.putString("userID",id.getText().toString());
                editor.apply();
                Log.e("MainActivity",id.getText().toString());

            }else{
                Toast.makeText(getApplicationContext(),"?????????ID????????????",Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.e("MainActivity","isCheck???false");
            //??????1???????????????SharedPreferences??????
            SharedPreferences sharedPreferences= getSharedPreferences("data", Context.MODE_PRIVATE);
            //??????2??? ?????????SharedPreferences.Editor??????
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("userID");
            editor.commit();

        }
    }


    private void getServerData(){
        //???????????????????????????url??????
        SharedPreferences sharedPreferences = getSharedPreferences("serverIP", Context.MODE_PRIVATE); //????????????
        String ip = sharedPreferences.getString("ip","");
        String url = ip+"/ostrich/api/mobile/server/list";//??????url
        String userID=id.getText().toString();//?????????i?????????
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.sendPostOkHttpRequest(url,userID, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Message msg = Message.obtain(); // ?????????????????????
                        msg.what = 100; // ????????????
                        // ??????3????????????????????? ??????Handler??????????????????????????????
                        mhandler.sendMessage(msg);
                        Log.e("MainActivity","onFailure-----"+e.getMessage());
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        Looper.loop();

                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
//                        Log.e("MainActivity","----"+response.body().string());
                        Message msg = Message.obtain(); // ?????????????????????
                        msg.what = 200; // ????????????
                        // ??????3????????????????????? ??????Handler??????????????????????????????
                        mhandler.sendMessage(msg);
                        parsJson(response.body().string());

                    }
                });
            }
        }).start();
    }

    private void parsJson(String data){
        try{
            JSONObject jsonObject = new JSONObject(data);
            int code = jsonObject.optInt("code");
            Log.e("MainActivity",Integer.toString(code));
            switch (code){
                case 200:
                    String ret = jsonObject.optString("ret");
                    JSONObject serverObject = new JSONObject(ret);
                    String server = serverObject.optString("server");
                    JSONArray jsonArray = new JSONArray(server);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject url = jsonArray.getJSONObject(i);
                        String ip = url.optString("ip");
                        String port = url.optString("port");
                        serverIps.add(new ServerIp(ip,port));
                        Log.e("MainActivity",ip+"  ___  "+port);
                    }
                    setConfig();
                    break;
                case 400:
                    Looper.prepare();
//                    ToastUtil.showToast(getApplicationContext(),0,"??????????????????");
                    Toast.makeText(getApplicationContext(),"??????????????????",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                case 401:
                    Looper.prepare();
//                    ToastUtil.showToast(getApplicationContext(),0,"????????????ID???????????????????????????");
                    Toast.makeText(getApplicationContext(),"????????????ID???????????????????????????",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                case 403:
                    Looper.prepare();
//                    ToastUtil.showToast(getApplicationContext(),0,"???ID????????????");
                    Toast.makeText(getApplicationContext(),"???ID????????????",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                case 404:
                    Looper.prepare();
//                    ToastUtil.showToast(getApplicationContext(),0,"??????????????????");
                    Toast.makeText(getApplicationContext(),"??????????????????",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                case 406:
                    Looper.prepare();
//                    ToastUtil.showToast(getApplicationContext(),0,"??????id,???????????????");
                    Toast.makeText(getApplicationContext(),"??????id,???????????????",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                case 500:
                    Looper.prepare();
//                    ToastUtil.showToast(getApplicationContext(),0,"?????????????????????");
                    Toast.makeText(getApplicationContext(),"?????????????????????",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
                default:
                    Looper.prepare();
//                    ToastUtil.showToast(getApplicationContext(),0,"?????????????????????????????????");
                    Toast.makeText(getApplicationContext(),"???????????????????????????",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    break;
            }


            Log.e("MainActivity",Integer.toString(serverIps.size()));

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onServiceConnected(final ITrojanService service) {
        LogHelper.i(TAG, "onServiceConnected");
        trojanService = service;
        Threads.instance().runOnWorkThread(new Task() {
            @Override
            public void onRun() {
                try {
                    final int state = service.getState();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateViews(state);
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onServiceDisconnected() {
        LogHelper.i(TAG, "onServiceConnected");
        trojanService = null;
    }

    @Override
    public void onStateChanged(int state, String msg) {
        LogHelper.i(TAG, "onStateChanged# state: " + state + " msg: " + msg);
        updateViews(state);
    }

    @Override
    public void onTestResult(final String testUrl, final boolean connected, final long delay, @NonNull final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showTestConnectionResult(testUrl, connected, delay, error);
            }
        });
    }

    private void showTestConnectionResult(String testUrl, boolean connected, long delay, @NonNull String error) {
        if (connected) {
            ToastUtil.showToast(getApplicationContext(),0,getString(R.string.connected_to__in__ms,
                    testUrl, String.valueOf(delay)));
//            Toast.makeText(getApplicationContext(), getString(R.string.connected_to__in__ms,
//                    testUrl, String.valueOf(delay)), Toast.LENGTH_LONG).show();
        } else {
            LogHelper.e(TAG, "TestError: " + error);
            ToastUtil.showToast(getApplicationContext(),0,getString(R.string.failed_to_connect_to__,
                    testUrl, "??????????????????"));
//            Toast.makeText(getApplicationContext(),
//                    getString(R.string.failed_to_connect_to__,
//                            testUrl, "??????????????????"),
//                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBinderDied() {
        LogHelper.i(TAG, "onBinderDied");
        connection.disconnect(this);
        // connect the new binder
        // todo is it necessary to re-connect?
        connection.connect(this, this);
    }

    /**
     * Test connection by invoking {@link ITrojanService#testConnection(String)}. Since {@link ITrojanService}
     * is from remote process, a {@link RemoteException} might be thrown. Test result will be delivered
     * to {@link #onTestResult(String, boolean, long, String)} by {@link TrojanConnection}.
     */
    private void testConnection() {
        ITrojanService service = trojanService;
        if (service == null) {
            showTestConnectionResult(CONNECTION_TEST_URL, false, 0L, getString(R.string.trojan_service_not_available));
        } else {
            try {
                service.testConnection(CONNECTION_TEST_URL);
            } catch (RemoteException e) {
                showTestConnectionResult(CONNECTION_TEST_URL, false, 0L, getString(R.string.trojan_service_error));
                e.printStackTrace();
            }
        }
    }

    /**
     * Show develop info in Logcat by invoking {@link ITrojanService#showDevelopInfoInLogcat}. Since {@link ITrojanService}
     * is from remote process, a {@link RemoteException} might be thrown.
     */
    private void showDevelopInfoInLogcat() {
        ITrojanService service = trojanService;
        if (service != null) {
            try {
                service.showDevelopInfoInLogcat();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearEditTextFocus() {
    }

    private void showSaveConfigResult(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        success ? R.string.main_save_success : R.string.main_save_failed,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public  void setConfig() {
        File file = new File(Globals.getTrojanConfigPath());
        if (file.exists()) {
            //??????????????????,????????????
            Log.e("MainActivity","??????????????????");
            String remoteUrl = serverIps.get(0).getIp();
            String port = serverIps.get(0).getPort();
            try {
                String str;
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] content = new byte[(int) file.length()];
                    fis.read(content);
                    str = new String(content);
                }
                JSONObject json = new JSONObject(str);
//                json.put("remote_addr", "52.193.142.13");
                json.put("remote_addr", remoteUrl);
                try {
                    int po = Integer.parseInt(port.trim());
                    json.put("remote_port", po);
                } catch (NumberFormatException e) {
                    // Ignore when we get invalid number
                    e.printStackTrace();
                }

//                json.put("remote_port", 443);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(json.toString().getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (proxyState == ProxyService.STATE_NONE || proxyState == ProxyService.STOPPED) {
                TrojanHelper.ShowConfig(Globals.getTrojanConfigPath());
                // start ProxyService
                Intent i = VpnService.prepare(getApplicationContext());
                if (i != null) {
                    startActivityForResult(i, VPN_REQUEST_CODE);
                } else {
                    ProxyHelper.startProxyService(getApplicationContext());
                }
            } else if (proxyState == ProxyService.STARTED) {
                // stop ProxyService
                ProxyHelper.stopProxyService(getApplicationContext());
            }
            serverListDataManager = new ServerListDataManager(Globals.getTrojanConfigListPath());
            connection.connect(this, this);
            Threads.instance().runOnWorkThread(new Task() {
                @Override
                public void onRun() {
                    PreferenceUtils.putBooleanPreference(getContentResolver(),
                            Uri.parse(Constants.PREFERENCE_URI),
                            Constants.PREFERENCE_KEY_FIRST_START, false);
                }
            });
        }else {
            Log.e("MainActivity","?????????????????????");
            String remoteUrl = serverIps.get(0).getIp();
            String port = serverIps.get(0).getPort();
            //?????????????????????,???????????????
            TrojanConfig ins = Globals.getTrojanConfigInstance();
            ins.setRemoteAddr(remoteUrl);
            try {
                int po = Integer.parseInt(port.trim());
                ins.setRemotePort(po);
            } catch (NumberFormatException e) {
                // Ignore when we get invalid number
                e.printStackTrace();
            }
            ins.setPassword("251f6edc");
            ins.setVerifyCert(false);
            String config = Globals.getTrojanConfigInstance().generateTrojanConfigJSON();
            File f = new File(Globals.getTrojanConfigPath());
            try {
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(config.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            launchServer();
        }

    }

    private void launchServer(){
        if (proxyState == ProxyService.STATE_NONE || proxyState == ProxyService.STOPPED) {
            //??????????????????????????????
            TrojanHelper.WriteTrojanConfig(
                    Globals.getTrojanConfigInstance(),
                    Globals.getTrojanConfigPath()
            );
            TrojanHelper.ShowConfig(Globals.getTrojanConfigPath());
            // start ProxyService
            Intent i = VpnService.prepare(getApplicationContext());
            if (i != null) {
                startActivityForResult(i, VPN_REQUEST_CODE);
            } else {
                ProxyHelper.startProxyService(getApplicationContext());
            }
        } else if (proxyState == ProxyService.STARTED) {
            // stop ProxyService
            ProxyHelper.stopProxyService(getApplicationContext());
        }

        serverListDataManager = new ServerListDataManager(Globals.getTrojanConfigListPath());
        connection.connect(this, this);
        Threads.instance().runOnWorkThread(new Task() {
            @Override
            public void onRun() {
                PreferenceUtils.putBooleanPreference(getContentResolver(),
                        Uri.parse(Constants.PREFERENCE_URI),
                        Constants.PREFERENCE_KEY_FIRST_START, false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SERVER_LIST_CHOOSE_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK && data != null) {
            shareLink = "";
            final TrojanConfig config = data.getParcelableExtra(ServerListActivity.KEY_TROJAN_CONFIG);
            if (config != null) {
                config.setCaCertPath(Globals.getCaCertPath());
                Globals.setTrojanConfigInstance(config);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TrojanHelper.WriteTrojanConfig(Globals.getTrojanConfigInstance(), Globals.getTrojanConfigPath());
                    }
                });
                shareLink = TrojanURLHelper.GenerateTrojanURL(config);
                verifySwitch.setChecked(config.getVerifyCert());
            }
        } else if (EXEMPT_APP_CONFIGURE_REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            if (ProxyService.STARTED == proxyState) {
                SnackbarUtils.showTextLong(rootViewGroup, R.string.main_restart_proxy_service_tip);
            }
        } else if (VPN_REQUEST_CODE == requestCode && RESULT_OK == resultCode) {
            ProxyHelper.startProxyService(getApplicationContext());
        }
    }

    public void checkUpdate(){
        UpdateManage updateManage = new UpdateManage();
        PackageManager packageManager = getPackageManager();
        try {
            //2.??????????????????????????????????????????
            //packageName : ?????????????????????
            //flags ??? ????????????????????????????????????????????????????????????????????????????????????
            //PackageManager.GET_ACTIVITIES : ?????????activity???????????????
            //getPackageName() : ???????????????????????????
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            //3.?????????????????????????????????
            getVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //????????????????????????
            e.printStackTrace();
        }

        Handler mhandle = new Handler(){
            public void handleMessage(Message msg) {
                if(msg.what==100){
                    ArrayList data = msg.getData().getParcelableArrayList("data");
                    JSONObject jsonObject = (JSONObject) data.get(0);
                    try{
                        String url = jsonObject.getString("apkurl");
                        String version = jsonObject.getString("version");
                        String des = jsonObject.getString("des");
                        if(version!=getVersion){
                            dialogUpdate(url,des,version);
                        }else {
                          Toast toast = Toast.makeText(getApplicationContext(),"??????????????????,???????????????",Toast.LENGTH_SHORT);
                          toast.setGravity(Gravity.CENTER, 0, 0);
                          toast.show();
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }



                }

            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.???????????????
                String url = ApiUrl.updataUrl;
                HttpUtil.sendGetServerOkHttpRequest(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("UpdateManage",e.getMessage());
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            Message mes = new Message();
                            mes.what = 100;
                            Bundle bundle = new Bundle();
                            ArrayList arr = new ArrayList();
                            arr.add(jsonObject);
                            bundle.putStringArrayList("data",arr);
                            mes.setData(bundle);
                            mhandle.sendMessage(mes);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });
            };
        }).start();
        Log.e("????????????",getVersion);
    }

    private void dialogUpdate(String url,String des,String version){
        //url??????apk??????????????????
        //des??????apk??????????????????
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //????????????
            builder.setTitle("?????????:"+version);
            //????????????
            builder.setIcon(R.drawable.ostrich_logo);
            //??????????????????
            builder.setMessage(des);
            //??????????????????
            builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //todo

                }
            });
            //??????????????????
            builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            //???????????????
//		builder.create().show();????????????
            builder.show();


    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SharedPreferences sharedPreferences= getSharedPreferences("data", Context .MODE_PRIVATE);
        String userId =sharedPreferences.getString("userID","");
        Log.e("MainActivity","??????ID"+userId);
        id.setText(userId);
//        File file = new File(Globals.getTrojanConfigPath());
//        if (file.exists()) {
//            try {
//                try (FileInputStream fis = new FileInputStream(file)) {
//                    byte[] content = new byte[(int) file.length()];
//                    fis.read(content);
//                    String contentStr = new String(content);
//                    TrojanConfig ins = Globals.getTrojanConfigInstance();
//                    ins.fromJSON(contentStr);
//                    verifySwitch.setChecked(ins.getVerifyCert());
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.disconnect(this);
    }
}
