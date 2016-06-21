package com.wzbicycle.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

//���ݿ������
public class DataOperator {

	private Context context;
	private SQLiteDatabase db;
	private DataHelper dataHelper;
	
	public DataOperator(Context context) {
		// TODO Auto-generated constructor stub
		this.context=context;
	}
	
	//�����ݿ�
	public void openDb(){
		if(db==null){
			dataHelper=new DataHelper(context);
			db=dataHelper.getWritableDatabase();
		}else{
			if(!db.isOpen()){
				dataHelper=new DataHelper(context);
				db=dataHelper.getWritableDatabase();
			}

		}
	}
	//�ر����ݿ�
	public void closeDb(){
		if(db!=null){
			if(db.isOpen()){
				db.close();
				Log.e("close database", "database closed");
			}
		}
	}
	
	//�����û�����,�����û���¼id�ж�
	public long insertUserData(UserData userData,String Id){
		long rowid = -1;
		if(db == null){
			openDb();
		}
		
	 	Cursor cursor = db.query(DataHelper.USER_TABLE, null, DataHelper.DATA_ID + "=? ",
				new String[]{userData.getId()}, null, null, null);

	 	if(cursor.getCount() <= 0){
	 		rowid = db.insert(DataHelper.USER_TABLE, null, enCodeContentValues(userData));
	 	}
	 	
	 	cursor.close();

		return rowid;
	}
	//����
	public void updatePhoneData(UserData newUserData){
		//��PhoneData��dataId������������
		if(db==null){
			openDb();
		}

		db.update(DataHelper.USER_TABLE, enCodeContentValues(newUserData), DataHelper.USER_ID + "=? ", new String[]{newUserData.getId()});

	}
	
	public ArrayList<UserData> getAllUserList(){// ȡ��ȫ����ϵ��
		Cursor cursor;
		if(db==null){
			openDb();
		}
		ArrayList<UserData> userList=new ArrayList<UserData>();

		cursor = db.rawQuery("select * from Contact", null);
		
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			for(int i = 0; i < cursor.getCount(); i++,cursor.moveToNext()){
				UserData userData = new UserData();
				userData.setId(cursor.getString(cursor.getColumnIndex(DataHelper.USER_ID)));	
				userData.setName(cursor.getString(cursor.getColumnIndex(DataHelper.NAME)));
				userData.setSearchRange(cursor.getInt(cursor.getColumnIndex(DataHelper.SEARCH_RANGE)));
				userData.setAvailBorrowed(cursor.getInt(cursor.getColumnIndex(DataHelper.AVAIL_BORROWED)));
				userData.setAvailReturn(cursor.getInt(cursor.getColumnIndex(DataHelper.AVAIL_RETURN)));

				userList.add(userData);
			}
		}
		else{
			
		}

		cursor.close();

		return userList;

	}
	
	public ContentValues enCodeContentValues(UserData userData){
		ContentValues cv=new ContentValues();
		cv.put(DataHelper.NAME, userData.getName());
		cv.put(DataHelper.USER_ID, userData.getId());
		cv.put(DataHelper.SEARCH_RANGE, userData.getSearchRange());
		cv.put(DataHelper.AVAIL_BORROWED,userData.getAvailBorrowed());
		cv.put(DataHelper.AVAIL_RETURN, userData.getAvailReturn());

		return cv;

	}

}
