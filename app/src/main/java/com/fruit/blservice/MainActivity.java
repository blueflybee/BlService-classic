package com.fruit.blservice;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fruit.library.bluetooth.BluetoothSPP;
import com.fruit.library.bluetooth.BluetoothState;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {


  private static final String TAG = "MainActivity";
  private static final int RC_ACCESS_COARSE_LOCATION = 1;

  private BluetoothSPP mBluetooth;
  private TextView mMsgTv;
  private TextView mStatusTv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMsgTv = (TextView) findViewById(R.id.tv_msg);
    mStatusTv = (TextView) findViewById(R.id.tv_status);

    Button btnclose = (Button) findViewById(R.id.btn_close);
    Button btnopen = (Button) findViewById(R.id.btn_open);
    Button btnSend = (Button) findViewById(R.id.btn_send);

    btnclose.setOnClickListener(this);
    btnopen.setOnClickListener(this);
    btnSend.setOnClickListener(this);

    mBluetooth = new BluetoothSPP(this);

    if (!mBluetooth.isBluetoothAvailable()) {
      Toast.makeText(getApplicationContext()
          , "Bluetooth is not available"
          , Toast.LENGTH_SHORT).show();
      finish();
    }
    mBluetooth.setDeviceTarget(BluetoothState.DEVICE_ANDROID);

    mBluetooth.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
      @Override
      public void onDataReceived(byte[] bytes, String s) {
        mMsgTv.append(s + "\n");
      }
    });

    mBluetooth.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
      @Override
      public void onDeviceConnected(String name, String address) {
        mStatusTv.setText("");
        mStatusTv.append("已经连接到设备： " + name + "\n");
        mStatusTv.append("设备地址： " + address + "\n");
      }

      @Override
      public void onDeviceDisconnected() {
        mStatusTv.setText("设备未连接");
      }

      @Override
      public void onDeviceConnectionFailed() {
        mStatusTv.setText("连接失败！");
      }
    });

    String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION};
    if (!EasyPermissions.hasPermissions(this, perms)) {
      EasyPermissions.requestPermissions(this, "连接蓝牙设备需要扫描蓝牙设备权限", RC_ACCESS_COARSE_LOCATION, perms);
    }

  }


  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_open:
        start();
        break;
      case R.id.btn_close:
        stop();
        break;

      case R.id.btn_send:
        send();
        break;

      default:
        break;
    }
  }

  private void send() {
    mBluetooth.send("亲，我是智能锁", true);
  }

  private void stop() {
    if (mBluetooth != null) {
      mBluetooth.stopService();
      if (!mBluetooth.isServiceAvailable()) {
        Toast.makeText(getApplicationContext()
            , "服务已经关闭"
            , Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void start() {
    if (!mBluetooth.isBluetoothEnabled()) {
      Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
    } else {
      if (!mBluetooth.isServiceAvailable()) {
        mBluetooth.setupService();
      }
      mBluetooth.startService(BluetoothState.DEVICE_ANDROID);

    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
      if (resultCode == Activity.RESULT_OK) {
        System.out.println("result_ok!");
        if (!mBluetooth.isServiceAvailable()) {
          mBluetooth.setupService();
        }
        mBluetooth.startService(BluetoothState.DEVICE_ANDROID);
      } else {
        Toast.makeText(getApplicationContext()
            , "Bluetooth was not enabled."
            , Toast.LENGTH_SHORT).show();
        finish();
      }
    } else if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
      // Do something after user returned from app settings screen, like showing a Toast.
      Toast.makeText(this, "returned from app settings", Toast.LENGTH_SHORT)
          .show();
    }
  }


  @Override
  public void onPermissionsGranted(int requestCode, List<String> perms) {
    Toast.makeText(this, "onPermissionsGranted", Toast.LENGTH_SHORT)
        .show();
  }

  @Override
  public void onPermissionsDenied(int requestCode, List<String> perms) {
    Toast.makeText(this, "onPermissionsDenied", Toast.LENGTH_SHORT).show();
    Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

    // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
    // This will display a dialog directing them to enable the permission in app settings.
    if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
      new AppSettingsDialog.Builder(this).build().show();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    // Forward results to EasyPermissions
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }
}
