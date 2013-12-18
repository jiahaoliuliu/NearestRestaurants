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
    	ERROR_REQUEST_NOK,                    // Generic error
    	ERROR_REQUEST_NOK_DATA_NOT_VALID,    // The data received is not valid
    	ERROR_REQUEST_NOK_HTTP_NO_CONNECTION, // The App does not has Internet connection
    	ERROR_REQUEST_NOK_ZERO_RESULTS,       // If no result has been returned
    	ERROR_REQUEST_NOK_OVER_QUERY_LIMIT,   // The query quota limit has been reached
    	ERROR_REQUEST_NOK_REQUEST_DENIED,     // The request has been denied
    	ERROR_REQUEST_NOK_INVALID_REQUEST,    // The request is invalid
    	ERROR_REQUEST_NOK_DATA_NOT_READY;     // The data requested is not ready yet
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
		case ERROR_SERVER_GENERIC:
			requestMessage = context.getResources().getString(R.string.error_message_server_generic);
			break;
		case ERROR_REQUEST_NOK_DATA_NOT_VALID:
			requestMessage = context.getResources().getString(R.string.error_message_data_not_valid);
			break;
		case ERROR_REQUEST_NOK:
			requestMessage = context.getResources().getString(R.string.error_message_server_generic);
			break;
		case ERROR_REQUEST_NOK_HTTP_NO_CONNECTION:
			requestMessage = context.getResources().getString(R.string.error_message_internet_connection);
			break;
		case ERROR_REQUEST_NOK_ZERO_RESULTS:
			requestMessage = context.getResources().getString(R.string.error_message_zero_results);
			break;
		case ERROR_REQUEST_NOK_OVER_QUERY_LIMIT:
			requestMessage = context.getResources().getString(R.string.error_message_over_query_limit);
			break;
		case ERROR_REQUEST_NOK_REQUEST_DENIED:
			requestMessage = context.getResources().getString(R.string.error_message_request_denied);
			break;
		case ERROR_REQUEST_NOK_INVALID_REQUEST:
			requestMessage = context.getResources().getString(R.string.error_message_invalid_request);
			break;
		case ERROR_REQUEST_NOK_DATA_NOT_READY:
			requestMessage = context.getResources().getString(R.string.error_message_data_not_ready);
			break;
		default:
			Log.e(LOG_TAG, "Request status not recognized " + requestStatus);
			break;
		}
    	return requestMessage;
    }
}
