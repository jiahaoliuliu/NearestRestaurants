package com.jiahaoliuliu.nearestrestaurants;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;
import com.jiahaoliuliu.nearestrestaurants.interfaces.Callback;
import com.jiahaoliuliu.nearestrestaurants.interfaces.OnPositionRequestedListener;
import com.jiahaoliuliu.nearestrestaurants.interfaces.OnUpdatePositionListener;
import com.jiahaoliuliu.nearestrestaurants.session.Session;
import com.jiahaoliuliu.nearestrestaurants.utils.PositionTracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

/**
 * The main class for which includes the map fragment and the list fragment.
 * It is the main fragment which creates the consistency between the fragments.
 * It is also the one which have the position of the user updated.
 * @author Jiahao Liu
 *
 */
public class NearestRestaurants extends SherlockFragmentActivity 
    implements OnPositionRequestedListener {

    private static final String LOG_TAG = NearestRestaurants.class.getSimpleName();

    // System data
    private Context context;
    private ActionBar actionBar;
    private Menu actionBarMenu;
    private FragmentManager fragmentManager;

    // Callback used to deal with the asynchronous operations
    private Callback actionBarMenuCallback;

    // The customized id of the action bar button
    private MenuItem menuViewListItem;
    private MenuItem menuViewMapItem;

    // The customized menu buttons id. This is because the normal id, which starts
    // with 0, could cause confusion.
    private static final int MENU_VIEW_LIST_BUTTON_ID = 10000;
    private static final int MENU_VIEW_MAP_BUTTON_ID = 10001;

    // Session
    private Session session;

    // For the users positions
    private PositionTracker positionTracker;
    // The broadcast receiver for the position
    private MyPositionBroadcastReceiver myPositionBReceiver;
    private LatLng myPosition;

    // The fragments
    private NearestRestaurantsMapFragment mapFragment;
    private NearestRestaurantsListFragment listFragment;

    // The interface for the fragment
    private OnUpdatePositionListener onUpdatePositionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearest_restaurants_layout);

        context = this;
        actionBar = getSupportActionBar();

        session = Session.getCurrentSession(context);
        fragmentManager = getSupportFragmentManager();

        // Get the user's position
        positionTracker = new PositionTracker(context);
        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter(PositionTracker.BROADCAST_POSITION_ACTION);
        myPositionBReceiver = new MyPositionBroadcastReceiver();
        context.registerReceiver(myPositionBReceiver, filter);

        // The first fragment shown in the map fragment
        showMapFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Show the users last position if it was set
        setSupportProgressBarIndeterminateVisibility(true);
        LatLng userLastPosition = session.getLastUserPosition();
        if (userLastPosition != null) {
            myPosition = userLastPosition;
            Toast.makeText(context, getResources().getString(R.string.updating_users_position), Toast.LENGTH_LONG).show();

            // Update the fragments position
            if (onUpdatePositionListener != null) {
                onUpdatePositionListener.updatePosition(myPosition);
            }
        } else {
            Toast.makeText(context, getResources().getString(R.string.looking_users_position), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Disable any indeterminate progress bar
        setSupportProgressBarIndeterminateVisibility(false);
    }

    /**
     * The Broadcast receiver registered to receive the position update
     * Intent from the position tracker.
     */
    private class MyPositionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "New position received.");
            double latitude = intent.getDoubleExtra(PositionTracker.BROADCAST_POSITION_LATITUDE, PositionTracker.DEFAULT_LATITUDE);
            double longitude = intent.getDoubleExtra(PositionTracker.BROADCAST_POSITION_LONGITUDE, PositionTracker.DEFAULT_LONGITUDE);
            
            if (latitude == PositionTracker.DEFAULT_LATITUDE || longitude == PositionTracker.DEFAULT_LONGITUDE) {
                Log.e(LOG_TAG, "Wrong position found: " + latitude + ":" + longitude);
                return;
            }

            Log.v(LOG_TAG, "The new position is " + latitude + " ," + longitude);

            // Update the position
            myPosition = new LatLng(latitude, longitude);
            session.setLastUserPosition(myPosition);
            if (onUpdatePositionListener != null) {
                onUpdatePositionListener.updatePosition(myPosition);
            }

            // Disable the progress bar
            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        positionTracker.finish();
        // Unregister the Broadcast receiver
        if (myPositionBReceiver != null) {
            context.unregisterReceiver(myPositionBReceiver);
        }
    }

    // =============================================== ActionBar =======================================================
    // Use the action bar to switch views
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Get the value of Menu
        actionBarMenu = menu;
        if (actionBarMenuCallback != null) {
            actionBarMenuCallback.done();
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Show the list fragment
        if (item.getItemId() == MENU_VIEW_LIST_BUTTON_ID) {
            showListFragment();
        // Show the map fragment
        } else if (item.getItemId() == MENU_VIEW_MAP_BUTTON_ID) {
            showMapFragment();
        }
        
        return true;
    }

    /**
     * Show the map fragment
     */
    private void showMapFragment() {
        // Create it if it has not been created before
        if (mapFragment == null) {
            mapFragment = new NearestRestaurantsMapFragment();
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, mapFragment, NearestRestaurantsMapFragment.class.toString());
        ft.commit();

        // Update the onUpdatePositionListener
        onUpdatePositionListener = mapFragment;

        // Modify the action bar menu to adapt it to the map fragment
        if (actionBarMenu == null) {
            actionBarMenuCallback = new Callback() {

                @Override
                public void done() {
                    showMapActionBar();
                    
                    // Null the call back so it won't be
                    // called again
                    actionBarMenuCallback = null;
                }
            };
        } else {
            showMapActionBar();
        }
    }

    /**
     * Show the action bar related with the map fragment.
     */
    private void showMapActionBar() {
        if (actionBarMenu == null) {
            Log.e(LOG_TAG, "Trying to adapt the action bar to the map when it is null");
            return;
        }

        // Remove any previous menu item
        actionBarMenu.clear();
        // Set the initial state of the menu item
        menuViewListItem = actionBarMenu.add(Menu.NONE, MENU_VIEW_LIST_BUTTON_ID, Menu
                .NONE, getResources().getString(R.string.action_bar_show_list));
        menuViewListItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    /**
     * Show the list fragment
     */
    private void showListFragment() {
        if (listFragment == null) {
            listFragment = new NearestRestaurantsListFragment();
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, listFragment, NearestRestaurantsListFragment.class.toString());
        ft.commit();
        
        // Update the onUpdatePositionListener
        onUpdatePositionListener = listFragment;
        
        // Modify the action bar menu to adapt it to the map fragment
        if (actionBarMenu == null) {
            actionBarMenuCallback = new Callback() {

                @Override
                public void done() {
                    showListActionBar();
                    
                    // Null the call back so it won't be
                    // called again
                    actionBarMenuCallback = null;
                }
            };
        } else {
            showListActionBar();
        }
    }

    /**
     * Show the action bar related with the map fragment
     */
    private void showListActionBar() {
        if (actionBarMenu == null) {
            Log.e(LOG_TAG, "Trying to adapt the action bar to the map when it is null");
            return;
        }

        // Remove any previous menu item
        actionBarMenu.clear();
        // Set the initial state of the menu item
        menuViewMapItem = actionBarMenu.add(Menu.NONE, MENU_VIEW_MAP_BUTTON_ID, Menu
                .NONE, getResources().getString(R.string.action_bar_show_map));
        menuViewMapItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    // =============================================== Interfaces =======================================================
    @Override
    public LatLng requestPosition() {
        return myPosition;
    }
}