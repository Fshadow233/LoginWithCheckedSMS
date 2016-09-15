package cqu.edu.test;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class MainActivity extends Activity implements View.OnClickListener{

    Button btn_getSMS;
    Button btn_register;
    EditText et_telephone;
    EditText et_password;
    EditText et_confirmPassword;
    EditText inputCodeEt;

    String label;            //用于检测是否输入正确是手机号
    int i=30;

    String utelephone,upassword,uconfirmPassword;
    private String CodeText;         //读取短信

    //调用短信API的密钥
    String APPKEY ="1701b4317226f";
    String APPSECRETE = "*******";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    public void init(){

        btn_getSMS=(Button)findViewById(R.id.bt_getSMS);
        btn_register=(Button)findViewById(R.id.btn_register);
        et_telephone=(EditText)findViewById(R.id.et_telephone);
        et_password=(EditText)findViewById(R.id.et_password);
        et_confirmPassword=(EditText)findViewById(R.id.et_confirmPassword);
        inputCodeEt=(EditText)findViewById(R.id.et_inputSMS);


        btn_getSMS.setOnClickListener(this);
        btn_register.setOnClickListener(this);

        //启动短信验证sdk
        SMSSDK.initSDK(this, APPKEY, APPSECRETE);
        EventHandler eventHandler = new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        //注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
    }

    @Override
    public void onClick(View view) {
        utelephone=et_telephone.getText().toString();
        upassword=et_password.getText().toString();
        uconfirmPassword=et_confirmPassword.getText().toString();
        label = checkInputType.checkInputType(utelephone);

        switch (view.getId()){
            case R.id.bt_getSMS:
                // 1. 通过规则判断手机号

                if (label != "valid") {
                    Log.d("telephone",label);
                    return;
                }
                // 2. 通过sdk发送短信验证
                Log.d("telephone", "开始发送");
                SMSSDK.getVerificationCode("86", utelephone);

                Log.d("telephone", "发送成功");
                // 3. 把按钮变成不可点击，并且显示倒计时（正在获取）
                btn_getSMS.setClickable(false);
                btn_getSMS.setText("重新发送(" + i + ")");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; i > 0; i--) {
                            handler.sendEmptyMessage(-9);
                            if (i <= 0) {
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(-8);
                    }
                }).start();
                break;
            case R.id.btn_register:
                //将收到的验证码和手机号提交再次核对
                SMSSDK.submitVerificationCode("86", utelephone, inputCodeEt
                        .getText().toString());
                break;
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == -9) {
                btn_getSMS.setText("重新发送(" + i + ")");
            } else if (msg.what == -8) {
                btn_getSMS.setText("获取验证码");
                btn_getSMS.setClickable(true);
                i = 30;
            } else {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                Log.e("event", "event=" + event);
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 短信注册成功后，返回LoginActivity,然后提示
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功

                        Toast.makeText(getApplicationContext(), "提交验证码成功",
                                Toast.LENGTH_SHORT).show();

                        if ( upassword.equals(uconfirmPassword)) {
                            Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getApplicationContext(), "注册失败", Toast.LENGTH_SHORT).show();
                        }


                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        Toast.makeText(getApplicationContext(), "正在获取验证码",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        ((Throwable) data).printStackTrace();
                    }
                }
            }
        }
    };

    private class SmsObserver extends ContentObserver {
        public SmsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            // TODO Auto-generated method stub
            StringBuilder sb = new StringBuilder();
            Cursor cursor = getContentResolver().query(
                    Uri.parse("content://sms/inbox"), null, null, null, null);
            cursor.moveToNext();
            sb.append("body=" + cursor.getString(cursor.getColumnIndex("body")));

            System.out.println(sb.toString());
            Pattern pattern = Pattern.compile("[^0-9]");
            Matcher matcher = pattern.matcher(sb.toString());
            CodeText = matcher.replaceAll("");
            inputCodeEt.setText(CodeText);
            cursor.close();
            super.onChange(selfChange);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
        getContentResolver().unregisterContentObserver(new SmsObserver(handler));
    }
}
