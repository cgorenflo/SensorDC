package com.example.wobistdu;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootupListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		try
		{
			Intent startServiceIntent = new Intent(arg0, WobistduService.class);
			ComponentName ret = arg0.startService(startServiceIntent);
			if(ret==null)
			{
				Log.e("Wobistdu", " start service null ");
			}
		}
		catch(Exception e)
		{
			Log.e("Wobistdu", " onreceive "+e);
		}
	
	}
	
}
