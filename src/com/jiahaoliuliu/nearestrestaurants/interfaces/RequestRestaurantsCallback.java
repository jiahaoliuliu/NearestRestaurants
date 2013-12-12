package com.jiahaoliuliu.nearestrestaurants.interfaces;

import java.util.List;

import com.jiahaoliuliu.nearestrestaurants.models.Restaurant;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;


/**
 * This class is used to implement the error completion handler.
 */
public interface RequestRestaurantsCallback {

    /**
     * Method called when the operation has been finished.
     * This class should be used only by Session to return a
     * specific element
     * @param restaurants   The list of restaurants parsed by the Session
     * @param nextPageToken The token for the next page. It could be null or empty
     * @param errorMessage  The error message to show if there is any error
     * @param requestStatus Indication of if any error happened or not
     */
    void done(final List<Restaurant> restaurants,
    		  final String nextPageToken,
    		  final String errorMessage,
    		  final RequestStatus requestStatus);
}
