package com.wzbicycle.controller;

import java.util.ArrayList;

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
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
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
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements MKOfflineMapListener{

	MapView mapView = null;
	BaiduMap map;
	Marker marker;
	InfoWindow mInfoWindow;		
	MKOfflineMap mOfflineMap;								//离线地图
	ArrayList<MKOLSearchRecord> cityRecords;					//可用城市列表
	
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
       // shapeLoadingDialog.setLoadingText("loading...");
        //shapeLoadingDialog.show();
        
    }
    
    //数据成员初始化
    private void init(){
    	stations = new ArrayList<BicycleStation>();
        mapView = (MapView)findViewById(R.id.bmapView);
        
        //离线地图
        mOfflineMap = new MKOfflineMap();
        mOfflineMap.init(this);
        cityRecords = mOfflineMap.getOfflineCityList();
        for (MKOLSearchRecord r : cityRecords) {
			if(r.cityName.contains("浙江")){
				mOfflineMap.start(r.cityID);
				break;
			}
		}
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);  
        intentFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);  
        MsgReceiver msgReceiver = new MsgReceiver();
        registerReceiver(msgReceiver, intentFilter);
        
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
        map.setMyLocationEnabled(true);						//开启定位功能
        // 构造定位数据  
//        MyLocationData locData = new MyLocationData.Builder()  
//            .accuracy(location.getRadius())  
//            // 此处设置开发者获取到的方向信息，顺时针0-360  
//            .direction(100).latitude(location.getLatitude())  
//            .longitude(location.getLongitude()).build();  
        
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
//						LatLng ll = tmarker.getPosition();
//						LatLng llNew = new LatLng(ll.latitude + 0.005,
//								ll.longitude + 0.005);
//						tmarker.setPosition(llNew);
						map.hideInfoWindow();
					}
				};
				LatLng ll = tmarker.getPosition();
				mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, listener);
				map.showInfoWindow(mInfoWindow);
				
				return true;
			}
		});
        
        LatLng point = new LatLng(27.963175, 120.400244);  
        MapStatus mapStatus = new MapStatus.Builder()
        .target(point)
        .zoom(12)
        .build();
        
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        map.setMapStatus(mapStatusUpdate);
        
        SetMarker();										//设置标记
    }

    //Marker设置
    private void SetMarker(){
    	//定义坐标点
    	LatLng point = new LatLng(25.963175, 125.400244);  
    	//构建Marker图标
    	BitmapDescriptor bitmap0 = BitmapDescriptorFactory.fromResource(R.drawable.location_logo);
    	BitmapDescriptor bitmap1 = BitmapDescriptorFactory.fromResource(R.drawable.location_logo1);
    	BitmapDescriptor bitmap2 = BitmapDescriptorFactory.fromResource(R.drawable.location_logo2);
    	
    	// 通过marker的icons设置一组图片，再通过period设置多少帧刷新一次图片资源
    	ArrayList<BitmapDescriptor>giflist = new ArrayList<BitmapDescriptor>();
    	giflist.add(bitmap0);
    	giflist.add(bitmap1);
    	giflist.add(bitmap2);
    	
    	//构建MarkerOption，用于在地图上添加Marker 
    	MarkerOptions options = new MarkerOptions()
				.position(point)
				.icons(giflist)
				.zIndex(9)
				.period(10)
				.draggable(true);						//设置手势拖拽
		options.animateType(MarkerAnimateType.grow);
    	marker = (Marker) map.addOverlay(options);
    	
    	map.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
			
			@Override
			public void onMarkerDragStart(Marker arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMarkerDragEnd(Marker arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMarkerDrag(Marker arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mapView.onDestroy();  
        mOfflineMap.destroy();
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
 
}
