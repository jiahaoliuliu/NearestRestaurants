package com.jiahaoliuliu.nearestrestaurants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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

    private static final int DEFAULT_ZOOM_LEVEL = 12;
    private Marker userActualPositionMarker;
    // The list of the restaurants markers
    private List<Marker> restaurantMarkers;
    private List<Restaurant> restaurants;

    private Context context;
    private Session session;

    private SupportMapFragment fragment;

    // The user position
    private LatLng myActualPosition;
    private boolean isUserPositionSet = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Check the onPositionRequestedListener
        try {
            onPositionRequestedListener = (OnPositionRequestedListener)activity;
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
            Log.v(LOG_TAG, "The fragment was null. Retrieving a new support map fragment");
            
            // If the users position is known at this point, create the map with the new
            // position
            myActualPosition = onPositionRequestedListener.requestPosition();
            if (myActualPosition != null) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myActualPosition)
                .zoom(DEFAULT_ZOOM_LEVEL)
                .build();
                GoogleMapOptions googleOptions = new GoogleMapOptions()
                    .camera(cameraPosition);
                fragment = SupportMapFragment.newInstance(googleOptions);
                isUserPositionSet = true;
            } else {
                fragment = SupportMapFragment.newInstance();
            }
            
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        googleMap = fragment.getMap();
        isGoogleMapValid = true;
        myActualPosition = onPositionRequestedListener.requestPosition();
        if (myActualPosition != null) {
            onPositionUpdated();
        }
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
    
    /**
     * Method invoked when the user position has been updated.
     * If the actual google map exists and it is valid, draw the users position
     * on the map and update the list of restaurants.
     */
    private void onPositionUpdated() {
        // Update the user position and the restaurants only
        // when the google map is valid
        if (isGoogleMapValid) {
            drawUsersNewPositionOnMaps();
            updateRestaurants();
        }
    }

    /**
     * Draw the position of the user on the map.
     * If the user position was drawn, remove it.
     */
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

        // Check if the users position has been set before
        // if not, move the map to such position with default zoom
        if (!isUserPositionSet) {
            googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            myActualPosition, DEFAULT_ZOOM_LEVEL));
            isUserPositionSet = true;
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
            public void done(List<Restaurant> newRestaurants,
                             String nextPageToken,
                             String errorMessage,
                             RequestStatus requestStatus) {
                if (!ErrorHandler.isError(requestStatus)) {
                    Log.v(LOG_TAG, "List of the restaurants returned correctly");
                    restaurants = newRestaurants;
                	drawRestaurantsOnTheMap();
                	if (nextPageToken != null && !nextPageToken.equalsIgnoreCase("")) {
                		getMoreRestaurants(nextPageToken);
                	}
                } else {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();

                    // If there is any error about Internet connection but the list of
                    // restaurants has been retrieved offline, draw them on the map
                    if (requestStatus == RequestStatus.ERROR_REQUEST_NOK_HTTP_NO_CONNECTION
                    		&& restaurants != null) {
                    	drawRestaurantsOnTheMap();
                    }
                }
            }
        });
    }

    /**
     * Get more restaurants and append them to the existence list of restaurants
     * @param nextPageToken
     */
    private void getMoreRestaurants(String nextPageToken) {
    	if (nextPageToken == null || nextPageToken.equalsIgnoreCase("")) {
    		Log.e(LOG_TAG, "Error trying to get the next page. The token is not valud");
    		return;
    	}
    	
    	session.getRestaurantsNearbyNextPage(nextPageToken, new RequestRestaurantsCallback() {
			
			@Override
			public void done(List<Restaurant> newRestaurants, String nextPageToken,
					String errorMessage, RequestStatus requestStatus) {
				if (!ErrorHandler.isError(requestStatus)) {
					restaurants.addAll(newRestaurants);
					drawRestaurantsOnTheMap();
                	if (nextPageToken != null && !nextPageToken.equalsIgnoreCase("")) {
                		getMoreRestaurants(nextPageToken);
                	}
				} else {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();

                    // If there is any error about Internet connection but the list of
                    // restaurants has been retrieved offLine, draw them on the map
                    if (requestStatus == RequestStatus.ERROR_REQUEST_NOK_HTTP_NO_CONNECTION
                    		&& restaurants != null) {
                    	restaurants = newRestaurants;
                    	drawRestaurantsOnTheMap();
                    }
				}
			}
		});
    }

    /**
     * Draw the list of the restaurants on the map.
     * If there was any restaurant already drawn on the map, remove them.
     * @param restaurants The list of the restaurants to be drawn.
     */
	private void drawRestaurantsOnTheMap() {
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
	}
}
