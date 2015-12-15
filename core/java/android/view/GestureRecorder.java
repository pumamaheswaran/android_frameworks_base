package android.view;

/**
 * Created by sahaj on 11/4/15.
 */
import java.util.HashMap;
import java.util.ArrayList;
import android.graphics.PointF;
import android.content.Context;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import android.os.Bundle;
import android.os.Environment;
import android.content.Intent;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * @hide
 */
public class GestureRecorder {

    public enum Action {
        WIFI_TOGGLE, BLUETOOTH_TOGGLE, DATA_TOGGLE, GPS_TOGGLE, RINGER_TOGGLE, MINIMIZE
    }

    private Event rootEvent = null;
    private static final String FILE_NAME = "Gesture.obj";
    private Context context;

    HashMap<Integer,String> eventDesc = new HashMap<Integer,String>();

    private ArrayList<Float> initialXCoordinates;
    private ArrayList<Float> initialYCoordinates;
    private ArrayList<Float> finalXCoordinates;
    private ArrayList<Float> finalYCoordinates;
    private float threshold = 10.0f;
    private float initialRadius;
    private float finalRadius;
    private static GestureRecorder gestureRecorder;

    private String TAG = "GestureRecorder";

    private String source = null;
    HashMap<String,Gesture> storedGestures;


    /**
     * @hide
     */
    private GestureRecorder() {
    }

    public void initGestureRecorder(Context context) {
        if(this.context == null){
            this.context = context;
        //    Log.d("GestureRecorder", "Context: "+context.getFilesDir().getAbsolutePath());
        }
        if(rootEvent == null) {
            rootEvent = retrieveEventFromFile();
        }
        if(rootEvent == null) {
            rootEvent = new Event(-1);
        }
        eventDesc.put(0,"Action Down");
        eventDesc.put(1,"Action Up");
        eventDesc.put(2,"Move");
        eventDesc.put(5,"Action Pointer Down");
        eventDesc.put(6,"Action Pointer Up");
        eventDesc.put(-1,"Gesture End");    
    }


    public String getActionDescription(int action) {
        return eventDesc.get(action);
    }

    /**
     * @hide
     */
    
    public static GestureRecorder getInstance() {
        if(gestureRecorder == null){
            gestureRecorder = new GestureRecorder();
        }

        return gestureRecorder;
    }

    /**
     * @hide
     */
    public Event storeEvent(Event currentEvent,MotionEvent me) {
        Event newEvent = null;
        int action = me.getActionMasked();

        if(currentEvent == null){
            if(rootEvent.retrieveEvent(action) == null) {
                if(action != 2) {
                    newEvent = new Event(action);
                    rootEvent.storeEvent(action,newEvent);
               //     Log.d("Presentation", "Stored event: " +action + " :"+ eventDesc.get(action));
                }else{
                    newEvent = currentEvent;
                }
            }else{
                return rootEvent.retrieveEvent(action);
            }

        }else{
            if(currentEvent.retrieveEvent(action) == null) {
                if(action != 2) {
                    newEvent = new Event(action);
                    currentEvent.storeEvent(action, newEvent);
             //       Log.d("Presentation", "Stored event: " +action + " :"+ eventDesc.get(action));
                }else{
                    newEvent = currentEvent;
                }
            }else{
                return currentEvent.retrieveEvent(action);
            }
        }
        return newEvent;
    }

    /**
     * @hide
     */
    public Event storeGesture(Event currentEvent,int gesture, String gestureName, String actionType) {
        Event newEvent = retrieveEvent(currentEvent,-1);
        if(newEvent == null) {
            newEvent = new Event(-1);
        }
        newEvent.setGesture(gesture);
        Gesture gesture_ = new Gesture();
        gesture_.setGesture(Event.findGesture(gesture));
        gesture_.setGestureName(gestureName);
        gesture_.setActionType(actionType);
        newEvent.storeGesture(gesture,gesture_);
        currentEvent.storeEvent(-1, newEvent);
        storeEventToFile();
        HashMap<String,Gesture> gMap = retrieveStoredGestureDesriptions();
        for (String key : gMap.keySet()) {
            Gesture g = gMap.get(key);
            g.setAMoreAccurateDesc(key);
        }
        Log.d("GestureRecorder","Gesture Stored::"+newEvent.getGestureMap());
        Log.d(TAG, "Gesture stored with gestureName:" + gesture_.getGestureName() + " and actionType:" + gesture_.getActionType());
        return newEvent;
    }

    public Event getRootEvent(){
        return rootEvent;
    }

    public void setRootEvent(Event event){
        this.rootEvent = event;
    }

    public void reloadRoot(){
        this.rootEvent = retrieveEventFromFile();
        Log.d("GestureRecorder","Root reloaded::");
        printEvents(this.rootEvent);    
    }

