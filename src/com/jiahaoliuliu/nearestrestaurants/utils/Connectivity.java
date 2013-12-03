package com.jiahaoliuliu.nearestrestaurants.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Connectivity {

	private static final String LOG_TAG = Connectivity.class.getSimpleName();

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

		Log.d(LOG_TAG, "Network info " + activeNetworkInfo);
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

}
