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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.BaseAdapter;

public class NearestRestaurantsList extends SherlockFragmentActivity {

	private static final String LOG_TAG = NearestRestaurantsList.class.getSimpleName();

	// System data
	private Context context;
	private ActionBar actionBar;
	// The customized id of the action bar button
	private static final int MENU_MAPS_BUTTON_ID = 10000;

	// Session
	private Session session;

	// Layout
	private ListView listView;
	private RestaurantListAdapter restaurantListAdapter;

	// The broadcast receiver for the position
	private PositionTracker positionTracker;
	private MyPositionBroadcastReceiver myPositionBReceiver;
	private LatLng myPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearest_restaurants_list_layout);

        context = this;
        actionBar = getSupportActionBar();
        
        session = Session.getCurrentSession(context);

        // Layout
        listView = (ListView)findViewById(R.id.listView);

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
					// Check if the restaurant list adapter exists before
					// If not
					if (restaurantListAdapter == null) {
						restaurantListAdapter = new RestaurantListAdapter(context, restaurants);
						listView.setAdapter(restaurantListAdapter);
					} else {
						restaurantListAdapter.setRestaurants(restaurants);
					}
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
    		Intent startMapsIntent = new Intent(this, NearestRestaurantsMaps.class);
    		startActivity(startMapsIntent);
    	}
    	
        return true;
    }

    private class RestaurantListAdapter extends BaseAdapter {
    	
    	private Context context;
    	private List<Restaurant> restaurants;
    	private LayoutInflater inflater;
    	
    	public RestaurantListAdapter(Context context, List<Restaurant> restaurants) {
    		this.context = context;
    		this.restaurants = restaurants;
    		inflater = LayoutInflater.from(context);
    	}

    	// Reset the list of the restaurants
    	public void setRestaurants(List<Restaurant> restaurants) {
    		this.restaurants = restaurants;
    		notifyDataSetChanged();
    	}

		@Override
		public int getCount() {
			return restaurants.size();
		}

		@Override
		public Object getItem(int position) {
			return restaurants.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			
			// No view holder is used because the time restriction
			View view = inflater.inflate(R.layout.row_layout, null);
			
			TextView restaurantTextView = (TextView)view.findViewById(R.id.restaurantNameTextView);
			Restaurant restaurant = restaurants.get(position);
			
			String name = restaurant.getName();
			if (name != null && !name.equalsIgnoreCase("")) {
				restaurantTextView.setText(name);
			}

			return view;
		}
    	
    }
 }