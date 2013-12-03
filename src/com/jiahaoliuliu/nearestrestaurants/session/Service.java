package com.jiahaoliuliu.nearestrestaurants.session;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

import com.jiahaoliuliu.nearestrestaurants.session.HttpRequest;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;
import com.jiahaoliuliu.nearestrestaurants.session.HttpRequest.RequestMethod;

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
     * The base of the server url
     */
    private static final String URL_BASE = "http://www.yelmocines.es/";

    // All cinemas
    private static final String URL_REL_ALL_CINEMAS = "json_generate/allcinemas/-1";
    private static final RequestMethod REQUEST_METHOD_ALL_CINEMAS = RequestMethod.RequestMethodGet;

    // All movies
    private static final String URL_REL_ALL_MOVIES = "json_generate/billboard_province/";
    private static final RequestMethod REQUEST_METHOD_ALL_MOVIES = RequestMethod.RequestMethodGet;
    
    // Cinemas by postal code and by movie id
    private static final String URL_REL_CINEMAS_BY_POSTAL_CODE_AND_MOVIE_ID = "json_generate/cinemas_for_movie/";
    private static final RequestMethod REQUEST_METHOD_CINEMAS_BY_POSTAL_CODE_AND_MOVIE_ID = RequestMethod.RequestMethodGet;

    /**
     * The empty constructor of the service.
     * It is used only for Anonymous users
     */
    Service() {};

}
