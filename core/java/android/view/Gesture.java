package android.view;

import java.io.Serializable;

/** 
 * @hide
 */
public class Gesture implements Serializable {
	
	private long time;
	private String gesture;
	private String gestureName;
	private String actionType;
	private String aMoreAccurateDesc;

	public Gesture() {
		time = System.currentTimeMillis();
	}

	public void setGesture(String gesture){
		this.gesture = gesture;
	}

	public String getGesture(){
		return gesture;
	}

	public String toString() {
		return aMoreAccurateDesc;
	}
	public String getGestureName() {
		return this.gestureName;
	}
	public String getActionType() {
		return this.actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public void setGestureName(String gestureName) {
		this.gestureName = gestureName;
	}
	public String getAMoreAccurateDesc() {
		return this.aMoreAccurateDesc;
	}
	public void setAMoreAccurateDesc(String aMoreAccurateDesc) {
		this.aMoreAccurateDesc = aMoreAccurateDesc;
	}
}