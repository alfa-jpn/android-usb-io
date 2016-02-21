package jp.jidosya.androidusbio.usbio_services;

import android.os.Binder;

import jp.jidosya.androidusbio.USBIOService;

public class USBIOServiceBinder extends Binder {
    private USBIOService service;

    public USBIOServiceBinder(USBIOService service) {
        this.service = service;
    }

    public USBIOService getService() {
        return this.service;
    }
}
