package jp.jidosya.androidusbio;

public class PortStatus {
    private byte _port = 0x00;

    public PortStatus(byte port) {
        _port = port;
    }

    public boolean get(int pin) {
        return ((_port & 0x01 << pin) == 0);
    }

    public void set(int pin, boolean value) {
        byte b = (byte)(0x01 << pin);
        if (value) {
            _port = (byte)(_port & ~b);
        } else {
            _port = (byte)(_port | b);
        }
    }

    public byte getPort() {
        return _port;
    }

    public boolean equals(Object o) {
        if(o != null && o instanceof PortStatus) {
            return((PortStatus) o)._port == _port;
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
