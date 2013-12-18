package com.jiahaoliuliu.nearestrestaurants.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.jiahaoliuliu.nearestrestaurants.interfaces.RequestJSONCallback;
import com.jiahaoliuliu.nearestrestaurants.interfaces.RequestRestaurantsCallback;
import com.jiahaoliuliu.nearestrestaurants.models.Restaurant;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;
import com.jiahaoliuliu.nearestrestaurants.session.Preferences.DoubleId;
import com.jiahaoliuliu.nearestrestaurants.session.Preferences.StringId;

/**
 * The Session class models a user's session. It is the intermediate level between Controllers and Service.
 */
public final class Session {

    private static final String LOG_TAG = Session.class.getSimpleName();
    // The default range, which is 1 mile (1609 meters)
    public static final int DEFAULT_RANGE = 1609;

    private Service service;

    private Preferences preferences;

    private static Session currentSession = null;

    // The database helper
    private RestaurantDBAdapter restaurantDBAdapter;

    // The data of the user
    private Context context;
    private HashMap<String, Restaurant> restaurants;
    private LatLng lastPositionKnown;

    /**
     * The constructor of the session.
     * Because it is a singleton, there is not parameters for the constructors and it's private
     */
    private Session() {
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final Session INSTANCE = new Session();
    }

    // It is synchronized to avoid problems with multithreading
    // Once get, it must initialize the service and the preferences based on the context
    private static Session getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // To avoid clone problem
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    //=========================================== Session ==============================
    /**
     * Get the current session.
     * @param context The context utilized to retrieve the data
     * @return The current session
    */
    public static synchronized Session getCurrentSession(Context context) {
    	if (Session.currentSession == null) {
            Session.sessionFromCurrentSession(context);
    	}

   		return Session.currentSession;
    }

    /**
     * Creates a new session from the data saved in the persistent data storage.
     * @param context The context utilized.
     */
    private static void sessionFromCurrentSession(final Context context) {

    	// The session itself
        final Session newSession = Session.getInstance();
        
        // The service
        Service newService = new Service();
        newSession.setService(newService);

        // The shared preferences
        Preferences preferences = new Preferences(context);
        newSession.setPreferences(preferences);

        // The database
        RestaurantDBAdapter restaurantDBAdapter = new RestaurantDBAdapter(context);
        newSession.setRestaurantDBAdapter(restaurantDBAdapter);

        // The hashmap of all the restaurants
        HashMap<String, Restaurant> restaurants = restaurantDBAdapter.getAllRestaurants();
        newSession.setRestaurants(restaurants);

        //Save the current session
        Session.setCurrentSession(newSession);
        Session.currentSession.saveAsCurrentSession(context);
    }

    /**
     * Save the session in a persistent way.
     * @param context  The context utilized to get the data
     */
    private void saveAsCurrentSession(Context context) {
    	// Save the context
    	this.context = context;
    }

    // Getters & setters
    private static synchronized void setCurrentSession(Session session) {
        Session.currentSession = session;
    }

    //=========================================== Basic methods ==============================

	public void getRestaurantsNearby(final LatLng myPosition,
			final RequestRestaurantsCallback requestRestaurantsCallback) {
		service.getRestaurantsNearby(myPosition, new RequestJSONCallback() {

			@Override
			public void done(JSONArray jsonArray, final String extraValue, RequestStatus requestStatus) {
				if (!ErrorHandler.isError(requestStatus)) {
					Log.v(LOG_TAG, "The list of the restaurants has been returned correctly");
						lastPositionKnown = myPosition;
						final List<Restaurant> restaurantsList = new ArrayList<Restaurant>();
						addRestaurantsToTheList(jsonArray, restaurantsList);
						requestRestaurantsCallback.done(restaurantsList, extraValue, null, RequestStatus.REQUEST_OK);
				} else {
					// If the error is about Internet connection, calculate the possible restaurants within the 
					// range and return it to the caller
					if (requestStatus == RequestStatus.ERROR_REQUEST_NOK_HTTP_NO_CONNECTION) {
						List<Restaurant> restaurantsNearsOffline = getNearRestaurants(myPosition);
						requestRestaurantsCallback.done(restaurantsNearsOffline,
								null,
								ErrorHandler.parseRequestStatus(context, jsonArray, requestStatus),
								requestStatus);
					} else {
						requestRestaurantsCallback.done(null,
								null,
								ErrorHandler.parseRequestStatus(context, jsonArray, requestStatus),
								requestStatus);
					}
				}
			}
		});
	}

