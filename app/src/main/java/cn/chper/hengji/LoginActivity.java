package cn.chper.hengji;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    protected TextView txtPhone;

    protected TextView txtCode;

    protected Button btnGetCode;

    protected Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtPhone = findViewById(R.id.txtPhone);
        txtCode = findViewById(R.id.txtCode);
        btnGetCode = findViewById(R.id.btnGetCode);
        btnLogin = findViewById(R.id.btnLogin);
    }

}
