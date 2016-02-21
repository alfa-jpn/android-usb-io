package jp.jidosya.androidusbio;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.util.Arrays;

import jp.jidosya.androidusbio.usbios.IUSBIOEventListener;
import jp.jidosya.androidusbio.usbios.PortStatus;
import jp.jidosya.androidusbio.usbios.USBIOException;

public class USBIO implements AutoCloseable {
    public static final String ACTION_USB_PERMISSION = "jp.jidosya.androidusbio.USB_PERMISSION";
    public final static int    USB_IO_VENDOR_ID      = 0x1352;
    public final static int    USB_IO_PRODUCT_IDS[]  = {0x0110, 0x0111, 0x0120, 0x0121};
    public final static int    USB_IO_DATA_SIZE      = 64;
    public final static byte   USB_IO_PORT1          = 0x01;
    public final static byte   USB_IO_PORT2          = 0x02;
    public final static byte   USB_IO_ORDER          = 0x20;

    private boolean isAttached;

    private IUSBIOEventListener listener;

    // Running Context;
    private Context context;

    // USB-IO Connection.
    private UsbDeviceConnection usbIOConnection;

    // Interface of USB-IO.
    private UsbInterface usbIOInterface;

    // USBManager
    private UsbManager usbManager;

    public boolean isAttached() { return isAttached; }

    // InitializeClass
    // @param [Context] context The context of Running.
    public USBIO(Context context) {
        this(context, null);
    }

    // InitializeClass
    // @throw [USBIOException] if Not found USBIO.
    //
    // @param [Context]             context  The context of Running.
    // @param [IUSBIOEventListener] listener Event Listener.
    public USBIO(Context context, IUSBIOEventListener listener) throws USBIOException {
        this.context  = context;
        this.listener = listener;

        this.isAttached = false;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        requestUSBDevicePermission(findUSBIO());
    }

    public void close() {
        detachUSBIO();
    }

    public void send(byte port, PortStatus ps) throws USBIOException {
        byte data[] = new byte[USB_IO_DATA_SIZE];
        data[0] = USB_IO_ORDER;
        data[1] = port;
        data[2] = ps.getPort();
        send(data);
    }

    public PortStatus receive(byte port) {
        return receive(port, false);
    }

    public PortStatus receive(byte port, boolean withInitialize) {
        byte data[] = new byte[USB_IO_DATA_SIZE];
        data[0] = USB_IO_ORDER;
        data[1] = port;
        data[2] = (byte)0xFF;

        if (withInitialize) {
            send(data);
        }
        receive(data);

        return new PortStatus(data[port]);
    }

    private UsbDevice findUSBIO() throws USBIOException {
        for(UsbDevice device : usbManager.getDeviceList().values()) {
            if(device.getVendorId() != USB_IO_VENDOR_ID) {
                continue;
            }
            if(Arrays.binarySearch(USB_IO_PRODUCT_IDS, device.getProductId()) < 0) {
                continue;
            }
            return device;
        }
        throw new USBIOException("Not Found USB-IO.");
    }

    private void requestUSBDevicePermission(final UsbDevice device) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    attachUSBIO(device);
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    detachUSBIO();
                }
            }
        }, filter);

        PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(device, intent);
    }

    private void attachUSBIO(UsbDevice device) {
        usbIOConnection = usbManager.openDevice(device);
        if(usbIOConnection != null) {
            usbIOInterface = device.getInterface(0);
            usbIOConnection.claimInterface(usbIOInterface, true);
            isAttached = true;

            if(listener != null) {
                listener.onAttached(this);
            }
        } else {
            if(listener != null) {
                listener.onError(this);
            }
        }
    }

    private void detachUSBIO() {
        isAttached = false;
        if (usbIOConnection != null) {
            if(usbIOInterface != null) {
                usbIOConnection.releaseInterface(usbIOInterface);
                usbIOInterface = null;
            }
            usbIOConnection.close();
            usbIOConnection = null;
        }

        if(listener != null) {
            listener.onDetached(this);
        }
    }

    private void receive(byte[] data) throws USBIOException {
        int size = usbIOConnection.bulkTransfer(usbIOInterface.getEndpoint(0), data, USB_IO_DATA_SIZE, 1000);
        if (size != USB_IO_DATA_SIZE) {
            throw new USBIOException(String.format("Invalid receive data size. expect %d, but %d", USB_IO_DATA_SIZE, size));
        }
    }

    private void send(byte[] data) throws USBIOException {
        int size = usbIOConnection.bulkTransfer(usbIOInterface.getEndpoint(1), data, USB_IO_DATA_SIZE, 1000);
        if (size != USB_IO_DATA_SIZE) {
            throw new USBIOException(String.format("Invalid sent data size. expect %d, but %d", USB_IO_DATA_SIZE, size));
        }
    }
}
