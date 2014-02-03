package edu.dartmouth.cs.myruns5;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class ChartFragment extends Fragment {

	private GraphicalView chartView;
	private float[] data;
	private UVIBroadcastReciever reciever;
	private IntentFilter filter;
	private View v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.chart_fragment, container, false);
		data = ((MainActivity) this.getActivity()).getUVIFragment().getWebUVI();
		//data = new float[]{1, 2, 4, 4, 7, 10, 11, 10, 9, 6, 5, 3, 2};
		drawChart(v);
		if (v == null)
			System.out.println("View is null!");
		else {
			v.setOnTouchListener(new SwipeListener(this.getActivity(), this
					.getFragmentManager(), ((MainActivity) this.getActivity())
					.getUVIFragment(), this,
					((MainActivity) this.getActivity()).getRecommendFragment(),
					2));
		}
		return v;
	}

	private XYMultipleSeriesDataset getBarDemoDataset() {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		
		if(data == null){
			System.out.println("data is null!");
			return dataset;
		}
		
		XYSeries series = new XYSeries("Web_UVI");
		for (int i = 0; i < data.length; i++) {
			series.add(i + 6, data[i]);
		}
		dataset.addSeries(series);
		
		XYSeries series1;
		for(int i = 0; i < data.length; i++){
			series1 = new XYSeries("Web_UVI" + i);
			series1.add(i + 6, data[i]);
			dataset.addSeries(series1);
		}

		return dataset;
	}

	public XYMultipleSeriesRenderer getBarDemoRenderer(float[] data) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setChartSettings(renderer);
		
		XYSeriesRenderer rg = new XYSeriesRenderer();
		rg.setColor(Color.parseColor("#008800"));
		rg.setPointStyle(PointStyle.CIRCLE);
		rg.setFillPoints(true);
		rg.setDisplayChartValues(true);
		rg.setChartValuesFormat(new DecimalFormat("0"));
		rg.setChartValuesTextAlign(Align.CENTER);
		rg.setChartValuesSpacing(24f);
		rg.setChartValuesTextSize(40);
		rg.setDisplayChartValuesDistance(5);
		rg.setLineWidth(8f);
		
		XYSeriesRenderer ry = new XYSeriesRenderer();
		ry.setColor(Color.parseColor("#FFDD33"));
		ry.setPointStyle(PointStyle.CIRCLE);
		ry.setFillPoints(true);
		
		XYSeriesRenderer ro = new XYSeriesRenderer();
		ro.setColor(Color.parseColor("#FF6600"));
		ro.setPointStyle(PointStyle.CIRCLE);
		ro.setFillPoints(true);
		
		XYSeriesRenderer rr = new XYSeriesRenderer();
		rr.setColor(Color.parseColor("#CC0000"));
		rr.setPointStyle(PointStyle.CIRCLE);
		rr.setFillPoints(true);
		
		XYSeriesRenderer rp = new XYSeriesRenderer();
		rp.setColor(Color.parseColor("#6600FF"));
		rp.setPointStyle(PointStyle.CIRCLE);
		rp.setFillPoints(true);
		
		renderer.addSeriesRenderer(rg);

		for (int i = 0; i < data.length; i++) {
			switch ((int)data[i]) {
			case 0:
			case 1:
			case 2:
				renderer.addSeriesRenderer(rg);
				break;
			case 3:
			case 4:
			case 5:
				renderer.addSeriesRenderer(ry);
				break;
			case 6:
			case 7:
				renderer.addSeriesRenderer(ro);
				break;
			case 8:
			case 9:
			case 10:
				renderer.addSeriesRenderer(rr);
				break;
			case 11:
				renderer.addSeriesRenderer(rp);
				break;
			default:
				renderer.addSeriesRenderer(rp);
				break;
			}
		}

		return renderer;
	}

	private void setChartSettings(XYMultipleSeriesRenderer renderer) {
		SimpleDateFormat sf = new SimpleDateFormat("MMM dd, yyyy");
		renderer.setChartTitle("Hourly UVI Forecast\n" + sf.format(new Date()));
		renderer.setLabelsColor(Color.BLUE);
		renderer.setXTitle("Time (Hour)");
		renderer.setYTitle("UV Index Level");
		renderer.setMargins(new int[]{0, 55, 10, 15});
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.parseColor("#D0E6FF"));
		renderer.setXAxisMin(5);
		renderer.setXAxisMax(19);
		renderer.setXLabels(15);
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(12);
		renderer.setYLabels(15);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setAxisTitleTextSize(30);
		renderer.setChartTitleTextSize(60);
		renderer.setShowLabels(true);
		renderer.setLabelsTextSize(30);
		renderer.setShowLegend(false);
		renderer.setPointSize(15f);
		renderer.setShowGrid(true);
		renderer.setDisplayValues(true);
		renderer.setZoomEnabled(false, false);
		renderer.setPanEnabled(false, false);
		// renderer.setPanLimits(new double[] { 0, 50, 0, 12 });
		// renderer.setZoomLimits(new double[] { 0, 50, 0, 12 });
	}

	public void drawChart(View v) {
		chartView = ChartFactory.getLineChartView(getActivity(),getBarDemoDataset(), getBarDemoRenderer(data));
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.chart_fragment_container);
		
		if (chartView == null) {
			System.out.println("Chart is null!");
			return;
		}
		if (layout == null) {
			System.out.println("Layout is null!");
			return;
		}
		layout.addView(chartView, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		return;
	}
	
	public void updateView(View v){
		drawChart(v);
	}

	// Recieves the current UVI broadcast updates from the Service
	class UVIBroadcastReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			data = arg1.getExtras().getFloatArray(
					UltravioletIndexService.WEB_UVI);
			updateView(v);
		}
	}

	private void registerReciever() {
		filter = new IntentFilter(UltravioletIndexService.CURRENT_UV_INDEX);
		reciever = new UVIBroadcastReciever();
		getActivity().registerReceiver(reciever, filter);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReciever();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (reciever != null) {
			getActivity().unregisterReceiver(reciever);
			reciever = null;
			filter = null;
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (reciever != null) {
			getActivity().unregisterReceiver(reciever);
			reciever = null;
			filter = null;
		}
	}

}
