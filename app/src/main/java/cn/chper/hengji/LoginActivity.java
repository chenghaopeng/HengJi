package cn.chper.hengji;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import cn.chper.hengji.api.ServiceImpl;
import cn.chper.hengji.api.SimpleResponse;
import cn.chper.hengji.util.MyToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    protected TextView txtPhone;

    protected TextView txtCode;

    protected Button btnGetCode;

    protected Button btnLogin;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtPhone = findViewById(R.id.txtPhone);
        txtCode = findViewById(R.id.txtCode);
        btnGetCode = findViewById(R.id.btnGetCode);
        btnLogin = findViewById(R.id.btnLogin);
        btnGetCode.setOnClickListener(view -> {
            String phone = txtPhone.getText().toString();
            if (!phone.matches("1[\\d]{10}")) {
                MyToast.show(this, "手机格式错误！");
                return;
            }
            ServiceImpl.instance.service.getCode(phone).enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                    if (!response.isSuccessful() || response.body().code != 0) {
                        String data = "";
                        if (response.isSuccessful()) data = (String) response.body().data;
                        MyToast.show(getApplicationContext(), "获取验证码失败！" + data);
                        return;
                    }
                    MyToast.show(getApplicationContext(), "验证码已发送，请注意查收！");
                    btnGetCode.setEnabled(false);
                    countDownTimer = new CountDownTimer(60 * 1000, 1 * 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            btnGetCode.setText("稍等" + String.valueOf(millisUntilFinished / 1000) + "秒");
                        }

                        @Override
                        public void onFinish() {
                            btnGetCode.setText("获取验证码");
                            btnGetCode.setEnabled(true);
                        }
                    };
                    countDownTimer.start();
                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    MyToast.show(getApplicationContext(), "获取验证码失败！");
                    t.printStackTrace();
                }
            });
        });
        btnLogin.setOnClickListener(view -> {
            String phone = txtPhone.getText().toString();
            if (!phone.matches("1[\\d]{10}")) {
                MyToast.show(this, "手机格式错误！");
                return;
            }
            String code = txtCode.getText().toString();
            if (!code.matches("[\\d]{6}")) {
                MyToast.show(this, "验证码为 6 位数字！");
                return;
            }
            ServiceImpl.instance.service.login(phone, code).enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                    if (!response.isSuccessful()) {
                        MyToast.show(getApplicationContext(), "请求失败，请检查网络！");
                        return;
                    }
                    if (response.body().code != 0) {
                        MyToast.show(getApplicationContext(), "手机号或验证码错误！");
                        return;
                    }
                    txtPhone.setText("");
                    txtCode.setText("");
                    ServiceImpl.instance.token = (String) response.body().data;
                    MyToast.show(getApplicationContext(), "欢迎使用！");
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    MyToast.show(getApplicationContext(), "登录失败！");
                    t.printStackTrace();
                }
            });
        });
    }

}
