package jp.jidosya.androidusbio.usbio_services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jp.jidosya.androidusbio.USBIOService;
import jp.jidosya.androidusbio.usbios.PortStatus;

public class PortStatusBroadcastReceiver extends BroadcastReceiver {
    private IPortStatusEventListener listener;

    // Initialize Receiver.
    // @param [IPortStatusEventListener] listener EventListener.
    public PortStatusBroadcastReceiver(IPortStatusEventListener listener) {
        this.listener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        if(USBIOService.ACTION_STATUS_CHANGED.equals(intent.getAction())) {
            listener.onPortStatusChanged(new PortStatus(intent.getByteExtra(USBIOService.PORT_BYTES_DATA, (byte) 0xFF)));
        }
    }
}
