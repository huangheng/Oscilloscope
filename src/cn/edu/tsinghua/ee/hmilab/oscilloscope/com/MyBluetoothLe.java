package cn.edu.tsinghua.ee.hmilab.oscilloscope.com;

import java.util.UUID;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;


public class MyBluetoothLe extends BluetoothLe
{
	private final static String TAG = "Oscillocope.MyBluetoothLe";
	
	public final static int BLE_CONNECTED = 1;
	public final static int BLE_SERVICE_DISCOVERY = 2;
    public final static int BLE_DISCONNECTED = 3;
	
    public final static UUID UUID_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    
    public final static UUID UUID_DATA_RECEIVE = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_DATA_SEND = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    
    public final static int RECV_TIME_OUT_SHORT	= 2000;		// 2000ms
    public final static int RECV_TIME_OUT_MIDDLE = 5000;	
    public final static int RECV_TIME_OUT_LONG	= 10000;	
    
    public final static int BLE_SEND_DATA_LEN_MAX = 20;
	
	
    private WaitEvent connectEvent = new WaitEvent();
    
    private WaitEvent stateEvent = new WaitEvent();
    
    private WaitEvent sendEvent = new WaitEvent();
    
    private int mBleState = BLE_DISCONNECTED;

	private static MyBluetoothLe mBluetooth = null;
	
	private OnBluetoothCallBack mBleCallBack = null;
	
	public interface OnBluetoothCallBack
	{
		public void OnBluetoothState(int state);
		public void OnReceiveData(byte[] recvData);
	}
	
	private MyBluetoothLe(Context context)
	{
		super(context);
	}
	
	public static synchronized MyBluetoothLe getInstance(Context context) 
	{
        if (mBluetooth == null) 
        {  
        	if(context == null)
        		return null;
        	
        	mBluetooth = new MyBluetoothLe(context);  
        }  
        return mBluetooth; 
    }
	
	public boolean connectDevice(String address, OnBluetoothCallBack bleCallBack)
	{
		mBleCallBack = bleCallBack;
		
		connectEvent.Init();
		if(!super.connectDevice(address, mGattCallback))
			return false;
		
		if(WaitEvent.SUCCESS != connectEvent.waitSignal(RECV_TIME_OUT_MIDDLE))
		{
			disconnectDevice();
			return false;
		}
		
		return true;
	}
	
	public void disconnectDevice()
	{
		mBleState = BLE_DISCONNECTED;
		mBleCallBack = null;
		super.disconnectDevice();
	}
	
	public boolean isConnect()
	{
		return (mBleState == BLE_SERVICE_DISCOVERY);
	}
	
	@SuppressLint("NewApi")
	public boolean wakeUpBle()
	{
		if(mBluetoothGatt == null)
			return false;
		
		BluetoothGattCharacteristic character;

		character = mBluetoothGatt.getService(UUID_SERVICE).getCharacteristic(UUID_DATA_RECEIVE);
		
		stateEvent.Init();					
		if(!setCharacteristicNotification(character, true))
			return false;
		
		if(WaitEvent.SUCCESS != stateEvent.waitSignal(RECV_TIME_OUT_MIDDLE))
			return false;
		
		return true;
	}
	
	@SuppressLint("NewApi")
	public boolean sendData(byte[] data) 
	{
		if(mBluetoothGatt == null)
			return false;
		
		BluetoothGattCharacteristic character;

		character = mBluetoothGatt.getService(UUID_SERVICE).getCharacteristic(UUID_DATA_SEND);
	
		int nCount = data.length/BLE_SEND_DATA_LEN_MAX;
		if(data.length%BLE_SEND_DATA_LEN_MAX != 0)
			nCount++;
		
		byte[] temp;
		for (int i = 0; i < nCount; i++) 
		{
			sendEvent.Init();

			if( (i+1) != nCount)
			{
				temp = new byte[BLE_SEND_DATA_LEN_MAX];
			}
			else
			{
				temp = new byte[data.length-BLE_SEND_DATA_LEN_MAX*i];
			}
			
			for (int j = 0; j < temp.length; j++) 
			{
				temp[j] = data[i*(BLE_SEND_DATA_LEN_MAX)+j];
			}
			
			character.setValue(temp);
			if(!mBluetoothGatt.writeCharacteristic(character))
				return false;
			
			if(WaitEvent.SUCCESS != sendEvent.waitSignal(RECV_TIME_OUT_MIDDLE))
				return false;
		}
		
		return true;
	}
	
