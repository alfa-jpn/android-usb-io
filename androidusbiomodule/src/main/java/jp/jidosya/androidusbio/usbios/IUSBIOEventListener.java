package jp.jidosya.androidusbio.usbios;

import java.util.EventListener;

import jp.jidosya.androidusbio.USBIO;

public interface IUSBIOEventListener extends EventListener {
    void onAttached(USBIO usbio);
    void onDetached(USBIO usbio);
    void onError(USBIO usbio);
}