	public void getRestaurantsNearbyNextPage(String nextPageToken,
			final RequestRestaurantsCallback requestRestaurantsCallback) {
		// Check the precondition
		if (nextPageToken == null || nextPageToken.equalsIgnoreCase("")) {
			Log.e(LOG_TAG, "Error trying to get the next page of restaurnats nearby."
					+ " The next page token is null");
			requestRestaurantsCallback.done(null, null, null, RequestStatus.ERROR_REQUEST_NOK_DATA_NOT_VALID);
			return;
		}

		service.getRestaurantsNearbyNextPage(nextPageToken, new RequestJSONCallback() {
			
			@Override
			public void done(JSONArray jsonArray, String newNextPageToken, RequestStatus requestStatus) {
				if (!ErrorHandler.isError(requestStatus)) {
					final List<Restaurant> restaurantsList = new ArrayList<Restaurant>();
					addRestaurantsToTheList(jsonArray, restaurantsList);
					requestRestaurantsCallback.done(restaurantsList, newNextPageToken, null, RequestStatus.REQUEST_OK);
				} else {
					// If the error is about Internet connection, calculate the possible restaurants within the 
					// range and return it to the caller
					if (requestStatus == RequestStatus.ERROR_REQUEST_NOK_HTTP_NO_CONNECTION) {
						List<Restaurant> restaurantsNearsOffline = getNearRestaurants(lastPositionKnown);
						requestRestaurantsCallback.done(restaurantsNearsOffline,
								null,
								ErrorHandler.parseRequestStatus(context, jsonArray, requestStatus),
								requestStatus);
					} else {
						requestRestaurantsCallback.done(null,
								null,
								ErrorHandler.parseRequestStatus(context, jsonArray, requestStatus),
								requestStatus);
					}
				}
			}
		});
	}

    //=========================================== Getters and setters ==============================
    private Service getService() {
        return service;
    }

    /**
     * Set the service as the service utilized for the sessions
     * This is private to prevent other to set the service
     * The service won't be set until the user has logged in.
     * @param service The service to set.
     */
    private void setService(Service service) {
        this.service = service;
    }

    private Preferences getPreferences() {
       return preferences;
    }

    /**
     * Set the preferences as the preferences utilized for the session.
     * This is private to prevent other ot set the preferences from outside
     * The preferences won't be set until the user has logged in.
     * @param preferences The preferences to set.
     */
    private void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    private RestaurantDBAdapter getRestaurantDBAdapter() {
    	return restaurantDBAdapter;
    }

    private void setRestaurantDBAdapter(RestaurantDBAdapter restaurantDBAdapter) {
    	this.restaurantDBAdapter = restaurantDBAdapter;
    }

    public LatLng getLastUserPosition() {

    	Double latitude = preferences.getDouble(DoubleId.LAST_USER_POSITION_LATITUDE);
    	Double longitude = preferences.getDouble(DoubleId.LAST_USER_POSITION_LONGITUDE);
    	
    	if (latitude != null && longitude != null) {
    		return new LatLng(latitude, longitude);
    	} else {
    		Log.w(LOG_TAG, "The user position is not set");
    		return null;
    	}
    }

    public void setLastUserPosition(LatLng userPosition) {
    	preferences.setDouble(DoubleId.LAST_USER_POSITION_LATITUDE, userPosition.latitude);
    	preferences.setDouble(DoubleId.LAST_USER_POSITION_LONGITUDE, userPosition.longitude);
    }

	private HashMap<String, Restaurant> getRestaurants() {
		return restaurants;
	}

	private void setRestaurants(HashMap<String, Restaurant> restaurants) {
		this.restaurants = restaurants;
	}

    //=========================================== Private methods ==============================

	/**
	 * Go through all the restaurants saved offline and return those which has range no farer than
	 * designed distance
	 * @param myPosition The actual position of the user
	 * @return           A list of restaurants whom are in the range which the center is the users position
	 */
	private List<Restaurant> getNearRestaurants(LatLng myPosition) {
		List<Restaurant> restaurantsList = new ArrayList<Restaurant>();
		if (myPosition == null) {
			Log.e(LOG_TAG, "Error trying to get the list of near restaurants when the user's position is null");
			return restaurantsList;
		}

		Location myLocation = new Location("");
		myLocation.setLatitude(myPosition.latitude);
		myLocation.setLongitude(myPosition.longitude);

		Set<String> ids = restaurants.keySet();
		for (String id: ids) {
			Restaurant restaurant = restaurants.get(id);
			LatLng restaurantPosition = restaurant.getPosition();
			Location restaurantLocation = new Location("");
			
			restaurantLocation.setLatitude(restaurantPosition.latitude);
			restaurantLocation.setLongitude(restaurantPosition.longitude);
			
			if (((int)restaurantLocation.distanceTo(myLocation)) <= DEFAULT_RANGE ) {
				restaurantsList.add(restaurant);
			}
		}

		return restaurantsList;
	}

	private void addRestaurantsToTheList(JSONArray jsonArray, List<Restaurant> restaurantsList) {
		// Parse the list of the restaurants
		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Restaurant restaurant = new Restaurant(jsonObject);
				restaurantsList.add(restaurant);
	
				// Check if the restaurant already exists in the hashMap
				// If not, insert it into the temporal hashmap
				// and the database
				if (!restaurants.containsKey(restaurant.getId())) {
					// Insert data
					restaurants.put(restaurant.getId(), restaurant);
					// Save the data into the database
					restaurantDBAdapter.insertNewRestaurant(restaurant);
				}
			} catch (JSONException e) {
				Log.e(LOG_TAG, "Error parsing the restaurant returned by Google at the position " +
			          i + " of" + jsonArray.toString());
			}
		}
	}
}