package com.jiahaoliuliu.nearestrestaurants.session;

import java.util.ArrayList;
import java.util.List;

import com.jiahaoliuliu.nearestrestaurants.models.Restaurant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RestaurantDBAdapter {

	private static final String LOG_TAG = RestaurantDBAdapter.class.getSimpleName();
	
	private static final String DATABASE_NAME = "Reataurants.db";
	
	private static final int DATABASE_VERSION = 5;

	private static final String DATABASE_TABLE = "restaurant";

	//Database fields
	private static final String KEY_ROW_ID = "_id";
	private static final String KEY_NAME_ID = "name";
	private static final String KEY_LATITUDE_ID = "latitude";
	private static final String KEY_LONGITUDE_ID = "longitude";
	
	private Context context;
	private SQLiteDatabase database;
	private RestaurantDbHelper dbHelper;
	
	public RestaurantDBAdapter (Context context) {
		this.context = context;
		dbHelper = new RestaurantDbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void openDatabase() throws SQLException {
		try {
			Log.i(LOG_TAG, "Creating a new table in the database if needed" + DATABASE_NAME);
			database = dbHelper.getWritableDatabase();
			Log.i(LOG_TAG, "Writable db get");
		} catch (SQLiteException ex) {
			database = dbHelper.getReadableDatabase();
			Log.i(LOG_TAG, "Readable db get");
		}
	}

	public void closeDatabase() {
		dbHelper.close();
	}
	
	/**
	 * Register a new restaurant inside of the database.
	 */
	public boolean insertNewRestaurant (Restaurant restaurant) {
		if (database == null || !database.isOpen()) {
			openDatabase();
		}

		ContentValues restaurantValues = createValues(restaurant);
		int rowId =  (int)database.insert(DATABASE_TABLE, null, restaurantValues);
		restaurant.setId(rowId);
		return rowId > 0;
	}

	
	/**
	 * Update an existence restaurant
	 * @param restaurant The restaurant to be updated
	 * @return           True if the restaurant has been updated
	 *                   False otherwise
	 */
	public boolean updateRestaurant (Restaurant restaurant) {
		if (database == null || !database.isOpen()) {
			openDatabase();
		}

		ContentValues updateValues = createValues(restaurant);
		return (database.update
				(DATABASE_TABLE, updateValues, KEY_ROW_ID + "=" + restaurant.getId(), null) > 0);
	}

	/**
	 * Delete a restaurant from the database
	 * @param row_id The row id of the restaurant
	 * @return       The number of row removed
	 */
	public boolean deleteRestaurantByRowId (int row_id) {
		if (database == null || !database.isOpen()) {
			openDatabase();
		}

		return database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + row_id, null) > 0;
	}

	/**
	 * Delete the content of the database. Use it with caution.
	 * @return
	 */
	public int deleteAll() {
		if (database == null || !database.isOpen()) {
			openDatabase();
		}

		int result = 0;

		if (database.delete(DATABASE_TABLE, "1" , null) >0) {
			result = 0;
		} else {
			result = 1;
		}
		
		return result;
	}
	
	//Return a Restaurant. If there is any error, return null
	public Restaurant getRestaurantByRowId (int row_id) throws SQLException  {
		if (database == null || !database.isOpen()) {
			openDatabase();
		}

		Restaurant result = null;

		Cursor mCursor = 
				database.query(DATABASE_TABLE,
							   new String[] {KEY_ROW_ID,
											 KEY_NAME_ID,
											 KEY_LATITUDE_ID,
											 KEY_LONGITUDE_ID
							                 },
				                KEY_ROW_ID + "=" + row_id,
				                null,
				                null,
				                null,
				                null);
		result = getNewRowFromCursor (mCursor, 0);
		mCursor.close();
		return result;
	}

	/*
	 * Returns all the restaurants from the database
	 */
	public List<Restaurant> getAllRestaurants() {
		if (database == null || !database.isOpen()) {
			openDatabase();
		}

		Cursor mCursor = 
				database.query(DATABASE_TABLE,
						   new String[] {KEY_ROW_ID,
										 KEY_NAME_ID,
										 KEY_LATITUDE_ID,
										 KEY_LONGITUDE_ID
		                 				},
							    null,
				                null,
				                null,
				                null,
				                null);
		List<Restaurant> restaurants = getAllRowsFromCursor(mCursor);
		mCursor.close();
		return restaurants;
	}
	
	public class RestaurantDbHelper extends SQLiteOpenHelper{
			
		public RestaurantDbHelper (Context context, String databaseName, CursorFactory factory, int version) {
			super (context, databaseName, null, version);
		}
		
		private String CREATE_TABLE =
				"create table if not exists " + DATABASE_TABLE + " ( " +
										   KEY_ROW_ID + " integer primary key autoincrement, " +
										   KEY_NAME_ID + " text not null, " +
										   KEY_LATITUDE_ID + " real, " +
										   KEY_LONGITUDE_ID + " real);";
	
		// Method is called during creation of the database
		@Override
		public void onCreate (SQLiteDatabase database) {
			Log.i(LOG_TAG, "Creating a new database " + DATABASE_TABLE);
			database.execSQL(CREATE_TABLE);
		}
		
		// Method is called when the database has been opened
		@Override
		public void onOpen(SQLiteDatabase database) {
			Log.i(LOG_TAG, "Opening the database " + DATABASE_TABLE);
			database.execSQL(CREATE_TABLE);
		}
		
		// Method is called during an upgrade of the database, e.g. if you increase the database version
		@Override
		public void onUpgrade (SQLiteDatabase database, int oldVersion, int newVersion ){
			Log.w(LOG_TAG, "Upgrading database from version " + 
				  oldVersion + " to " + newVersion + " , which will destroy all old data");

			// Drop the old database
			database.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		}
	}

	//Private methods
	//Create a content values for the database based on a Restaurant
	// All the values are checked.
	private ContentValues createValues (Restaurant restaurant) {
		
		Log.i(LOG_TAG, "Creating the content values from a restaurant");
		ContentValues contentValues = new ContentValues();
	
		//Get each one of the fields
		// Name
		String name = restaurant.getName();
		contentValues.put(KEY_NAME_ID, name);

		// Latitude
		double latitude = restaurant.getPosition().latitude;
		contentValues.put(KEY_LATITUDE_ID, latitude);

		// Longitude
		double longitude = restaurant.getPosition().longitude;
		contentValues.put(KEY_LONGITUDE_ID, longitude);

		return contentValues;
	}

	//Return an restaurant. If there is any error, return null
	private Restaurant getNewRowFromCursor (Cursor mCursor, int position) {
		Restaurant result = null;
		
		// Get basic data
		if (mCursor == null) {
			Log.w(LOG_TAG, "Cursor = null. Not row found");
		} else if (!mCursor.moveToPosition(position)){
			Log.w(LOG_TAG, "Position not reachable. The number of elements is " + mCursor.getCount() + "  but requested to access " +
					"to the position " + position);
		} else {	
	
			// Row id
			int rowId = mCursor.getInt(mCursor.getColumnIndex(KEY_ROW_ID));
			
			// Name
			String name = mCursor.getString(mCursor.getColumnIndex(KEY_NAME_ID));

			// Latitude
			double latitude = mCursor.getDouble(mCursor.getColumnIndex(KEY_LATITUDE_ID));

			// Longitude
			double longitude = mCursor.getDouble(mCursor.getColumnIndex(KEY_LONGITUDE_ID));

			result = new Restaurant(rowId, name, latitude, longitude);
		}
		
		return result;
	}

	private List<Restaurant> getAllRowsFromCursor(Cursor mCursor) {
		List<Restaurant> restaurants = new ArrayList<Restaurant>();
		
		int count = mCursor.getCount();
		for (int i = 0; i < count; i++) {
			Restaurant tempRestaurant = getNewRowFromCursor(mCursor, i);
			restaurants.add(tempRestaurant);
		}
		
		return restaurants;
	}

}