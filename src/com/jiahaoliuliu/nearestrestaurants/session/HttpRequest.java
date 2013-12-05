package com.jiahaoliuliu.nearestrestaurants.session;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.jiahaoliuliu.nearestrestaurants.interfaces.RequestDataCallback;
import com.jiahaoliuliu.nearestrestaurants.interfaces.RequestJSONCallback;
import com.jiahaoliuliu.nearestrestaurants.interfaces.RequestStringCallback;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler;
import com.jiahaoliuliu.nearestrestaurants.session.ErrorHandler.RequestStatus;

/**
 * The Request class used to establish REST communication with the server.
 */
public class HttpRequest {

    /**
     * The tag used in logs.
     */
    private static final String LOG_TAG = HttpRequest.class.getSimpleName();

    private static final HashMap<String, String> DEFAULT_HEADER_FIELDS = new HashMap<String, String>();
    private static final HashMap<String, String> DEFAULT_PARAMETERS = null;

    private RequestStatus requestStatus = RequestStatus.REQUEST_OK;

    /**
     * Enumerated data which represents the REST methods used in the Request.
     */
    enum RequestMethod {
        /**
         * Method get.
         */
        RequestMethodGet,
        /**
         * Method post.
         */
        RequestMethodPost,
        /**
         * Method put.
         */
        RequestMethodPut,
        /**
         * Method delete.
         */
        RequestMethodDelete
    }

    /**
     * The uri used to establish the REST communication.
     */
    private Uri uri;

    /**
     * The list of parameters to sent to the server.
     */
    private Map<String, String> parameters;

    private HttpEntity entity;

    /**
     * The request method established.
     */
    private final RequestMethod requestMethod;

    /**
     * The headers used.
     */
    private final Map<String, String> headerFields;

    /**
     * The instance of server Fetcher which is used to communicates with the server.
     */
    private ServerFetcher serverFetcher;

    /**
     * The instance of server Fetcher which is used to communicates with the server.
     */
    private final ExecutorServiceSingleton executorServiceSingleton;


    /**
     * The threadPool which contains all the threads used to communicate with the server.
     */
    private final ExecutorService threadPool;

    /**
     * The handler used to execute all the callbacks in the main thread.
     */
    private final Handler handler;

    /**
     * The main constructor of the class. For other list of parameters, use the method create instead.
     * @param uri           The URI of the server
     * @param parameters    The list of the parameters used
     * @param requestMethod The request method
     */
    private HttpRequest(Uri uri, Map<String, String> headerFields,
    			Map<String, String> parameters, HttpEntity entity,
    			RequestMethod requestMethod) {

        // If the request method is get, all the parameters is shown in the uri
        this.uri = uri;
        this.parameters = parameters;
        this.entity = entity;
        this.requestMethod = requestMethod;
        // Other data
        this.headerFields = headerFields;

        this.handler = new Handler();
        this.executorServiceSingleton = ExecutorServiceSingleton.instance();
        this.threadPool = executorServiceSingleton.getExecutorService();

    }

    static HttpRequest create(Uri uri, RequestMethod requestMethod) {
    	return new HttpRequest(uri, DEFAULT_HEADER_FIELDS, DEFAULT_PARAMETERS, null, requestMethod);
    }
    
    /**
     * The method used to create an instance of HttpRequest
     * @param uri           The URI of the server
     * @param parameters    The list of the parameters used
     * @param requestMethod The request method
     * @return              An instance of HttpRequest
     */
    static HttpRequest create(Uri uri, RequestMethod requestMethod, Map<String, String> parameters) {
    	return new HttpRequest(uri, DEFAULT_HEADER_FIELDS, parameters, null, requestMethod);
    }

    /**
     * The method used to create an instance of HttpRequest
     * @param uri           The URI of the server
     * @param headerFields  The header fields created by the called class.
     * @param jsonEntity    The json Entity to send to the server
     * @param requestMethod The request method
     * @return              An instance of HttpRequest
     */
	static HttpRequest create(Uri uri, Map<String, String> headerFields, HttpEntity jsonEntity,
			RequestMethod requestMethod) {
		return new HttpRequest(uri, headerFields, DEFAULT_PARAMETERS, jsonEntity, requestMethod);
	}

