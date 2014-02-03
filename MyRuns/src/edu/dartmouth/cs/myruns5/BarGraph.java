package edu.dartmouth.cs.myruns5;

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class BarGraph extends Activity {

	private ArrayList<Integer> selectedItems; // Used to figure which index of fbFriends to access
	private ArrayList<ParseUser> fbFriends; // Stores all Facebook Friends that user is Friends with
	private ParseUser currentUser; 
	
	//deep red, dark blue, dark green, magenta, brown
	private String[] colors = {"#911000", "#003399", "#006600", "#CC00CC", "#663300"};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//setContentView(R.layout.fragment_friends);
		
		//Initialize Parse for grabbing Facebook friends
		Parse.initialize(this, "2zU6YnzC8DLSMJFuAOiLNr3MD6X0ryG52mZsxoo0", "m4rlzlSWyUvgcEkNULlVqRBlsX2iGRilskltCqYG");
		ParseFacebookUtils.initialize(((Integer)R.string.app_id).toString());
		currentUser = ParseUser.getCurrentUser();
		fbFriends = (ArrayList<ParseUser>)currentUser.get("fb_friends");
		
		Intent intent = getIntent();
		selectedItems = (ArrayList<Integer>) intent.getSerializableExtra("selectedItems");

		//Don't use Serializable for ParseUser objects due to errors
		//fbFriends = (ArrayList<ParseUser>) intent.getSerializableExtra("fbFriends");
		
		displayChart();
	}
	
	public void displayChart() {	
		int numFriends = selectedItems.size();
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		
		//Initialize different series for user/friends
		XYSeries you = new XYSeries("You");
        XYSeries friend2 = new XYSeries("Friend2");
        XYSeries friend3 = new XYSeries("Friend3");
        XYSeries friend4 = new XYSeries("Friend4");
        XYSeries friend5 = new XYSeries("Friend5");
        
        //Set UV data points based on selected friends
        you.add(1,currentUser.getInt("UVI"));
        friend2.add(2,numFriends>0 ? fbFriends.get(selectedItems.get(0)).getInt("UVI") : 0);
        friend3.add(3,numFriends>1 ? fbFriends.get(selectedItems.get(1)).getInt("UVI") : 0);
        friend4.add(4,numFriends>2 ? fbFriends.get(selectedItems.get(2)).getInt("UVI") : 0);
        friend5.add(5,numFriends>3 ? fbFriends.get(selectedItems.get(3)).getInt("UVI") : 0);
        
        //Add series to dataset
        dataset.addSeries(you);
        dataset.addSeries(friend2);
        dataset.addSeries(friend3);
        dataset.addSeries(friend4);
        dataset.addSeries(friend5);
		
        //Set up the chart design
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.setChartTitle("Relative UV Exposure");
 		mRenderer.setXTitle("Facebook Friends");
 		mRenderer.setYTitle("UV Exposure");
 		mRenderer.setAxesColor(Color.BLACK);
 		mRenderer.setLabelsColor(Color.RED);
 	    mRenderer.setApplyBackgroundColor(true);
 	    mRenderer.setBackgroundColor(Color.WHITE);
 	    mRenderer.setMarginsColor(Color.WHITE);
 	    mRenderer.setBarSpacing(-0.5);
       	mRenderer.setMargins(new int[] {20, 30, 15, 0});
       	mRenderer.setShowLegend(false);
       	mRenderer.setPanEnabled(false);
       	mRenderer.setZoomEnabled(false, false);
       	mRenderer.setAxisTitleTextSize(24);
       	mRenderer.setChartTitleTextSize(32);
       	mRenderer.setLabelsTextSize(16);
       	mRenderer.setLegendTextSize(16);
       	mRenderer.setBarWidth(50);
       	mRenderer.setXAxisMin(-1.5);
       	mRenderer.setXAxisMax(7.5);
       	mRenderer.setYAxisMin(0);
       	mRenderer.setYAxisMax(200);
       	mRenderer.setYLabelsAlign(Align.RIGHT);
       	mRenderer.setXLabelsColor(Color.BLACK);
       	mRenderer.setYLabelsColor(0, Color.BLACK);
       	mRenderer.setXLabels(0);
       	mRenderer.addXTextLabel(-0.5, "You");
       	mRenderer.addXTextLabel(1.25, numFriends>0 ? fbFriends.get(selectedItems.get(0)).getString("name") : "");
       	mRenderer.addXTextLabel(3, numFriends>1 ? fbFriends.get(selectedItems.get(1)).getString("name") : "");
       	mRenderer.addXTextLabel(4.75, numFriends>2 ? fbFriends.get(selectedItems.get(2)).getString("name") : "");
       	mRenderer.addXTextLabel(6.5, numFriends>3 ? fbFriends.get(selectedItems.get(3)).getString("name") : "");
	    
	    //Customize bar 1
		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setColor(Color.parseColor(colors[0])); //deep red
		renderer.setDisplayChartValues(true);
	    renderer.setChartValuesTextSize(24);
	    mRenderer.addSeriesRenderer(renderer);
	  
	    //Customize bar 2
	    XYSeriesRenderer renderer2 = new XYSeriesRenderer();
	    renderer2.setColor(Color.parseColor(colors[1])); //dark blue
	    renderer2.setDisplayChartValues(true);
	    renderer2.setChartValuesTextSize(24);
	    mRenderer.addSeriesRenderer(renderer2);

	    //Customize bar 3
	    XYSeriesRenderer renderer3 = new XYSeriesRenderer();
	    renderer3.setColor(Color.parseColor(colors[2])); //dark green
	    renderer3.setDisplayChartValues(true);
	    renderer3.setChartValuesTextSize(24);
	    mRenderer.addSeriesRenderer(renderer3);

	    //Customize bar 4
	    XYSeriesRenderer renderer4 = new XYSeriesRenderer();
	    renderer4.setColor(Color.parseColor(colors[3])); //magenta
	    renderer4.setDisplayChartValues(true);
	    renderer4.setChartValuesTextSize(24);
	    mRenderer.addSeriesRenderer(renderer4);

		//Customize bar 5
	    XYSeriesRenderer renderer5 = new XYSeriesRenderer();
	    renderer5.setColor(Color.parseColor(colors[4])); //brown
	    renderer5.setDisplayChartValues(true);
	    renderer5.setChartValuesTextSize(24);
	    mRenderer.addSeriesRenderer(renderer5);
	    
		Intent intent = ChartFactory.getBarChartIntent(this, dataset,mRenderer, Type.DEFAULT);
		startActivity(intent);
	}
}
