package com.jiahaoliuliu.nearestrestaurants.interfaces;

public interface OnProgressBarShowRequestListener {

	/**
	 * Show the progress bar to the user
	 * It is requested for a time consuming operation
	 */
	public void showProgressBar();
	
	/**
	 * Hide the progress bar shown
	 */
	public void hidePorgressBar();
}
