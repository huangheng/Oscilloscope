package cn.edu.tsinghua.ee.hmilab.oscilloscope.com;

public class MyBluetoothException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public static final int ERROR_WAKE_UP_FAILED = 1;
	public static final int ERROR_SEND_FAILED = 2;
	public static final int ERROR_RECEIVE_TIME_OUT = 3;
	public static final int ERROR_RECEIVE_DATA = 4;
	public static final int ERROR_NOT_CONNECT = 5;

	private int mError;
	
	public MyBluetoothException(int error)
	{
		mError = error;
	}
	
	public String getErrorMsg()
	{
		switch (mError)
		{
		case ERROR_WAKE_UP_FAILED:
			return "wake up failed";
			
		case ERROR_SEND_FAILED:
			return "send failed";
			
		case ERROR_RECEIVE_TIME_OUT:
			return "receive time out";

		case ERROR_RECEIVE_DATA:
			return "receive data error";
			
		case ERROR_NOT_CONNECT:
			return "not connect";
					
		default:
			return "unknown error";
		}
	}
}
