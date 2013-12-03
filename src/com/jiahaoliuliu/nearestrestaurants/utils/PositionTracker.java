package com.jiahaoliuliu.nearestrestaurants.utils;

import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

@SuppressLint("NewApi")
public class PositionTracker {

	public static final String BROADCAST_POSITION_ACTION = "com.jiahaoliuliu.nearestrestaurants.positiontracker.positionchanged";
	public static final String BROADCAST_POSITION_LATITUDE = "com.jiahaoliuliu.nearestrestaurants.positiontracker.positionchenged.latitude";
	public static final String BROADCAST_POSITION_LONGITUDE = "com.jiahaoliuliu.nearestrestaurants.positiontracker.positionchanged.longitude";
	
	public static final double DEFAULT_LATITUDE = -190.0;
	public static final double DEFAULT_LONGITUDE = -190.0;

 	private LocationManager locationManager;
 	private final Criteria criteria = new Criteria();
 	private static int minUpdateTime = 30*1000; // 30 seconds
 	private static int minUpdateDistance = 300; // 300m
 	
 	private static final String LOG_TAG = PositionTracker.class.getSimpleName();
 	
 	private Context context;
 	
 	public PositionTracker (Context context) {
 		this.context = context;
 		
 		// Get a reference to the Location Manager
 		String svcName = Context.LOCATION_SERVICE;
 		locationManager = (LocationManager)context.getSystemService(svcName);
 		
 		// Specify Location Provider criteria
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setPowerRequirement(Criteria.POWER_LOW);
 		criteria.setAltitudeRequired(false);
 		criteria.setBearingRequired(false);
 		criteria.setSpeedRequired(false);
 		criteria.setCostAllowed(false);
 		
 		// Only for Android 3.0 and above
 		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
	 		criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
	 		criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
	 		criteria.setBearingAccuracy(Criteria.NO_REQUIREMENT);
	 		criteria.setSpeedAccuracy(Criteria.NO_REQUIREMENT);
 		}
 		//  End of Android 3.0 and above only
 		
 		registerListener();
 	}
 	
 	private void registerListener() {
 		// The best provider, no matter if it is enabled or not
 		String bestProvider = locationManager.getBestProvider(criteria, false);
 
 		// The best provider which is enabled 
 		String bestAvailableProvider = locationManager.getBestProvider(criteria, true);
 
 		Log.d(LOG_TAG, bestProvider + " / " + bestAvailableProvider);
 		
 		Location lastKnownLocation;
 		
 		// If there is not providers
 		if (bestProvider == null) {
 			Log.d(LOG_TAG, "No Location Provider exists on device.");
 		// If all the providers are available
 		// For the GPS, it must enable all the providers because it doesn't work in houses.
 		} else if (bestProvider.equals(bestAvailableProvider) && !bestProvider.equals(LocationManager.GPS_PROVIDER)) {
 			lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);
 			reactToLocationChange(lastKnownLocation);
 			locationManager.requestLocationUpdates(bestAvailableProvider, minUpdateTime, minUpdateDistance, bestAvailableProviderListener);
 		// If there is some provider which is not enabled
 		} else {
 			// Try to use the best provider
 			lastKnownLocation = locationManager.getLastKnownLocation(bestAvailableProvider);
 			reactToLocationChange(lastKnownLocation);
 			locationManager.requestLocationUpdates(bestProvider, minUpdateDistance, minUpdateDistance, bestProviderListener);
 			
 			// If there is any available provider, use them also
 	 		// For the GPS, it must enable all the providers because it doesn't work in houses.
 			if (bestAvailableProvider != null && !bestAvailableProvider.equals(LocationManager.GPS_PROVIDER)) {
 				lastKnownLocation = locationManager.getLastKnownLocation(bestAvailableProvider);
 				reactToLocationChange(lastKnownLocation);
 				locationManager.requestLocationUpdates(bestAvailableProvider, minUpdateTime, minUpdateDistance, bestAvailableProviderListener);
 			// Otherwise, try to activate all the providers
 			} else {
 				List<String> allProviders = locationManager.getAllProviders();
 				for (String provider: allProviders) {
 	 				lastKnownLocation = locationManager.getLastKnownLocation(provider);
 	 				reactToLocationChange(lastKnownLocation);
 					locationManager.requestLocationUpdates(provider, 0, 0, bestProviderListener);
 				}
 				Log.w(LOG_TAG, "No Location Providers currently available");
 			}
 		}
 	}
 	
 	public void finish() {
 		unregisterAllListeners();
 		Log.v(LOG_TAG, "Finish");
 	}
 	
 	private void unregisterAllListeners() {
 		locationManager.removeUpdates(bestProviderListener);
 		locationManager.removeUpdates(bestAvailableProviderListener);
 	}
 	
 	private void reactToLocationChange(Location location) {
 		if (location != null) {
	 		Log.v(LOG_TAG, "Position get: Latitude " + location.getLatitude() + " Longitude: " + location.getLongitude());
	 		Intent broadcastPositionIntent = new Intent(BROADCAST_POSITION_ACTION);
	 		broadcastPositionIntent.putExtra(BROADCAST_POSITION_LATITUDE, location.getLatitude());
	 		broadcastPositionIntent.putExtra(BROADCAST_POSITION_LONGITUDE, location.getLongitude());
	 		context.sendBroadcast(broadcastPositionIntent);
 		}
 	}
 	
 	private LocationListener bestProviderListener = new LocationListener() {
 		public void onLocationChanged(Location location) {
 			reactToLocationChange(location);
 		}
 		
 		public void onProviderDisabled(String provider) {
 		}
 		
 		public void onProviderEnabled(String provider) {
 			registerListener();
 		}
 		
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			
 		}
 	};
 	
 	private LocationListener bestAvailableProviderListener = new LocationListener() {
 		public void onProviderEnabled(String provider) {
 		}
 		
 		public void onProviderDisabled(String provider) {
 			registerListener();
 		}
 		
 		public void onLocationChanged(Location location) {
 			reactToLocationChange(location);
 		}
 		
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			
 		}
 	};
}
