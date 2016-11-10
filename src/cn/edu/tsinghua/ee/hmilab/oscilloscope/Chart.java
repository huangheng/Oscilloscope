package cn.edu.tsinghua.ee.hmilab.oscilloscope;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;

public class Chart {
	public static final int LINE_CHART = 0x01;
	public static final int BAR_CHART = 0x02;
	
	public static final long MAX_SIZE = 10000000;
	
	public XYMultipleSeriesRenderer multipleSeriesRenderer;
	public GraphicalView mGraphicalView;
	
	private XYMultipleSeriesDataset multipleSeriesDataset;
	private XYSeries mSeries;
	private XYSeriesRenderer mRenderer;
	private Context context;
	private int type;
	
	public Chart(Context context) {
		super();
		this.context = context;
		this.type = LINE_CHART;
	}
	
	public Chart(Context context, int type) {
		super();
		this.context = context;
		this.type = type;
	}
	
	public GraphicalView getGraphicalView() {
		switch (type) {
			case LINE_CHART : mGraphicalView = ChartFactory.getLineChartView(context, 
					multipleSeriesDataset, multipleSeriesRenderer);
				break;
			case BAR_CHART : mGraphicalView = ChartFactory.getBarChartView(context, 
					multipleSeriesDataset, multipleSeriesRenderer, Type.DEFAULT);
				break;
			default : mGraphicalView = null;
		}
		return mGraphicalView;
	}
	
	public void setXYMultipleSeriesDataset(String curveTitle) {
		multipleSeriesDataset = new XYMultipleSeriesDataset();
		mSeries = new XYSeries(curveTitle);
		multipleSeriesDataset.addSeries(mSeries);
	}
	
	public void setXYMultipleSeriesRenderer(double minX, double maxX, double minY, double maxY, 
			String chartTitle, String xTitle, String yTitle, int axeColor,
			int labelColor, int curveColor, int gridColor) {
		multipleSeriesRenderer = new XYMultipleSeriesRenderer();
		if (chartTitle != null) {
			multipleSeriesRenderer.setChartTitle(chartTitle);
		}
		multipleSeriesRenderer.setXTitle(xTitle);
		multipleSeriesRenderer.setYTitle(yTitle);
		multipleSeriesRenderer.setRange(new double[] { minX, maxX, minY, maxY });
		multipleSeriesRenderer.setLabelsColor(labelColor);
		multipleSeriesRenderer.setXLabelsColor(labelColor);
		multipleSeriesRenderer.setYLabelsColor(0, labelColor);
		multipleSeriesRenderer.setXLabels(10);
		multipleSeriesRenderer.setYLabels(10);
		multipleSeriesRenderer.setXLabelsAlign(Align.RIGHT);
		multipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
		multipleSeriesRenderer.setAxisTitleTextSize(20);
		multipleSeriesRenderer.setChartTitleTextSize(20);
		multipleSeriesRenderer.setLabelsTextSize(20);
		multipleSeriesRenderer.setLegendTextSize(20);
		//multipleSeriesRenderer.setPointSize(2f);
		multipleSeriesRenderer.setFitLegend(true);
		multipleSeriesRenderer.setMargins(new int[] { 40, 40, 30, 30 });
		multipleSeriesRenderer.setShowGrid(true);
		multipleSeriesRenderer.setZoomEnabled(true, true);
		multipleSeriesRenderer.setPanEnabled(true, true);
		multipleSeriesRenderer.setAxesColor(axeColor);
		multipleSeriesRenderer.setGridColor(gridColor);
		multipleSeriesRenderer.setBackgroundColor(Color.WHITE);
		multipleSeriesRenderer.setMarginsColor(Color.WHITE);
		multipleSeriesRenderer.setInScroll(true);
		mRenderer = new XYSeriesRenderer();
		mRenderer.setColor(curveColor);
		//mRenderer.setPointStyle(PointStyle.CIRCLE);
		if (type == LINE_CHART) {
			multipleSeriesRenderer.setPointSize(2f);
			mRenderer.setPointStyle(PointStyle.CIRCLE);
		}
		if (type == BAR_CHART) {
			multipleSeriesRenderer.setBarWidth(2.0f);
		}
		multipleSeriesRenderer.addSeriesRenderer(mRenderer);
	}
	public void reset(String curveTitle){
		multipleSeriesDataset.removeSeries(mSeries);
		mSeries = null;
		mSeries = new XYSeries(curveTitle);
		multipleSeriesDataset.addSeries(mSeries);
		multipleSeriesRenderer.setXAxisMax(1000);
		multipleSeriesRenderer.setXAxisMin(0);
		
	}
	public void setXAxis(double min, double max) {
		multipleSeriesRenderer.setXAxisMax(max);
		multipleSeriesRenderer.setXAxisMin(min);
	}
	public void setYAxis(double min, double max) {
		multipleSeriesRenderer.setYAxisMax(max);
		multipleSeriesRenderer.setYAxisMin(min);
	}
	public void add(double x, double y) {
		mSeries.add(x, y);
	}
	public void add(XYSeries mXYSeries) {
		multipleSeriesDataset.removeSeries(mSeries);
		mSeries = mXYSeries;
		multipleSeriesDataset.addSeries(mSeries);
	}
	public int getItemCount() {
		return mSeries.getItemCount();
	}
	public void setChartTitle(String chartTitle) {
		multipleSeriesRenderer.setChartTitle(chartTitle);
	}
	public void repaint() {
		mGraphicalView.repaint();
	}
	public void update() {
		int length = mSeries.getItemCount();
		double range = multipleSeriesRenderer.getXAxisMax() - multipleSeriesRenderer.getXAxisMin();
		if (length > range) {
			multipleSeriesRenderer.setXAxisMax(length);
			multipleSeriesRenderer.setXAxisMin(length-range);
		}
		mGraphicalView.repaint();
	}
		
}
