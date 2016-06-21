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

//用户界面
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
		
		initListName();													//初始化用户设置界面的列表操作名称
		
		textView = (TextView)findViewById(R.id.textView1);
		textView.setText("查询范围：" + (MainActivity.userData.getSearchRange())
				+ " 要求可借数量：" + ( MainActivity.userData.getAvailBorrowed() > -1 ? MainActivity.userData.getAvailBorrowed() : ">30")
				+ " 要求可还数量：" + ( MainActivity.userData.getAvailReturn() > -1 ? MainActivity.userData.getAvailReturn() : ">30"));
		
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
					builder.setMessage("请选择待设置的参数\n这些参数将影响系统推荐附近站点情况");  
			        builder.setTitle("提示");  
			        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
			            public void onClick(DialogInterface dialog, int which) {  
			            	
			            	textView.setText("查询范围：" + (MainActivity.userData.getSearchRange())
			        				+ " 要求可借数量：" + ( MainActivity.userData.getAvailBorrowed() > -1 ? MainActivity.userData.getAvailBorrowed() : ">30")
			        				+ " 要求可还数量：" + ( MainActivity.userData.getAvailReturn() > -1 ? MainActivity.userData.getAvailReturn() : ">30"));

			                dialog.dismiss();  
			                //设置你的操作事项 
			            }  
			        });  
			        
			        builder.setNegativeButton("取消",  
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
		listName.add("推荐设置");
		listName.add("离线地图下载");
		listName.add("开发中...");
		
		listPic = new ArrayList<Integer>();
		listPic.add(R.drawable.pic0);
		listPic.add(R.drawable.pic1);
		listPic.add(R.drawable.pic1);
	}
	
}
