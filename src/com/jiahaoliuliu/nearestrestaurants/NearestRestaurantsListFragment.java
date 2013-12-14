package com.jiahaoliuliu.nearestrestaurants;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.android.gms.maps.model.LatLng;
import com.jiahaoliuliu.nearestrestaurants.interfaces.OnPositionRequestedListener;
import com.jiahaoliuliu.nearestrestaurants.interfaces.OnUpdatePositionListener;
import com.jiahaoliuliu.nearestrestaurants.interfaces.RequestRestaurantsCallback;
import com.jiahaoliuliu.nearestrestaurants.models.Restaurant;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler;
import com.jiahaoliuliu.nearestrestaurants.session.Session;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;

/**
 * The fragment used to show the list of the restaurants when the users position
 * is known.
 * @author Jiahao Liu
 */
public class NearestRestaurantsListFragment extends SherlockListFragment
    implements OnUpdatePositionListener, OnScrollListener {

    private static final String LOG_TAG = NearestRestaurantsListFragment.class.getSimpleName();

    // Interfaces
    private OnPositionRequestedListener onPositionRequestedListener;

    private Context context;
    private Session session;

    // The list adapter
    private RestaurantListAdapter restaurantListAdapter;

    // The user position
    private LatLng myActualPosition; 

    // The token for the next page
    private String nextPageToken;
    
    // Set if the list is loading or not
    private boolean isLoadingMoreRestaurants = false;;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.context = activity;
        session = Session.getCurrentSession(context);

        // Check the onPositionRequestedListener
        try {
            onPositionRequestedListener = (OnPositionRequestedListener)activity;
            myActualPosition = onPositionRequestedListener.requestPosition();
            updateRestaurants();
        } catch (ClassCastException classCastException) {
            Log.e(LOG_TAG, "The attached activity must implements the OnPositionRequestedListener", classCastException);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
        Bundle savedInstanceState) {
        // Set the adapter
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);      
        getListView().setOnScrollListener(this);
    }

    @Override
    public void updatePosition(LatLng newPosition) {
        myActualPosition = newPosition;
        updateRestaurants();
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
            public void done(List<Restaurant> restaurants,
                             String newNextPageToken,
                             String errorMessage,
                             RequestStatus requestStatus) {
                if (!ErrorHandler.isError(requestStatus)) {
                    Log.v(LOG_TAG, "List of the restaurants returned correctly");
                    nextPageToken = newNextPageToken;
                    showRestaurantList(restaurants);
                } else {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();

                    // If there is any error about Internet connection but the list of
                    // restaurants has been retrieved offline, draw them on the map
                    if (requestStatus == RequestStatus.ERROR_REQUEST_NOK_HTTP_NO_CONNECTION
                    		&& restaurants != null) {
                    	showRestaurantList(restaurants);
                    }

                }
            }
        });
    }

    /**
     * Show the list of restaurants as list
     * @param restaurants
     */
    private void showRestaurantList(List<Restaurant> restaurants) {
        // Check if the restaurant list adapter exists before
        // If not
        if (restaurantListAdapter == null) {
            restaurantListAdapter = new RestaurantListAdapter(context, restaurants);
            setListAdapter(restaurantListAdapter);
        } else {
            restaurantListAdapter.setRestaurants(restaurants);
        }
    }

    /**
     * The base adapter for the list of the restaurants.
     */
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

        // Add new restaurants to the list
        public void addMoreRestaurants(List<Restaurant> newRestaurants) {
        	this.restaurants.addAll(newRestaurants);
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
            restaurantTextView.setText(name);
            return view;
        }
    }

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		boolean loadMoreRestaurants =
				firstVisibleItem + visibleItemCount >= totalItemCount;

		boolean moreDataAvailable =
				(nextPageToken != null && !nextPageToken.equalsIgnoreCase(""));

		if (loadMoreRestaurants && moreDataAvailable && !isLoadingMoreRestaurants) {
			Log.v(LOG_TAG, "Load more restaurants request send and more restaurants are available. Loading.");
			isLoadingMoreRestaurants = true;
			// The app is requesting for more data and there is more data available.
			// Requesting them
			Toast.makeText(context, R.string.loadin_new_restaurants, Toast.LENGTH_LONG).show();
			session.getRestaurantsNearbyNextPage(nextPageToken, new RequestRestaurantsCallback() {
				
				@Override
				public void done(List<Restaurant> newRestaurants, String newNextPageToken,
						String errorMessage, RequestStatus requestStatus) {
					isLoadingMoreRestaurants = false;
					if (!ErrorHandler.isError(requestStatus)) {
						nextPageToken = newNextPageToken;
						restaurantListAdapter.addMoreRestaurants(newRestaurants);
					} else {
						Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
						
	                    // If there is any error about Internet connection but the list of
	                    // restaurants has been retrieved offLine, reset the list
	                    if (requestStatus == RequestStatus.ERROR_REQUEST_NOK_HTTP_NO_CONNECTION
	                    		&& newRestaurants != null) {
	                    	showRestaurantList(newRestaurants);
	                    }
					}
				}
			});
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// Do nothing
	}

}
