package com.wzbicycle.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes.Name;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.R.integer;
import android.util.JsonToken;

//公共工具类
public class BicycleStation {
	
	private String url = "http://218.93.33.59:85/map/wzmap/ibikestation.asp?id=";
	
	private int id;													//站点Id
	private String name;											//站点名字
	private double lat,lng;											//站点经纬度
	private int capacity;											//站点可借总数
	private int availBike;											//站点可借数量
	private String address;											//站点地址名称
	
	public BicycleStation(int id) {
		// TODO Auto-generated constructor stub
		url += id;
		
		String result = "";  
        BufferedReader in = null;  
        try {  
            URL realUrl = new URL(url);  
            // 打开和URL之间的连接  
            URLConnection conn = realUrl.openConnection();  
//            // 设置通用的请求属性  
            conn.setRequestProperty("accept", "*/*");  
            conn.setRequestProperty("connection", "Keep-Alive");  
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");  
            // 建立实际的连接  
            
            //conn.setConnectTimeout(10000000);
            conn.connect();  
//            // 获取所有响应头字段  
//            Map<String, List<String>> map = conn.getHeaderFields();  
//            // 遍历所有的响应头字段  
//            for (String key : map.keySet()) {  
//                System.out.println(key + "--->" + map.get(key));  
//            }  
            // 定义BufferedReader输入流来读取URL的响应  
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
            String line;  
            while ((line = in.readLine()) != null) {  
                result += "\n" + line;  
            }  
            
            result = result.substring(19);
            
            JSONTokener jsonTokener = new JSONTokener(result);
            JSONObject object = (JSONObject)jsonTokener.nextValue();
            String Name = object.getString("station");
            
            JSONArray jsonArray = new JSONArray(Name);
            for(int i=0;i<jsonArray.length();i++){  
                JSONObject jsonobject2=jsonArray.getJSONObject(i);  
                
                this.name = jsonobject2.getString("name");
                this.address = jsonobject2.getString("address");
                this.id = jsonobject2.getInt("id");
                this.lat = jsonobject2.getDouble("lat");
                this.lng = jsonobject2.getDouble("lng");
                this.capacity = jsonobject2.getInt("capacity");
                this.availBike = jsonobject2.getInt("availBike");
                
            }  
            
        } catch (Exception e) {  
            System.out.println("发送GET请求出现异常！" + e);  
            e.printStackTrace();  
        }  
        // 使用finally块来关闭输入流  
        finally {  
            try {  
                if (in != null) {  
                    in.close();  
                }  
                
            } catch (IOException ex) {  
                ex.printStackTrace();  
            }  
        }  
		
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getAvailBike() {
		return availBike;
	}

	public void setAvailBike(int availBike) {
		this.availBike = availBike;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
}
