package jp.jidosya.example_app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import jp.jidosya.androidusbio.IPortStatusEventListener;
import jp.jidosya.androidusbio.PortStatus;
import jp.jidosya.androidusbio.PortStatusBroadcastReceiver;
import jp.jidosya.androidusbio.USBIOService;


public class MainActivity extends Activity implements IPortStatusEventListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(USBIOService.ACTION_STATUS_CHANGED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(new PortStatusBroadcastReceiver(this), filter);

        startService(new Intent(this, jp.jidosya.androidusbio.USBIOService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, jp.jidosya.androidusbio.USBIOService.class));
    }

    public void onUSBIOPortStatusChanged(PortStatus status) {
        Log.d("example-ap", "onPortStatusChanged: " + status.toString());
    }
}
