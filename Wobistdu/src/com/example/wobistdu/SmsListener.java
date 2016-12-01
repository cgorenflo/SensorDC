package com.example.wobistdu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;




public class SmsListener extends BroadcastReceiver {

	private static String TAG="wobistdu";
	private static String PASSCODE;
    public static String onetofive = "12345";

    @Override
    public void onReceive(Context context, Intent intent) {
        
    	WSmsMessage msg = ReceiveSms(intent);
    	
    	if(!msg.getSender().equals(""))
    	{
    		updatePasscode(context);
    		
    		if(msg.contains(PASSCODE))
    		{
    			SendLocationBySMS(context, msg.getSender());
    		}
    	}
    	
        
    }
    
    
    
    
    private void updatePasscode(Context context)
    {
    	try
    	{
    		SharedPreferences settings = context.getSharedPreferences("wobistdu", 0);
    		PASSCODE=settings.getString("passcode", onetofive);
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG,"updatePasscode "+e);
    	}
    }
    
    private WSmsMessage ReceiveSms(Intent intent)
    {
    	
    	if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        return new WSmsMessage(msg_from,msgBody);
                        
                    }
                }catch(Exception e){
                            Log.e(TAG,"ReceiveSms "+e);
                }
            }
        }
    	return new WSmsMessage("","");
    }
    
    
    public static boolean SendLocationBySMS(Context context, String destination)
    {
    	try 
    	{
    	LocationManager lm = turnGPSOn(context);
		
		String loc="Loc,";
		
		Location gps=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(gps!=null)
			loc+=gps.getLatitude()+","+gps.getLongitude()+",";
		
		Location net=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if(net!=null)
			loc+=net.getLatitude()+","+net.getLongitude()+",";
		
		WSmsMessage tosend = new WSmsMessage(destination, loc);
		SendSMS(tosend);
		return true;
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG,"SendLocationBySMS "+e);
    		return false;
    	}
    	
    }
    
    private static void SendSMS(WSmsMessage msgtosend)
    {
    	try
    	{
    		SmsManager smsManager = SmsManager.getDefault();
    		smsManager.sendTextMessage(msgtosend.getSender(), null, msgtosend.getMessageBody(), null, null);
    		
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG,"SendSMS "+e);
    	}
    	
    }
	private static LocationManager turnGPSOn(Context context) {
		try {
			
			LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			
			boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (!gps) {
				
				 Intent intent = new Intent(
				 "android.location.GPS_ENABLED_CHANGE");
				 intent.putExtra("enabled", true);
				 context.sendBroadcast(intent);
				
				 String provider = Settings.Secure.getString(
				 context.getContentResolver(),
				 Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
				 if (!provider.contains("gps")) { // if gps is disabled
				 final Intent poke = new Intent();
				 poke.setClassName("com.android.settings",
				 "com.android.settings.widget.SettingsAppWidgetProvider");
				 poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
				 poke.setData(Uri.parse("3"));
				 context.sendBroadcast(poke);
				 }
				
				
//				Intent intent1 = new Intent(
//						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//				intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				context.startActivity(intent1);
				// Log.w(TAG, "gps radio was off ", this.getClass());
				
			 }
			return lm;
			
		} catch (Exception e) {
			Log.e(TAG, "turnonGPS " + e);
			return null;
		}

	}
    
    
}