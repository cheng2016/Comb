package com.cds.comb.module;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cds.comb.BaseActivity;
import com.cds.comb.R;
import com.cds.comb.data.entity.Light;
import com.cds.comb.util.Logger;
import com.cds.comb.util.PermissionHelper;
import com.cds.comb.view.HorizontalListView;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;

import java.security.Permission;
import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static final String BLE_BROADCAST = "PetComb";

    ListView listView;

    LightAdapter lightAdapter;

    HorizontalListView hlvSimpleListView;

    IndicatorAdapter indicatorAdapter;

    private ImageView img_loading;
    private Animation operatingAnim;
    private ProgressDialog progressDialog;

    boolean isConnect = false;

    private BleDevice mBleDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic notifyCharacteristic, writeCharacteristic;

    PermissionHelper mHeper;

    EditText repeatEdit;

    TextView tempTv;

    CheckBox checkBox;

    Button uploadBtn;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        hlvSimpleListView = (HorizontalListView) findViewById(R.id.hlvSimpleList);
        listView = (ListView) findViewById(R.id.list_view);

        progressDialog = new ProgressDialog(this);
        img_loading = findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        findViewById(R.id.title).setOnClickListener(this);

        repeatEdit = (EditText) findViewById(R.id.repeat_edit);
        checkBox = findViewById(R.id.checkBox);
        uploadBtn = findViewById(R.id.upload_btn);
        uploadBtn.setOnClickListener(this);
        tempTv = findViewById(R.id.temp_tv);
    }

    @Override
    protected void initData() {
        lightAdapter = new LightAdapter(this);
        listView.setAdapter(lightAdapter);
        indicatorAdapter = new IndicatorAdapter(this);
        hlvSimpleListView.setAdapter(indicatorAdapter);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setOperateTimeout(5000);
        setScanRule();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mHeper = new PermissionHelper(this);
            mHeper.requestPermissions("App lacks the necessary permissions, is it open?", new PermissionHelper.PermissionListener() {
                @Override
                public void doAfterGrand(String... permission) {

                }

                @Override
                public void doAfterDenied(String... permission) {
                    Toast.makeText(MainActivity.this, "Please provide the necessary permissions", Toast.LENGTH_LONG).show();
                }
            }, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void setScanRule() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
//                .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                .setDeviceName(true, BLE_BROADCAST)   // 只扫描指定广播名的设备，可选
//                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(false)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    showDialog(bleDevice);
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    void showDialog(final BleDevice bleDevice) {
        new AlertDialog.Builder(this).setTitle("查到到设备是否连接")
                .setMessage(bleDevice.getName())
                .setPositiveButton("连接", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connect(bleDevice);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();

    }

    private void connect(BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
//                statusTv.setText("ble onConnectFail");
                isConnect = false;
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                isConnect = true;
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, getString(R.string.connect_success), Toast.LENGTH_LONG).show();
                tempTv.setText("ble onConnectSuccess");
                mBleDevice = bleDevice;
                mBluetoothGatt = gatt;
                mGattService = mBluetoothGatt.getServices().get(mBluetoothGatt.getServices().size() - 1);
                writeCharacteristic = mGattService.getCharacteristics().get(2);
                notifyCharacteristic = mGattService.getCharacteristics().get(3);

                receiveNofity();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                isConnect = false;
                progressDialog.dismiss();
                tempTv.setText("ble onDisConnected");
                if (isActiveDisConnected) {
                    Toast.makeText(MainActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    String lastData;

    void receiveNofity() {
        BleManager.getInstance().notify(
                mBleDevice,
                notifyCharacteristic.getService().getUuid().toString(),
                notifyCharacteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        tempTv.setText("onNotifySuccess");
                        Logger.i(TAG, "onNotifySuccess");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                        Logger.i(TAG, "onNotifyFailure：" + exception.toString());

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        Logger.i(TAG, "onCharacteristicChanged：" + HexUtil.formatHexString(notifyCharacteristic.getValue(), true));
                        if (TextUtils.isEmpty(lastData)) {
                            lastData = "onCharacteristicChanged：" + HexUtil.formatHexString(notifyCharacteristic.getValue(), true);
                        } else {
                            if (lastData.endsWith(" ff ff") || lastData.startsWith("write success")) {
                                lastData = "onCharacteristicChanged：" + HexUtil.formatHexString(notifyCharacteristic.getValue(), true);
                            } else {
                                lastData = lastData + " " + HexUtil.formatHexString(notifyCharacteristic.getValue(), true);
                            }
                        }
                        tempTv.setText(lastData);
                    }
                });
    }

    void xiafaAction(int model) {
        byte bytes[];
        if (lightAdapter.getDataList() != null && lightAdapter.getDataList().size() > 0) {
            bytes = new byte[6 + lightAdapter.getDataList().size() * 3 + 1];
            bytes[0] = 0X0F;
            bytes[1] = 0X65;
            bytes[2] = 0X01;
            bytes[3] = 0X00;
            bytes[4] = (byte) model;
            bytes[5] = (byte) (checkBox.isChecked() ? Integer.valueOf(repeatEdit.getText().toString().trim()).intValue() : 1);
            for (int i = 0; i < lightAdapter.getDataList().size(); i++) {
                Light bean = lightAdapter.getDataList().get(i);
                bytes[6 + i * 3] = (byte) Integer.valueOf(bean.getMw()).intValue();
                bytes[7 + i * 3] = (byte) (Integer.valueOf(bean.getTime()).intValue() >> 8);
                bytes[8 + i * 3] = (byte) Integer.valueOf(bean.getTime()).intValue();
            }
            bytes[bytes.length - 1] = checkSum(bytes);
            bytes[1] = (byte) (bytes.length - 2);
            Logger.i(TAG, "sendStr：" + byteArrayToHexStr(bytes));

            byte[] arrayOfByte;
            int i = 1;
            for (; bytes.length - (i * 20) > 0; i++) {
                arrayOfByte = new byte[20];
                System.arraycopy(bytes, (i - 1) * 20, arrayOfByte, 0, 20);
                writeData(arrayOfByte);
                Logger.i(TAG, "第" + i + "次分段写入：" + byteArrayToHexStr(arrayOfByte));
            }
            if (bytes.length % 20 != 0) {
                arrayOfByte = new byte[bytes.length % 20];
                System.arraycopy(bytes, (i - 1) * 20, arrayOfByte, 0, bytes.length % 20);
                writeData(arrayOfByte);
                Logger.i(TAG, "最后分段写入：" + byteArrayToHexStr(arrayOfByte));
            }

            String end = "FFFF";
            byte[] endByte = hexStringToByteArray(end);
            writeData(endByte);
        }
    }

    public static byte checkSum(byte[] bytes4) {
        byte sum = 0;
        for (int i = 2; i < bytes4.length - 1; i++) {
            sum += bytes4[i];
        }
        sum += 0X01;
        System.out.println("checkSum：" + sum);
        return sum;
    }

    void writeData(final byte[] data) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().write(
                        mBleDevice,
                        writeCharacteristic.getService().getUuid().toString(),
                        writeCharacteristic.getUuid().toString(),
                        data,
                        new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                Logger.i(TAG, "onWriteSuccess，write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                            addText(txt, "write success, current: " + current
//                                                    + " total: " + total
//                                                    + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                        tempTv.setText("write success, current: " + current
                                                + " total: " + total
                                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                    }
                                });
                            }

                            @Override
                            public void onWriteFailure(final BleException exception) {
                                Logger.e(TAG, "onWriteFailure：" + exception.toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                            addText(txt, exception.toString());
                                        tempTv.setText(exception.toString());
                                    }
                                });
                            }
                        });
            }
        }, 300);
    }


    /**
     * byte[]转十六进制String
     *
     * @param byteArray
     * @return
     */
    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    /**
     * 将String转换为byte[]
     *
     * @param s String
     * @return byte[]
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    protected void onStop() {
        super.onStop();
        BleManager.getInstance().cancelScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title:
                startActivity(new Intent().setClass(MainActivity.this, TestActivity.class));
                break;
            case R.id.upload_btn:
                if (isConnect) {
                    xiafaAction(1);
                } else {
                    startScan();
//                    Toast.makeText(this, "Please connect the device first", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
