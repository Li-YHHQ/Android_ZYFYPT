package com.example.zyfypt613lsl.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.common.Common;
import com.example.zyfypt613lsl.service.UserService;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RegisterActivity extends BaseActivity {
    // 声明控件
    private EditText etUsername, etPassword, etConfirmPassword, etTel, etEmail;
    private RadioGroup rgRole;
    private RadioButton rbStudent, rbTeacher;
    private Button btnRegister, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // 初始化视图
        initViews();
        
        // 移除不必要的SessionID处理

        // 设置边距适配 - 使用根布局作为目标视图
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        // 设置边距适配 - 使用根布局作为目标视图
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> register());

        // 返回登录按钮点击事件
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        // 绑定控件ID
        etUsername = findViewById(R.id.etname_1);
        etPassword = findViewById(R.id.etpass_11);
        etConfirmPassword = findViewById(R.id.etpass_12);
        etTel = findViewById(R.id.ettel);
        etEmail = findViewById(R.id.etemail);
        rgRole = findViewById(R.id.rg_role);
        rbStudent = findViewById(R.id.rb_student);
        rbTeacher = findViewById(R.id.rb_teacher);
        btnRegister = findViewById(R.id.etRegister);
        btnBack = findViewById(R.id.etRettolo);

        // 默认选中学生角色
        rbStudent.setChecked(true);
    }

    private void register() {
        // 获取输入值
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String tel = etTel.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        int roleid = rbStudent.isChecked() ? 2 : 3; // 2代表学生，3代表教师

        // 输入验证
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || tel.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "请填写所有必填项", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证电话号码格式
        if (!android.util.Patterns.PHONE.matcher(tel).matches()) {
            Toast.makeText(this, "请输入有效的电话号码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证邮箱格式
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建Retrofit对象，使用ScalarsConverterFactory处理字符串响应
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 调用新的注册API
        UserService service = retrofit.create(UserService.class);
        Call<String> call = service.register(username, password, tel, roleid, email);
        
        // 打印请求信息用于调试
        Log.d("RegisterActivity", "请求URL: " + Common.BASEURL + "api.php/reg");
        Log.d("RegisterActivity", "请求参数: username=" + username + ", password=" + password + ", tel=" + tel + ", roleid=" + roleid + ", email=" + email);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body();

                    // 根据返回结果判断注册是否成功
                    // API返回"1"表示成功，其他情况为JSON格式的错误信息
                    if (result.equals("1")) {
                        // 注册成功
                        Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                        // 跳转到登录页面
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        finish();
                    } else {
                        // 注册失败，显示错误信息
                        // 尝试解析JSON错误信息
                        try {
                            JSONObject jsonError = new JSONObject(result);
                            if (jsonError.has("error")) {
                                Toast.makeText(RegisterActivity.this, jsonError.getString("error"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "注册失败: " + result, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            // 如果不是JSON格式，直接显示
                            Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                // 打印详细错误信息用于调试
                Log.e("RegisterActivity", "网络请求失败", throwable);
                String errorMessage = "网络异常，请检查网络连接";
                
                // 根据不同的异常类型提供更具体的错误信息
                if (throwable instanceof java.net.UnknownHostException) {
                    errorMessage = "无法连接到服务器，请检查服务器地址是否正确";
                } else if (throwable instanceof java.net.ConnectException) {
                    errorMessage = "连接被拒绝，请检查服务器是否运行或防火墙设置";
                } else if (throwable instanceof java.net.SocketTimeoutException) {
                    errorMessage = "连接超时，请稍后重试";
                } else if (throwable instanceof retrofit2.HttpException) {
                    errorMessage = "服务器返回错误: " + ((retrofit2.HttpException) throwable).code();
                }
                
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
