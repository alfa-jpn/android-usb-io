package jp.jidosya.androidusbio.usbios;

public class PortStatus {
    private byte port = 0x00;

    public PortStatus(byte port) {
        this.port = port;
    }

    public boolean get(int pin) {
        return ((port & 0x01 << pin) == 0);
    }

    public void set(int pin, boolean value) {
        byte b = (byte)(0x01 << pin);
        if (value) {
            port = (byte)(port & ~b);
        } else {
            port = (byte)(port | b);
        }
    }

    public byte getPort() {
        return port;
    }

    public boolean equals(Object o) {
        if(o != null && o instanceof PortStatus) {
            return((PortStatus) o).port == port;
        }
        return false;
    }

    public String toString() {
        String s = "";
        for(int i = 0; i < 8; ++i) {
            if (get(i)) {
                s += "o";
            } else {
                s += "x";
            }
        }
        return s;
    }
}
