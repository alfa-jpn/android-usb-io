package jp.jidosya.androidusbio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class USBBroadcastReceiver extends BroadcastReceiver {

    private String _permitAction;

    private UsbDevice _device;

    private IUSBEventListener _listener;

    // Initialize Receiver.
    // @param [String]            permitAction Signal of PermitAction.
    // @param [UsbDevice]         device        USB-IO Device.
    // @param [IUSBEventListener] listener      EventListener.
    public USBBroadcastReceiver(String permitAction, UsbDevice device, IUSBEventListener listener) {
        _permitAction = permitAction;
        _device = device;
        _listener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(_permitAction.equals(action)) {
            if(!_device.equals(intent.getParcelableExtra(UsbManager.EXTRA_DEVICE))){
                return;
            }
            _listener.onUSBPermitted(_device);
        }

        if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            _listener.onUSBDisconnected(_device);
        }
    }
}
