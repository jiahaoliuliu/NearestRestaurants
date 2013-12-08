package com.jiahaoliuliu.nearestrestaurants.interfaces;

import com.google.android.gms.maps.model.LatLng;

public interface OnUpdatePositionListener {

	public abstract void updatePosition(LatLng newPosition);
}
