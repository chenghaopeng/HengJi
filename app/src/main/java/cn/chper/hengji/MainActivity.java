package cn.chper.hengji;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

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

    private Button btnCheck;

    private BluetoothManager bluetoothManager;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothLeScanner bluetoothLeScanner;

    private BluetoothLeAdvertiser bluetoothLeAdvertiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lblUic = findViewById(R.id.lblUic);
        btnCheck = findViewById(R.id.btnCheck);
        btnCheck.setOnClickListener(view -> {
            startActivity(new Intent(this, UicActivity.class));
        });
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
//        if (bluetoothManager == null || bluetoothAdapter == null || bluetoothLeAdvertiser == null || bluetoothLeScanner == null) {
//            MyToast.show(this, "请打开蓝牙后再使用！");
//            Log.d("[蓝牙]", "ERROR!");
//            Log.d("[蓝牙]", bluetoothManager.toString());
//            Log.d("[蓝牙]", bluetoothAdapter.toString());
//            Log.d("[蓝牙]", bluetoothLeScanner.toString());
//            Log.d("[蓝牙]", bluetoothLeAdvertiser.toString());
//            this.finish();
//            return;
//        }
        refreshUic();
        startScan();
    }

    @Override
    public void onBackPressed() {
        long offset = System.currentTimeMillis() - lastPressedTime;
        if (offset < 2000) this.finish();
        else MyToast.show(this, "再按一次退出登录！");
        lastPressedTime = System.currentTimeMillis();
    }

    private void startAdvertise(String uuid) {
        if (bluetoothLeAdvertiser == null) return;
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

    private void stopAdvertise() {
        if (bluetoothLeAdvertiser == null) return;
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
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

    private void startScan() {
        Log.d("[蓝牙]", "接收！");
        bluetoothLeScanner.startScan(scanCallback);
    }

    private void stopScan() {
        bluetoothLeScanner.stopScan(scanCallback);
    }

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            byte[] scanRecord = result.getScanRecord().getBytes();
            int startIndex = 0;
            boolean iBeaconFound = false;
            while (startIndex <= 5) {
                if (((int) scanRecord[startIndex + 2] & 0xff) == 0x02 && ((int) scanRecord[startIndex + 3] & 0xff) == 0x15) {
                    iBeaconFound = true;
                    break;
                }
                startIndex++;
            }
            if (!iBeaconFound) return;
            int major = (scanRecord[startIndex + 20] & 0xff) * 0x100 + (scanRecord[startIndex + 21] & 0xff);
            int minor = (scanRecord[startIndex + 22] & 0xff) * 0x100 + (scanRecord[startIndex + 23] & 0xff);
            Log.d("[接收到的 major 和 minor]", major + " " + minor);
            if (major != 7947 || minor != 8036) return;
            byte[] uuidByte = new byte[16];
            System.arraycopy(scanRecord, startIndex + 4, uuidByte, 0, 16);
            StringBuilder uuidBuilder = new StringBuilder();
            for (int i = 0; i < 16; ++i) {
                int x = uuidByte[i] & 0xff;
                String y = Integer.toHexString(x);
                if (y.length() < 2) uuidBuilder.append("0");
                uuidBuilder.append(y);
            }
            String uuidRaw = uuidBuilder.toString();
            String uuid = uuidRaw.substring(0, 8) + "-" + uuidRaw.substring(8, 12) + "-" + uuidRaw.substring(12, 16) + "-" + uuidRaw.substring(16, 20) + "-" + uuidRaw.substring(20);
            Log.d("[蓝牙]", "探测到" + uuid);
            validateAndWriteUic(uuid);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("[蓝牙]", "搜索失败！");
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
                stopAdvertise();
                startAdvertise(ServiceImpl.instance.uic);
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                MyToast.show(getApplicationContext(), "请求失败！");
            }
        });
    }

    private void validateAndWriteUic(String uic) {
        ServiceImpl.instance.service.judgeUic(uic).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful() && response.body().code == 0) {
                    writeUic(uic);
                    Log.d("[写入]", uic);
                }
                else {

                    Log.d("[有问题]", uic);
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                MyToast.show(getApplicationContext(), "无法连接网络！");
                t.printStackTrace();
            }
        });

    }

    private void writeUic(String uic) {
        FileOutputStream out;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("uics", Context.MODE_APPEND);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(uic + "\n");
        }
        catch (Exception e) {
            MyToast.show(this, "文件写入失败！");
            e.printStackTrace();
        }
        try {
            if (writer != null) writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}