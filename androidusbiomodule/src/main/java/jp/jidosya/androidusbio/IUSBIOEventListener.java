package jp.jidosya.androidusbio;

import java.util.EventListener;

public interface IUSBIOEventListener extends EventListener {
    public void onUSBIOAttached(USBIO usbio);
    public void onUSBIODetached(USBIO usbio);
}
