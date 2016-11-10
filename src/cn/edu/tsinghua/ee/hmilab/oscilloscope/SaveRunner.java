package cn.edu.tsinghua.ee.hmilab.oscilloscope;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.util.Log;


public class SaveRunner implements Runnable {
	private static final String TAG = "Oscillosope.Save";
	private static final long MAX_LENGTH = 1000000; 
	
	enum State {
		STOP, RUN, SUSPEND
	}
	
	private PipedInputStream in;
	private volatile State state = State.RUN;
	
	private byte[] buffer = new byte[1024];
	private RandomAccessFile raf = null;
	private File file = null;
	private long filelen = 0;
	
	SaveRunner(PipedInputStream in) {
		this.in = in;
	}
	public synchronized void shutdown() {
		state = State.STOP;
	}
	
	public synchronized void suspend() {
		state = State.SUSPEND;
	}
	
	public synchronized void resume() {
		state = State.RUN;
	}
	
	public synchronized boolean isStop() {
		return state == State.STOP;
	}
	@SuppressLint({ "SimpleDateFormat", "SdCardPath" })
	private void init() {
		try {
			String path;
			path = "/sdcard/HMILab";
			file = new File(path);
			if (!file.exists()) {
				file.mkdir();
				Log.d(TAG, "mkdir:/sdcard/HMILab");
			}
			path = "/sdcard/HMILab/Data";
			file = new File(path);
			if (!file.exists()) {
				file.mkdir();
				Log.d(TAG, "mkdir:/sdcard/HMILab/Data");
			}
		
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String strDate = formatter.format(date);
			String filename = "/sdcard/HMILab/Data/" + "data_" + strDate + ".txt";
			file = new File(filename);
			if (!file.exists()) {
				file.createNewFile();
				Log.d(TAG, "create file:" + filename);
			}
			raf = new RandomAccessFile(file, "rw");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (state != State.STOP){
			while(state == State.SUSPEND){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} 
			init();
			filelen = 0;
			while (state == State.RUN && filelen < MAX_LENGTH) {
				try {
					int len = in.read(buffer);
					if (len > 0) {
						String str = new String(buffer, 0, len);
						raf.write(str.getBytes());
					} else {
						Log.d(TAG, "len == 0, filelen=" + filelen);
					}
					
					filelen = raf.length();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.d(TAG, "filelen=" + filelen);
			try {
				raf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
