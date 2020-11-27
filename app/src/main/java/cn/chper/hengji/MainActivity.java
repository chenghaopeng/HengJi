package cn.chper.hengji;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import cn.chper.hengji.api.Service;
import cn.chper.hengji.api.ServiceImpl;
import cn.chper.hengji.api.SimpleResponse;
import cn.chper.hengji.util.MyToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private long lastPressedTime = 0;

    private TextView lblUic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lblUic = findViewById(R.id.lblUic);
        ServiceImpl.instance.service.refreshUic(ServiceImpl.instance.token).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (!response.isSuccessful() || response.body().code != 0) {
                    MyToast.show(getApplicationContext(), "登录已失效，请重新登录！");
                    return;
                }
                ServiceImpl.instance.uic = (String) response.body().data;
                lblUic.setText(ServiceImpl.instance.uic);
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                MyToast.show(getApplicationContext(), "请求失败！");
            }
        });
    }

    @Override
    public void onBackPressed() {
        long offset = System.currentTimeMillis() - lastPressedTime;
        if (offset < 2000) this.finish();
        else MyToast.show(this, "再按一次退出登录！");
        lastPressedTime = System.currentTimeMillis();
    }

}