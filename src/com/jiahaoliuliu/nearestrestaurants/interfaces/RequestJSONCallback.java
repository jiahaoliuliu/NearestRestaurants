package com.jiahaoliuliu.nearestrestaurants.interfaces;

import org.json.JSONArray;

import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;

/**
 * This class is used to implement the error completion handler.
 */
public interface RequestJSONCallback {

    /**
     * Method called when the operation has been finished.
     * @param jsonObject    The JSON object returned by the server
     * @param requestStatus Indication of if any error happened or not
     */
    void done(final JSONArray jsonArray, final RequestStatus requestStatus);
}
