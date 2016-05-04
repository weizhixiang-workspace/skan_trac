package com.tatarinov.BluetoothDataAnalyzer.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tatarinov.BluetoothDataAnalyzer.Activities.MainActivity;
import com.tatarinov.BluetoothDataAnalyzer.GlobalPreferences;
import com.tatarinov.BluetoothDataAnalyzer.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothService {    
    private static final String TAG = BluetoothService.class.getSimpleName();
    private static final int AutoReconnectionTryCount = 3;
    
    private GlobalPreferences _appPreferences;

    private BluetoothAdapter _adapter;
    
    private final Handler _handler;
    
    private int _state;
    
    private int _connectionStatus;
        
    //private AcceptThread _acceptThread;
    private ConnectThread _connectThread;
    private ConnectedThread _connectedThread; 
    
    public boolean isAvailable(){    	    	    	    
    	if(_adapter == null){
    		return false;
    	}
    	return true;    	
    }
       
    public boolean isEnabled(){    	
    	if (_adapter != null && _adapter.isEnabled()){
    		return true;
    	}
    	return false;
    }    	 
            
    public synchronized void connect(String address) {
    	if (_appPreferences.isDebug()) {
    		Log.d(TAG, String.format("connect to: %s", address));
        }
    	
    	closeConnection();
    	
    	if (!BluetoothAdapter.checkBluetoothAddress(address)){
    		return;
    	}    	    
    	    
    	BluetoothDevice device = _adapter.getRemoteDevice(address);
    	
    	_connectionStatus = 0;
    	
    	_connectThread = new ConnectThread(device);
    	_connectThread.start();
         setState(BluetoothConnectionState.STATE_CONNECTING);       
    }
    
    public BluetoothService(Context context, Handler handler){
    	_adapter = BluetoothAdapter.getDefaultAdapter();
        _state = BluetoothConnectionState.STATE_NONE;
        _handler = handler;
        _appPreferences = GlobalPreferences.getInstance(context);
    }
   
    private synchronized void setState(int state) {    	
        if (_appPreferences.isDebug()) {
            Log.d(TAG, String.format("setState() %s -> %s", _state, state));
        }

        _state = state;

        // Give the new state to the Handler so the UI Activity can update
        _handler.obtainMessage(BluetoothMessages.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return _state;
    }
    
    private synchronized void closeConnection(){    
    	if (_connectThread != null) {
            _connectThread.cancel();
            _connectThread = null;
        }

        if (_connectedThread != null) {
            _connectedThread.cancel();
            _connectedThread = null;
        }

        /*if (_acceptThread == null) {
        	_acceptThread.cancel();            
            _acceptThread = null;
        }*/
        setState(BluetoothConnectionState.STATE_NONE);
    }

    public synchronized void resume() {
    	if (_state != BluetoothConnectionState.STATE_NONE){
    		return;
    	}    	    	    
    	start();
    }
    
    private synchronized void start(){
    	//_acceptThread = new AcceptThread();
    	//_acceptThread.start();
    	setState(BluetoothConnectionState.STATE_LISTEN);
    }
    
    public synchronized void stop() {
        if (_appPreferences.isDebug()) {
            Log.d(TAG, "stop");
        }

        closeConnection();        
    }
    
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (_appPreferences.isDebug()) {
            Log.d(TAG, "connected");
        }
                      
        // Start the thread to manage the connection and perform transmissions
        _connectedThread = new ConnectedThread(socket);
        _connectedThread.start();

        // Send the name of the connected device back to the UI Activity
        String devAddress = device.getAddress();
        String devName = device.getName();
            
        Message msg = _handler.obtainMessage(BluetoothMessages.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.kDeviceName, String.format("%s (%s)", devName, devAddress));
        msg.setData(bundle);
        _handler.sendMessage(msg);
        setState(BluetoothConnectionState.STATE_CONNECTED);
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (_state != BluetoothConnectionState.STATE_CONNECTED) return;
            r = _connectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    private void connectionFailed() {    	
        setState(BluetoothConnectionState.STATE_LISTEN);
        // Send a failure message back to the Activity
        Message msg = _handler.obtainMessage(BluetoothMessages.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.kToast, "Unable to connect device");
        msg.setData(bundle);
        _handler.sendMessage(msg);
    }

    private void connectionLost() {    	
    	if (_connectionStatus == 0){
    		this.connectionFailed();
    		return;
    	}

        if (_appPreferences.isAutoReconnect() && _connectionStatus > 1) {
            setState(BluetoothConnectionState.STATE_DISCONNECTED);
            // Send a reconnect message back to the Activity            
            if (_connectionStatus-- == AutoReconnectionTryCount){
            	Message msg = _handler.obtainMessage(BluetoothMessages.MESSAGE_TOAST);
            	Bundle bundle = new Bundle();
                bundle.putString(MainActivity.kToast, "Connection lost, need reconnect!");
                msg.setData(bundle);
                _handler.sendMessage(msg);
            }                        

        } else {
        	_connectionStatus = 0;
            setState(BluetoothConnectionState.STATE_LISTEN);
            // Send a reconnect message back to the Activity
            Message msg = _handler.obtainMessage(BluetoothMessages.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.kToast, "Connection lost");
            msg.setData(bundle);
            _handler.sendMessage(msg);
        }
    }

    private BluetoothSocket getBluetoothSocket(BluetoothDevice device) {
        BluetoothSocket socket = null;
        final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
                socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            } else {
                socket = device.createInsecureRfcommSocketToServiceRecord(SERIAL_UUID);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "listen() failed", e);
        }

        return socket;
    }

    /*private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothSocket mmServerSocket;

        public AcceptThread() {
            mmServerSocket = getBluetoothSocket();
        }

        public void run() {
            if (_appPreferences.isDebug()) {
                Log.d(TAG, "BEGIN mAcceptThread" + this);
            }

            setName("AcceptThread");
            BluetoothSocket socket;
            // Listen to the server socket if we're not connected
            while (_state != BluetoothConnectionState.STATE_CONNECTED) {
                socket = mmServerSocket;
                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (_state) {
                            case BluetoothConnectionState.STATE_LISTEN:
                            case BluetoothConnectionState.STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case BluetoothConnectionState.STATE_NONE:
                            case BluetoothConnectionState.STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }

            if (_appPreferences.isDebug()) {
                Log.i(TAG, "END mAcceptThread");
            }
        }

        public void cancel() {
            if (_appPreferences.isDebug()) {
                Log.d(TAG, "cancel " + this);
            }

            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }*/

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            mmSocket = getBluetoothSocket(mmDevice);
        }

        public synchronized void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            _adapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException io_e) {
                    Log.e(TAG, "unable to close() socket during connection failure", io_e);
                }
                // Start the service over to restart listening mode                 
                _connectThread = null;
                BluetoothService.this.stop();
                BluetoothService.this.start();
                return;
            } 
            _connectThread = null;
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private boolean IsStopped = false;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }    
        
        private boolean dataPackageParse(ArrayList<Integer> buffer){
        	final int kPackageSize = 4;        	        	       
        	boolean isHeader = true;
        	
        	while (!IsStopped) {
        		 try {
        			 int data = mmInStream.read();        			 
        			 if (data == -1)
                         return false;
        			         			      			
        			 if (isHeader){
        				 if (data != SkanTracProtocol.DataHighHeader)       					 
        					 return true;
        				 isHeader = false;
        				 buffer.add(data);
        				 
        			 } else if (buffer.size() < kPackageSize){
        				 buffer.add(data);
        				 
        			 } else {        				         				         		
        				 if (data == Utils.getCRC(buffer)) {
        					 int dataToRead = 32768 - ((buffer.get(2) << 8) | buffer.get(3));        					 
        					 _connectionStatus = AutoReconnectionTryCount;
        					 _handler.obtainMessage(BluetoothMessages.MESSAGE_READ_DATA, 
        							 dataToRead, -1).sendToTarget();        					 
        				 }
        				 return true;
        			 }
        			 
        		 } catch (IOException e) {
                     if (!IsStopped) {
                         connectionLost();
                     }        
                     return false;
                 }               
        	}
        	return !IsStopped;       
        }
        
        private boolean chargePackageParse(ArrayList<Integer> buffer){
        	final int kPackageSize = 4;        	        	       
        	boolean isHeader = true;
        	
        	while (!IsStopped) {
          		 try {
          			 int data = mmInStream.read();        			 
          			 if (data == -1)
                           return false;
          			         			      			
          			 if (isHeader){
          				 if (data != SkanTracProtocol.ChargeHighHeader)       					 
          					 return true;
          				 isHeader = false;
          				 buffer.add(data);
          				 
          			 } else if (buffer.size() < kPackageSize){
          				 buffer.add(data);
          				 
          			 } else {        				                  				 
          				 if (data == Utils.getCRC(buffer)) {
          					 int dataToRead = ((buffer.get(2) << 8) | buffer.get(3));        					 
          					 _connectionStatus = AutoReconnectionTryCount;
          					_handler.obtainMessage(BluetoothMessages.MESSAGE_READ_CHARGE, 
          							 dataToRead, -1).sendToTarget();        					 
          				 }
          				 return true;
          			 }
          			 
          		 } catch (IOException e) {
                       if (!IsStopped) {
                           connectionLost();
                       }        
                       return false;
                   }               
          	}
        	return !IsStopped;        	        	  
        }
        
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            ArrayList<Integer> buffer = new ArrayList<Integer>();

            while (!IsStopped) {
                try {
                    int data = mmInStream.read();
                    if (data == -1)
                        return;
                    
                    if (data == SkanTracProtocol.DataLowHeader){
                    	buffer.add(data);
                    	if (!dataPackageParse(buffer))
                    		break;
                    	buffer.clear();
                    }
                    	
                    else if (data == SkanTracProtocol.ChargeLowHeader){                    	
                    	buffer.add(data);                    	
                    	if (!chargePackageParse(buffer))
                    		break;
                    	buffer.clear();                    	
                    }                                    

                } catch (IOException e) {
                    if (!IsStopped) {
                        connectionLost();
                    }
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                _handler.obtainMessage(BluetoothMessages.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                IsStopped = true;
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }               
    }
    
    class SkanTracProtocol {    	
    	public static final int DataLowHeader = 162;
    	public static final int DataHighHeader = 42;
    	
    	public static final int ChargeLowHeader = 123;
    	public static final int ChargeHighHeader = 32;
    }
}