	@SuppressLint("NewApi")
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
	{
		@SuppressLint("NewApi")
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState)
		{
			// String intentAction;
			if (newState == BluetoothProfile.STATE_CONNECTED)
			{
				mBleState = BLE_CONNECTED;
				mBluetoothGatt.discoverServices();
				Log.d(TAG, "connect successful " + status);
			}
			else if (newState == BluetoothProfile.STATE_DISCONNECTED)
			{
				mBleState = BLE_DISCONNECTED;
				if(mBleCallBack != null)
					mBleCallBack.OnBluetoothState(mBleState);
				
				Log.d(TAG, "Disconnected from GATT server "+mBleState);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status)
		{
			if(status == BluetoothGatt.GATT_SUCCESS)
				mBleState = BLE_SERVICE_DISCOVERY;
			connectEvent.setSignal(status == BluetoothGatt.GATT_SUCCESS);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status)
		{
			if (status == BluetoothGatt.GATT_SUCCESS)
			{
				
				final byte[] data = characteristic.getValue();
				if (data != null && data.length > 0)
				{
				}
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status)
		{
			if (characteristic.getUuid().equals(UUID_DATA_SEND))
			{
				sendEvent.setSignal(status == BluetoothGatt.GATT_SUCCESS);
			}
			else
			{
				sendEvent.setSignal(false);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic)
		{
			
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0)
			{
				if(mBleCallBack != null)
					mBleCallBack.OnReceiveData(data);
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status)
		{
			if (status == BluetoothGatt.GATT_SUCCESS)
			{
				Log.d(TAG, "Descript success ");
				if(ConvertData.cmpBytes(descriptor.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE))
				{
					stateEvent.setSignal(true);
				}
			}
			else
			{
				stateEvent.setSignal(false);
			}
		}
	};
    
	private class WaitEvent
	{
		public final static int ERROR_OTHER = 2;
		public final static int ERROR_TIME_OUT = 1;
		public final static int SUCCESS = 0;
		
		private Object mSignal;
		private boolean mFlag;
		private int mResult;		
	    
	    private MyThread myThread;

		public WaitEvent()
		{
			mSignal = new Object();
			mFlag = true;
			mResult = SUCCESS;
		}
		public void Init()
		{
			mFlag = true;
			mResult = SUCCESS;
			Log.d(TAG, "Init Event");
		}
		public int waitSignal(int millis)
		{
			myThread = new MyThread();
			myThread.startThread(millis);
			if(!mFlag)
				return mResult;
			
			synchronized (mSignal)
			{
				try
				{
					Log.d(TAG, "waitSignal ");	
					mSignal.wait();
					Log.d(TAG, "waitSignal over");
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			return mResult;
		}
		
		public void setSignal(boolean bSuccess)
		{
			synchronized (mSignal)
			{
				Log.d(TAG, "setSignal");
				mFlag = false;
				if(!bSuccess)
					mResult = ERROR_OTHER;
				if(myThread != null)
					myThread.stopThread();
				mSignal.notify();
			}
		}
		
		private void waitTimeOut()
		{
			Log.d(TAG, "waitTimeOut");
			mResult = ERROR_TIME_OUT;
			setSignal(true);
		}
		
		class MyThread extends Thread
		{
			boolean mThreadAlive = false;
			int mCount = 0;
			int mTotal = 0;
			public void startThread(int millis)
			{
				mTotal = millis/10;
				mCount = 0;
				mThreadAlive = true;
				start();
				Log.d(TAG, "runable start");
			}
			
			public void stopThread()
			{
				Log.d(TAG, "runable stop");
				mThreadAlive = false;
			}
			
			@Override
			public void run()
			{
				while(true)
				{
					
					try
					{
						mCount++;
						Thread.sleep(10);
						
						if(!mThreadAlive)
						{
							return ;
						}
						
						if(mCount > mTotal)		
						{
							waitTimeOut();
							return ;
						}
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			
		}
	}
	
}