    /*
     * This method returns the gesture associated with the event action.
     * @hide
     */
    public Gesture getGesture(Event currentEvent,int gesture) {
        Event gestureEvent = currentEvent.retrieveEvent(-1);
        Gesture gesture_ = gestureEvent.getGesture(gesture);
        Log.d("GestureRecorder","Gesture Retrieved::"+gesture_);
        return gesture_;
    }

    /**
     * @hide
     */
    public int identifyMotion(ArrayList initialXCoordinates, ArrayList initialYCoordinates, ArrayList finalXCoordinates,
                                 ArrayList finalYCoordinates, int numberOfTouches) {
        float radius = 0;
        int gesture = 0;
        if (numberOfTouches >= 3)
        {

            PointF[] points = new PointF[3];
            for (int i = 0; i < 3; i++) {
                PointF new_point = new PointF((float)initialXCoordinates.get(i), (float)initialYCoordinates.get(i));
                points[i] = new_point;
            }
            float startRadius = getRadius(points);

            PointF[] points_end = new PointF[3];
            for (int i = 0; i < 3; i++) {
                PointF new_point = new PointF((float)finalXCoordinates.get(i), (float)finalYCoordinates.get(i));
                points_end[i] = new_point;
            }
            float endRadius = getRadius(points_end);

            radius = endRadius - startRadius;
        }
        float movementX=0, movementY=0;
        for(int i=0; i< numberOfTouches; i++){
            movementX+= (float)initialXCoordinates.get(i) - (float)finalXCoordinates.get(i);
            movementY+= (float)initialYCoordinates.get(i) - (float)finalYCoordinates.get(i);
        }
        movementX = movementX/numberOfTouches;
        movementY = movementY/numberOfTouches;

        if(Math.abs(radius) > 200) {
            if (radius < 0){
                Log.d("Presentation", "Pinch");
                gesture = Event.GESTURE_PINCH;
            }else{
                gesture = Event.GESTURE_SPREAD;
                Log.d("Presentation", "Spread");
            }
        }
        else if(Math.abs(movementX) < 100 && Math.abs(movementY)> 100){
            if(movementY < 0){
                gesture = Event.GESTURE_SWIPE_DOWN;
                Log.d("Presentation", "Swipe down");
            }else{
                gesture = Event.GESTURE_SWIPE_UP;
                Log.d("Presentation", "Swipe up");
            }
        }
        else if(Math.abs(movementY) < 100 && Math.abs(movementX)> 100){
            if(movementX < 0){
                gesture = Event.GESTURE_SWIPE_RIGHT;
                Log.d("Presentation", "Swipe right");
            }else{
                gesture = Event.GESTURE_SWIPE_LEFT;
                Log.d("Presentation", "Swipe left");
            }
        }
        else{
            gesture = Event.GESTURE_LONG_PRESS;
            Log.d("Presentation", "Long Press");
        }
        return gesture;
    }
    /**
     * @hide
     */
    public float getRadius(PointF points[]) {
        float radius = 0.0f;
        float[][] A = new float[3][3];
        float[] B = new float[3];
        for (int i = 0; i < 3; i++) {
            A[i][0] = points[i].x;
            A[i][1] = points[i].y;
            A[i][2] = 1;

            B[i] = points[i].x * points[i].x + points[i].y * points[i].y;
        }


/*        B[0] = (one.x * one.x + one.y * one.y);
        B[1] = (two.x * two.x + two.y * two.y);
        B[2] = (three.x * three.x + three.y * three.y);*/

        A = MatrixUtil.invert(A);
        float[] answer = new float[3];
        for (int i = 0; i < A.length; i++) {
            answer[i] = A[i][0] * B[0] + A[i][1] * B[1] + A[i][2] * B[2];
        }
        float temp1 = Math.abs(answer[0]) / 2;
        float temp2 = Math.abs(answer[1]) / 2;
        float rhs = (answer[2] + temp1 * temp1 + temp2 * temp2);
        radius = (float) Math.sqrt(rhs);
        return radius;
    }

    /**
     * @hide
     */
    public Event retrieveEvent(Event currentEvent,int action) {
      //  Log.d("GestureRecorder","currentEvent:"+action+":"+currentEvent);
        Event retVal = null;
        if(currentEvent == null){
       //     Log.d("GestureRecorder","returning from root event:");
        //    Log.d("GestureRecorder","root event map:"+rootEvent.getMap());
            if(rootEvent.getMap() != null){
         //       Log.d("GestureRecorder","root event map keys:"+rootEvent.getMap().keySet());
            }
            retVal = rootEvent.retrieveEvent(action);
        }else{
            retVal = currentEvent.retrieveEvent(action);
        }
       // Log.d("GestureRecorder","returning:"+retVal);
        return retVal;
    }

