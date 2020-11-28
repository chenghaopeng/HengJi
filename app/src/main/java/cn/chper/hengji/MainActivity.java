package cn.chper.hengji;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

    private BluetoothManager bluetoothManager;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothLeAdvertiser bluetoothLeAdvertiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lblUic = findViewById(R.id.lblUic);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        refreshUic();
    }

    @Override
    public void onBackPressed() {
        long offset = System.currentTimeMillis() - lastPressedTime;
        if (offset < 2000) this.finish();
        else MyToast.show(this, "再按一次退出登录！");
        lastPressedTime = System.currentTimeMillis();
    }

    private void startAdvertise(String uuid) {
//        if (bluetoothManager == null || bluetoothAdapter == null || bluetoothLeAdvertiser == null) return;
        AdvertiseSettings.Builder advertiseSettingsBuilder = new AdvertiseSettings.Builder();
        advertiseSettingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        advertiseSettingsBuilder.setTimeout(0);
        advertiseSettingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings advertiseSettings = advertiseSettingsBuilder.build();

        AdvertiseData.Builder advertiseDataBuilder = new AdvertiseData.Builder();
        String beaconType = "0215";
        String beaconUuid = uuid.replace("-", "");
//        String beaconMajor = Integer.toHexString(7947);
//        String beaconMinor = Integer.toHexString(8036);
//        String beaconMeasuredPower = Integer.toHexString(-59);
//        while (beaconMajor.length() < 4) beaconMajor = "0" + beaconMajor;
//        while (beaconMinor.length() < 4) beaconMinor = "0" + beaconMinor;
//        while (beaconMeasuredPower.length() < 2) beaconMeasuredPower = "0" + beaconMeasuredPower;
        String beaconMajor = "1f0b";
        String beaconMinor = "1f64";
        String beaconMeasuredPower = "c5";
        String data = beaconType + beaconUuid + beaconMajor + beaconMinor + beaconMeasuredPower;
        data = data.toLowerCase();
        byte[] dataByte = new byte[data.length() / 2];
        for (int i = 0; i < dataByte.length; ++i) {
            dataByte[i] = (byte) (Integer.parseInt(data.substring(i * 2, i * 2 + 2), 16));
        }
        advertiseDataBuilder.addManufacturerData(0x004c, dataByte);
        AdvertiseData advertiseData = advertiseDataBuilder.build();

        Log.d("[蓝牙]", "发射！");
        bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
    }

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d("[蓝牙]", "启动成功！");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d("[蓝牙]", "启动失败！");
        }
    };

    private void refreshUic() {
        ServiceImpl.instance.service.refreshUic(ServiceImpl.instance.token).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (!response.isSuccessful() || response.body().code != 0) {
                    MyToast.show(getApplicationContext(), "登录已失效，请重新登录！");
                    return;
                }
                ServiceImpl.instance.uic = (String) response.body().data;
                lblUic.setText(ServiceImpl.instance.uic);
                startAdvertise(ServiceImpl.instance.uic);
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                MyToast.show(getApplicationContext(), "请求失败！");
            }
        });
    }

}