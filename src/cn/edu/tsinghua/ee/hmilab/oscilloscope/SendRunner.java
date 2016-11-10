package cn.edu.tsinghua.ee.hmilab.oscilloscope;

import java.io.IOException;
import java.io.PipedOutputStream;

import android.util.Log;

public class SendRunner implements Runnable {
	private static final String TAG = "Oscillosope.Send";
	
	private PipedOutputStream out;
	public boolean isRun = true;
	public boolean isRecord = true;
	public boolean isPlot = true;
	public int count = 0;
	
	SendRunner(PipedOutputStream out) {
		this.out = out;
	}
    public void reset() {
    	count = 0;
    }
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.d(TAG,"send thread:" + Thread.currentThread().getId());
		while (true) {
			double s = 100*Math.random();
			if (isPlot) {
				MainActivity.mChart.add(count++, s);
			}
			String str = "" + System.currentTimeMillis() + " " + s + "\n";
			try {
				if (isRecord) {
					out.write(str.getBytes());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			while(!isRun) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		/*
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
	}
	

}
