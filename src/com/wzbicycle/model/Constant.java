package com.wzbicycle.model;


//全局常量定义累
public class Constant {

	public static final int USER_SETTING_ITEM0 = 0;				//用户设置界面第0个条目
	public static final int USER_SETTING_ITEM1 = 1;				//用户设置界面第1个条目
	public static final int USER_SETTING_ITEM2 = 2;				//用户设置界面第2个条目
	
	public static final int STATION_LOAD_ALL = 1;				//站点全部加载模式
	public static final int STATION_LOAD_PRIORITY = 2;			//站点推荐加载模式
	
	public static final int SEARCH_RANGE[] = new int[]{500,1500,2000,5000};	//查找范围
	public static final int AVAIL_BORROWED[] = new int[]{1,15,30,-1};	//可借数量设置范围
}
