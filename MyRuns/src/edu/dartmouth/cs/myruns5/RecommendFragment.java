package edu.dartmouth.cs.myruns5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class RecommendFragment extends PreferenceFragment {
	// All the recommendation strings
	private final String recommend_low = "A UV Index reading of 0 to 2 means low danger from the sun's UV rays for the average person.\n\nRecommendations:\n-Wear sunglasses on bright days.\n-If you burn easily, cover up and use broad spectrum SPF 30+ sunscreen.\n-Watch out for bright surfaces, like sand, water and snow, which reflect UV and increase exposure.";
	private final String recommend_moderate = "A UV Index reading of 3 to 5 means moderate risk of harm from unprotected sun exposure.\n\nRecommendations:\n-Stay in shade near midday when the sun is strongest.\n-If outdoors, wear protective clothing, a wide-brimmed hat, and UV-blocking sunglasses.\n-Generously apply broad spectrum SPF 30+ sunscreen every 2 hours, even on cloudy days, and after swimming or sweating.\n-Watch out for bright surfaces, like sand, water and snow, which reflect UV and increase exposure.";
	private final String recommend_high = "A UV Index reading of 6 to 7 means high risk of harm from unprotected sun exposure. Protection against skin and eye damage is needed.\n\nRecommendations:\n-Reduce time in the sun between 10 a.m. and 4 p.m.\n-If outdoors, seek shade and wear protective clothing, a wide-brimmed hat, and UV-blocking sunglasses.\n-Generously apply broad spectrum SPF 30+ sunscreen every 2 hours, even on cloudy days, and after swimming or sweating. \n-Watch out for bright surfaces, like sand, water and snow, which reflect UV and increase exposure.";
	private final String recommend_veryhigh = "A UV Index reading of 8 to 10 means very high risk of harm from unprotected sun exposure. Take extra precautions because unprotected skin and eyes will be damaged and can burn quickly.\n\nRecommendations:\n-Minimize sun exposure between 10 a.m. and 4 p.m.\n-If outdoors, seek shade and wear protective clothing, a wide-brimmed hat, and UV-blocking sunglasses.\n-Generously apply broad spectrum SPF 30+ sunscreen every 2 hours, even on cloudy days, and after swimming or sweating. \n-Watch out for bright surfaces, like sand, water and snow, which reflect UV and increase exposure.";
	private final String recommend_extreme = "A UV Index reading of 11 or more means extreme risk of harm from unprotected sun exposure. Take all precautions because unprotected skin and eyes can burn in minutes.\n\nRecommendations:\n-Try to avoid sun exposure between 10 a.m. and 4 p.m.\n-If outdoors, seek shade and wear protective clothing, a wide-brimmed hat, and UV-blocking sunglasses.\n-Generously apply broad spectrum SPF 30+ sunscreen every 2 hours, even on cloudy days, and after swimming or sweating.\n-Watch out for bright surfaces, like sand, water and snow, which reflect UV and increase exposure.";
	private int currentUVI;
	private UVIBroadcastReciever reciever;
	private IntentFilter filter;
	private View v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Get current UV index from uviFragment
		currentUVI = ((MainActivity) this.getActivity()).getUVIFragment().getUVI();
		v = inflater.inflate(R.layout.uvi_recommendation, container, false);
		if (v == null)
			System.out.println("View is null!");
		else
			//Set on swipe listener to implement swipe function
			v.setOnTouchListener(new SwipeListener(this.getActivity(), this
					.getFragmentManager(), ((MainActivity) this.getActivity())
					.getUVIFragment(), ((MainActivity) this.getActivity())
					.getChartFragment(), this, 0));

		//update view display
		updateDisplay(v);
		return v;
	}

	private void updateDisplay(View v) {
		TextView uvi = (TextView) v.findViewById(R.id.recommend_uvi);
		EditText text = (EditText) v.findViewById(R.id.recommend_text);
		ImageView hat = (ImageView) v.findViewById(R.id.hat);
		ImageView hide = (ImageView) v.findViewById(R.id.hide);

		uvi.setText(String.valueOf(currentUVI));

		//Based on currentUVI, change recommendation displaying
		switch (currentUVI) {
		case 0:
		case 1:
		case 2:
			text.setText(recommend_low);
			uvi.setTextColor(getResources().getColor(R.color.green));
			break;
		case 3:
		case 4:
		case 5:
			text.setText(recommend_moderate);
			uvi.setTextColor(getResources().getColor(R.color.yellow));
			break;
		case 6:
		case 7:
			text.setText(recommend_high);
			uvi.setTextColor(getResources().getColor(R.color.orange));
			break;
		case 8:
		case 9:
		case 10:
			text.setText(recommend_veryhigh);
			uvi.setTextColor(getResources().getColor(R.color.red));
			break;
		case 11:
			text.setText(recommend_extreme);
			uvi.setTextColor(getResources().getColor(R.color.purple));
			break;
		default:
			text.setText(recommend_extreme);
			uvi.setTextColor(getResources().getColor(R.color.purple));
			break;
		}
		text.setFocusable(false);

		//Change recommendation icons
		if (currentUVI <= 2) {
			hat.setVisibility(View.INVISIBLE);
			hide.setVisibility(View.INVISIBLE);
		} else {
			hat.setVisibility(View.VISIBLE);
			hide.setVisibility(View.VISIBLE);
		}
	}

	// Recieves the current UVI broadcast updates from the Service
	class UVIBroadcastReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			float uvi = arg1.getExtras().getFloat(
					UltravioletIndexService.CURRENT_UV_INDEX);
			currentUVI = (int) uvi;
			updateDisplay(v);
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
