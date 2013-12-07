package com.jiahaoliuliu.nearestrestaurants;

import java.util.HashMap;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

public class NearestRestaurantsMapFragment extends Fragment {

	private static final String LOG_TAG = NearestRestaurantsMapFragment.class.getSimpleName();
	
	private static final int ZOOM_ANIMATION_LEVEL = 5;
	private static final int MOST_ZOOM_LEVEL = 1;

	private static View view;
	private Context context;
	private Activity activity;
	private FragmentManager supportFragmentManager;
	
	private GoogleMap googleMap;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		/*
		// Check the implementation
		try {
			listViajerosProvider = (ListViajerosProvider) activity;
		} catch (ClassCastException classCastException) {
			Log.e(LOG_TAG, "The attached class has not implemented ListViajerosProvider. ", classCastException);
			throw new ClassCastException(activity.toString() + " must implement ListViajerosProvider.");
		}
		
		try {
			onUrlReceivedListener = (OnUrlReceivedListener) activity;
		} catch (ClassCastException classCastException) {
			Log.e(LOG_TAG, "The attached class has not implemented OnUrlReceivedListener. ", classCastException);
			throw new ClassCastException(activity.toString() + " must implement OnUrlReceivedListener.");
		}*/

		this.context = activity;
		this.activity = activity;

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    if (view != null) {
	        ViewGroup parent = (ViewGroup) view.getParent();
	        if (parent != null)
	            parent.removeView(view);
	    }

	    try {
	        view = inflater.inflate(R.layout.map_fragment_layout, container, false);
		    int isEnabled = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		    if (isEnabled != ConnectionResult.SUCCESS) {
		        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(isEnabled, activity, 0);
		        if (errorDialog != null) {
	        		errorDialog.show();
		        }
		    } else {

				supportFragmentManager = this.getActivity().getSupportFragmentManager();
				// Get the map
				googleMap = ((SupportMapFragment)supportFragmentManager
						.findFragmentById(R.id.map))
						.getMap();
			}
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    	Log.e(LOG_TAG, "Error inflating the view.", e);
	    }
	    return view;
	}
}
