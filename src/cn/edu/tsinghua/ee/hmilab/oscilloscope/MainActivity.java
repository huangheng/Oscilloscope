package cn.edu.tsinghua.ee.hmilab.oscilloscope;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.GraphicalView;
import org.achartengine.model.XYSeries;


import cn.edu.tsinghua.ee.hmilab.oscilloscope.com.MyBluetoothLe;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = "Oscillosope.MainActivity";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    
	private static final int FILE_SELECTT_CODE = 0x01;
	
	private static final int MSG_UPDATE = 0x01;
	private static final int MSG_LOAD = 0x02;
	private static final int MSG_SUSPEND = 0x03;
	private static final int MSG_RESUME = 0x04;
	
    public static Chart mChart = null;
	
    private String mDeviceName = null;
    private String mDeviceAddress = null;
    private MyBluetoothLe mble;
    
    private Handler handler = null;
    private Timer timer = new Timer();
    private TimerTask task = null;
    
    private PipedInputStream in = new PipedInputStream();
    private PipedOutputStream out = new PipedOutputStream();
    
    private final SaveRunner save = new SaveRunner(in);
    //private final Send send = new Send(out);
    private final Thread saveThread = new Thread(save);
    //private final Thread sendThread = new Thread(send);
    private String filepath = null;
    
    private boolean isRun = true;
    private boolean isPaint = true;
    private boolean isPlot = true;
    private boolean isRecord = true;
    private int count = 0;

    
    private Context context = null;
    private Button mButton = null;
    private Button loadButton = null;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		
		final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Log.d(TAG, mDeviceName +" " + mDeviceAddress);
        mble = MyBluetoothLe.getInstance(getApplicationContext());
        
		
		mChart = new Chart(this, Chart.LINE_CHART);
		mChart.setXYMultipleSeriesDataset("curve");
        mChart.setXYMultipleSeriesRenderer(0, 1000, -100, 100, "Signal Strength", "x", "y",
        		Color.BLACK, Color.BLACK, Color.RED, Color.BLACK);
        GraphicalView chartview = mChart.getGraphicalView();
        LinearLayout layout = (LinearLayout)findViewById(R.id.chart);
        
        WindowManager wm = this.getWindowManager();
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        Log.d(TAG, point.x + " " + point.y);
        layout.addView(chartview, new LayoutParams(point.x - 100, 700));
        
        
        try {
			in.connect(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        saveThread.start();
        //sendThread.start();
        
        
        mButton = (Button)findViewById(R.id.button);
        mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				if (isRun) {
					Message msg = new Message();
					msg.what = MSG_SUSPEND;
					handler.sendMessage(msg);
					
					isRun = false;
				} else {
					Message msg = new Message();
					msg.what = MSG_RESUME;
					handler.sendMessage(msg);
					
					isRun = true;
				}
			}
		});
        
        loadButton = (Button)findViewById(R.id.load);
        loadButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Message msg = new Message();
				msg.what = MSG_SUSPEND;
				handler.sendMessage(msg);
				
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				try {
					startActivityForResult(Intent.createChooser(intent, "Load a file"), FILE_SELECTT_CODE);
				} catch (android.content.ActivityNotFoundException e) {
					e.printStackTrace();
					Toast.makeText(getBaseContext(), "Please install a File Manager", Toast.LENGTH_LONG).show();
				}
				
				
			}
		});
       
        
        handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case MSG_UPDATE:
					if (isPaint) {
						if (mChart.getItemCount() > Chart.MAX_SIZE) {
							mChart.reset("curve");
							count = 0;//send.reset();
						}
						mChart.update();
					}
					break;
				case MSG_LOAD:
					XYSeries mSeries = (XYSeries)msg.obj;
					mChart.add(mSeries);
					mChart.setChartTitle(filepath);
					mChart.setXAxis(0, mSeries.getItemCount());
					mChart.setYAxis(-10, 110);
					mChart.repaint();
					Toast.makeText(context, "load file successfully!", Toast.LENGTH_LONG).show();
					Log.d(TAG,"load");
					break;
				case MSG_SUSPEND:
					//save.suspend();
					//send.isRun = false;
					isPlot = false;//send.isPlot = false;
					isPaint = false;
					isRun = false;
					mButton.setText("resume");
					Log.d(TAG, "suspend");
					break;
				case MSG_RESUME:
					//save.resume();
					//send.isRun = true;
					isPlot = true;//send.isPlot = true;
					isPaint = true;
					count = 0;//send.reset();
					isRun = true;
					mButton.setText("suspend");
					mChart.reset("curve");
					mChart.setChartTitle("Signal Strength");
					mChart.repaint();
					Log.d(TAG, "resume");
					break;
					
				}
				
				super.handleMessage(msg);
			}
        };
        
        task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message message = new Message();
	    		message.what = MSG_UPDATE;
	    		handler.sendMessage(message);
			}
        	
        };
        
        timer.schedule(task, 0, 30);
        
        new Handler().post(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int i;
				for (i = 0; i < 5; i ++) {
					Log.d(TAG, "connecting...");
					if (mble.connectDevice(mDeviceAddress, bleCallBack)) {
						break;
					}
				}
				if (i == 5) {
					Log.d(TAG, "connect fail");
					return;
				}
				Log.d(TAG, "conneced");
				if (mble.wakeUpBle()) {
					Log.d(TAG, "wakeUpBle = true");
				} else {
					Log.d(TAG, "wakeUpBle = false");
				}
			}
        });
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECTT_CODE:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				filepath = Util.getPath(this, uri);
                LoadTask loadTask = new LoadTask(context);
				try {
					loadTask.execute(filepath);
					loadTask.setTaskFinishListener(new LoadTask.TaskFinishListener() {
						
						@Override
						public void TaskFinish(XYSeries arg0, boolean arg1) {
							// TODO Auto-generated method stub
							if(arg1 == true) {
								Message msg = new Message();
								msg.what = MSG_LOAD;
								msg.obj = arg0;
								handler.sendMessage(msg);
							}
							Log.d(TAG, "finish:" + arg1);
							
						}
					});
				} catch (Exception e) {
        			e.printStackTrace();
        		}
				Toast.makeText(context, "loading file:" + filepath, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	MyBluetoothLe.OnBluetoothCallBack bleCallBack = new MyBluetoothLe.OnBluetoothCallBack() {

		@Override
		public void OnBluetoothState(int state) {
			// TODO Auto-generated method stub
			if(state == MyBluetoothLe.BLE_DISCONNECTED) {
				onBackPressed();
			}
			
		}

		@Override
		public void OnReceiveData(byte[] recvData) {
			// TODO Auto-generated method stub
			for (int i = 0; i < recvData.length; i ++) {
				Log.d(TAG, "receive data:" + recvData[i]);
				if (isPlot) {
					mChart.add(count++, recvData[i]);
				}
				String str = "" + System.currentTimeMillis() + " " + recvData[i] + "\n";
				if (isRecord) {
					try {
						out.write(str.getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}
			
		}
		
	};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
