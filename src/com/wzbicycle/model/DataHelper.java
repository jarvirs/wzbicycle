package com.wzbicycle.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {

	// 数据库的建立常采用SQLiteOpenHelper类来实现
	// 数据库文件的建立在构造函数中实现
	// 数据库表等信息的建立往往在onCreate中实现
	// 数据库版本的更新在onUpgrade中实现
	
	private static final String DATABASE_NAME="UserDb.db";
	public static final String USER_TABLE="Contact";						//表名
	public static final String DATA_ID="_id";
	public static final String NAME="name";									//昵称
	public static final String USER_ID = "userid";							//用户登录id
	public static final String SEARCH_RANGE="searchrange";					//查找范围
	public static final String AVAIL_BORROWED="availborrowed";				//可借数
	public static final String AVAIL_RETURN="availreturn";					//可还数
	
	SQLiteDatabase db;
	
	public DataHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);//第4个参数是版本号
		//当版本号大于原数据库版本号时，会自动调用onUpgrade
		//创建数据库，第二个参数是数据库名，可在ddms中file explore从虚拟机data/data/项目文件包/databases中将相应数据库导出来
		
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS Contact");//删除表

		db.execSQL("CREATE TABLE  Contact (_id INTEGER PRIMARY KEY AUTOINCREMENT, userid TEXT,name TEXT, searchrange INTEGER, availborrowed INTEGER, availreturn INTEGER)");
	
		// onCreate只在数据库未建立时首次使用，建立后，关闭activity再开启activity也未必能触发该onCreate
		ContentValues cv = new ContentValues();
		cv.put(NAME, "未登录");
		cv.put(USER_ID, "");//ContentValues类似于map类的操作，前面是key，后面是value
		cv.put(SEARCH_RANGE,500);
		cv.put(AVAIL_BORROWED, 0);
		cv.put(AVAIL_RETURN, 0);
		db.insert(USER_TABLE, USER_ID, cv);//第一个参数是表名，第二个参数是允许为空的列，第三个参数是插入的记录	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
//		db.execSQL("DROP TABLE IF EXISTS Contact");//删除表

		onCreate(db);//这里没有更新，只是调用onCreate重新建了一下表
	}
	
	public void clearData(SQLiteDatabase db){//删除联系人和短信表，再创建相应空表
		
		db.execSQL("DROP TABLE IF EXISTS Contact");//删除表

		
		onCreate(db);//这里没有更新，只是调用onCreate重新建了一下表
		
	}

}
