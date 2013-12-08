package com.jiahaoliuliu.nearestrestaurants;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.jiahaoliuliu.nearestrestaurants.interfaces.OnPositionRequestedListener;
import com.jiahaoliuliu.nearestrestaurants.interfaces.OnUpdatePositionListener;

public class NearestRestaurantsListFragment extends SherlockListFragment
	implements OnUpdatePositionListener{

	private static final String LOG_TAG = NearestRestaurantsListFragment.class.getSimpleName();

	// Interfaces
	private OnPositionRequestedListener onPositionRequestedListener;

	private Context context;

	// The user position
	private LatLng myActualPosition;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Check the onPositionRequestedListener
		try {
			onPositionRequestedListener = (OnPositionRequestedListener)activity;
			myActualPosition = onPositionRequestedListener.requestPosition();
		} catch (ClassCastException classCastException) {
			Log.e(LOG_TAG, "The attached activity must implements the OnPositionRequestedListener", classCastException);
		}
		
		this.context = activity;
	}

	@Override  
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,  
	    Bundle savedInstanceState) {
		
		// Set the adapter
		return super.onCreateView(inflater, container, savedInstanceState);
	  }

	@Override
	public void updatePosition(LatLng newPosition) {
		myActualPosition = newPosition;
	}
}
