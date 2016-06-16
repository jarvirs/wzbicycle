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
import com.example.wzbicycle.R;
import com.wzbicycle.component.ShapeLoadingDialog;
import com.wzbicycle.model.BicycleStation;

import android.R.color;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements MKOfflineMapListener{

	ImageView loginImageView;
	// 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    boolean isFirstLoc = true; // 是否首次定位
	
	MapView mapView = null;
	BaiduMap map;
	Marker marker;
	InfoWindow mInfoWindow;		
	MKOfflineMap mOfflineMap;								//离线地图
	ArrayList<MKOLSearchRecord> cityRecords;				//可用城市列表
	
	ArrayList<BicycleStation>stations;						//站点数据集合
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        //注意该方法要再setContentView方法之前实现  
        SDKInitializer.initialize(getApplicationContext());  
        setContentView(R.layout.activity_main); 
        
        init();
        BaiDuMapDel();									//百度地图操作
        getStationInfo();								//获取站点信息
               
        
        //ShapeLoadingDialog shapeLoadingDialog = new ShapeLoadingDialog(getApplicationContext());
        //shapeLoadingDialog.setLoadingText("loading...");
        //shapeLoadingDialog.show();
        
    }
    
    //数据成员初始化
    private void init(){
    	loginImageView = (ImageView)findViewById(R.id.imageView1);
    	stations = new ArrayList<BicycleStation>();
        mapView = (MapView)findViewById(R.id.bmapView);
        
        
//        //离线地图
//        mOfflineMap = new MKOfflineMap();
//        mOfflineMap.init(this);
//        cityRecords = mOfflineMap.getOfflineCityList();
//        for (MKOLSearchRecord r : cityRecords) {
//			if(r.cityName.contains("浙江")){
//				mOfflineMap.start(r.cityID);
//				break;
//			}
//		}
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);  
        intentFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);  
        MsgReceiver msgReceiver = new MsgReceiver();
        registerReceiver(msgReceiver, intentFilter);
        
        loginImageView.setOnClickListener(new ImageView.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "123", Toast.LENGTH_LONG);
			}
		});
    }
    
    private void getStationInfo(){
    	
    	//启动线程获取json
        new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
		
				int tag[] = new int[1000];						//避免重复添加
				boolean loadCompleted = false;					//是否加载完毕
				for (int i : tag) {
					i = 0;
				}
				while(true){
					for(int i = 1;; i ++){
						BicycleStation station = new BicycleStation(i);
						if(station.getName() == "" || station.getName() == null){
							loadCompleted = true;
							break;
						}
						if(tag[i] == 0){
							stations.add(station);
							tag[i] = 1;
							loadStation(station);
						}
					}
					
					if(loadCompleted)
						break;
				}
			}
        	
        }.start();

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
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
//        
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
//        
//        LatLng point = new LatLng(27.963175, 120.400244);  
//        MapStatus mapStatus = new MapStatus.Builder()
//        .target(point)
//        .zoom(12)
//        .build();
//        
//        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
//        map.setMapStatus(mapStatusUpdate);

    }
    
    private void loadStation(BicycleStation station){

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
				.zIndex(9);						//设置手势拖拽
    	marker = (Marker) map.addOverlay(options);

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
        mOfflineMap.destroy();
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
	public void onGetOfflineMapState(int arg0, int state) {
		// TODO Auto-generated method stub
		switch (arg0) {
		case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
			MKOLUpdateElement update = mOfflineMap.getUpdateInfo(state);
			// 处理下载进度更新提示
//			if (update != null) {
//				stateView.setText(String.format("%s : %d%%", update.cityName,
//						update.ratio));
//				updateView();
//			}
		}
			break;
		case MKOfflineMap.TYPE_NEW_OFFLINE:
			// 有新离线地图安装
			Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
			break;
		case MKOfflineMap.TYPE_VER_UPDATE:
			// 版本更新提示
			// MKOLUpdateElement e = mOffline.getUpdateInfo(state);
				
            break;
        default:
            break;
        }
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
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
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
    
    
 
}
