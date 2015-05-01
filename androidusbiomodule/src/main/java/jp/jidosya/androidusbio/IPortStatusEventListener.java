package jp.jidosya.androidusbio;

import java.util.EventListener;

public interface IPortStatusEventListener extends EventListener {
    public void onUSBIOPortStatusChanged(PortStatus status);
}
