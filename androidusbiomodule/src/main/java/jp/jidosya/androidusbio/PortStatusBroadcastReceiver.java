package jp.jidosya.androidusbio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PortStatusBroadcastReceiver extends BroadcastReceiver {
    private IPortStatusEventListener _listener;

    // Initialize Receiver.
    // @param [IPortStatusEventListener] listener EventListener.
    public PortStatusBroadcastReceiver(IPortStatusEventListener listener) {
        _listener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        if(USBIOService.ACTION_STATUS_CHANGED.equals(intent.getAction())) {
            _listener.onUSBIOPortStatusChanged(new PortStatus(intent.getByteExtra("PortStatus", (byte)0xFF)));
        }
    }
}
