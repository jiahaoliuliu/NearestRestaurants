package com.jiahaoliuliu.nearestrestaurants.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Restaurant {
	private static final String LOG_TAG = Restaurant.class.getSimpleName();

	private static final String NAME_KEY = "name";
	private String name;
	
	private static final String GEOMETRY_KEY = "geometry";
	private static final String LOCATION_KEY = "location";
	private static final String LATITUDE_KEY = "lat";
	private static final String LONGITUDE_KEY = "lng";
	
	private LatLng position;

	public Restaurant() {
		super();
	}

	public Restaurant(String name, LatLng position) {
		super();
		this.name = name;
		this.position = position;
	}

	// Getting the restaurant by JSON.
	// Since there is only few useful data,
	// no extra parser is needed. (GSON, Jackson)
	// Here is an example:
	/* {
    "geometry" : {
        "location" : {
           "lat" : -33.86820,
           "lng" : 151.1945860
        }
     },
     "icon" : "http://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png",
     "id" : "268f3a99e1bd1a67b57fc561f67135d071c21a2c",
     "name" : "The Star",
     "rating" : 4.20,
     "reference" : "CnRlAAAAkiq-buNnUe3P5uFPUYnQgs6w7AfPjMW72Oc_geqe3PVD2CGRFWG0O4AgQVwSOmIJ6ybuDAAnVMih48rVQtY5nm26s3UvLA-ssFActUG5O1ewNbuFWX6Ju0Tt9RvtngW6qPIMyaXvIp2W3OAZP8cQBxIQwb-1uGtXr66nibtELRX8SBoULLTlL1odZ_COiuuMN0u_R-Ilx6A",
     "types" : [ "restaurant", "lodging", "food", "casino", "establishment" ],
     "vicinity" : "80 Pyrmont Street, Pyrmont"
  	}, */
	public Restaurant(String jsonString) {
		try {
			JSONObject jsonObject = new JSONObject(jsonString);

			// Get the name
			if (jsonObject.has(NAME_KEY)) {
				name = jsonObject.getString(NAME_KEY);
			}

			// Get the Position
			if (jsonObject.has(GEOMETRY_KEY)) {
				JSONObject geometryJSONObject = jsonObject.getJSONObject(GEOMETRY_KEY);
				if (geometryJSONObject.has(LOCATION_KEY)) {
					JSONObject locationJSONObject = geometryJSONObject.getJSONObject(LOCATION_KEY);
					if (locationJSONObject.has(LATITUDE_KEY)) {
						double latitude = locationJSONObject.getDouble(LATITUDE_KEY);
						double longitude = locationJSONObject.getDouble(LONGITUDE_KEY);
						position = new LatLng(latitude, longitude);
					}
				}
			}
		} catch (JSONException e) {
			Log.e(LOG_TAG, "Error parsing the restaurant", e);
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LatLng getPosition() {
		return position;
	}

	public void setPosition(LatLng position) {
		this.position = position;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Restaurant other = (Restaurant) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Restaurant [name=" + name + 
				", position=" + position.latitude + "," + position.longitude  + "]";
	}
}
