package com.jiahaoliuliu.nearestrestaurants.session;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
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

    private Service service;

    private Preferences preferences;

    private static Session currentSession = null;

    // The database helper
    private RestaurantDBAdapter restaurantDBAdapter;

    // The data of the user
    private Context context;

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

        RestaurantDBAdapter restaurantDBAdapter = new RestaurantDBAdapter(context);
        newSession.setRestaurantDBAdapter(restaurantDBAdapter);
        
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

	public void getRestaurantsNearby(LatLng myPosition,
			final RequestRestaurantsCallback requestRestaurantsCallback) {
		service.getRestaurantsNearby(myPosition, new RequestJSONCallback() {
			
			@Override
			public void done(JSONArray jsonArray, RequestStatus requestStatus) {
				if (!ErrorHandler.isError(requestStatus)) {
					Log.v(LOG_TAG, "The list of the restaurants has been returned correctly");
					// Remove all previous data from the list of restaurants
					List<Restaurant> restaurants = new ArrayList<Restaurant>();
					
					// Remove any previous data from the database
					restaurantDBAdapter.deleteAll();

						// Parse the list of the restaurants
						for (int i = 0; i < jsonArray.length(); i++) {
							try {
								JSONObject jsonObject = jsonArray.getJSONObject(i);
								Restaurant restaurant = new Restaurant(jsonObject);
								restaurants.add(restaurant);
								
								// Save the data into the database
								restaurantDBAdapter.insertNewRestaurant(restaurant);
							} catch (JSONException e) {
								Log.e(LOG_TAG, "Error parsing the restaurant returned by Google at the position " +
							          i + " of" + jsonArray.toString());
							}
						}

						// If everything went OK, return it.
						requestRestaurantsCallback.done(restaurants, null, RequestStatus.REQUEST_OK);
				} else {
					requestRestaurantsCallback.done(null,
							ErrorHandler.parseRequestStatus(context, jsonArray, requestStatus),
							requestStatus);
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
}