package com.wzbicycle.controller;

import java.util.ArrayList;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.example.wzbicycle.R;
import com.wzbicycle.component.ShapeLoadingDialog;
import com.wzbicycle.model.BicycleStation;
import com.wzbicycle.model.Constant;
import com.wzbicycle.model.DataOperator;
import com.wzbicycle.model.UserData;

import android.R.color;
import android.R.integer;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{

	//用户数据
	public static UserData userData;
	private static DataOperator dataOperator;
	
	//三个线程同时启动，负责四个区域的加载，提高效率
	private LoadStationInfo loadStationInfo1;
	private LoadStationInfo loadStationInfo2;
	private LoadStationInfo loadStationInfo3;
	
	// 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    boolean isFirstLoc = true; 									// 是否首次定位
	
	private ImageView loginImageView;							//登录头像
	public ImageView loadingView;								//加载图像
	public CheckBox checkBox_Priority;							//显示推荐点
	public CheckBox checkBox_All;								//显示所有点
	public Button btn_search;									//查找按钮
	
	MapView mapView = null;
	BaiduMap map;
	InfoWindow mInfoWindow;		
	
	public ArrayList<BicycleStation>stations;						//站点数据集合
	public ArrayList<BicycleStation>stations_op;						//站点数据集合
	public ArrayList<Marker>markers;								//Markers
	
	boolean loadCompleted1 = false;					//1区域是否加载完毕 	(1-275左右)（鹿城区）
	boolean loadCompleted2 = false;					//2区域是否加载完毕	(2001-2183左右)（瓯海区）
	boolean loadCompleted3 = false;					//3区域是否加载完毕	(3001-3178左右)
	boolean loadCompleted4 = false;					//4区域是否加载完毕	(6001-6089左右)
	
	boolean loadedAll = false;

	int loadedTag[];										//已经加载过的点
	int tag[];												//避免重复添加,提高效率
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        //注意该方法要再setContentView方法之前实现  
        SDKInitializer.initialize(getApplicationContext());  
        setContentView(R.layout.activity_main); 
        
        init();																		//初始化
        BaiDuMapDel();																//百度地图操作
        getStationInfo();															//获取站点信息
               
        
        //ShapeLoadingDialog shapeLoadingDialog = new ShapeLoadingDialog(getApplicationContext());
        //shapeLoadingDialog.setLoadingText("loading...");
        //shapeLoadingDialog.show();
        
    }
    
    //数据成员初始化
    private void init(){
    	loginImageView = (ImageView)findViewById(R.id.imageView1);
    	loadingView = (ImageView)findViewById(R.id.imageView2);
    	
    	AlphaAnimation anima = new AlphaAnimation(0.3f, 1.0f);  
        anima.setDuration(30000);								// 设置动画显示时间
        loadingView.startAnimation(anima);  
        anima.setAnimationListener(new AnimationImpl());  
    	
    	checkBox_Priority = (CheckBox)findViewById(R.id.checkBox1);
    	checkBox_All = (CheckBox)findViewById(R.id.checkBox2);
    	btn_search = (Button)findViewById(R.id.button1);
    	btn_search.setClickable(false);
    	
    	stations = new ArrayList<BicycleStation>();
    	stations_op = new ArrayList<BicycleStation>();
    	markers = new ArrayList<Marker>();
        mapView = (MapView)findViewById(R.id.bmapView);
        
    	dataOperator = new DataOperator(this);
    	dataOperator.openDb();
    	
    	loadedTag = new int[10000];
    	for (int i : loadedTag) {
			i = 0;
		}
    	tag = new int[10000];
    	for (int i : tag) {
    		i = 0;
    	}
    	
        initUserData();									//用户数据初始化
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);  
        intentFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);  
        MsgReceiver msgReceiver = new MsgReceiver();
        registerReceiver(msgReceiver, intentFilter);
        
        loginImageView.setOnClickListener(new ImageView.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				@SuppressWarnings("deprecation")
				int version = Integer.valueOf(android.os.Build.VERSION.SDK); 

				Intent intent=new Intent(MainActivity.this,UserInfoActivity.class);  
				startActivity(intent);
				if(version > 5 ){ 													//特效
					overridePendingTransition(R.anim.zoomin, R.anim.zoomout); 
				}
			}
		});
        
        //为加载完毕时不可点击
        checkBox_All.setClickable(false);
        checkBox_Priority.setClickable(false);
      
        checkBox_All.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(arg1){										//全部显示
					showStationInfo(Constant.STATION_LOAD_ALL);
				}	
				else{
					showStationInfo(Constant.STATION_LOAD_PRIORITY);
				}
			}
		});
        btn_search.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				EditText tv = (EditText)findViewById(R.id.editText1);
				final String searchText = tv.getText().toString();
				btn_search.setClickable(false);								//处理阶段不可用
				
				new Thread(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						
						//先清空
						for (Marker marker : markers) {
							marker.remove();
						}
						for (BicycleStation station : stations) {				
							loadedTag[station.getId()] = 0;
						}
						
						//选择性加添
						for (BicycleStation station : stations) {
							if(station.getAddress().contains(searchText) ||
									station.getName().contains(searchText)){
								loadStation(station);
							}
						}
						btn_search.setClickable(true);								//处理阶段可用
					}
				}.start();
			}
		});
    }
    //用户数据初始化
    private void initUserData(){
    	userData = new UserData();
    	
    	ArrayList<UserData>lst = dataOperator.getAllUserList();
    	if(lst.size() > 0)
    		userData = lst.get(lst.size() - 1);							//默认取最后一个
    	else {															//不存在则自己创建一个
			userData.setId("");
			userData.setName("未登录");
			userData.setSearchRange(500);
			userData.setAvailBorrowed(1);
			userData.setAvailReturn(1);
			
			dataOperator.insertUserData(userData, "");
		}
    }
    //更新用户信息
    public static void editUserInfo(){
    	dataOperator.updatePhoneData(userData);
    }
    
    
    //百度地图信息、数据处理
    private void BaiDuMapDel(){
        map = mapView.getMap();
        map.setTrafficEnabled(true);						//打开交通图
        map.setMyLocationEnabled(true);

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); 								//打开gps
        option.setCoorType("bd09ll"); 							//设置坐标类型
        option.setAddrType("all");								//显示地址
        option.setScanSpan(1000);								//刷新频率(ms)
        option.disableCache(true);								//启用缓存
