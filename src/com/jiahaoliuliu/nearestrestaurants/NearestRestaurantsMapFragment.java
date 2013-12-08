package com.jiahaoliuliu.nearestrestaurants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.jiahaoliuliu.nearestrestaurants.interfaces.OnPositionRequestedListener;
import com.jiahaoliuliu.nearestrestaurants.interfaces.OnUpdatePositionListener;
import com.jiahaoliuliu.nearestrestaurants.interfaces.RequestRestaurantsCallback;
import com.jiahaoliuliu.nearestrestaurants.models.Restaurant;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;
import com.jiahaoliuliu.nearestrestaurants.session.Session;

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

	// Google Map
	private GoogleMap googleMap;
	private boolean isGoogleMapValid = false;
	private static final int ZOOM_ANIMATION_LEVEL = 5;
	private static final int MOST_ZOOM_LEVEL = 1;
	private static final int DEFAULT_ZOOM_LEVEL = 12;
	private Marker userActualPositionMarker;
	// The list of the restaurants
	private List<Marker> restaurantMarkers;

	private Context context;
	private Session session;

	private SupportMapFragment fragment;

	// The user position
	private LatLng myActualPosition;
	private boolean positionSetAtFirstTime = false;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Check the onPositionRequestedListener
		try {
			onPositionRequestedListener = (OnPositionRequestedListener)activity;
			myActualPosition = onPositionRequestedListener.requestPosition();
			if (myActualPosition != null) {
				onPositionUpdated();
			}
		} catch (ClassCastException classCastException) {
			Log.e(LOG_TAG, "The attached activity must implements the OnPositionRequestedListener", classCastException);
		}
		
		this.context = activity;
		session = Session.getCurrentSession(context);
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
	    googleMap = fragment.getMap();
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
		onPositionUpdated();
	}
	
	private void onPositionUpdated() {
		drawUsersNewPositionOnMaps();
		updateRestaurants();
	}
	
	private void drawUsersNewPositionOnMaps() {
		if (myActualPosition == null) {
			Log.e(LOG_TAG, "Trying to draw the users new position when her position is null");
			return;
		}

		// If the market position is the user actual position, exit
		if (userActualPositionMarker != null && userActualPositionMarker.getPosition().equals(myActualPosition)) {
			Log.w(LOG_TAG, "The new position is the same as the old one: " + myActualPosition.latitude + " ," + myActualPosition.longitude);
			return;
		}

		// Remove the actual marker, if exists
		if (userActualPositionMarker != null) {
			userActualPositionMarker.remove();
		}

		userActualPositionMarker = googleMap.addMarker(new MarkerOptions()
				.position(myActualPosition)
				.title(getResources().getString(R.string.users_position))
				);
		
		// Move the map to the user's position if it is the first time that the app
		// get her position
		if (positionSetAtFirstTime) {
			// Move the map to the users position
			googleMap.animateCamera(
					CameraUpdateFactory.newLatLngZoom(myActualPosition, DEFAULT_ZOOM_LEVEL));
			positionSetAtFirstTime = false;
		}
	}

	/**
	 * Update the list of the restaurants based on the position of the user
	 */
	private void updateRestaurants() {
		if (myActualPosition == null) {
			Log.e(LOG_TAG, "Trying to update the list of the restaurants when the position of the user is unknown.");
			return;
		}

		session.getRestaurantsNearby(myActualPosition, new RequestRestaurantsCallback() {
			
			@Override
			public void done(List<Restaurant> restaurants, String errorMessage,
					RequestStatus requestStatus) {
				if (!ErrorHandler.isError(requestStatus)) {
					Log.v(LOG_TAG, "List of the restaurants returned correctly");

					// Remove any previous markers
					if (restaurantMarkers != null) {
						for (Marker marker : restaurantMarkers) {
							marker.remove();
						}
					}

					restaurantMarkers = new ArrayList<Marker>();
					for (Restaurant restaurant: restaurants) {
						Log.v(LOG_TAG, "Restaurant returned " + restaurant.toString());
						
						if (restaurant.getPosition() == null) {
							Log.w(LOG_TAG, "The position of the restaurant is unknown " + restaurant);
							continue;
						}
						
						Marker marker = googleMap.addMarker(
								new MarkerOptions()
									.title(restaurant.getName())
									.position(restaurant.getPosition())
									// Use different color for the icon of the restaurant
									.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
								);
						restaurantMarkers.add(marker);
					}
				} else {
					Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}
