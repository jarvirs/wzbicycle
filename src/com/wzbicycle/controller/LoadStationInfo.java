package com.wzbicycle.controller;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.util.Log;
import android.view.View;

import com.baidu.mapapi.model.LatLng;
import com.wzbicycle.model.BicycleStation;

public class LoadStationInfo extends Thread{
	
	private boolean isClose = false;  

    private boolean isPause = false;  
    
    private int loadPart;				//待加载的区域部分   1为路鹿城区  2为瓯海区  3为其他区域
    private int begin;
    private int end;
    
    private MainActivity activity;
    
    public LoadStationInfo(int loadPart,MainActivity activity){
    	this.loadPart = loadPart;
    	this.activity = activity;
    }
    public LoadStationInfo(int begin,int end,MainActivity activity){
    	this.loadPart = -1;
    	this.begin = begin;
    	this.end = end;
    	
    	this.activity = activity;
    }
    
    public synchronized void addStation(BicycleStation newStation) {  
    	activity.stations.add(newStation);  
        //有数据要处理时唤醒线程  
        this.notify();  
    } 
    
    /** 
     * 暂停线程 
     */  
    public synchronized void onThreadPause() {  
        isPause = true;  
    }  
    /** 
     * 线程等待,不提供给外部调用 
     */  
    private void onThreadWait() {  
        try {  
            synchronized (this) {  
                this.wait();  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    /** 
     * 线程继续运行 
     */  
    public synchronized void onThreadResume() {  
        isPause = false;  
        this.notify();  
    }  
    /** 
     * 关闭线程 
     */  
    public synchronized void closeThread() {  
        try {  
            notify();  
            setClose(true);  
            interrupt();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    public boolean isClose() {  
        return isClose;  
    }  
  
    public void setClose(boolean isClose) {  
        this.isClose = isClose;  
    }  
    
    @Override  
    public void run() {  
        while (!isClose && !isInterrupted()) {  
        	if(activity.loadCompleted1 && activity.loadCompleted2 
        			&& activity.loadCompleted3){
        			Log.i("aaaaaa","1111");
        			activity.checkBox_All.setClickable(true);
        			activity.btn_search.setClickable(true);
        			
                break;
        	}
            if (!isPause) {  
                
            	if(loadPart == 1 && !activity.loadCompleted2){					//瓯海区
            		//2区域加载
					for(int i = 2001;; i ++){
						if(activity.tag[i] != 0)
							continue;
						
						BicycleStation station = new BicycleStation(i);
						if(station.getName() == "" || station.getName() == null){
							activity.loadCompleted2 = true;
							break;
						}

						addStation(station);
						activity.tag[i] = 1;
						
						if(activity.isPriorityPos(station)){
							activity.loadStation(station);	
						}
					}
            	}
            	else if(loadPart == 2 && !activity.loadCompleted1){						//鹿城区
            		//1区域加载
					for(int i = 1;; i ++){
						if(activity.tag[i] != 0)
							continue;
						
						BicycleStation station = new BicycleStation(i);
						if(station.getName() == "" || station.getName() == null){
							activity.loadCompleted1 = true;
							break;
						}
						
						addStation(station);
						activity.tag[i] = 1;
						if(activity.isPriorityPos(station)){
							activity.loadStation(station);
						}
						
					}
            		
            	}
            	else if( !activity.loadCompleted3){
            		//3区域加载
					for(int i = 3001;; i ++ ){
						if(activity.tag[i] != 0)
							continue;
						BicycleStation station = new BicycleStation(i);
						if(station.getName() == "" || station.getName() == null){
							activity.loadCompleted3 = true;
							break;
						}
						addStation(station);
						activity.tag[i] = 1;
						if(activity.isPriorityPos(station)){
							activity.loadStation(station);
						}
					
					}
					//3个特殊点,888,1000,1001,6666
					int special[] = {888,1000,1001,6666};
					for(int i = 0;(activity.tag[888] == 0) || (activity.tag[1000] == 0) || (activity.tag[1001] == 0) || (activity.tag[6666] == 0);i++){
						if(i >= 4)
							i = 0;
						if(activity.tag[special[i]] != 0)
							continue;
						BicycleStation station = new BicycleStation(special[i]);
						if(station.getName() == "" || station.getName() == null){
							break;
						}

						addStation(station);
						activity.tag[special[i]] = 1;
						if(activity.isPriorityPos(station)){
							activity.loadStation(station);
						}
					}
            	}
            		
            	
            	
            } else {  
                onThreadWait();  
            }  
        }  
    }
}
