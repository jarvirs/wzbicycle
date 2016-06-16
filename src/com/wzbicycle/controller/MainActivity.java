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
	MKOfflineMap mOfflineMap;								//���ߵ�ͼ
	ArrayList<MKOLSearchRecord> cityRecords;					//���ó����б�
	
	ArrayList<BicycleStation>stations;						//վ�����ݼ���
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext  
        //ע��÷���Ҫ��setContentView����֮ǰʵ��  
        SDKInitializer.initialize(getApplicationContext());  
        setContentView(R.layout.activity_main); 
        
        init();
        BaiDuMapDel();									//�ٶȵ�ͼ����
        getStationInfo();								//��ȡվ����Ϣ
               
        
        //ShapeLoadingDialog shapeLoadingDialog = new ShapeLoadingDialog(getApplicationContext());
       // shapeLoadingDialog.setLoadingText("loading...");
        //shapeLoadingDialog.show();
        
    }
    
    //���ݳ�Ա��ʼ��
    private void init(){
    	stations = new ArrayList<BicycleStation>();
        mapView = (MapView)findViewById(R.id.bmapView);
        
        //���ߵ�ͼ
        mOfflineMap = new MKOfflineMap();
        mOfflineMap.init(this);
        cityRecords = mOfflineMap.getOfflineCityList();
        for (MKOLSearchRecord r : cityRecords) {
			if(r.cityName.contains("�㽭")){
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
    	
    	//�����̻߳�ȡjson
        new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
		
				int tag[] = new int[1000];						//�����ظ����
				boolean loadCompleted = false;					//�Ƿ�������
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
    
    //�ٶȵ�ͼ��Ϣ�����ݴ���
    private void BaiDuMapDel(){
        map = mapView.getMap();
        map.setTrafficEnabled(true);						//�򿪽�ͨͼ
        map.setMyLocationEnabled(true);						//������λ����
        // ���춨λ����  
//        MyLocationData locData = new MyLocationData.Builder()  
//            .accuracy(location.getRadius())  
//            // �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360  
//            .direction(100).latitude(location.getLatitude())  
//            .longitude(location.getLongitude()).build();  
        
        //��ǵ���¼�
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
        
        SetMarker();										//���ñ��
    }

    //Marker����
    private void SetMarker(){
    	//���������
    	LatLng point = new LatLng(25.963175, 125.400244);  
    	//����Markerͼ��
    	BitmapDescriptor bitmap0 = BitmapDescriptorFactory.fromResource(R.drawable.location_logo);
    	BitmapDescriptor bitmap1 = BitmapDescriptorFactory.fromResource(R.drawable.location_logo1);
    	BitmapDescriptor bitmap2 = BitmapDescriptorFactory.fromResource(R.drawable.location_logo2);
    	
    	// ͨ��marker��icons����һ��ͼƬ����ͨ��period���ö���֡ˢ��һ��ͼƬ��Դ
    	ArrayList<BitmapDescriptor>giflist = new ArrayList<BitmapDescriptor>();
    	giflist.add(bitmap0);
    	giflist.add(bitmap1);
    	giflist.add(bitmap2);
    	
    	//����MarkerOption�������ڵ�ͼ�����Marker 
    	MarkerOptions options = new MarkerOptions()
				.position(point)
				.icons(giflist)
				.zIndex(9)
				.period(10)
				.draggable(true);						//����������ק
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

    	//������Ϣ
    	String info = "";
    	info += station.getId() + " " + station.getName() + "\n";
    	info += "�ɽӳ�����" + station.getAvailBike() + "\n";
    	info += "��ͣ��λ��" + station.getCapacity() + "\n";
    	info += "��ַ��" + station.getAddress() + "\n";
		//���������
    	LatLng point = new LatLng(station.getLat(), station.getLng());  
    	//����MarkerOption�������ڵ�ͼ�����Marker 
    	BitmapDescriptor ico = BitmapDescriptorFactory.fromResource(R.drawable.marker);
    	MarkerOptions options = new MarkerOptions()
				.position(point)
				.icon(ico)
				.title(info)
				.zIndex(9);						//����������ק
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
        //��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���  
        mapView.onDestroy();  
        mOfflineMap.destroy();
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        //��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���  
        mapView.onResume();  
    }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        //��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���  
        mapView.onPause();  
    }
	@Override
	public void onGetOfflineMapState(int arg0, int state) {
		// TODO Auto-generated method stub
		switch (arg0) {
		case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
			MKOLUpdateElement update = mOfflineMap.getUpdateInfo(state);
			// �������ؽ��ȸ�����ʾ
//			if (update != null) {
//				stateView.setText(String.format("%s : %d%%", update.cityName,
//						update.ratio));
//				updateView();
//			}
		}
			break;
		case MKOfflineMap.TYPE_NEW_OFFLINE:
			// �������ߵ�ͼ��װ
			Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
			break;
		case MKOfflineMap.TYPE_VER_UPDATE:
			// �汾������ʾ
			// MKOLUpdateElement e = mOffline.getUpdateInfo(state);

                break;
            default:
                break;
        }
	}  
 
}
