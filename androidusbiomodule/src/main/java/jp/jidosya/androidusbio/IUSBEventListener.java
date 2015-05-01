package jp.jidosya.androidusbio;

import android.hardware.usb.UsbDevice;
import java.util.EventListener;

public interface IUSBEventListener extends EventListener {
    public void onUSBPermitted(UsbDevice device);
    public void onUSBDisconnected(UsbDevice device);
}
