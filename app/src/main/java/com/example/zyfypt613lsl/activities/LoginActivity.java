package com.example.zyfypt613lsl.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.bean.UserBean;
import com.example.zyfypt613lsl.common.Common;
import com.example.zyfypt613lsl.service.UserService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText etname, etpass;
    private MaterialButton btnlogin, btnRegister;
    private SwitchMaterial sw;

    private SharedPreferences sp;

    private String username, password, SessionID;
    private boolean remember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        sp = getSharedPreferences("login", MODE_PRIVATE);
        readSP();
    }
    private  void saveSP()
    {
        //用户名、密码、开关状态、SessionID
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("username",username);
        editor.putString("password",password);
        editor.putBoolean("remember",sw.isChecked());
        editor.putString("SessionID",SessionID);
        editor.apply(); // 使用apply替代commit提高性能
    }

    private  void readSP()
    {
        //判断是否记住密码
        if(sp.getBoolean("remember",false))
        {
            //读物账号
            etname.setText(sp.getString("username",""));
            //读取密码
            etpass.setText(sp.getString("password",""));
        }
        //读取开关状态
        sw.setChecked(sp.getBoolean("remember",false));


    }






    private void initViews() {
        etname=findViewById(R.id.etname);
        etpass=findViewById(R.id.etpass);
        btnlogin=findViewById(R.id.btnlogin);
        sw=findViewById(R.id.switch1);
        btnRegister=findViewById(R.id.button);



        // 添加注册按钮（假设您的布局中有id为btn_register的按钮）
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnlogin.setOnClickListener(v->{

            //5使用接口--（1）//创建Retrofit对象
            Retrofit retrofit=new Retrofit.Builder()
                    .baseUrl(Common.BASEURL)
                    //.baseUrl(Common.BASEURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            //5使用接口--（2）//定义API请求
            UserService service=retrofit.create(UserService.class);
            Call<UserBean> call=service.login(etname.getText().toString(),
                    etpass.getText().toString());

            //5使用接口--（3）//发送请求并处理结果
            call.enqueue(new Callback<UserBean>() {
                @Override
                public void onResponse(Call<UserBean> call, Response<UserBean> response) {
                    Log.d(TAG, "onResponse: successful=" + response.isSuccessful() + ", body=" + response.body());
                    //有响应的数据返回，接口和数据是正确的，包括用户名和密码错误也会在这里
                    if(response.isSuccessful()&&response.body()!=null)
                    {
                        if((response.body().getError()!=null)&&(response.body().getError().contains("不正确")))
                        {
                            Log.d(TAG, "用户名密码不正确");
                            Toast.makeText(LoginActivity.this,"用户名密码不正确",Toast.LENGTH_SHORT).show();

                        }
                        else {
                            Log.d(TAG, "登录成功");
                            Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                            username=etname.getText().toString();//获取用户名
                            password=etpass.getText().toString();//获取密码
                            SessionID=response.body().getSessionid();//获取SessionID
                            saveSP();//保存登录信息

                            //登录成功后，跳转到主界面
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);

                            //关闭登录界面
                            finish();
                        }
                    } else {
                        Log.e(TAG, "登录响应不成功或body为空");
                        Toast.makeText(LoginActivity.this,"登录失败：响应无效",Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<UserBean> call, Throwable throwable) {
                    //没有响应的数据返回，请求失败，网络问题，接口或参数有错误
                    Log.e(TAG, "登录失败", throwable);
                    Toast.makeText(LoginActivity.this,"登录失败: " + throwable.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });

        });
    }
}