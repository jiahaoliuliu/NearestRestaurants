package com.jiahaoliuliu.nearestrestaurants;

import java.lang.reflect.Field;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;
import com.jiahaoliuliu.nearestrestaurants.interfaces.OnPositionRequestedListener;
import com.jiahaoliuliu.nearestrestaurants.interfaces.OnUpdatePositionListener;

/**
 * The map fragment class to show the Google Map.
 * This includes the solution for Duplicated Id in the case of switch of Map fragment
 * http://stackoverflow.com/questions/14565460/error-opening-supportmapfragment-for-second-time
 * And the solution for "activity has been destroyed"
 * http://stackoverflow.com/questions/19239175/java-lang-illegalstateexception-activity-has-been-destroyed-using-fragments
 * @author Jiahao Liu
 *
 */
public class NearestRestaurantsMapFragment extends Fragment 
	implements OnUpdatePositionListener {

	private static final String LOG_TAG = NearestRestaurantsMapFragment.class.getSimpleName();

	// Interfaces
	private OnPositionRequestedListener onPositionRequestedListener;

	private static final int ZOOM_ANIMATION_LEVEL = 5;
	private static final int MOST_ZOOM_LEVEL = 1;

	private Context context;

	private SupportMapFragment fragment;
	private GoogleMap map;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    return inflater.inflate(R.layout.map_fragment_layout, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    FragmentManager fm = getChildFragmentManager();
	    fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
	    if (fragment == null) {
	        fragment = SupportMapFragment.newInstance();
	        fm.beginTransaction().replace(R.id.map, fragment).commit();
	    }
	}

	@Override
	public void onResume() {
	    super.onResume();
        map = fragment.getMap();
        //map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
	}

	@Override
	public void onDetach() {
	    super.onDetach();

	    try {
	        Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
	        childFragmentManager.setAccessible(true);
	        childFragmentManager.set(this, null);

	    } catch (NoSuchFieldException e) {
	        throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	    }
	}

	@Override
	public void updatePosition(LatLng newPosition) {
		myActualPosition = newPosition;
	}
}
