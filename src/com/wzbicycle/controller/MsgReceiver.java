/**
 * 
 */
package com.wzbicycle.controller;

import com.baidu.mapapi.SDKInitializer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * @author Administrator
 *
 */
public class MsgReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if(action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)){
			Toast.makeText(context, "ÍøÂç³ö´í", Toast.LENGTH_LONG);
		}
	}

	
}
