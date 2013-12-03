package com.jiahaoliuliu.nearestrestaurants;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class NearestRestaurants extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nearest_restaurants_layout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nearest_restaurants, menu);
		return true;
	}

}
