package com.wzbicycle.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {

	// ���ݿ�Ľ���������SQLiteOpenHelper����ʵ��
	// ���ݿ��ļ��Ľ����ڹ��캯����ʵ��
	// ���ݿ�����Ϣ�Ľ���������onCreate��ʵ��
	// ���ݿ�汾�ĸ�����onUpgrade��ʵ��
	
	private static final String DATABASE_NAME="UserDb.db";
	public static final String USER_TABLE="Contact";						//����
	public static final String DATA_ID="_id";
	public static final String NAME="name";									//�ǳ�
	public static final String USER_ID = "userid";							//�û���¼id
	public static final String SEARCH_RANGE="searchrange";					//���ҷ�Χ
	public static final String AVAIL_BORROWED="availborrowed";				//�ɽ���
	public static final String AVAIL_RETURN="availreturn";					//�ɻ���
	
	SQLiteDatabase db;
	
	public DataHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);//��4�������ǰ汾��
		//���汾�Ŵ���ԭ���ݿ�汾��ʱ�����Զ�����onUpgrade
		//�������ݿ⣬�ڶ������������ݿ���������ddms��file explore�������data/data/��Ŀ�ļ���/databases�н���Ӧ���ݿ⵼����
		
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS Contact");//ɾ����

		db.execSQL("CREATE TABLE  Contact (_id INTEGER PRIMARY KEY AUTOINCREMENT, userid TEXT,name TEXT, searchrange INTEGER, availborrowed INTEGER, availreturn INTEGER)");
	
		// onCreateֻ�����ݿ�δ����ʱ�״�ʹ�ã������󣬹ر�activity�ٿ���activityҲδ���ܴ�����onCreate
		ContentValues cv = new ContentValues();
		cv.put(NAME, "δ��¼");
		cv.put(USER_ID, "");//ContentValues������map��Ĳ�����ǰ����key��������value
		cv.put(SEARCH_RANGE,500);
		cv.put(AVAIL_BORROWED, 0);
		cv.put(AVAIL_RETURN, 0);
		db.insert(USER_TABLE, USER_ID, cv);//��һ�������Ǳ������ڶ�������������Ϊ�յ��У������������ǲ���ļ�¼	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
//		db.execSQL("DROP TABLE IF EXISTS Contact");//ɾ����

		onCreate(db);//����û�и��£�ֻ�ǵ���onCreate���½���һ�±�
	}
	
	public void clearData(SQLiteDatabase db){//ɾ����ϵ�˺Ͷ��ű��ٴ�����Ӧ�ձ�
		
		db.execSQL("DROP TABLE IF EXISTS Contact");//ɾ����

		
		onCreate(db);//����û�и��£�ֻ�ǵ���onCreate���½���һ�±�
		
	}

}
