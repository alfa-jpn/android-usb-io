package jp.jidosya.androidusbio.usbio_services;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import jp.jidosya.androidusbio.USBIOService;
import jp.jidosya.androidusbio.usbios.PortStatus;
import jp.jidosya.androidusbio.usbios.USBIOException;

public class USBIOServiceController {

    private Activity activity;
    private ServiceConnection connection;
    private USBIOService usbioService;

    public USBIOServiceController(Activity activity) {
        this.activity = activity;
        this.connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                usbioService = ((USBIOServiceBinder)service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                usbioService = null;

            }
        };
        Intent intent = new Intent(activity, jp.jidosya.androidusbio.USBIOService.class);
        activity.startService(intent);
        activity.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
    }

    public void listen(IPortStatusEventListener listener) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(USBIOService.ACTION_STATUS_CHANGED);
        this.activity.registerReceiver(new PortStatusBroadcastReceiver(listener), filter);
    }

    public void send(byte port, PortStatus ps) throws USBIOException {
        if (this.usbioService == null) {
            throw new USBIOException("USB-IO service is not initialized.");
        }
        this.usbioService.send(port, ps);
    }

    public PortStatus receive(byte port) throws USBIOException {
        if (this.usbioService == null) {
            throw new USBIOException("USB-IO service is not initialized.");
        }
        return this.usbioService.receive(port);
    }

    public void close() {
        this.activity.unbindService(this.connection);
    }
}
