package com.jiahaoliuliu.nearestrestaurants;

import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;
import com.jiahaoliuliu.nearestrestaurants.interfaces.Callback;
import com.jiahaoliuliu.nearestrestaurants.models.Restaurant;
import com.jiahaoliuliu.nearestrestaurants.session.Session;
import com.jiahaoliuliu.nearestrestaurants.utils.PositionTracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

public class NearestRestaurants extends SherlockFragmentActivity {

	private static final String LOG_TAG = NearestRestaurants.class.getSimpleName();

	// System data
	private Context context;
	private ActionBar actionBar;
	private Menu actionBarMenu;
	private FragmentManager fragmentManager;

	// Callback used to deal with the asynchronous operations
	private Callback actionBarMenuCallback;
	
	// The customized id of the action bar button
	private MenuItem menuViewListItem;
	private static final int MENU_VIEW_LIST_BUTTON_ID = 10000;

	// Session
	private Session session;
	
	// The broadcast receiver for the position
	private PositionTracker positionTracker;
	private MyPositionBroadcastReceiver myPositionBReceiver;
	private LatLng myPosition;
	private boolean positionSetAtFirstTime = true;

	// The fragments
	private WorldMapFragment worldMapFragment;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearest_restaurants_layout);

        context = this;
        actionBar = getSupportActionBar();
        
        session = Session.getCurrentSession(context);
        fragmentManager = getSupportFragmentManager();

		// Get the user's position
		positionTracker = new PositionTracker(context);
		// Register the broadcast receiver
		IntentFilter filter = new IntentFilter(PositionTracker.BROADCAST_POSITION_ACTION);
		myPositionBReceiver = new MyPositionBroadcastReceiver();
		context.registerReceiver(myPositionBReceiver, filter);

		showWorldMapFragment();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
		// Show the users last position if it was set
		setSupportProgressBarIndeterminateVisibility(true);
		LatLng userLastPosition = session.getLastUserPosition();
		if (userLastPosition != null) {
			myPosition = userLastPosition;
			Toast.makeText(context, getResources().getString(R.string.updating_users_position), Toast.LENGTH_LONG).show();
			
			// Show the list of the last restaurants saved
			List<Restaurant> restaurants = session.getLastRestaurantsSaved();
			
		} else {
			Toast.makeText(context, getResources().getString(R.string.looking_users_position), Toast.LENGTH_LONG).show();
		}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	// Disable any indeterminate progress bar
    	setSupportProgressBarIndeterminateVisibility(false);
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
			session.setLastUserPosition(myPosition);
			// Disable the progress bar
			setSupportProgressBarIndeterminateVisibility(false);
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

	// Use the action bar to switch views
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Get the value of Menu
    	actionBarMenu = menu;
    	if (actionBarMenuCallback != null) {
    		actionBarMenuCallback.done();
    	}
    	
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	if (item.getItemId() == MENU_VIEW_LIST_BUTTON_ID) {
    		// Go to List
    		Intent startListActivityIntent = new Intent(this, NearestRestaurantsList.class);
    		startActivity(startListActivityIntent);
    	}
    	
        return true;
    }

    /**
     * Show the world map fragment
     */
    private void showWorldMapFragment() {
		worldMapFragment = new WorldMapFragment();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.content_frame, worldMapFragment, WorldMapFragment.class.toString());
		ft.commit();
		
		// Modify the action bar menu to adapt it to the world map fragment
		if (actionBarMenu == null) {
			actionBarMenuCallback = new Callback() {

				@Override
				public void done() {
					showWorldMapActionBar();
					
					// Null the call back so it won't be
					// called again
					actionBarMenuCallback = null;
				}
			};
		} else {
			showWorldMapActionBar();
		}
    }
    
    private void showWorldMapActionBar() {
    	if (actionBarMenu == null) {
    		Log.e(LOG_TAG, "Trying to adapt the action bar to the world map when it is null");
    		return;
    	}
    	
    	// Remove any previous menu item
    	actionBarMenu.clear();
    	// Set the initial state of the menu item
    	menuViewListItem = actionBarMenu.add(Menu.NONE, MENU_VIEW_LIST_BUTTON_ID, Menu
        		.NONE, getResources().getString(R.string.action_bar_show_list));
    	menuViewListItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

}