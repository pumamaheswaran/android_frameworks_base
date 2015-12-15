package android.view;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.os.Bundle;
import java.util.HashMap;
import java.lang.InterruptedException;

public class GestureReceiver extends BroadcastReceiver{
	HashMap<Integer,String> eventDesc = new HashMap<Integer,String>();
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("GestureReceiver","Receiving Broadcast --");
		int value = intent.getExtras().getInt("bucketno");
		
        Bundle bundle = intent.getExtras();
		eventDesc.put(0,"Action Down");
        eventDesc.put(1,"Action Up");
        eventDesc.put(2,"Move");
        eventDesc.put(5,"Action Pointer Down");
        eventDesc.put(6,"Action Pointer Up");
        eventDesc.put(-1,"Gesture End");    
        Event event = (Event)bundle.getSerializable("gesture");
        Log.d("GestureReceiver", " Received:  "+event);
        
        //GestureRecorder gestureRecorder = GestureRecorder.getInstance();
        //gestureRecorder.reloadRoot();
        //gestureRecorder.storeEventToFile();
        //printEvents(event);
	}
    private void printEvents(Event event) { 

     //   Log.d("Presentation", "printEvents entry ");

        if(event == null){
            return;
        }

        if(event.getMap() == null){
            return;
        }

        for(Integer key : event.getMap().keySet()){
            Log.d("GestureReceiver", " Retrieved:  "+eventDesc.get(key));
            printEvents(event.getMap().get(key));
        }
    }

}