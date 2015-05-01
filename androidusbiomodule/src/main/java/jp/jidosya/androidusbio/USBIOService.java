package jp.jidosya.androidusbio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class USBIOService extends Service implements Runnable, IUSBIOEventListener {
    public static final String ACTION_STATUS_CHANGED = "jp.jidosya.androidusbio.STATUS_CHANGED";
    public final static String SERVICE_NAME          = "USB-IO Service";

    // Instance of status.
    private boolean _isRunning;

    private USBIO _usbIO;

    @Override
    public void onCreate() {
        super.onCreate();
        _usbIO = new USBIO(this, this);
        Log.d(SERVICE_NAME, "Started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _isRunning = false;
        if(_usbIO != null) {
            _usbIO.close();
        }
        Log.d(SERVICE_NAME, "Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(SERVICE_NAME, "onBind.");
        return null;
    }

    public void onUSBIOAttached(USBIO usbio) {
        startUSBIOPolling();
    }

    public void onUSBIODetached(USBIO usbio) {
        _isRunning = false;
    }

    public void run() {
        PortStatus prev = null;

        Log.d(SERVICE_NAME, "Start Polling");
        while(_isRunning) {
            try {
                PortStatus current = _usbIO.receive(USBIO.USB_IO_PORT1, true);
                if(current == null) {
                    continue;
                }

                if (prev == null || !prev.equals(current)) {
                    emitStatusChangedBroadCastMessage(current);
                    prev = current;
                }

                Thread.sleep(25);
            } catch (Exception e) {
                Log.e(SERVICE_NAME, e.toString());
            }
        }
    }

    private void startUSBIOPolling() {
        _isRunning = true;
        new Thread(this).start();
    }

    private void emitStatusChangedBroadCastMessage(PortStatus status) {
        Intent intent = new Intent();
        intent.putExtra("PortStatus", status.getPort());
        intent.setAction(ACTION_STATUS_CHANGED);
        getBaseContext().sendBroadcast(intent);
    }
}
