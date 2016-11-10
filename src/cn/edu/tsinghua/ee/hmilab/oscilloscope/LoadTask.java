package cn.edu.tsinghua.ee.hmilab.oscilloscope;

import java.io.File;
import java.io.RandomAccessFile;

import org.achartengine.model.XYSeries;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadTask extends AsyncTask<String, Integer, XYSeries>{
	private static final String TAG = "Oscilloscope.LoadTask";
	private Context context = null;
	private TextView mTextView = null;
	private Dialog dialog = null;
	private ProgressBar progressBar = null;
	private boolean isRun = true;
	public TaskFinishListener taskFinishListener;
	
	
	public static interface TaskFinishListener {
		void TaskFinish(XYSeries arg0, boolean arg1);
	}
	public void setTaskFinishListener(TaskFinishListener taskFinishListener) {
		this.taskFinishListener = taskFinishListener;
	}
	LoadTask(Context context) {
		this.context = context;
	}
	@SuppressLint({ "NewApi", "InflateParams" })
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		LayoutInflater mLayoutInflater = LayoutInflater.from(context);
		View view = mLayoutInflater.inflate(R.layout.dialog_load, null);
		dialog = new AlertDialog.Builder(context).
				setTitle(R.string.app_name).
				setView(view).
				setIcon(R.drawable.ic_launcher).
				setNegativeButton("Cancel", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						isRun = false;
						Log.d(TAG, "cancel");
						
					}
				}).create();
		//dialog.setTitle(R.string.app_name);
		//dialog.setContentView(R.layout.dialog_load);
		dialog.setCancelable(false);
		mTextView = (TextView)view.findViewById(R.id.progress);
		progressBar = (ProgressBar)view.findViewById(R.id.progressbar);
		dialog.show();
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(XYSeries result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}
	@SuppressLint("NewApi")
	@Override
	protected void onCancelled(XYSeries result) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onCancelled");
		super.onCancelled(result);
	}
	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		progressBar.setProgress(values[0]);
		mTextView.setText(values[0] + "%");
		super.onProgressUpdate(values);
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
	}

	@Override
	protected XYSeries doInBackground(String... params) {
		// TODO Auto-generated method stub
		XYSeries mSeries = new XYSeries("curve");
		try {
			File file = new File(params[0]);
			Log.d(TAG, "load:" + params[0]);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			long filelen = raf.length();
			long length = 0;
			int nowprg = 0, lastprg = 0;
			String str = raf.readLine();
			int count = 0;
			while (str != null && isRun) {
				length = length + str.length();
				nowprg = (int)(100 * (double)length / (double) filelen) + 2;
				if (nowprg - lastprg >= 1) {
					publishProgress(nowprg);
					lastprg = nowprg;
				}
				double s = 0.0;
				String[] as = str.split(" ");
				s = Double.valueOf(as[1]);
				mSeries.add(count, s);
				count ++;
				str = raf.readLine();
			}
			Log.d(TAG, "load file finish. " + mSeries.getItemCount());
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		dialog.dismiss();
		if (isRun) {
			publishProgress(100);
			taskFinishListener.TaskFinish(mSeries, true);
		} else {
			taskFinishListener.TaskFinish(mSeries, false);
		}
		return mSeries;
	}

}