	static HttpRequest create(Uri uri, Map<String, String> headerFields, RequestMethod requestMethod) {
		return new HttpRequest(uri, headerFields, DEFAULT_PARAMETERS, null, requestMethod);
	}

	static HttpRequest create(Uri uri, Map<String, String> headerFields, RequestMethod requestMethod, Map<String, String> parameters) {
		return new HttpRequest(uri, headerFields, parameters, null, requestMethod);
	}

    /**
     * Method used to send request to the server, which parse the content returned to a json object.
     * @param jsonHandler The callback to call when the communication finishes.
     */
    protected void performRequestWithJSONHandler(final RequestJSONCallback jsonHandler) {
        /*
         * Add new header
         */
        headerFields.put("Accept", "application/json");

        serverFetcher = new ServerFetcher(requestMethod, uri,
                parameters, entity, headerFields,
                new RequestStringCallback() {

                @Override
                public void done(final String stringObtained, final RequestStatus requestStatus) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // If there is any data, try to parse it as JSON
	                        if (stringObtained != null && !stringObtained.equals("")) {
	                            try {
	                                // For latin languages, the codification must be ISO 8859-1(ISO-8859-1)
	                                //http://es.wikipedia.org/wiki/ISO_8859-1
	                                Log.v(HttpRequest.LOG_TAG, stringObtained);
	                                // Parsing json data
	                                // If there is not error, it returns a json object
	                                if (!ErrorHandler.isError(requestStatus)) {
	                                	// Check the Result returned by Google
	                                	JSONObject jsonObject = new JSONObject(stringObtained);
	                                	// Check the status
	                                	if (jsonObject.has("status")) {
	                                		String resultStatus = jsonObject.getString("status");
	                                		// If the result status is not ok, then something went wrong
	                                		if (!resultStatus.equalsIgnoreCase("OK")) {
	                                			Log.e(LOG_TAG, "The result returned is not ok " + resultStatus);
	                                			jsonHandler.done(null, RequestStatus.ERROR_REQUEST_NOK_DATA_VALIDATION);
	                                			return;
	                                		}
	                                		
	                                		// if the result is not found, return error
	                                		if (!jsonObject.has("results")) {
	                                			Log.e(LOG_TAG, "The results does not exist");
	                                			jsonHandler.done(null, RequestStatus.ERROR_REQUEST_NOK_DATA_VALIDATION);
	                                			return;
	                                		}
	                                		
	                                		JSONArray jsonArray = new JSONArray(jsonObject.getString("results"));
			                                jsonHandler.done(jsonArray, requestStatus);
	                                		
	                                	// if the status does not exists, then there must be some data error
	                                	} else {
	                                		Log.e(LOG_TAG, "The status does not exist.");
	                                		jsonHandler.done(null, RequestStatus.ERROR_REQUEST_NOK_DATA_VALIDATION);
	                                	}
	                                } else {
	                                	jsonHandler.done(null, requestStatus);
	                                }
	                            // If there is any problem parsing the server data returned
	                            // Return the error about the server data
	                            } catch (Exception exception) {
	                                Log.e(LOG_TAG, exception.getLocalizedMessage(), exception);
	                                jsonHandler.done(null, RequestStatus.ERROR_SERVER_GENERIC);
	                            }
	                        } else {
	                            jsonHandler.done(null, requestStatus);
	                        }
                        }
                    });
                }
            });
        threadPool.execute(serverFetcher);
    }

    /**
     * Informs that the request has been finished or not.
     * @return True if the request has been finished.
     *         False otherwise
     */
    protected boolean hasFinished() {
        return (serverFetcher == null || !serverFetcher.isRunning);
    }

    /**
     * Method used to cancel the actual request.
     */
    protected void cancelRequest() {
        if (serverFetcher != null && serverFetcher.isRunning) {
            serverFetcher.stopFetching();
        }

        serverFetcher = null;
    }

    protected Uri getUri() {
        return uri;
    }

    /**
     * The runnable class used to connect with the server.
     */
    private class ServerFetcher implements Runnable {

        /**
         * The tag utilized for the log.
         */
        private static final String LOG_TAG = "ServerFetcher";

        /**
         * The registration time out before it launches the exception.
         */
        private static final int REGISTRATION_TIMEOUT = 3 * 1000;

        /**
         * The wait time out before it launches the exception.
         */
        private static final int WAIT_TIMEOUT = 30 * 1000;

        /**
         * The http client utilized.
         */
        private final HttpClient httpClient = new DefaultHttpClient();

        /**
         * The variable to record the running state (Yes/No).
         */
        private boolean isRunning = false;
        /**
         * The list of parameters of the HTTP client.
         */
        private final HttpParams params = httpClient.getParams();

        /**
         * The http response from the server.
         */
        private HttpResponse response;

        /**
         * The URI to connect.
         */
        private final Uri uri;

        /**
         * The list of parameters that includes in the http request.
         */
        private final Map<String, String> parameters;
        
        private HttpEntity entity;

        /**
         * The headers used.
         */
        private final Map<String, String> headerFields;

        /**
         * The callback to call when the operation finishes.
         */
        private final RequestStringCallback requestStringCallback;


        /**
         *  The final data obtained from the server.
         */
        private String stringObtained;

        /**
         * The main constructor.
         * @param requestMethod The REST method to perform
         * @param uri The Uri of the server to connect
         * @param parameters The list of parameters to be added to the HTTP request
         * @param headerFields The header of the HTTP request
         * @param requestDataCallback The Callback to call when the operation finishes
         */
        ServerFetcher(RequestMethod requestMethod, Uri uri, Map<String,
                String> parameters, HttpEntity entity, Map<String, String> headerFields,
                RequestStringCallback requestStringCallback) {
            this.uri = uri;
            this.parameters = parameters;
            this.entity = entity;
            this.headerFields = headerFields;
            this.requestStringCallback = requestStringCallback;
        }

        /**
         * The method which tells the actual state of the operation.
         * @return True if the server has not returned the response yet
         *         False otherwise
         */
        public boolean isRunning() {
            return isRunning;
        }

        /**
         * This methods stops the communication with the server.
         */
        public void stopFetching() {
            /*
             * TODO Cancel the http request
             */
        }

        @Override
        public void run() {
            Log.v(ServerFetcher.LOG_TAG, ServerFetcher.LOG_TAG + " running");
            isRunning = true;

            try {
                /*
                 * Set the connection parameters
                 */
                HttpConnectionParams.setConnectionTimeout(params, ServerFetcher.REGISTRATION_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, ServerFetcher.WAIT_TIMEOUT);
                ConnManagerParams.setTimeout(params, ServerFetcher.WAIT_TIMEOUT);

                /*
                 * Create ServerFetcher and prepare the data
                 */
                if (requestMethod == RequestMethod.RequestMethodGet) {
                    HttpGet httpGet = new HttpGet(uri.toString());

                    /*
                     * Add the headers
                     */
                    for (String key : headerFields.keySet()) {
                        httpGet.addHeader(key, headerFields.get(key));
                    }

                    // Check if the entity already exists
                    //Add the values in the parameters
                	String stringUrl = uri.toString();
                    if (parameters != null) {
                    	if (!stringUrl.endsWith("?")) {
                    		stringUrl += "?";
                    	}

                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        for (String key : parameters.keySet()) {
                            nameValuePairs.add(new BasicNameValuePair(key, parameters.get(key)));
                        }
                        
                        String paramString = URLEncodedUtils.format(nameValuePairs,"UTF-8");
                        stringUrl += paramString;
                    }
                    httpGet.setURI(new URI(stringUrl));
                    /*
                     * Response from the Http Request
                     */
                    response = httpClient.execute(httpGet);

                } else if (requestMethod == RequestMethod.RequestMethodPost) {
                    HttpPost httpPost = new HttpPost(uri.toString());

                    /*
                     * Add the headers
                     */
                    for (String key : headerFields.keySet()) {
                        httpPost.addHeader(key, headerFields.get(key));
                    }

                    // Check if the entity already exists
                    if (entity != null) {
                    	httpPost.setEntity(entity);
                    } else {
	                    //Add the values in the parameters
	                    if (parameters != null) {
	                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	                        for (String key : parameters.keySet()) {
	                            nameValuePairs.add(new BasicNameValuePair(key, parameters.get(key)));
	                        }
	                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
	                    }
                    }

                    /*
                     * Response from the Http Request
                     */
                    response = httpClient.execute(httpPost);

                } else if (requestMethod == RequestMethod.RequestMethodPut) {
                	HttpPut httpPut = new HttpPut(uri.toString());
                    /*
                     * Add the headers
                     */
                    for (String key : headerFields.keySet()) {
                    	httpPut.addHeader(key, headerFields.get(key));
                    }

                    // Check if the entity already exists
                    if (entity != null) {
                    	httpPut.setEntity(entity);
                    } else {
	                    //Add the values in the parameters
	                    if (parameters != null) {
	                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	                        for (String key : parameters.keySet()) {
	                            nameValuePairs.add(new BasicNameValuePair(key, parameters.get(key)));
	                        }
	                        httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
	                    }
                    }

                    //Send the http put
                    response = httpClient.execute(httpPut);
                } else if (requestMethod == RequestMethod.RequestMethodDelete) {
                	HttpDelete httpDelete = new HttpDelete(uri.toString());
                    /*
                     * Add the headers
                     */
                    for (String key : headerFields.keySet()) {
                    	httpDelete.addHeader(key, headerFields.get(key));
                    }

                    // Sending the request to the server
                    response = httpClient.execute(httpDelete);
                }

                StatusLine statusLine = response.getStatusLine();
                Log.v(ServerFetcher.LOG_TAG, "Status code " + statusLine.getStatusCode());

                int responseCode = statusLine.getStatusCode();
                if (responseCode == HttpStatus.SC_OK){
                    // Get the returned data
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    stringObtained = out.toString("UTF8");

                    if (responseCode == HttpStatus.SC_OK) {
                        requestStatus = RequestStatus.REQUEST_OK;
                    // For the rest of the cases, there is an error.
                    } else {
                    	requestStatus = RequestStatus.ERROR_REQUEST_NOK;
                    }
                } else {
                    requestStatus = RequestStatus.ERROR_REQUEST_NOK;
                }
            } catch (HttpHostConnectException httpHostConnectException) {
            	Log.e(LOG_TAG, httpHostConnectException.getLocalizedMessage(), httpHostConnectException);
            	requestStatus = RequestStatus.ERROR_REQUEST_NOK_HTTP_HOST_NO_CONNECTION;
            } catch (ConnectTimeoutException connectTimeoutException) {
            	Log.e(LOG_TAG, connectTimeoutException.getLocalizedMessage(), connectTimeoutException);
            	requestStatus = RequestStatus.ERROR_REQUEST_NOK_HTTP_HOST_NO_CONNECTION;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error requesting data to the backend. " + uri.toString(), e);
                requestStatus = RequestStatus.ERROR_REQUEST_NOK;
            } finally {
                isRunning = false;
                if (requestStringCallback != null) {
                	requestStringCallback.done(stringObtained, requestStatus);
                }
            }
        }
    }

    /**
     * The class which creates the threadPool as singleton.
     */
    private static final class ExecutorServiceSingleton {

        /**
         * The number of the threads which are running in parallel.
         */
        private static final int MAXIMUM_NUM_RUNNING_THREAD = 4;

        /**
         * A class to hold the singleton.
         */
        private static class SingletonHolder {
            /**
             * The instance of the class.
             */
            private static final ExecutorServiceSingleton INSTANCE = new ExecutorServiceSingleton();
        }

        /**
         * Creates the executor server as soft reference.
         */
        private SoftReference<ExecutorService> executorServiceReference = new SoftReference<ExecutorService>(
                createExecutorService());

        /**
         * The empty constructor of the class.
         */
        private ExecutorServiceSingleton() {
        };

        /**
         * The public method to return the instance.
         * @return A instance of the Singleton holder
         */
        static ExecutorServiceSingleton instance() {
            return SingletonHolder.INSTANCE;
        }

        /**
         * Method used to get the executor service.
         * @return The executor service. Create it if it has not been created before
         */
        ExecutorService getExecutorService() {
            ExecutorService executorService = executorServiceReference.get();

            if (executorService == null) {
                // (the reference was cleared)
                executorService = createExecutorService();
                executorServiceReference = new SoftReference<ExecutorService>(executorService);
            }

            return executorService;
        }

        /**
         * The method which creates a threadPool with limit number of threads.
         * Those threads will be re-utilized and they should exist while the application is running.
         * @return A threadPool with always same number of threads
         */
        private ExecutorService createExecutorService() {
            return Executors.newFixedThreadPool(ExecutorServiceSingleton.MAXIMUM_NUM_RUNNING_THREAD);
        }
    }
}
