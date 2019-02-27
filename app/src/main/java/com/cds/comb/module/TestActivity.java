package com.cds.comb.module;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cds.comb.BaseActivity;
import com.cds.comb.R;
import com.cds.comb.util.Logger;
import com.cds.comb.util.PermissionHelper;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;


import butterknife.OnClick;

/**
 * @Author: chengzj
 * @CreateDate: 2019/2/20 11:01
 * @Version: 3.0.0
 */
public class TestActivity extends BaseActivity implements View.OnClickListener {
    public static final String BLE_BROADCAST = "PetComb";

    AppCompatTextView title;

    EditText modelEdit;

    EditText repeatEdit;

    EditText irEdit;

    EditText irTimeEdit;

    EditText redEdit;

    EditText redTimeEdit;

    EditText irEdit2;

    EditText irTimeEdit2;

    EditText redEdit2;

    EditText redTimeEdit2;

    Button xiafaBtn;

    EditText queryModelEdit;

    Button queryBtn;

    boolean isConnect = false;

    private BleDevice mBleDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic notifyCharacteristic, writeCharacteristic;

    private TextView statusTv;
    private ImageView img_loading;
    private Animation operatingAnim;
    private ProgressDialog progressDialog;


    PermissionHelper mHeper;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_test;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        findViewById(R.id.right_button).setOnClickListener(this);
        progressDialog = new ProgressDialog(this);

        title = (AppCompatTextView) findViewById(R.id.title);
        modelEdit = (EditText) findViewById(R.id.model_edit);
        repeatEdit = (EditText) findViewById(R.id.repeat_edit);
        irEdit = (EditText) findViewById(R.id.ir_edit);
        irTimeEdit = (EditText) findViewById(R.id.ir_time_edit);
        redEdit = (EditText) findViewById(R.id.red_edit);
        redTimeEdit = (EditText) findViewById(R.id.red_time_edit);
        irEdit2 = (EditText) findViewById(R.id.ir_edit2);
        irTimeEdit2 = (EditText) findViewById(R.id.ir_time_edit2);
        redEdit2 = (EditText) findViewById(R.id.red_edit2);
        redTimeEdit2 = (EditText) findViewById(R.id.red_time_edit2);
        xiafaBtn = (Button) findViewById(R.id.xiafa_btn);
        queryModelEdit = (EditText) findViewById(R.id.query_model_edit);
        queryBtn = (Button) findViewById(R.id.query_btn);

