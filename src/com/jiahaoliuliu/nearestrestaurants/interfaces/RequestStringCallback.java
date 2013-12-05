package com.jiahaoliuliu.nearestrestaurants.interfaces;

import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;

/**
 * This class is used to implement the error completion handler.
 */
public interface RequestStringCallback {

    /**
     * Method called when the operation has been finished.
     * @param string        The String returned by the server
     * @param requestStatus Indication of if any error happened or not
     */
    void done(final String string, final RequestStatus requestStatus);
}
