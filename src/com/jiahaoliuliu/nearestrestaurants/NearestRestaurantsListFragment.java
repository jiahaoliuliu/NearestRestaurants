package com.jiahaoliuliu.nearestrestaurants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class NearestRestaurantsListFragment extends SherlockFragment{

	@Override  
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,  
	    Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_layout, container, false);
	  }
}
