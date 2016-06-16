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
	// ��λ���
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    boolean isFirstLoc = true; // �Ƿ��״ζ�λ
	
	MapView mapView = null;
	BaiduMap map;
	Marker marker;
	InfoWindow mInfoWindow;		
	MKOfflineMap mOfflineMap;								//���ߵ�ͼ
	ArrayList<MKOLSearchRecord> cityRecords;				//���ó����б�
	
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
        //shapeLoadingDialog.setLoadingText("loading...");
        //shapeLoadingDialog.show();
        
    }
    
    //���ݳ�Ա��ʼ��
    private void init(){
    	loginImageView = (ImageView)findViewById(R.id.imageView1);
    	stations = new ArrayList<BicycleStation>();
        mapView = (MapView)findViewById(R.id.bmapView);
        
        
//        //���ߵ�ͼ
//        mOfflineMap = new MKOfflineMap();
//        mOfflineMap.init(this);
//        cityRecords = mOfflineMap.getOfflineCityList();
//        for (MKOLSearchRecord r : cityRecords) {
//			if(r.cityName.contains("�㽭")){
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
        map.setMyLocationEnabled(true);

        // ��λ��ʼ��
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // ��gps
        option.setCoorType("bd09ll"); // ������������
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
//        
//        mCurrentMode = LocationMode.NORMAL;
//        map.setMyLocationConfigeration(new MyLocationConfiguration(
//                        mCurrentMode, true, null));
//        
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
    	
    	super.onCreateOptionsMenu(menu);
		menu.add(0,1,0,"�Ƽ�����");
		menu.add(0,2,1,"�ָ�����");
    	
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
        //��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���  
        mapView.onDestroy();  
        mOfflineMap.destroy();
        super.onDestroy();  
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
	
	
	/**
     * ��λSDK��������
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view ���ٺ��ڴ����½��յ�λ��
            if (location == null || mapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
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
