package com.jiahaoliuliu.nearestrestaurants.session;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
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
        Preferences preferences = new Preferences(context);

        final Session newSession = Session.getInstance();
        Service newService = new Service();
        newSession.setService(newService);
        newSession.setPreferences(preferences);

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

    //=========================================== Getters and setters ==============================
    /*
    private Service getService() {
        return service;
    }*/

    /**
     * Set the service as the service utilized for the sessions
     * This is private to prevent other to set the service
     * The service won't be set until the user has logged in.
     * @param service The service to set.
     */
    private void setService(Service service) {
        this.service = service;
    }

    public Preferences getPreferences() {
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
}