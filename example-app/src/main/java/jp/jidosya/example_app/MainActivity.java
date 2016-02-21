package jp.jidosya.example_app;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import jp.jidosya.androidusbio.usbio_services.IPortStatusEventListener;
import jp.jidosya.androidusbio.usbio_services.USBIOServiceController;
import jp.jidosya.androidusbio.usbios.PortStatus;
import jp.jidosya.androidusbio.usbio_services.PortStatusBroadcastReceiver;
import jp.jidosya.androidusbio.USBIOService;
import jp.jidosya.androidusbio.usbios.USBIOException;


public class MainActivity extends Activity implements IPortStatusEventListener {
    USBIOServiceController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.controller = new USBIOServiceController(this);
        this.controller.listen(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.controller.close();
        stopService(new Intent(this, jp.jidosya.androidusbio.USBIOService.class));
    }

    public void onPortStatusChanged(PortStatus status) {
        Log.d("example-ap", "onPortStatusChanged: " + status.toString());
    }
}
