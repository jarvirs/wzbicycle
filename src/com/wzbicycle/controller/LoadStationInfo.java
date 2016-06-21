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
    
    private int loadPart;				//�����ص����򲿷�   1Ϊ·¹����  2Ϊ걺���  3Ϊ��������
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
        //������Ҫ����ʱ�����߳�  
        this.notify();  
    } 
    
    /** 
     * ��ͣ�߳� 
     */  
    public synchronized void onThreadPause() {  
        isPause = true;  
    }  
    /** 
     * �̵߳ȴ�,���ṩ���ⲿ���� 
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
     * �̼߳������� 
     */  
    public synchronized void onThreadResume() {  
        isPause = false;  
        this.notify();  
    }  
    /** 
     * �ر��߳� 
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
                
            	if(loadPart == 1 && !activity.loadCompleted2){					//걺���
            		//2�������
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
            	else if(loadPart == 2 && !activity.loadCompleted1){						//¹����
            		//1�������
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
            		//3�������
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
					//3�������,888,1000,1001,6666
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
