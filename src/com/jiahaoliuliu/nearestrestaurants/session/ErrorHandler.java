package com.jiahaoliuliu.nearestrestaurants.session;

import org.json.JSONArray;

import com.jiahaoliuliu.nearestrestaurants.R;
import com.jiahaoliuliu.nearestrestaurants.utils.Connectivity;

import android.content.Context;
import android.util.Log;

public class ErrorHandler {

	private static final String LOG_TAG = ErrorHandler.class.getSimpleName();

    public enum RequestStatus {
        // Ok messages
    	REQUEST_OK,
    	
    	// Error from the user session
    	ERROR_SERVER_GENERIC,
    	
    	// Error when it is communicating with the server
    	ERROR_REQUEST_NOK, ERROR_REQUEST_NOK_DATA_VALIDATION,
    	ERROR_REQUEST_NOK_HTTP_HOST_NO_CONNECTION;
    }

	public static boolean isError(RequestStatus requestStatus) {
		boolean result = true;
		switch (requestStatus) {
		case REQUEST_OK:
			result = false;
			break;
		default:
			result = true;
		}
		return result;
	}

    public static String parseRequestStatus(Context context, JSONArray jsonErrorArray, RequestStatus requestStatus) {
    	// Initialize the exception message with the generic error message
		String requestMessage = context.getResources().getString(R.string.message_request_ok);

		switch (requestStatus) {
		case REQUEST_OK:
			requestMessage = context.getResources().getString(R.string.message_request_ok);
			break;
		case ERROR_REQUEST_NOK_HTTP_HOST_NO_CONNECTION:
			// Check if the user has internet connection
			if (!Connectivity.isNetworkAvailable(context)) {
				requestMessage = context.getResources().getString(R.string.error_message_internet_connection);
			} else {
				requestMessage = context.getResources().getString(R.string.error_message_server_generic);
			}
			break;
		case ERROR_SERVER_GENERIC:
			requestMessage = context.getResources().getString(R.string.error_message_server_generic);
			break;
		// If the error type is known by the server
		case ERROR_REQUEST_NOK_DATA_VALIDATION:
			requestMessage = context.getResources().getString(R.string.error_message_data_invalid);
			break;
		case ERROR_REQUEST_NOK:
			requestMessage = context.getResources().getString(R.string.error_message_server_generic);
			break;
		default:
			Log.e(LOG_TAG, "Request status not recognized " + requestStatus);
			break;
		}
    	return requestMessage;
    }
}
