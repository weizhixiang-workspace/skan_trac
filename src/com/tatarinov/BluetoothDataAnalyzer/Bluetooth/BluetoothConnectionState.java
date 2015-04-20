package com.tatarinov.BluetoothDataAnalyzer.Bluetooth;

public final class   BluetoothConnectionState {
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_DISCONNECTED = 4;
}
