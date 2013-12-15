package com.jiahaoliuliu.nearestrestaurants;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jiahaoliuliu.nearestrestaurants.models.Restaurant;

/**
 * The base adapter for the list of the restaurants.
 */
public class RestaurantListAdapter extends BaseAdapter {
    
    private Context context;
    private List<Restaurant> restaurants;
    private LayoutInflater inflater;
    
    public RestaurantListAdapter(Context context, List<Restaurant> restaurants) {
        this.context = context;
        this.restaurants = restaurants;
        inflater = LayoutInflater.from(context);
    }

    // Reset the list of the restaurants
    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
        notifyDataSetChanged();
    }

    // Add new restaurants to the list
    public void addMoreRestaurants(List<Restaurant> newRestaurants) {
    	this.restaurants.addAll(newRestaurants);
    	notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return restaurants.size();
    }

    @Override
    public Object getItem(int position) {
        return restaurants.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        // Keeps reference to avoid future findViewById()
    	RestaurantViewHolder restaurantViewHolder;
 
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_layout, viewGroup, false);
 
            restaurantViewHolder = new RestaurantViewHolder();
            restaurantViewHolder.nameTextView =
            		(TextView) convertView.findViewById(R.id.restaurantNameTextView);
            restaurantViewHolder.vicinityTextView =
            		(TextView) convertView.findViewById(R.id.restaurantVicinityTextView);

            convertView.setTag(restaurantViewHolder);
        } else {
        	restaurantViewHolder = (RestaurantViewHolder) convertView.getTag();
        }
 
        Restaurant restaurant = restaurants.get(position);

        // Set the name
        restaurantViewHolder.nameTextView.setText(restaurant.getName());
        
        // Set the vicinity
        String vicinity = restaurant.getVicinity();
        if (vicinity != null && !vicinity.equalsIgnoreCase("")) {
        	restaurantViewHolder.vicinityTextView.setText(vicinity);
        }
        
        return convertView;
    }
    
    static class RestaurantViewHolder {
        TextView nameTextView;
        TextView vicinityTextView;
    }
}