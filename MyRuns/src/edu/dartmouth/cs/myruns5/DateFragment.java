package edu.dartmouth.cs.myruns5;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;

public class DateFragment extends Fragment {

	private String filename, date;
	private View v;
	private Context mContext;
	private int[] data = new int[19];
	private GraphicalView chartView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//filename = ((MainActivity) this.getActivity()).getFileName();
		v = inflater.inflate(R.layout.date_fragment, container, false);
		mContext = this.getActivity();
		Button datePick = (Button) v.findViewById(R.id.date_button);
		datePick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Calendar calendar = Calendar.getInstance();
				DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker datePicker, int year,
							int month, int dayOfMonth) {
						date = (month + 1) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "-" + year;
						System.out.println(date);
						drawChart(date);
					}
				};

				DatePickerDialog dialog = new DatePickerDialog(mContext,
						dateListener, calendar.get(Calendar.YEAR), calendar
								.get(Calendar.MONTH), calendar
								.get(Calendar.DAY_OF_MONTH));

				dialog.show();
			}
		});
		return v;
	}

	private XYMultipleSeriesDataset getBarDemoDataset(String date) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		String info = null, line;
		try {
			FileInputStream inputStream = this.getActivity().openFileInput(
					filename);
			BufferedReader r = new BufferedReader(new InputStreamReader(
					inputStream));
			String total;
			System.out.println("Date is " + date);
			while ((line = r.readLine()) != null) {
				total = new String(line);
				//System.out.println(total);
				System.out.println("Check date is " + total.split("\t")[0]);
				if (total.split("\t")[0].equals(date)) {
					info = total.split("\t")[1];
					break;
				}
			}
			r.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (info == null) {
			System.out.println("No data");
			for (int i = 0; i < 19; i++) {
				XYSeries series = new XYSeries("UVI" + i);
				series.add(i, 0);
				dataset.addSeries(series);
			}
		} else {
			String[] pairs = info.split(" ");
			for (String s : pairs) {
				data[Integer.parseInt(s.split(":")[0])] = Integer.parseInt(s
						.split(":")[1]);
			}
			for (int i = 0; i < 19; i++) {
				XYSeries series = new XYSeries("UVI" + i);
				series.add(i, data[i]);
				dataset.addSeries(series);

				/*
				 * CategorySeries series = new CategorySeries("UVI" + i);
				 * series.set(i, "xxx", data[i]);
				 * dataset.addSeries(series.toXYSeries());
				 */
			}
		}

		return dataset;
	}

	public XYMultipleSeriesRenderer getBarDemoRenderer(int[] data) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		SimpleSeriesRenderer rg = new SimpleSeriesRenderer();
		rg.setColor(Color.parseColor("#008800"));
		SimpleSeriesRenderer ry = new SimpleSeriesRenderer();
		ry.setColor(Color.parseColor("#FFDD33"));
		SimpleSeriesRenderer ro = new SimpleSeriesRenderer();
		ro.setColor(Color.parseColor("#FF6600"));
		SimpleSeriesRenderer rr = new SimpleSeriesRenderer();
		rr.setColor(Color.parseColor("#CC0000"));
		SimpleSeriesRenderer rp = new SimpleSeriesRenderer();
		rp.setColor(Color.parseColor("#6600FF"));

		for (int i = 0; i < data.length; i++) {
			switch (data[i]) {
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

		setChartSettings(renderer);
		return renderer;
	}

	private void setChartSettings(XYMultipleSeriesRenderer renderer) {
		renderer.setChartTitle("Chart demo");
		renderer.setXTitle("Time (Hour)");
		renderer.setYTitle("UV Index Level");
		renderer.setXAxisMin(-5);
		renderer.setXAxisMax(45);
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(12);
		renderer.setBarSpacing(4);
		renderer.setBarWidth(32);
		renderer.setAxisTitleTextSize(30);
		renderer.setChartTitleTextSize(60);
		renderer.setShowLabels(false);
		// renderer.setLabelsTextSize(30);
		renderer.setShowLegend(false);
		// renderer.setLegendTextSize(30);
		renderer.setShowGrid(true);
		renderer.setDisplayValues(true);
		renderer.setZoomEnabled(false, false);
		renderer.setPanEnabled(false, false);
		// renderer.setPanLimits(new double[] { 0, 50, 0, 12 });
		// renderer.setZoomLimits(new double[] { 0, 50, 0, 12 });
	}

	public GraphicalView drawChart(String date) {
		chartView = ChartFactory
				.getBarChartView(getActivity(), getBarDemoDataset(date),
						getBarDemoRenderer(data), Type.DEFAULT);
		LinearLayout layout = (LinearLayout) v
				.findViewById(R.id.date_fragment_container);
		// layout.removeAllViews();
		if (chartView == null) {
			System.out.println("Chart is null!");
			return null;
		}
		if (layout == null) {
			System.out.println("Layout is null!");
			return null;
		}
		layout.removeAllViews();
		layout.addView(chartView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		return chartView;
	}

}
