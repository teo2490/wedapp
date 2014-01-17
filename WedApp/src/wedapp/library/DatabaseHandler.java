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

	/**
	 * It is used to create the SQLite database
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
//		String CREATE_LOGIN_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LOGIN + "("
//				+ KEY_EMAIL + " TEXT PRIMARY KEY," 
//				+ KEY_NAME + " TEXT,"
//				+ KEY_CITY + " TEXT,"
//				+ KEY_ADDRESS + " TEXT,"
//				+ KEY_BUILD + " TEXT,"
//				+ KEY_PHONE + " TEXT" + ")";
//		db.execSQL(CREATE_LOGIN_TABLE);
	}
	
	/**
	 * It is used to create Login table in SQLite database
	 */
	public void createLogin() {
		String CREATE_LOGIN_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LOGIN + "("
				+ KEY_EMAIL + " TEXT PRIMARY KEY," 
				+ KEY_NAME + " TEXT,"
				+ KEY_CITY + " TEXT,"
				+ KEY_ADDRESS + " TEXT,"
				+ KEY_BUILD + " TEXT,"
				+ KEY_PHONE + " TEXT" + ")";
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(CREATE_LOGIN_TABLE);
	}
	
	/**
	 * It is used to create List table in SQLite database
	 */
	public void createList(){
		String CREATE_LIST_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LIST + "("
				+ KEY_ID + " TEXT PRIMARY KEY" + ")";
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(CREATE_LIST_TABLE);
	}

	/**
	 * It deletes old tables (if they exist) and creates a new database.
	 * (This method is mandatory)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);

		// Create tables again
		onCreate(db);
	}
	
	/**
	 * It deletes old tables (if they exist)
	 */
	public void upgradeDatabase() {
		SQLiteDatabase db = this.getWritableDatabase();
		//db.delete(TABLE_LOGIN, null, null);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
		//onCreate(db);
	}

	/**
	 * It stores merchant details in database
	 * 
	 * @param email e-mail of the shop
	 * @param name name of the shop
	 * @param city city in which the shop is placed
	 * @param address address of the shop
	 * @param build_number build number of the shop
	 * @param phone phone number of the shop
	 */
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
	
	/**
	 * It stores list id in database
	 * 
	 * @param id id of the list
	 */
	public void addList(String id) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ID, id);
		db.insert(TABLE_LIST, null, values);
		db.close();
	}
	
	/**
	 * It gets merchant's data from the database
	 * 
	 * @return HashMap<String, String> key-value pairs for merchant's data
	 */
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
	
	/**
	 * It gets list id
	 * 
	 * @return String the list id
	 */
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
	 * It is used to check if a row is in SQLite database
	 * 
	 * @return int number of row counted (either 0 or 1)
	 */
	public int getRowLoginCount() {
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
	 * It is used to check if a row is in SQLite database
	 * 
	 * @return int number of row counted (either 0 or 1)
	 */
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
	 * It deletes Login table
	 */
	public void resetLoginTables(){
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_LOGIN, null, null);
		db.close();
	}
	
	/**
	 * It deletes Login table
	 */
	public void resetListTable(){
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_LIST, null, null);
		db.close();
	}

}