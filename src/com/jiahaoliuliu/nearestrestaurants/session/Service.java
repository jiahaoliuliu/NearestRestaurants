package com.jiahaoliuliu.nearestrestaurants.session;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.jiahaoliuliu.nearestrestaurants.session.HttpRequest;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;
import com.jiahaoliuliu.nearestrestaurants.session.HttpRequest.RequestMethod;
import com.jiahaoliuliu.nearestrestaurants.interfaces.RequestJSONCallback;
/**
 * This class is used to communicates with the remote server.
 * If there is any error, HTTPRequest class will return an error code
 * then the server will translate the error code to the application
 * specific error code and give it to the upper class. The upper class
 * will check the error and decide what to do.
 */
public class Service {

    /**
     * The tag used for log.
     */
    private static final String LOG_TAG = Service.class.getSimpleName();
    
    /**
     * The base URL
     */
    private static final String URL_BASE = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

    // The API key
    private static final String API_KEY_KEY = "key";
    private static final String API_KEY_VALUE = "AIzaSyBZZOHr87HRb5dVBNpWSp3GF6w2_q7DlQ0";
    
    // Location
    private static final String LOCATION_KEY = "location";
    
    // Radius
    private static final String RADIUS_KEY = "radius";
    private static final String RADIUS_VALUE = String.valueOf(Session.DEFAULT_RANGE); // The radius is defined by the session

    // Sensor
    private static final String SENSOR_KEY = "sensor";
    private static final String SENSOR_VALUE = "false"; // Because the sensor is always false, it is better to save it as String

    // Types (Restaurant)
    private static final String TYPES_KEY = "types";
    private static final String TYPES_VALUE = "restaurant";

    // Next page token
    private static final String NEXT_PAGE_TOKEN_KEY = "pagetoken";
    
    // Get the list of the restaurants nearby
    private static final RequestMethod RESTAURANTS_NEARBY_REQUEST_METHOD = RequestMethod.RequestMethodGet; 

    /**
     * The empty constructor of the service.
     * It is used only for Anonymous users
     */
    Service() {};

	protected void getRestaurantsNearby(
			LatLng myPosition,
			final RequestJSONCallback requestJSONCallback) {

		Uri finalUri = Uri.parse(URL_BASE);

		// The parameters
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(API_KEY_KEY, API_KEY_VALUE);
        parameters.put(LOCATION_KEY, myPosition.latitude + "," + myPosition.longitude);
        parameters.put(RADIUS_KEY, RADIUS_VALUE);
        parameters.put(SENSOR_KEY, SENSOR_VALUE);
        parameters.put(TYPES_KEY, TYPES_VALUE);

        HttpRequest httpRequest = HttpRequest.create(finalUri, RESTAURANTS_NEARBY_REQUEST_METHOD, parameters);

        httpRequest.performRequestWithJSONHandler(new RequestJSONCallback() {

			@Override
			public void done(JSONArray jsonArray, String extraValue, RequestStatus requestStatus) {
				requestJSONCallback.done(jsonArray, extraValue, requestStatus);
			}
		});
	}

	/**
	 * Get the next page of Restaurants Nearby
	 * @param nextPageToken       The token returned by the server to get the next page
	 * @param requestJSONCallback The callback to call when the data is ready
	 */
	protected void getRestaurantsNearbyNextPage(String nextPageToken,
			final RequestJSONCallback requestJSONCallback) {
		Uri finalUri = Uri.parse(URL_BASE);

		// The parameters
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(API_KEY_KEY, API_KEY_VALUE);
        parameters.put(SENSOR_KEY, SENSOR_VALUE);
        parameters.put(NEXT_PAGE_TOKEN_KEY, nextPageToken);

        HttpRequest httpRequest = HttpRequest.create(finalUri, RESTAURANTS_NEARBY_REQUEST_METHOD, parameters);

        httpRequest.performRequestWithJSONHandler(new RequestJSONCallback() {

			@Override
			public void done(JSONArray jsonArray, String extraValue, RequestStatus requestStatus) {
				requestJSONCallback.done(jsonArray, extraValue, requestStatus);
			}
		});
	}
}
