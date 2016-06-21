package com.wzbicycle.controller;

import java.util.ArrayList;

import com.example.wzbicycle.R;
import com.wzbicycle.model.Constant;
import com.wzbicycle.view.CustomDialog;
import com.wzbicycle.view.CustomDialog.Builder;
import com.wzbicycle.view.UserSettingAdapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.media.session.PlaybackState.CustomAction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

//�û�����
public class UserInfoActivity extends Activity{

	private ListView listView;
	private TextView textView;
	private ArrayList<String>listName;
	private ArrayList<Integer>listPic;
	
	private UserSettingAdapter arrayAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		
		initListName();													//��ʼ���û����ý�����б��������
		
		textView = (TextView)findViewById(R.id.textView1);
		textView.setText("��ѯ��Χ��" + (MainActivity.userData.getSearchRange())
				+ " Ҫ��ɽ�������" + ( MainActivity.userData.getAvailBorrowed() > -1 ? MainActivity.userData.getAvailBorrowed() : ">30")
				+ " Ҫ��ɻ�������" + ( MainActivity.userData.getAvailReturn() > -1 ? MainActivity.userData.getAvailReturn() : ">30"));
		
		listView = (ListView)findViewById(R.id.listView1);
		arrayAdapter = new UserSettingAdapter(this, android.R.layout.simple_list_item_1, listName,listPic);
		listView.setAdapter(arrayAdapter);
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case Constant.USER_SETTING_ITEM0:
					
					CustomDialog.Builder builder = new CustomDialog.Builder(UserInfoActivity.this);
					builder.setMessage("��ѡ������õĲ���\n��Щ������Ӱ��ϵͳ�Ƽ�����վ�����");  
			        builder.setTitle("��ʾ");  
			        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
			            public void onClick(DialogInterface dialog, int which) {  
			            	
			            	textView.setText("��ѯ��Χ��" + (MainActivity.userData.getSearchRange())
			        				+ " Ҫ��ɽ�������" + ( MainActivity.userData.getAvailBorrowed() > -1 ? MainActivity.userData.getAvailBorrowed() : ">30")
			        				+ " Ҫ��ɻ�������" + ( MainActivity.userData.getAvailReturn() > -1 ? MainActivity.userData.getAvailReturn() : ">30"));

			                dialog.dismiss();  
			                //������Ĳ������� 
			            }  
			        });  
			        
			        builder.setNegativeButton("ȡ��",  
			                new android.content.DialogInterface.OnClickListener() {  
			                    public void onClick(DialogInterface dialog, int which) {  
			                        dialog.dismiss();  
			                    }  
			                });  
			  
			        builder.create().show();  
			        
					break;
				case Constant.USER_SETTING_ITEM1:
					break;
				default:
					break;
				}
			}
		});
		
	}
	
	private void initListName(){
		listName = new ArrayList<String>();
		listName.add("�Ƽ�����");
		listName.add("���ߵ�ͼ����");
		listName.add("������...");
		
		listPic = new ArrayList<Integer>();
		listPic.add(R.drawable.pic0);
		listPic.add(R.drawable.pic1);
		listPic.add(R.drawable.pic1);
	}
	
}
