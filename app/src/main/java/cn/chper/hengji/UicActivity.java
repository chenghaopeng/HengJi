package cn.chper.hengji;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.chper.hengji.api.ServiceImpl;
import cn.chper.hengji.api.SimpleResponse;
import cn.chper.hengji.util.MyToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UicActivity extends AppCompatActivity {

    private TextView txtUics;

    private TextView lblRisk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uic);
        txtUics = findViewById(R.id.txtUics);
        lblRisk = findViewById(R.id.lblRisk);
        readUics();
    }

    private void readUics() {
        FileInputStream in;
        BufferedReader reader = null;
        Set<String> uicss = new HashSet<>();
        StringBuilder uics = new StringBuilder();
        try {
            in = openFileInput("uics");
            reader = new BufferedReader(new InputStreamReader(in));
            String uic = "";
            while ((uic = reader.readLine()) != null) {
                uicss.add(uic);
            }
            for (String u : uicss) {
                uics.append(u);
                uics.append("\n");
            }
            txtUics.setText(uics.toString());
            ServiceImpl.instance.service.riskUic().enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                    if (response.isSuccessful() && response.body().code == 0) {
                        List<String> uics = (ArrayList<String>) response.body().data;
                        for (String uic : uics) {
                            if (txtUics.getText().toString().toLowerCase().contains(uic.toLowerCase())) {
                                lblRisk.setText("你可能有暴露风险！");
                            }
                        }
                        System.out.println(uics);
                    }
                    else {
                        MyToast.show(getApplicationContext(), "请求失败！");
                    }
                }

                @Override
                public void onFailure(Call<SimpleResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
