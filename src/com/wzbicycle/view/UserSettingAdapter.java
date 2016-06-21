package com.wzbicycle.view;

import java.util.ArrayList;

import com.example.wzbicycle.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//用户界面的adapter
public class UserSettingAdapter extends ArrayAdapter<String>{

	private Context context;
	private ArrayList<String> dataList;
	private ArrayList<Integer> picList;
	
	public UserSettingAdapter(Context context, int resource,ArrayList<String> objects,ArrayList<Integer>pics) {
		super(context, resource,objects);
		// TODO Auto-generated constructor stub
		
		this.context = context;
		this.dataList = objects;
		this.picList = pics;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView==null){
			LayoutInflater layoutInflater=LayoutInflater.from(context);
			convertView=layoutInflater.inflate(R.layout.userlist, null, false);	
		}
		
		TextView tv = (TextView)convertView.findViewById(R.id.textView1);
		ImageView iv = (ImageView)convertView.findViewById(R.id.imageView1);
		
		tv.setText(dataList.get(position));		
		iv.setImageResource(picList.get(position));
		
		return convertView;
	}

	
}
