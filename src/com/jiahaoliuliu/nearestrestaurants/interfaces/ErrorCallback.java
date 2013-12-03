package com.jiahaoliuliu.nearestrestaurants.interfaces;

import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;

/**
 * This class is used to implement the error completion handler.
 */
public interface ErrorCallback {

    /**
     * Method called when the operation has been finished.
     * @param requestStatus Indication of if any error happened or not
     */
    void done(final RequestStatus error);
}
