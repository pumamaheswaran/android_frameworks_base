package android.view;

import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sahaj on 12/4/15.
 */
public class Util {
    private static final String TAG = "Presentation";
    private PointF []startPoints;
    private PointF []endPoints;
    double initialDistance[];
    double finalDistance[];
    double averageInitialDistance;
    double averageFinalDistance;
    float startTime, endTime;
    float startX,startY,endX,endY;
    int moveCount;

    public Util(float startTime, float endTime, int moveCount){
        this.startTime = startTime;
        this.endTime = endTime;
        this.moveCount = moveCount;
    }
    public int help( ArrayList<Float> initialXCoordinates, ArrayList<Float> initialYCoordinates, ArrayList<Float> finalXCoordinates, ArrayList<Float> finalYCoordinates){
        startPoints = new PointF[initialXCoordinates.size()];
        endPoints = new PointF[initialXCoordinates.size()];
        int numberOfDistances = 0;
        if(initialXCoordinates.size() == 3)
            numberOfDistances = 3;
        else if(initialXCoordinates.size() == 4)
            numberOfDistances = 6;
        else
            numberOfDistances = 10;
        initialDistance = new double[numberOfDistances];
        for(int i=0;i<initialXCoordinates.size();i++){
            startPoints[i] = new PointF();
            startPoints[i].set(initialXCoordinates.get(i),initialYCoordinates.get(i));
            startX += initialXCoordinates.get(i);
            startY += initialYCoordinates.get(i);
        }
        int count = 0;
        for(int i=0;i<startPoints.length;i++){
            for(int j=i+1;j<startPoints.length;j++){
                initialDistance[count++] = getDistance(startPoints[i],startPoints[j]);
            }
        }
        count = 0;
        averageInitialDistance = getAverageDistance(initialDistance);
        Log.d(TAG,"Util : Average Initial Distance : "+ averageInitialDistance);

        finalDistance = new double[numberOfDistances];
        for(int i=0;i<finalXCoordinates.size();i++){
            endPoints[i] = new PointF();
            endPoints[i].set(finalXCoordinates.get(i),finalYCoordinates.get(i));
            endX += finalXCoordinates.get(i);
            endY += finalYCoordinates.get(i);
        }
        for(int i=0;i<endPoints.length;i++){
            for(int j=i+1;j<endPoints.length;j++){
                finalDistance[count++] = getDistance(endPoints[i],endPoints[j]);
            }
        }
        count = 0;
        averageFinalDistance = getAverageDistance(finalDistance);
        Log.d(TAG,"Util : Average Final Distance : "+ averageFinalDistance);

        if(numberOfDistances == 3)
            return threeFingerProcessing();
        else if(numberOfDistances == 6)
            return fourFingerProcessing();
        else if(numberOfDistances == 10)
            return fiveFingerProcessing();
        return -1;
    }

    private int threeFingerProcessing(){
        float startRadius = getRadius(startPoints[0], startPoints[1], startPoints[2]);
        float endRadius = getRadius(endPoints[0], endPoints[1], endPoints[2]);
        if( moveCount < 20 && Math.abs(averageFinalDistance - averageInitialDistance) < 10){
            Log.d(TAG, "Difference Time : " +(endTime - startTime));
            if(endTime - startTime >300)
                return Event.GESTURE_LONG_PRESS;
                //Log.d(TAG,"Hold");
            else
                return Event.GESTURE_TAP;
                //Log.d(TAG,"Tap");
        }
        else if(Math.abs(averageFinalDistance - averageInitialDistance) < 250) {
            Log.d(TAG,"X : "+startX/3+","+endX/3);
            Log.d(TAG,"Y : "+startY/3+","+endY/3);
            startY /=3;
            endY /=3;
            if(Math.abs(startY - endY) < 300) {
                if(startX > endX)
                    return Event.GESTURE_SWIPE_LEFT;
                    //Log.d(TAG, "Swipe Left");
                else
                    return Event.GESTURE_SWIPE_RIGHT;
                    //Log.d(TAG, "Swipe Right");
            }
            else {
                if(startY > endY)
                    return Event.GESTURE_SWIPE_UP;
                    //Log.d(TAG, "Swipe Up");
                else
                    return Event.GESTURE_SWIPE_DOWN;
                    //Log.d(TAG, "Swipe Down");
            }
        }
        else {
            if(startRadius > endRadius)
                return Event.GESTURE_PINCH;
                //Log.d(TAG, "Pinch");
            else
                return Event.GESTURE_SPREAD;
                //Log.d(TAG, "Spread");
        }

    }

