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

	//�û�����
	public static UserData userData;
	private static DataOperator dataOperator;
	
	//�����߳�ͬʱ�����������ĸ�����ļ��أ����Ч��
	private LoadStationInfo loadStationInfo1;
	private LoadStationInfo loadStationInfo2;
	private LoadStationInfo loadStationInfo3;
	
	// ��λ���
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    boolean isFirstLoc = true; 									// �Ƿ��״ζ�λ
	
	private ImageView loginImageView;							//��¼ͷ��
	public ImageView loadingView;								//����ͼ��
	public CheckBox checkBox_Priority;							//��ʾ�Ƽ���
	public CheckBox checkBox_All;								//��ʾ���е�
	public Button btn_search;									//���Ұ�ť
	
	MapView mapView = null;
	BaiduMap map;
	InfoWindow mInfoWindow;		
	
	public ArrayList<BicycleStation>stations;						//վ�����ݼ���
	public ArrayList<BicycleStation>stations_op;						//վ�����ݼ���
	public ArrayList<Marker>markers;								//Markers
	
	boolean loadCompleted1 = false;					//1�����Ƿ������� 	(1-275����)��¹������
	boolean loadCompleted2 = false;					//2�����Ƿ�������	(2001-2183����)��걺�����
	boolean loadCompleted3 = false;					//3�����Ƿ�������	(3001-3178����)
	boolean loadCompleted4 = false;					//4�����Ƿ�������	(6001-6089����)
	
	boolean loadedAll = false;

	int loadedTag[];										//�Ѿ����ع��ĵ�
	int tag[];												//�����ظ����,���Ч��
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext  
        //ע��÷���Ҫ��setContentView����֮ǰʵ��  
        SDKInitializer.initialize(getApplicationContext());  
        setContentView(R.layout.activity_main); 
        
        init();																		//��ʼ��
        BaiDuMapDel();																//�ٶȵ�ͼ����
        getStationInfo();															//��ȡվ����Ϣ
               
        
        //ShapeLoadingDialog shapeLoadingDialog = new ShapeLoadingDialog(getApplicationContext());
        //shapeLoadingDialog.setLoadingText("loading...");
        //shapeLoadingDialog.show();
        
    }
    
    //���ݳ�Ա��ʼ��
    private void init(){
    	loginImageView = (ImageView)findViewById(R.id.imageView1);
    	loadingView = (ImageView)findViewById(R.id.imageView2);
    	
    	AlphaAnimation anima = new AlphaAnimation(0.3f, 1.0f);  
        anima.setDuration(30000);								// ���ö�����ʾʱ��
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
    	
        initUserData();									//�û����ݳ�ʼ��
        
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
				if(version > 5 ){ 													//��Ч
					overridePendingTransition(R.anim.zoomin, R.anim.zoomout); 
				}
			}
		});
        
        //Ϊ�������ʱ���ɵ��
        checkBox_All.setClickable(false);
        checkBox_Priority.setClickable(false);
      
        checkBox_All.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(arg1){										//ȫ����ʾ
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
				btn_search.setClickable(false);								//����׶β�����
				
				new Thread(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						
						//�����
						for (Marker marker : markers) {
							marker.remove();
						}
						for (BicycleStation station : stations) {				
							loadedTag[station.getId()] = 0;
						}
						
						//ѡ���Լ���
						for (BicycleStation station : stations) {
							if(station.getAddress().contains(searchText) ||
									station.getName().contains(searchText)){
								loadStation(station);
							}
						}
						btn_search.setClickable(true);								//����׶ο���
					}
				}.start();
			}
		});
    }
    //�û����ݳ�ʼ��
    private void initUserData(){
    	userData = new UserData();
    	
    	ArrayList<UserData>lst = dataOperator.getAllUserList();
    	if(lst.size() > 0)
    		userData = lst.get(lst.size() - 1);							//Ĭ��ȡ���һ��
    	else {															//���������Լ�����һ��
			userData.setId("");
			userData.setName("δ��¼");
			userData.setSearchRange(500);
			userData.setAvailBorrowed(1);
			userData.setAvailReturn(1);
			
			dataOperator.insertUserData(userData, "");
		}
    }
    //�����û���Ϣ
    public static void editUserInfo(){
    	dataOperator.updatePhoneData(userData);
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
        option.setOpenGps(true); 								//��gps
        option.setCoorType("bd09ll"); 							//������������
        option.setAddrType("all");								//��ʾ��ַ
        option.setScanSpan(1000);								//ˢ��Ƶ��(ms)
        option.disableCache(true);								//���û���
//        option.setPriority(LocationClientOption.GpsFirst);	//GPS����
        mLocClient.setLocOption(option);
        mLocClient.start();
        
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
        
    }
    
    public void loadStation(BicycleStation station){

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
				.zIndex(9);											//����������ק
    	Marker marker;
    	marker = (Marker) map.addOverlay(options);
    	markers.add(marker);
    	
    	loadedTag[station.getId()] = 1;								//���Ϊ�Ѽ���

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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			commitExit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	public void commitExit(){
		new AlertDialog.Builder(this).setTitle("�˳�����").setPositiveButton("�˳�" , new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				onDestroy();
				System.exit(0);//�����˳�����

			}
		}).setNegativeButton("ȡ��", null).show();
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
            userData.Latitute = location.getLatitude();
            userData.Longitute = location.getLongitude();
            
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())						//�ص㾫��location.getRadius()
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
    //��ȡ����վ����Ϣ
    //�߼��ر���ʵ���������ڸս���ʱ���ã������޸�����ʾģʽ�����showStation����ֱ����ʾ
    private void getStationInfo(){
    	
    	loadStationInfo1 = new LoadStationInfo(1, this);
    	loadStationInfo2 = new LoadStationInfo(2, this);
    	loadStationInfo3 = new LoadStationInfo(3, this);
  	
    	loadStationInfo1.start();
    	loadStationInfo2.start();
    	loadStationInfo3.start();

    	//���������߳�
    	new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while(true){
					if(loadedAll){
						for (BicycleStation station : stations_op) {
							if(loadedTag[station.getId()] == 0 && isPriorityPos(station))				//�����ظ�����
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
    
    //����model��ʾ�Ƽ��������ȫ����
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
							if(loadedTag[station.getId()] == 0)				//�����ظ�����
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
							if(loadedTag[station.getId()] == 0 && isPriorityPos(station))				//�����ظ�����
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
    
    //�Ƿ����Ƽ���
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
    //�ж��Ƿ�Ϊ�Ƽ���Χ�ڵĵ�
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
            skip(); // ������������ת�����ҳ��  
        }  
  
        @Override  
        public void onAnimationRepeat(Animation animation) {  

        }  
  
    }  
  
    private void skip() {  
    	loadingView.setVisibility(View.GONE);
    }  
}