        statusTv = findViewById(R.id.status_tv);
        img_loading = findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());

        mHeper = new PermissionHelper(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            mHeper.requestPermissions("App lacks the necessary permissions, is it open?", new PermissionHelper.PermissionListener() {
                @Override
                public void doAfterGrand(String... permission) {

                }

                @Override
                public void doAfterDenied(String... permission) {
                    Toast.makeText(TestActivity.this, "Please provide the necessary permissions", Toast.LENGTH_LONG).show();
                }
            }, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void initData() {
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setOperateTimeout(5000);
        setScanRule();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_button:
                if (checkPermissions()) {
                    if (!isConnect) {
                        startScan();
                    } else {
                        Toast.makeText(this, "Device is connected", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private boolean checkPermissions() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @OnClick({R.id.xiafa_btn, R.id.query_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.xiafa_btn:
                if (isConnect) {
                    xiafaAction();
                } else {
                    Toast.makeText(this, "Please connect the device first", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.query_btn:
                if (isConnect) {
                    handler.sendEmptyMessage(1);
                } else {
                    Toast.makeText(this, "Please connect the device first", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    void xiafaAction() {
        byte sendStr[] = new byte[103];
        sendStr[0] = 0X0F;
        sendStr[1] = 0X65;
        sendStr[2] = 0X01;
        sendStr[3] = 0X00;
        sendStr[4] = (byte) Integer.valueOf(modelEdit.getText().toString().trim()).intValue();
        sendStr[5] = (byte) Integer.valueOf(repeatEdit.getText().toString().trim()).intValue();

        sendStr[6] = (byte) Integer.valueOf(irEdit.getText().toString().trim()).intValue();
        sendStr[7] = ((byte) (Integer.valueOf(irTimeEdit.getText().toString().trim()).intValue() >> 8));
        sendStr[8] = (byte) Integer.valueOf(irTimeEdit.getText().toString().trim()).intValue();
        sendStr[9] = (byte) Integer.valueOf(redEdit.getText().toString().trim()).intValue();
        sendStr[10] = (byte) (Integer.valueOf(redTimeEdit.getText().toString().trim()).intValue() >> 8);
        sendStr[11] = (byte) Integer.valueOf(redTimeEdit.getText().toString().trim()).intValue();

        sendStr[12] = (byte) Integer.valueOf(irEdit2.getText().toString().trim()).intValue();
        sendStr[13] = ((byte) (Integer.valueOf(irTimeEdit2.getText().toString().trim()).intValue() >> 8));
        sendStr[14] = (byte) Integer.valueOf(irTimeEdit2.getText().toString().trim()).intValue();
        sendStr[15] = (byte) Integer.valueOf(redEdit2.getText().toString().trim()).intValue();
        sendStr[16] = (byte) (Integer.valueOf(redTimeEdit2.getText().toString().trim()).intValue() >> 8);
        sendStr[17] = (byte) Integer.valueOf(redTimeEdit2.getText().toString().trim()).intValue();

        for (int i = 1; i <= 14; i++) {
            sendStr[12 + i * 6] = 10;
            sendStr[13 + i * 6] = 0;
            sendStr[14 + i * 6] = 10;
            sendStr[15 + i * 6] = 10;
            sendStr[16 + i * 6] = 0;
            sendStr[17 + i * 6] = 10;
        }
        sendStr[102] = checkSum(sendStr);
        sendStr[1] = (byte) (sendStr.length - 2);
        Logger.i(TAG, "sendStr：" + byteArrayToHexStr(sendStr));

        byte[] arrayOfByte;
        int i = 1;
        for (; sendStr.length - (i * 20) > 0; i++) {
            arrayOfByte = new byte[20];
            System.arraycopy(sendStr, (i - 1) * 20, arrayOfByte, 0, 20);
            writeData(arrayOfByte);
            Logger.i(TAG, "第" + i + "次分段写入：" + byteArrayToHexStr(arrayOfByte));
        }

        if (sendStr.length % 20 != 0) {
            arrayOfByte = new byte[sendStr.length % 20];
            System.arraycopy(sendStr, (i - 1) * 20, arrayOfByte, 0, sendStr.length % 20);
            writeData(arrayOfByte);
            Logger.i(TAG, "最后分段写入：" + byteArrayToHexStr(arrayOfByte));
        }
        String end = "FFFF";
        byte[] bytes = hexStringToByteArray(end);
        writeData(bytes);
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
//                mDeviceAdapter.clearScanDevice();
//                mDeviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                statusTv.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
//                mDeviceAdapter.addDevice(bleDevice);
//                mDeviceAdapter.notifyDataSetChanged();
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    showDialog(bleDevice);
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                statusTv.setText(getString(R.string.start_scan));
            }
        });
    }

    void showDialog(final BleDevice bleDevice) {
        new AlertDialog.Builder(this).setTitle("Found if the device is connected")
                .setMessage(bleDevice.getName())
                .setPositiveButton("connection", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connect(bleDevice);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
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
                statusTv.setText("ble onConnectFail");
                isConnect = false;
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                isConnect = true;
                progressDialog.dismiss();
                Toast.makeText(TestActivity.this, getString(R.string.connect_success), Toast.LENGTH_LONG).show();

                statusTv.setText("ble onConnectSuccess");

                mBleDevice = bleDevice;

                mBluetoothGatt = gatt;

                mGattService = mBluetoothGatt.getServices().get(mBluetoothGatt.getServices().size() - 1);

                writeCharacteristic = mGattService.getCharacteristics().get(2);

                notifyCharacteristic = mGattService.getCharacteristics().get(3);

                handler.sendEmptyMessage(0);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                isConnect = false;
                progressDialog.dismiss();
                statusTv.setText("ble onDisConnected");
                if (isActiveDisConnected) {
                    Toast.makeText(TestActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TestActivity.this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
//                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    BleManager.getInstance().notify(
                            mBleDevice,
                            notifyCharacteristic.getService().getUuid().toString(),
                            notifyCharacteristic.getUuid().toString(),
                            new BleNotifyCallback() {

                                @Override
                                public void onNotifySuccess() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                        addText(txt, "notify success");
                                            statusTv.setText("notify success");
                                        }
                                    });
                                }

                                @Override
                                public void onNotifyFailure(final BleException exception) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                        addText(txt, exception.toString());
                                            statusTv.setText(exception.toString());
                                        }
                                    });
                                }

                                @Override
                                public void onCharacteristicChanged(byte[] data) {
                                    Logger.i(TAG, "onCharacteristicChanged：" + HexUtil.formatHexString(notifyCharacteristic.getValue(), true));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                        addText(txt, HexUtil.formatHexString(characteristic.getValue(), true));
                                            if (statusTv.getText().toString().endsWith(" ff ff") || statusTv.getText().toString().startsWith("write success")) {
                                                statusTv.setText("onCharacteristicChanged："+HexUtil.formatHexString(notifyCharacteristic.getValue(), true));
                                            } else {
                                                statusTv.setText(statusTv.getText().toString() + " " + HexUtil.formatHexString(notifyCharacteristic.getValue(), true));
                                            }
                                        }
                                    });
                                }
                            });
                    break;
                case 1:
                    byte[] bytes4 = new byte[8];
                    bytes4[0] = 0X0F;
                    bytes4[1] = 0X04;
                    bytes4[2] = 0X02;
                    bytes4[3] = 0X00;
                    bytes4[4] = (byte) Integer.valueOf(queryModelEdit.getText().toString()).intValue();
                    bytes4[5] = 0X04;
//                    String hex = byteArrayToHexStr(bytes4);
//                    byte[] bs = hexStringToByteArray("0F0402000104FFFF");
                    writeData(bytes4);
                    break;
            }
        }
    };

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
                                Logger.i(TAG,"onWriteSuccess，write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                            addText(txt, "write success, current: " + current
//                                                    + " total: " + total
//                                                    + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                        statusTv.setText("write success, current: " + current
                                                + " total: " + total
                                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                    }
                                });
                            }

                            @Override
                            public void onWriteFailure(final BleException exception) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                            addText(txt, exception.toString());
                                        statusTv.setText(exception.toString());
                                    }
                                });
                            }
                        });
            }
        }, 300);
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


    public static void main(String[] args) {
        System.out.println("============================  string byte 互转 start ================================");

        byte[] bytes = new byte[]{0X0F, 0x04, 0x02, 0X00};

        System.out.println("bytes ：" + Arrays.toString(bytes));

        String s = byteArrayToHexStr(bytes);

        System.out.println("byte to string：" + s);

        byte[] bytes2 = hexStringToByteArray("03");

        System.out.println("byte：" + Arrays.toString(bytes2));

        System.out.println("byte to string：" + byteArrayToHexStr(bytes2));

        System.out.println("============================ test ================================");
        byte[] bs = hexStringToByteArray("0F0402000104FFFF");
        System.out.println(Arrays.toString(bs));
        System.out.println("---------------------------------------");

        byte[] bytes4 = new byte[6];
        bytes4[0] = 0X0F;
        bytes4[1] = 0X04;
        bytes4[2] = 0X02;
        bytes4[3] = 0X00;
        bytes4[4] = 0X01;
        bytes4[5] = 0X04;
        System.out.println(Arrays.toString(bytes4));

        System.out.println("---------------------------------------");

        bytes4[bytes4.length - 1] = checkSum(bytes4);

        bytes4[1] = (byte) (bytes4.length - 2);

        System.out.println("length ：" + (bytes4.length - 2));

        System.out.println(Arrays.toString(bytes4));

        System.out.println("----------------  char byte 互转 -----------------------");

        char[] chars = new char[]{0x0f, 0x04, 0x02, 0x00, 0x01, 0x04};
        System.out.println("chars ：" + Arrays.toString(chars));

        String str = byteArrayToHexStr(bytes4);
        char[] arr = str.toCharArray();
        System.out.println("byte to char ：" + Arrays.toString(arr));

        String s1 = "0F0402000104";
        char[] chars1 = s1.toCharArray();
        System.out.println("string to char ：" + Arrays.toString(chars1));

        str = Arrays.toString(arr);

        byte[] bytes1 = hexStringToByteArray(str);

        System.out.println("byte to string ：" + Arrays.toString(bytes1));

        System.out.println("--------------   demo   ------------------------");
        String s2 = "0F65010001050A001414001E1E00282800320A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A0A000A28";
        System.out.println("原字符串：" + s2);
        byte[] bytes5 = hexStringToByteArray(s2);
        System.out.println(Arrays.toString(bytes5));
        checkSum(bytes5);
        bytes5[1] = (byte) (bytes5.length - 2);
        System.out.println("length ：" + (bytes5.length - 2));

        System.out.println("byte to string：" + byteArrayToHexStr(bytes5));

        byte[] bytes3 = new byte[]{10, 20 >> 8, 20, 20, 30 >> 8, 30};

        System.out.println("byte3 to string：" + byteArrayToHexStr(bytes3));

        System.out.println("---------------------------------------");

/*        String content = "4";
        System.out.println("原字符串：" + content);
        String hex2Str = conver2HexStr(content.getBytes());
        System.out.println("\n转换为二进制的表示形式：" + hex2Str);
        byte[] b = conver2HexToByte(hex2Str);
        System.out.println("二进制字符串还原：" + new String(b));

        String hex16Str = conver16HexStr(content.getBytes());
        System.out.println("\n转换为十六进制的表示形式:" + hex16Str);
        System.out.println("十六进制字符串还原:" + new String(conver16HexToByte(hex16Str)));*/


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
     * byte数组转换为二进制字符串,每个字节以","隔开
     **/
    public static String conver2HexStr(byte[] b) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            result.append(Long.toString(b[i] & 0xff, 2) + ",");
        }
        return result.toString().substring(0, result.length() - 1);
    }

    /**
     * 二进制字符串转换为byte数组,每个字节以","隔开
     **/
    public static byte[] conver2HexToByte(String hex2Str) {
        String[] temp = hex2Str.split(",");
        byte[] b = new byte[temp.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = Long.valueOf(temp[i], 2).byteValue();
        }
        return b;
    }


    /**
     * byte数组转换为十六进制的字符串
     **/
    public static String conver16HexStr(byte[] b) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            if ((b[i] & 0xff) < 0x10)
                result.append("0");
            result.append(Long.toString(b[i] & 0xff, 16));
        }
        return result.toString().toUpperCase();
    }

    /**
     * 十六进制的字符串转换为byte数组
     **/
    public static byte[] conver16HexToByte(String hex16Str) {
        char[] c = hex16Str.toCharArray();
        byte[] b = new byte[c.length / 2];
        for (int i = 0; i < b.length; i++) {
            int pos = i * 2;
            b[i] = (byte) ("0123456789ABCDEF".indexOf(c[pos]) << 4 | "0123456789ABCDEF".indexOf(c[pos + 1]));
        }
        return b;
    }


    /**
     * byte转char
     *
     * @param bytes
     * @return
     */
//    public static char[] getChars(byte[] bytes) {
//        Charset cs = Charset.forName("UTF-8");
//        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
//        bb.put(bytes);
//        bb.flip();
//        CharBuffer cb = cs.decode(bb);
//        return cb.array();
//    }

    /**
     * char转byte
     *
     * @param chars
     * @return
     */
/*    public static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }*/
    public static char byteToChar(byte[] b) {
        char c = (char) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
        return c;
    }

    public static byte[] charToByte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }
}


