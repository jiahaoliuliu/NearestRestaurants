package com.jiahaoliuliu.nearestrestaurants;

import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.jiahaoliuliu.nearestrestaurants.interfaces.RequestRestaurantsCallback;
import com.jiahaoliuliu.nearestrestaurants.models.Restaurant;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler;
import com.jiahaoliuliu.nearestrestaurants.session.Session;
import com.jiahaoliuliu.nearestrestaurants.utils.PositionTracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Toast;

public class NearestRestaurantsList extends SherlockFragmentActivity {

	private static final String LOG_TAG = NearestRestaurantsList.class.getSimpleName();

	// System data
	private Context context;
	private ActionBar actionBar;
	// The customized id of the action bar button
	private static final int MENU_MAPS_BUTTON_ID = 10000;

	// Session
	private Session session;

	// The broadcast receiver for the position
	private PositionTracker positionTracker;
	private MyPositionBroadcastReceiver myPositionBReceiver;
	private LatLng myPosition;
	private Marker userActualPositionMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearest_restaurants_list_layout);

        context = this;
        actionBar = getSupportActionBar();
        
        session = Session.getCurrentSession(context);

		// Show that it is waiting for the user's position
		setProgressBarIndeterminateVisibility(true);
		Toast.makeText(context, getResources().getString(R.string.looking_users_position), Toast.LENGTH_LONG).show();
		
		// Get the user's position
		positionTracker = new PositionTracker(context);
		// Register the broadcast receiver
		IntentFilter filter = new IntentFilter(PositionTracker.BROADCAST_POSITION_ACTION);
		myPositionBReceiver = new MyPositionBroadcastReceiver();
		context.registerReceiver(myPositionBReceiver, filter);
    }
    
	private class MyPositionBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(LOG_TAG, "New position received.");
			double latitude = intent.getDoubleExtra(PositionTracker.BROADCAST_POSITION_LATITUDE, PositionTracker.DEFAULT_LATITUDE);
			double longitude = intent.getDoubleExtra(PositionTracker.BROADCAST_POSITION_LONGITUDE, PositionTracker.DEFAULT_LONGITUDE);
			
			if (latitude == PositionTracker.DEFAULT_LATITUDE || longitude == PositionTracker.DEFAULT_LONGITUDE) {
				Log.e(LOG_TAG, "Wrong position found: " + latitude + ":" + longitude);
				return;
			}
			
			Log.v(LOG_TAG, "The new position is " + latitude + " ," + longitude);

			// Update the position
			myPosition = new LatLng(latitude, longitude);
			drawUsersNewPositionOnMaps();
			updateRestaurants();
			
			// Disable the progress bar
			setProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		positionTracker.finish();
		// Unregister the Broadcast receiver
		if (myPositionBReceiver != null) {
			context.unregisterReceiver(myPositionBReceiver);
		}
	}
	
	private void drawUsersNewPositionOnMaps() {
		if (myPosition == null) {
			Log.e(LOG_TAG, "Trying to draw the users new position when her position is null");
			return;
		}

		// If the market position is the user actual position, exit
		if (userActualPositionMarker != null && userActualPositionMarker.getPosition().equals(myPosition)) {
			Log.w(LOG_TAG, "The new position is the same as the old one: " + myPosition.latitude + " ," + myPosition.longitude);
			return;
		}

		// Remove the actual marker, if exists
		if (userActualPositionMarker != null) {
			userActualPositionMarker.remove();
		}

	}

	/**
	 * Update the list of the restaurants based on the position of the user
	 */
	private void updateRestaurants() {
		if (myPosition == null) {
			Log.e(LOG_TAG, "Trying to update the list of the restaurants when the position of the user is unknown.");
			return;
		}

		session.getRestaurantsNearby(myPosition, new RequestRestaurantsCallback() {
			
			@Override
			public void done(List<Restaurant> restaurants, String errorMessage,
					RequestStatus requestStatus) {
				if (!ErrorHandler.isError(requestStatus)) {
					Log.v(LOG_TAG, "List of the restaurants returned correctly");

				} else {
					Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	// Use the action bar to switch views
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

    	menu.add(Menu.NONE, MENU_MAPS_BUTTON_ID, Menu
        		.NONE, "Maps")
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	if (item.getItemId() == MENU_MAPS_BUTTON_ID) {
    		// Go to Maps
    		Intent startMapsIntent = new Intent(this, NearestRestaurants.class);
    		startActivity(startMapsIntent);
    	}
    	
        return true;
    }

 }