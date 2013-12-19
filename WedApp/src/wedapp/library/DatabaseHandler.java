package wedapp.library;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "wedapp";

	// Login table name
	private static final String TABLE_LOGIN = "login";
	private static final String TABLE_LIST = "list";

	// Login Table Columns names
	private static final String KEY_EMAIL = "email";
	private static final String KEY_NAME = "name";
	private static final String KEY_CITY = "city";
	private static final String KEY_ADDRESS = "address";
	private static final String KEY_BUILD = "build_number";
	private static final String KEY_PHONE = "phone";
	private static final String KEY_ID = "_id";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
//		String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
//				+ KEY_EMAIL + " TEXT PRIMARY KEY," 
//				+ KEY_NAME + " TEXT,"
//				+ KEY_CITY + " TEXT,"
//				+ KEY_ADDRESS + " TEXT,"
//				+ KEY_BUILD + " TEXT,"
//				+ KEY_PHONE + " TEXT" + ")";
//		db.execSQL(CREATE_LOGIN_TABLE);
	}
	
	public void createList(){
		String CREATE_LIST_TABLE = "CREATE TABLE " + TABLE_LIST + "("
				+ KEY_ID + " TEXT PRIMARY KEY" + ")";
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(CREATE_LIST_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);

		// Create tables again
		onCreate(db);
	}
	
	public void upgradeDatabase() {
		SQLiteDatabase db = this.getWritableDatabase();
		//db.delete(TABLE_LOGIN, null, null);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
		//onCreate(db);
	}

	/**
	 * Storing user details in database
	 * */
	public void addUser(String email, String name, String city, String address, String build_number, String phone) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_EMAIL, email);
		values.put(KEY_NAME, name);
		values.put(KEY_CITY, city);
		values.put(KEY_ADDRESS, address);
		values.put(KEY_BUILD, build_number);
		values.put(KEY_PHONE, phone);

		// Inserting Row
		db.insert(TABLE_LOGIN, null, values);
		db.close(); // Closing database connection
	}
	
	public void addList(String id) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ID, id);
		db.insert(TABLE_LIST, null, values);
		db.close();
	}
	
	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> getUserDetails(){
		HashMap<String,String> user = new HashMap<String,String>();
		String selectQuery = "SELECT * FROM " + TABLE_LOGIN;
		 
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
        	user.put("email", cursor.getString(0));
        	user.put("name", cursor.getString(1));
        	user.put("city", cursor.getString(2));
        	user.put("address", cursor.getString(3));
        	user.put("build_number", cursor.getString(4));
        	user.put("phone", cursor.getString(5));
        }
        cursor.close();
        db.close();
		return user;
	}
	
	public String getListId() {
		String list = new String();
		String selectQuery = "SELECT * FROM " + TABLE_LIST;
		SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
        	list = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return list;
	}

	/**
	 * Getting user login status
	 * return true if rows are there in table
	 * */
	public int getRowCount() {
		String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();
		
		// return row count
		return rowCount;
	}
	
	/**
	 * Getting user login status
	 * return true if rows are there in table
	 * */
	public int getRowListCount() {
		String countQuery = "SELECT  * FROM " + TABLE_LIST;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();
		
		// return row count
		return rowCount;
	}
	
	/**
	 * Re crate database
	 * Delete all tables and create them again
	 * */
	public void resetTables(){
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_LOGIN, null, null);
		db.close();
	}
	
	public void resetListTable(){
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_LIST, null, null);
		db.close();
	}

}