package com.wzbicycle.model;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;

import android.R.integer;

//用户信息类
public class UserData {

	public static double Latitute;				//当前定位的纬度
	public static double Longitute;				//当前定位的经度
	
	public String Id;							//用户登录ID
	public String Name;							//用户昵称
	public int SearchRange;						//查找范围
	public int AvailBorrowed;					//可借车数
	public int AvailReturn;						//可还车数
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public int getSearchRange() {
		return SearchRange;
	}
	public void setSearchRange(int searchRange) {
		SearchRange = searchRange;
	}
	public int getAvailBorrowed() {
		return AvailBorrowed;
	}
	public void setAvailBorrowed(int availBorrowed) {
		AvailBorrowed = availBorrowed;
	}
	public int getAvailReturn() {
		return AvailReturn;
	}
	public void setAvailReturn(int availReturn) {
		AvailReturn = availReturn;
	}
	
	
}
