package com.wzbicycle.model;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;

import android.R.integer;

//�û���Ϣ��
public class UserData {

	public static double Latitute;				//��ǰ��λ��γ��
	public static double Longitute;				//��ǰ��λ�ľ���
	
	public String Id;							//�û���¼ID
	public String Name;							//�û��ǳ�
	public int SearchRange;						//���ҷ�Χ
	public int AvailBorrowed;					//�ɽ賵��
	public int AvailReturn;						//�ɻ�����
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