    private int fourFingerProcessing(){
        Log.d(TAG,"fourFingerProcessing");
        if(moveCount < 20 && Math.abs(averageFinalDistance - averageInitialDistance) < 5){
            Log.d(TAG, "Difference Time : " +(endTime - startTime));
            if(endTime - startTime >300)
                return Event.GESTURE_LONG_PRESS;
                //Log.d(TAG,"Hold");
            else
                return Event.GESTURE_TAP;
                //Log.d(TAG,"Tap");
        }
        else if(averageInitialDistance > 2*averageFinalDistance){
            return Event.GESTURE_PINCH;
            // Log.d(TAG,"Pinch");
        }
        else if(averageFinalDistance > 2*averageInitialDistance){
            return Event.GESTURE_SPREAD;
            //Log.d(TAG,"Spread");
        }
        else{
            Log.d(TAG,"X : "+startX/4+","+endX/4);
            Log.d(TAG,"Y : "+startY/4+","+endY/4);
            startY /=4;
            endY /=4;
            if(Math.abs(startY - endY) < 300) {
                if(startX > endX)
                    return Event.GESTURE_SWIPE_LEFT;
                    //Log.d(TAG, "Swipe Left");
                else
                    return Event.GESTURE_SWIPE_RIGHT;
                    //Log.d(TAG, "Swipe Right");
            }
            else {
                if(startY > endY)
                    return Event.GESTURE_SWIPE_UP;
                    //Log.d(TAG, "Swipe Up");
                else
                    return Event.GESTURE_SWIPE_DOWN;
                    //Log.d(TAG, "Swipe Down");
            }

        }

    }

    private int fiveFingerProcessing() {
        Log.d(TAG,"fiveFingerProcessing");
        if(moveCount < 20 && Math.abs(averageFinalDistance - averageInitialDistance) < 5){
            Log.d(TAG, "Difference Time : " +(endTime - startTime));
            if(endTime - startTime >300)
                return Event.GESTURE_LONG_PRESS;
                //Log.d(TAG,"Hold");
            else
                return Event.GESTURE_TAP;
            //Log.d(TAG,"Tap");
        }
        else if(averageInitialDistance > 2*averageFinalDistance){
            return Event.GESTURE_PINCH;
            // Log.d(TAG,"Pinch");
        }
        else if(averageFinalDistance > 2*averageInitialDistance){
            return Event.GESTURE_SPREAD;
            //Log.d(TAG,"Spread");
        }
        else{
            Log.d(TAG,"X : "+startX/5+","+endX/5);
            Log.d(TAG,"Y : "+startY/5+","+endY/5);
            startY /=4;
            endY /=4;
            if(Math.abs(startY - endY) < 300) {
                if(startX > endX)
                    return Event.GESTURE_SWIPE_LEFT;
                    //Log.d(TAG, "Swipe Left");
                else
                    return Event.GESTURE_SWIPE_RIGHT;
                //Log.d(TAG, "Swipe Right");
            }
            else {
                if(startY > endY)
                    return Event.GESTURE_SWIPE_UP;
                    //Log.d(TAG, "Swipe Up");
                else
                    return Event.GESTURE_SWIPE_DOWN;
                //Log.d(TAG, "Swipe Down");
            }

        }
    }

    private double getDistance(PointF one, PointF two){
        return Math.sqrt(Math.pow(one.x - two.x,2)+Math.pow(one.y- two.y,2));
    }

    private double getAverageDistance(double []distanceArray){
        double averageDistance = 0;
        for(int i=0;i<distanceArray.length;i++)
            averageDistance+=distanceArray[i];
        averageDistance = averageDistance/distanceArray.length;
        return averageDistance;
    }

    public float getRadius(PointF one,PointF two,PointF three){
        float radius = 0.0f;
        float [][]A = new float[3][3];
        A[0][0] = one.x; A[0][1] = one.y; A[0][2] = 1;
        A[1][0] = two.x; A[1][1] = two.y; A[1][2] = 1;
        A[2][0] = three.x; A[2][1] = three.y; A[2][2] = 1;
        float []B = new float[3];
        B[0] = (one.x*one.x + one.y*one.y);
        B[1] = (two.x*two.x + two.y*two.y);
        B[2] = (three.x*three.x + three.y*three.y);
        A = MatrixUtil.invert(A);
        float []answer = new float[3];
        for(int i=0;i<A.length;i++){
            answer[i] = A[i][0]*B[0] + A[i][1]*B[1] + A[i][2]*B[2] ;
        }
        System.out.println(Arrays.toString(answer));
        float temp1 = Math.abs(answer[0])/2;
        float temp2 = Math.abs(answer[1])/2;
        float rhs = (answer[2] + temp1*temp1 + temp2*temp2);
        radius = (float)Math.sqrt(rhs);
        return radius;
    }
}
