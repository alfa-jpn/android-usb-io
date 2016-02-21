package jp.jidosya.androidusbio.usbio_services;

import java.util.EventListener;

import jp.jidosya.androidusbio.usbios.PortStatus;

public interface IPortStatusEventListener extends EventListener {
    void onPortStatusChanged(PortStatus status);
}
