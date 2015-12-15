package android.view;

import java.util.HashMap;
import java.io.Serializable;

/**
 * @hide
 */
public class Event implements Serializable {

    public static final int GESTURE_PINCH  = 1;
    public static final int GESTURE_SPREAD = 2;
    public static final int GESTURE_SWIPE_DOWN = 3;
    public static final int GESTURE_SWIPE_UP = 4;
    public static final int GESTURE_SWIPE_RIGHT = 5;
    public static final int GESTURE_SWIPE_LEFT = 6;
    public static final int GESTURE_LONG_PRESS = 7;
    public static final int GESTURE_TAP = 8;

    private HashMap<Integer,Event> eventMap;
    private HashMap<Integer,Gesture> gestureMap;
    private int action;
    private int gesture;

    public static String findGesture(int g) {
        String gest = null;
        switch(g){
            case 1: 
                gest = "Pinch";
                break;
            case 2:
                gest = "Spread";
                break;
            case 3:
                gest = "swipe down";
                break;
            case 4:
                gest = "swipe up";
                break;
            case 5:
                gest = "swipe right";
                break;
            case 6:
                gest = "swipe left";
                break;
            case 7:
                gest = "long press";
                break;
            case 8:
                gest = "tap";
                break;
        }
        return gest;
    }

    public static int gestureReverseLookup(String gesture) {
        int rt = -1;
        switch(gesture) {
            case "Pinch":
                rt = 1;
                break;
            case "Spread":
                rt = 2;
                break;
            case "swipe down":
                rt = 3;
                break;
            case "swipe up":
                rt = 4;
                break;
            case "swipe right":
                rt = 5;
                break;
            case "swipe left":
                rt = 6;
                break;
            case "long press":
                rt = 7;
                break;
            case "tap":
                rt = 8;
                break;
        }
        return rt;
    }

    /**
     * @hide
     */
    public Event(int action){
        eventMap = new HashMap<Integer,Event>();
        this.action = action;
    }

    /**
     * @hide
     */
    public void storeEvent(int action, Event event){
        eventMap.put(action,event);
    }

    /**
     * @hide
     */
    public Event retrieveEvent(int action){
        return eventMap.get(action);
    }

    /**
     * @hide
     */
    public HashMap<Integer,Event> getMap(){
        return eventMap;
    }

    /**
     * @hide
     */
    public int getAction(){
        return action;
    }

    /**
     * @hide
     */
    public void setGesture(int gesture){
        this.gesture = gesture;
    }

    /**
     * @hide
     */
    public int getGesture(){
        return gesture;
    }

    /**
     * @hide
     */
    public void storeGesture(int gesture,Gesture gesture_){
    //    this.gesture = gesture;
        if(gestureMap == null){
            gestureMap = new HashMap<Integer,Gesture>();
        }
        gestureMap.put(gesture,gesture_);
    }

    public HashMap<Integer,Gesture> getGestureMap(){
        return gestureMap;
    } 

    /**
     * @hide
     */
    public Gesture getGesture(int gesture){
        if(gestureMap != null){
            return gestureMap.get(gesture);
        }
        return null;
    }
}
