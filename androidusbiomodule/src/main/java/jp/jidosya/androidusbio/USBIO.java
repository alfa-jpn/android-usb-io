package jp.jidosya.androidusbio;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by develop on 15/05/02.
 */
public class USBIO implements IUSBEventListener {
    public static final String ACTION_USB_PERMISSION = "jp.jidosya.androidusbio.USB_PERMISSION";
    public final static int    USB_IO_VENDOR_ID      = 0x1352;
    public final static int    USB_IO_PRODUCT_IDS[]  = {0x0110, 0x0111, 0x0120, 0x0121};
    public final static int    USB_IO_DATA_SIZE      = 64;
    public final static byte   USB_IO_PORT1          = 0x01;
    public final static byte   USB_IO_PORT2          = 0x02;
    public final static byte   USB_IO_ORDER          = 0x20;

    private boolean _isAttached;

    private IUSBIOEventListener _listener;

    // Running Context;
    private Context _context;

    // USB-IO Connection.
    private UsbDeviceConnection _usbIOConnection;

    // Interface of USB-IO.
    private UsbInterface _usbIOInterface;

    // USBManager
    private UsbManager _usbManager;

    public boolean isAttached() { return _isAttached; }

    // InitializeClass
    // @param [Context] context The context of Running.
    public USBIO(Context context) {
        _context = context;
        initialize();
    }

    // InitializeClass
    // @param [Context]             context  The context of Running.
    // @param [IUSBIOEventListener] listener Event Listener.
    public USBIO(Context context, IUSBIOEventListener listener) {
        _listener = listener;
        _context = context;
        initialize();
    }

    private void initialize() {
        _isAttached = false;
        _usbManager = (UsbManager)_context.getSystemService(Context.USB_SERVICE);

        UsbDevice device = FindUSBIO();
        if(device != null) {
            requestUSBDevicePermission(device);
        } else {
            Log.e("USB-IO", "Not Found USB-IO");
        }
    }

    public void close() {
        detachUSBIO();
    }

    public boolean send(byte port, PortStatus ps) {
        byte bt[] = new byte[USB_IO_DATA_SIZE];
        bt[0] = USB_IO_ORDER;
        bt[1] = port;
        bt[2] = ps.getPort();

        int sendSize = _usbIOConnection.bulkTransfer(_usbIOInterface.getEndpoint(1), bt, USB_IO_DATA_SIZE, 1000);

        return sendSize == USB_IO_DATA_SIZE;
    }

    public PortStatus receive(byte port) {
        return receive(port, false);
    }

    public PortStatus receive(byte port, boolean withInitialize) {
        byte bt[] = new byte[USB_IO_DATA_SIZE];
        bt[0] = USB_IO_ORDER;
        bt[1] = port;
        bt[2] = (byte)0xFF;

        if (withInitialize) {
            int sendSize = _usbIOConnection.bulkTransfer(_usbIOInterface.getEndpoint(1), bt, USB_IO_DATA_SIZE, 1000);
            if(sendSize != USB_IO_DATA_SIZE) {
                return null;
            }
        }

        int receiveSize = _usbIOConnection.bulkTransfer(_usbIOInterface.getEndpoint(0), bt, USB_IO_DATA_SIZE, 1000);
        if (receiveSize != USB_IO_DATA_SIZE) {
            return null;
        }

        return new PortStatus(bt[port]);
    }

    public void onUSBPermitted(UsbDevice device) {
        attachUSBIO(device);
    }

    public void onUSBDisconnected(UsbDevice device) {
        detachUSBIO();
    }

    private UsbDevice FindUSBIO() {
        for(UsbDevice device : _usbManager.getDeviceList().values()) {
            if(device.getVendorId() != USB_IO_VENDOR_ID) {
                continue;
            }
            if(Arrays.binarySearch(USB_IO_PRODUCT_IDS, device.getProductId()) < 0) {
                continue;
            }
            return device;
        }
        return null;
    }

    private void requestUSBDevicePermission(UsbDevice device) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        _context.registerReceiver(new USBBroadcastReceiver(ACTION_USB_PERMISSION, device, this), filter);

        PendingIntent intent = PendingIntent.getBroadcast(_context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        _usbManager.requestPermission(device, intent);
    }

    private void attachUSBIO(UsbDevice device) {
        _usbIOConnection = _usbManager.openDevice(device);
        if(_usbIOConnection != null) {
            _usbIOInterface = device.getInterface(0);
            _usbIOConnection.claimInterface(_usbIOInterface, true);
            _isAttached = true;

            if(_listener != null) {
                _listener.onUSBIOAttached(this);
            }
            Log.d("USB-IO", "Attache USB-IO: " + device.getProductId());
        } else {
            Log.e("USB-IO", "Can not attache USB-IO: " + device.getProductId());
        }
    }

    private void detachUSBIO() {
        _isAttached = false;
        if (_usbIOConnection != null) {
            if(_usbIOInterface != null) {
                _usbIOConnection.releaseInterface(_usbIOInterface);
                _usbIOInterface = null;
            }
            _usbIOConnection.close();
            _usbIOConnection = null;

            if(_listener != null) {
                _listener.onUSBIODetached(this);
            }
        }
        Log.d("USB-IO", "Detach USB-IO");
    }
}