    /**
     * @hide
     */
    public void storeEventToFile() {
        Log.d(TAG,"storeEventToFile function");
        Environment.setUserRequired(false);

        File root = android.os.Environment.getExternalStorageDirectory();
      //  Log.d(TAG,"External file system root: "+root);
 

        File dir = new File (root.getAbsolutePath() + "/storage");
      //  dir.mkdirs();
        File file = new File(dir,FILE_NAME);

        if(file.exists()){
            file.delete();
        }

        Log.d("GestureRecorder", root.getAbsolutePath());
       // File file = new File(context.getFilesDir(), FILE_NAME);
 
        FileOutputStream outputStream = null;
        ObjectOutputStream oos = null;
     
        try{
            outputStream = new FileOutputStream(file);
            oos = new ObjectOutputStream(outputStream);
            oos.writeObject(rootEvent);
            oos.flush();
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"Failed to write");
        } finally{
            try{
                oos.close();
                outputStream.close();
                Log.d(TAG,"Written to file");
            }catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"Failed to write");
            }    
        }
        reloadRoot();
    }

    /**
     * @hide
     */
    private void printEvents(Event event) { 

     //   Log.d("Presentation", "printEvents entry ");

        if(event == null){
            return;
        }

        if(event.getMap() == null){
            return;
        }

        for(Integer key : event.getMap().keySet()){
            Log.d("Presentation", "GestureRecorder : event:  "+eventDesc.get(key));
            Event e2 = event.getMap().get(key);
            if(e2 != null){
                if(e2.getGestureMap()!= null ){
                    Log.d("Presentation", "GestureRecorder : gesture:  "+e2.getGestureMap());
                }
            }
            printEvents(e2);
        }
    }

    
    /**
     * @hide
     */
    public Event retrieveEventFromFile() {
        Event event = null;
        Log.d("GestureRecorder", "retrieveEventFromFile");
        Environment.setUserRequired(false);
        //Log.d(TAG,"storeEventToFile function");
        File root = android.os.Environment.getExternalStorageDirectory();
       // Log.d(TAG,"External file system root: "+root);
 
        File dir = new File (root.getAbsolutePath() + "/storage");
       // File dir =  null;
        // if("APP".equals(source)){
        //     dir = new File("/storage/emulated/legacy/storage");
        // }else{
        //     dir = new File("/mnt/shell/emulated/0/storage");
        // }
       // File file = new File(dir,FILE_NAME);

        Log.d("GestureRecorder", root.getAbsolutePath() + "/storage");
 
        File file = new File(dir, FILE_NAME);

        if(file.exists()) {
            FileInputStream inputStream;
            ObjectInputStream is;
         
            try{
                inputStream = new FileInputStream(file);
                is = new ObjectInputStream(inputStream);
                event = (Event) is.readObject();
                is.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"Failed to read");
            } 
        }else{
            Log.d("GestureRecorder", "File Does not Exist");
        }
        Log.d("GestureRecorder", "File retrieved");
        
        return event;
    }

    public Event retrieveEventFromIntent() {
        // Intent intent = context.getIntent();
        // Bundle bundle = intent.getExtras();

        Event event = null;//(Event)bundle.getSerializable("gesture");
        return event;
    }

    public void clearStoredEvents() {
       // rootEvent = null;
        //initGestureRecorder();
    }

public void deleteGesture(int numberOfFingers,int gestureCode) {
        Event ev1 = retrieveEvent(rootEvent,MotionEvent.ACTION_DOWN);
        for(int i=1;i<numberOfFingers;i++){
            ev1 = retrieveEvent(ev1,MotionEvent.ACTION_POINTER_DOWN);
        }
        for(int i=1;i<numberOfFingers;i++){
            ev1 = retrieveEvent(ev1,MotionEvent.ACTION_POINTER_UP);
        }
        ev1 = retrieveEvent(ev1,MotionEvent.ACTION_UP);
        ev1 = retrieveEvent(ev1,-1);
        ev1.getGestureMap().remove(gestureCode);
    }

    /**
     * 
     */
    public HashMap<String,Gesture> retrieveStoredGestureDesriptions() { 
        storedGestures = new HashMap<String,Gesture>();
        Log.d("Loading stored gestures","---------------------------------------");
        loadGestureDetails(rootEvent,0);
        Log.d("Loaded stored gestures",""+storedGestures);
        return storedGestures;        
    }

    public void loadGestureDetails(Event event,int count) {
        if(event != null){
            if(event.getMap() != null && event.getMap().keySet() != null && event.getMap().keySet().size() > 0 ){
                for(Integer key : event.getMap().keySet()){
                    if(key == -1){
                        Event stEvent = event.getMap().get(key);
                        if(stEvent.getGestureMap() != null && stEvent.getGestureMap().size() > 0){
                             for(Integer key2 : stEvent.getGestureMap().keySet()){
                                storedGestures.put(""+count+" Finger "+Event.findGesture(key2),stEvent.getGestureMap().get(key2));       
                             }   
                        }
                    } else{
                        Event stEvent = event.getMap().get(key);
                        if(key == MotionEvent.ACTION_DOWN || key == MotionEvent.ACTION_POINTER_DOWN){
                            loadGestureDetails(stEvent,count+1);
                        }else{
                            loadGestureDetails(stEvent,count);
                        }
                    }
                }
            }
        }
    }
}
