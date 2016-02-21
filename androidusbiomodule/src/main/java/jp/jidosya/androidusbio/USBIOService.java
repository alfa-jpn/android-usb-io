package jp.jidosya.androidusbio;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import jp.jidosya.androidusbio.usbio_services.IPortStatusEventListener;
import jp.jidosya.androidusbio.usbio_services.PortStatusBroadcastReceiver;
import jp.jidosya.androidusbio.usbio_services.USBIOServiceBinder;
import jp.jidosya.androidusbio.usbios.IUSBIOEventListener;
import jp.jidosya.androidusbio.usbios.PortStatus;
import jp.jidosya.androidusbio.usbios.USBIOException;

public class USBIOService extends Service implements Runnable, IUSBIOEventListener {
    public static final String ACTION_STATUS_CHANGED = "jp.jidosya.androidusbio.STATUS_CHANGED";
    public static final String PORT_BYTES_DATA       = "PortStatus";
    public final static String SERVICE_NAME          = "USB-IO Service";

    // Instance of status.
    private boolean isRunning;
    private USBIO usbIO;
    private USBIOServiceBinder binder;

    @Override
    public void onCreate() {
        super.onCreate();
        this.binder = new USBIOServiceBinder(this);
        startPolling();
        Log.d(SERVICE_NAME, "Started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.isRunning = false;
        Log.d(SERVICE_NAME, "Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(SERVICE_NAME, "onBind.");
        return this.binder;
    }

    @Override
    public void onAttached(USBIO usbio) {
        Log.d(SERVICE_NAME, "USB-IO was attached.");
    }

    @Override
    public void onDetached(USBIO usbio) {
        this.usbIO = null;
        Log.d(SERVICE_NAME, "USB-IO was detached.");
    }

    @Override
    public void onError(USBIO usbio) {
        this.usbIO = null;
        Log.d(SERVICE_NAME, "USB-IO was failed connection.");
    }

    public void send(byte port, PortStatus ps) throws USBIOException {
        if (this.usbIO == null) {
            throw new USBIOException("USB-IO is not initialized.");
        }
        this.usbIO.send(port, ps);
    }

    public PortStatus receive(byte port) throws USBIOException {
        if (this.usbIO == null) {
            throw new USBIOException("USB-IO is not initialized.");
        }
        return this.usbIO.receive(port);
    }

    public void run() {
        PortStatus prev = null;

        Log.d(SERVICE_NAME, "Start Polling");
        while(isRunning) {
            try {
                if (this.usbIO == null) {
                    Thread.sleep(5000);
                    this.usbIO = new USBIO(this, this);
                } else {
                    if (this.usbIO.isAttached()) {
                        PortStatus current = usbIO.receive(USBIO.USB_IO_PORT1, true);

                        if (prev == null || !prev.equals(current)) {
                            emitStatusChangedBroadCastMessage(current);
                            prev = current;
                        }
                    }
                    Thread.sleep(25);
                }
            } catch (Exception e) {
                this.usbIO = null;
                Log.d(SERVICE_NAME, e.toString());
            }
        }

        if(this.usbIO != null) {
            this.usbIO.close();
        }
    }

    private void startPolling() {
        isRunning = true;
        new Thread(this).start();
    }

    private void emitStatusChangedBroadCastMessage(PortStatus status) {
        Intent intent = new Intent();
        intent.putExtra(PORT_BYTES_DATA, status.getPort());
        intent.setAction(ACTION_STATUS_CHANGED);
        getBaseContext().sendBroadcast(intent);
    }
}