//        option.setPriority(LocationClientOption.GpsFirst);	//GPS优先
        mLocClient.setLocOption(option);
        mLocClient.start();
        
//        mCurrentMode = LocationMode.NORMAL;
//        map.setMyLocationConfigeration(new MyLocationConfiguration(
//                        mCurrentMode, true, null));
//        
        //标记点击事件
        map.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(final Marker tmarker) {
				// TODO Auto-generated method stub
				Button button = new Button(getApplicationContext());
				button.setBackgroundResource(R.drawable.popup);
				OnInfoWindowClickListener listener = null;
				button.setText(tmarker.getTitle());
				button.setBackgroundColor(Color.WHITE);
				button.setTextColor(Color.BLACK);
				listener = new OnInfoWindowClickListener() {
					
					@Override
					public void onInfoWindowClick() {
						// TODO Auto-generated method stub
						map.hideInfoWindow();
					}
				};
				LatLng ll = tmarker.getPosition();
				mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, listener);
				map.showInfoWindow(mInfoWindow);
				
				return true;
			}
		});
        
    }
    
    public void loadStation(BicycleStation station){

    	//构造信息
    	String info = "";
    	info += station.getId() + " " + station.getName() + "\n";
    	info += "可接车数：" + station.getAvailBike() + "\n";
    	info += "可停车位：" + station.getCapacity() + "\n";
    	info += "地址：" + station.getAddress() + "\n";
		//定义坐标点
    	LatLng point = new LatLng(station.getLat(), station.getLng());  
    	//构建MarkerOption，用于在地图上添加Marker 
    	BitmapDescriptor ico = BitmapDescriptorFactory.fromResource(R.drawable.marker);
    	MarkerOptions options = new MarkerOptions()
				.position(point)
				.icon(ico)
				.title(info)
				.zIndex(9);											//设置手势拖拽
    	Marker marker;
    	marker = (Marker) map.addOverlay(options);
    	markers.add(marker);
    	
    	loadedTag[station.getId()] = 1;								//标记为已加载

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	
    	super.onCreateOptionsMenu(menu);
		menu.add(0,1,0,"推荐设置");
		menu.add(0,2,1,"恢复设置");
    	
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
    
    @Override  
    protected void onDestroy() {  
    	mLocClient.stop();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mapView.onDestroy();  
        super.onDestroy();  
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
        mapView.onResume();  
    }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        mapView.onPause();  
    }
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			commitExit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	public void commitExit(){
		new AlertDialog.Builder(this).setTitle("退出程序").setPositiveButton("退出" , new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				onDestroy();
				System.exit(0);//彻底退出程序

			}
		}).setNegativeButton("取消", null).show();
	}
    	
	/**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null) {
                return;
            }
            userData.Latitute = location.getLatitude();
            userData.Longitute = location.getLongitude();
            
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())						//地点精度location.getRadius()
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            map.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
        
        public void onReceivePoi(BDLocation poiLocation) {
        	
        }
    }
    //获取所有站点信息
    //边加载边现实，所以用于刚进入时调用，后期修改了显示模式后采用showStation方法直接显示
    private void getStationInfo(){
    	
    	loadStationInfo1 = new LoadStationInfo(1, this);
    	loadStationInfo2 = new LoadStationInfo(2, this);
    	loadStationInfo3 = new LoadStationInfo(3, this);
  	
    	loadStationInfo1.start();
    	loadStationInfo2.start();
    	loadStationInfo3.start();

    	//启动监听线程
    	new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while(true){
					if(loadedAll){
						for (BicycleStation station : stations_op) {
							if(loadedTag[station.getId()] == 0 && isPriorityPos(station))				//避免重复加载
								loadStation(station);
						}
					}
					
					if(!loadedAll && loadCompleted1 && loadCompleted2 && loadCompleted3){
						stations_op = stations;
						loadedAll = true;
					}
				}
			}
    		
    	}.start();
    }   
    
    //根据model显示推荐点或者是全部点
    private void showStationInfo(final int model){
    	
    	checkBox_All.setClickable(false);
    	new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				
				switch (model) {
				case Constant.STATION_LOAD_ALL:

					//pauseAllThread();
					//synchronized (stations) {
						for (BicycleStation station : stations) {				
							if(loadedTag[station.getId()] == 0)				//避免重复加载
								loadStation(station);
						}
				//	}
					
					
				//	resumeAllThread();

					break;
				case Constant.STATION_LOAD_PRIORITY:
				
					for (Marker marker : markers) {
						marker.remove();
					}
					for (BicycleStation station : stations) {				
						loadedTag[station.getId()] = 0;
					}
					//pauseAllThread();
				//	synchronized (stations) {
						for (BicycleStation station : stations) {
							if(loadedTag[station.getId()] == 0 && isPriorityPos(station))				//避免重复加载
								loadStation(station);
						}
				//	}

				//	resumeAllThread();
					break;
				default:
					break;
				}
				checkBox_All.setClickable(true);
			}
    		
    		
    	}.start();

    }
    
    //是否是推荐点
    public Boolean isPriorityPos(BicycleStation station) {
    	int returnNum = userData.getAvailReturn() > -1 ? userData.getAvailReturn() : 30;
    	int borrownNum = userData.getAvailBorrowed() > -1 ? userData.getAvailBorrowed() : 30;
    	
		if(station.getCapacity() > returnNum
				&& station.getAvailBike() > borrownNum
				&& isPosInRange(new LatLng(station.getLat(), station.getLng()))){
					return true;
				}
		return false;
	}
    //判断是否为推荐范围内的点
    public Boolean isPosInRange(LatLng pt){
    	
    	LatLng pcenter = new LatLng(userData.Latitute, userData.Longitute);
    	int range = userData.getSearchRange();
    	
    	return SpatialRelationUtil.isCircleContainsPoint(pcenter, range, pt);
    }
    
    private class AnimationImpl implements AnimationListener {  
    	  
        @Override  
        public void onAnimationStart(Animation animation) {  
            loadingView.setBackgroundResource(R.drawable.loading_after);  
        }  
  
        @Override  
        public void onAnimationEnd(Animation animation) {  
            skip(); // 动画结束后跳转到别的页面  
        }  
  
        @Override  
        public void onAnimationRepeat(Animation animation) {  

        }  
  
    }  
  
    private void skip() {  
    	loadingView.setVisibility(View.GONE);
    }  
}
