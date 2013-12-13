// Copyright 2007-2013 metaio GmbH. All rights reserved.
package com.metaio.Template;

import java.io.FileInputStream;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.GestureHandlerAndroid;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.EVISUAL_SEARCH_STATE;
import com.metaio.sdk.jni.GestureHandler;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IGeometryVector;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IVisualSearchCallback;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector2d;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.sdk.jni.VisualSearchResponseVector;
import com.metaio.tools.io.AssetsManager;

public class Template extends ARViewActivity 
{
	private IGeometry mModel;
	private IGeometry kaGeBunShin;	// the sub model
	private IGeometryVector mModelVector;
	
	private MetaioSDKCallbackHandler mCallbackHandler;
	// addition
	private Vector2d mMidPoint;
	float x;
	float y;
	double lastDis = 0;
	boolean isStartAnimation = false;
	float radiansConst = (float) 1.5707957651346171970720366937891 *0;	// if *1 close 90 degrees
	float rightAngleConst = (float) 1.5707957651346171970720366937891;	// 90 degree
	float scale = (float) 0.03;
	int unit = 120;
	
	private int mGestureMask;
	private GestureHandlerAndroid mGestureHandler;
	private MediaPlayer mMediaPlayer;
	MediaTimeThread mediaTimeControl;
	static private DanceHandler danceHandler;
	//private Handler danceHandler;
	
	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		mCallbackHandler = new MetaioSDKCallbackHandler();
		
		mGestureMask = GestureHandler.GESTURE_ALL;
		mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);
		
		mMidPoint = new Vector2d();
		
		// music load
		/*try {
			mMediaPlayer = new MediaPlayer();
			FileInputStream fis = new FileInputStream(AssetsManager.getAssetPath("psy_gangnamStyle.mp3"));
			mMediaPlayer.setDataSource(fis.getFD());
			mMediaPlayer.prepare();
			fis.close();
		} catch (Exception e) {
			mMediaPlayer = null;
		}*/
		
		// here according to music time setting dancing style
		danceHandler = new DanceHandler();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		super.onTouch(v, event);
		
		mGestureHandler.onTouch(v, event);
		/*
		if(event.getPointerCount() == 1 && mModel.isVisible()){
			IGeometry isGeometry = metaioSDK.getGeometryFromScreenCoordinates((int)x, (int)y, true);
			if(isGeometry != null){
				Vector3d translation = mModel.getTranslation();
				float moveX = event.getX() - this.x;
				float moveY = (event.getY() - this.y) * -1;
				float moveValue = (float) 5;
				//float rangeDet = (float) 1;
				float microMove = (float) 0.1;
				
				for(int i = 0 ; i < moveValue/microMove ; i++){
					if(moveX > 0)
						translation.setX(translation.getX() + microMove);
					else if(moveX < 0)
						translation.setX(translation.getX() - microMove);
					
					if(moveY > 0)
						translation.setY(translation.getY() + microMove);
					else if(moveY < 0)
						translation.setY(translation.getY() - microMove);
					
					mModel.setTranslation(translation);
				}
				
				mModel.setTranslation(translation);
			}
			this.x = event.getX();
			this.y = event.getY();
		}
		*/
		//pinch model
		if(event.getPointerCount() == 2 && mModel.isVisible()){
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			double nowDis = Math.sqrt(x * x + y * y);
			
			if(lastDis < nowDis)
				mModel.setScale(mModel.getScale().add(new Vector3d(0.001f)));	// tiger is 1f
			else if(lastDis > nowDis)
				mModel.setScale(mModel.getScale().subtract(new Vector3d(0.001f)));	// tiger is 1f
			lastDis = nowDis;
		}
		return true;
	}
	
	@Override
	protected int getGUILayout() 
	{
		// Attaching layout to the activity
		return R.layout.template;	// return to ARViewActivity, that change layout from main to template
	}

	public void onButtonClick(View v)
	{
		finish();
	}
	
	@Override
	protected void loadContents() 
	{
		try
		{
			mModelVector = new IGeometryVector();	// store model for control
			
			// Getting a file path for tracking configuration XML file
			String trackingConfigFile = AssetsManager.getAssetPath("TrackingData_MarkerlessFast.xml");
			
			// Assigning tracking configuration
			boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
			
			MetaioDebug.log("Tracking data loaded: " + result); 
	        
			// Getting a file path for a 3D geometry
			String metaioManModel = AssetsManager.getAssetPath("metaioman.md2");
			if (metaioManModel != null) 
			{
				// Loading 3D geometry
				mModel = metaioSDK.createGeometry(metaioManModel);
				mModelVector.add(mModel);	// add in vector
				if (mModel != null) 
				{
					mGestureHandler.addObject(mModel, 1);	// gesture addition
					mModel.setName("Id 0");					// set Name
					// Set geometry properties
					mModel.setTranslation(new Vector3d(0, 0, 0));
					mModel.setRotation(new Rotation(-radiansConst, 0f, 0f));
					mModel.setScale(scale);
					mModel.setAnimationSpeed(90f);
					
					createKaGeBunShin();	// other model should be load
				}
				else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "+metaioManModel);
			}
		}catch (Exception e){
			
		}
	}
	
	private void createKaGeBunShin() {
		String metaioManModel = AssetsManager.getAssetPath("metaioman.md2");
		
		for(int i = 0 ; i < 4 ; i++){
			kaGeBunShin = metaioSDK.createGeometry(metaioManModel);
			mGestureHandler.addObject(kaGeBunShin, 2+i);	// add to gesture handler
			// Set geometry properties
			int line = unit;
			switch (i) {
			case 0:
				kaGeBunShin.setTranslation(new Vector3d(-line, line, 0));
				break;
			case 1:
				kaGeBunShin.setTranslation(new Vector3d(line, line, 0));
				break;
			case 2:
				kaGeBunShin.setTranslation(new Vector3d(-line, -line, 0));
				break;
			case 3:
				kaGeBunShin.setTranslation(new Vector3d(line, -line, 0));
				break;
			}
			kaGeBunShin.setRotation(new Rotation(-radiansConst, 0f, 0f));
			kaGeBunShin.setScale(scale);
			kaGeBunShin.setAnimationSpeed(90f);
			
			kaGeBunShin.setVisible(false);
			
			kaGeBunShin.setName("Id "+(i+1));
			mModelVector.add(kaGeBunShin);
		}
	}
	
	@Override
	protected void onGeometryTouched(IGeometry geometry) 
	{
		if(!isStartAnimation){
			playSound();
			for(int i = 0 ; i < mModelVector.size() ; i++){
				mModelVector.get(i).startAnimation("dance_start", true);
			}
			isStartAnimation = true;
		}
		else{
			// geometry stop
			/*mMediaPlayer.stop();
			mediaTimeControl.interrupt();
			for(int i = 0 ; i < mModelVector.size() ; i++){
				if(i != 0) mModelVector.get(i).setVisible(false);
				mModelVector.get(i).stopAnimation();
			}
			isStartAnimation = false;*/
		}	
	}
	
	private void playSound()
	{
		MetaioDebug.log("Playing sound");
		try {
			mMediaPlayer = new MediaPlayer();
			FileInputStream fis = new FileInputStream(AssetsManager.getAssetPath("psy_gangnamStyle.mp3"));
			mMediaPlayer.setDataSource(fis.getFD());
			mMediaPlayer.prepare();
			fis.close();
		} catch (Exception e) {
			mMediaPlayer = null;
		}
		mMediaPlayer.start();
		
		mediaTimeControl = new MediaTimeThread(){		// new thread for send music time
			public void run() {
				int doOnce = 0; 
				while (true) {
					int second = mMediaPlayer.getCurrentPosition()/100;
					if(second == 36 && doOnce < second){
						appearModel(1);
						doOnce = second;
					}
					if(second == 75 && doOnce < second){
						appearModel(2);
						doOnce = second;
					}
					if(second == 109 && doOnce < second){
						appearModel(3);
						doOnce = second;
					}
					if(second == 125 && doOnce < second){
						glistenAllModel();
						doOnce = second;
					}
					if(second == 150 && doOnce < second){	// rotate id_0 ~ id_4
						Message[] msgList = new Message[5];
						for(int i = 0 ; i < 5 ; i++){
							rotateModel(msgList[i], i, CW, 4);
						}
						doOnce = second;
					}
					if(second == 220 && doOnce < second){	// rotate id_0 ~ id_4
						Message[] msgList = new Message[5];
						for(int i = 0 ; i < 5 ; i++){
							rotateModel(msgList[i], i, CCW, 4);	// notice 4 model
						}
						doOnce = second;
					}
					if(second == 275 && doOnce < second){
						glistenAllModel();
						doOnce = second;
					}
					if(second == 290 && doOnce < second){
						Message[] msgList = new Message[5];
						for(int i = 1 ; i < 5 ; i++){
							moveModel(msgList[i], i, dirctX, right, unit);	// move right id_1 ~ id_4
							rotateModel(msgList[i], i, CCW, 1);				// rotate id_1 ~ id_4
						}
						rotateModel(msgList[0], 0, CCW, 1);					// rotate id_0
						doOnce = second;
					}
					if(second == 330 && doOnce < second){
						Message[] msgList = new Message[5];
						for(int i = 0 ; i < 5 ; i++){
							moveModel(msgList[i], i, dirctY, down, unit);	// move down id_0 ~ id_4
							rotateModel(msgList[i], i, CW, 1);				// rotate id_0 ~ id_4
						}
						doOnce = second;
					}
					if(second == 365 && doOnce < second){
						Message[] msgList = new Message[5];
						for(int i = 1 ; i < 5 ; i++){
							moveModel(msgList[i], i, dirctX, left, unit);	// move left id_1 ~ id_4
							rotateModel(msgList[i], i, CW, 1);				// rotate id_1 ~ id_4
						}
						rotateModel(msgList[0], 0, CW, 1);					// rotate id_0
						doOnce = second;
					}
					if(second == 400 && doOnce < second){
						Message[] msgList = new Message[5];
						for(int i = 0 ; i < 5 ; i++){
							moveModel(msgList[i], i, dirctY, up, unit);	// move up id_0 ~ id_4
							rotateModel(msgList[i], i, CW, 1);			// rotate id_0
						}
						doOnce = second;
					}
					if(second == 420 && doOnce < second){
						glistenAllModel();
						doOnce = second;
					}
					if(second == 440 && doOnce < second){
						mModelVector.get(0).startAnimation("tapd_start", true);
						doOnce = second;
					}
					if(second == 675 && doOnce < second){
						for(int i = 0 ; i < 5 ; i++){
							mModelVector.get(i).startAnimation("end_start");
						}
						doOnce = second;
					}
				}
			};
		};
		mediaTimeControl.start();
	}
	
	@Override
	public void onSurfaceChanged(int width, int height) 
	{
		super.onSurfaceChanged(width, height);
		// Update mid point of the view
		mMidPoint.setX(width/2f);
		mMidPoint.setY(height/2f);
		
		this.x = mMidPoint.getX();
		this.x = mMidPoint.getY();
	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() 
	{
		return mCallbackHandler;
	}
	
	
	@SuppressLint("HandlerLeak")
	class DanceHandler extends Handler
	{
		public void handleMessage(final Message msg) {	// while get message from Media Control Thread
			// according to the music to do what you want to do
			super.handleMessage(msg);
			final Bundle bundle = msg.getData();
			switch (msg.what) {
			case 1:		// rotate one circle
				new MyThread(){
					public void run() {
						int id = bundle.getInt("id");
						String clockwise = bundle.getString("clockwise");
						int rightAngleNum = bundle.getInt("rightAngleNum");
						int direct = (clockwise.equals("CCW")) ? 1 : -1;
						IGeometry tmpModel = mModelVector.get(id);
						float nowAngle = tmpModel.getRotation().getEulerAngleRadians().getZ();
						for(float i = 0 ; i < (rightAngleNum * rightAngleConst) ; i+=0.01){
							tmpModel.setRotation(new Rotation(0f, 0f, nowAngle + direct * i));
							delayForRotate();
						}
					};
				}.start();
				break;
			case 2:		// appear model
				new MyThread(){
					public void run() {
						int id = bundle.getInt("id");
						IGeometry tmpModel = mModelVector.get(id);
						tmpModel.setVisible(true);
					}
				}.start();
				break;
			case 3:		// glisten model
				new MyThread(){
					public void run() {
						int tempo = 18;
						boolean glisten = false;
						for(int i = 0 ; i < tempo ; i++){	// five rhythm
							for(int j = 0 ; j < 5 ; j++){	// five model
								mModelVector.get(j).setVisible(glisten);
							}
							delayForGlisten(i, tempo);				// using thread sleep
							if(glisten) glisten = false;
							else glisten = true;
						}
					};
				}.start();
				break;
			case 4:		// move to some direction
				new MyThread(){
					public void run() {
						int id = bundle.getInt("id");
						String directHead = "move ";
						String directX = bundle.getString("directX");
						String directY = bundle.getString("directY");
						IGeometry tmpModel = mModelVector.get(id);
						Vector3d mTrans = tmpModel.getTranslation();
						float mX = 0;
						float mY = 0;
						int movingDis = bundle.getInt("movingDis");
						int movingX = getXDirect(directHead+directX);
						int movingY = getYDirect(directHead+directY);
						
						for(int i = 0 ; i < movingDis ; i++){
							mX = mTrans.getX();
							mY = mTrans.getY();
							mTrans.setX(mX + movingX);
							mTrans.setY(mY + movingY);
							tmpModel.setTranslation(mTrans);
							try {
								Thread.sleep(15);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
				}.start();
				break;
			default:
				
			}
		};
	}
	
	class MyThread extends Thread
	{
		int id;
		
		public MyThread() {
			
		}
		
		public MyThread(int id) {
			this.id = id;
		}
		
		public void delayForRotate(){
			try {
				Thread.sleep(8);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void delayForGlisten(int i, int tempo){
			try {
				Thread.sleep(10*tempo - 10*i);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public int getXDirect(String direct){
			if(direct.equals("move right")) return 1;
			else if(direct.equals("move left")) return -1;
			else return 0;
		}
		
		public int getYDirect(String direct){
			if(direct.equals("move up")) return 1;
			else if(direct.equals("move down")) return -1;
			else return 0;
		}
	}
	
	
	class MediaTimeThread extends Thread
	{
		final String CW = "CW";	// clockwise
		final String CCW = "CCW";	// counter clockwise
		final String dirctX = "directX";
		final String dirctY = "directY";
		final String up = "up";
		final String down = "down";
		final String right = "right";
		final String left = "left";
		
		public MediaTimeThread(){
			
		}
		
		public void appearModel(int id){
			Message msg = new Message();
			msg.what = 2;	// appear
			Bundle bundle = new Bundle();
			bundle.putInt("id", id);	// id_1
			msg.setData(bundle);	// set bundle in msg
			danceHandler.sendMessage(msg);
		}
		
		public void glistenAllModel(){
			Message msg = new Message();
			msg.what = 3;	// glisten id_0 ~ id_4
			danceHandler.sendMessage(msg);
		}
		
		public void rotateModel(Message msg, int id, String clockwise, int rightAngleNum){
			msg = new Message();
			msg.what = 1;
			Bundle bundle = new Bundle();
            bundle.putInt("id", id);
            bundle.putString("clockwise", clockwise);	// CW or CCW
            bundle.putInt("rightAngleNum", rightAngleNum);		// 360 degree
			msg.setData(bundle);	// set bundle in msg
			danceHandler.sendMessage(msg);
		}
		
		public void moveModel(Message msg, int id, String coordinate, String direct, int unit){
			msg = new Message();
			msg.what = 4;
			Bundle bundle = new Bundle();
            bundle.putInt("id", id);
            bundle.putString(coordinate, direct);
            bundle.putInt("movingDis", unit);
			msg.setData(bundle);	// set bundle in msg
			danceHandler.sendMessage(msg);
		}
	}
	
	
	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback 
	{

		@Override
		public void onSDKReady() 
		{
			MetaioDebug.log("The SDK is ready");
		}
		
		@Override
		public void onAnimationEnd(IGeometry geometry, String animationName) 
		{
			MetaioDebug.log("animation ended" + animationName);
		}
		
		@Override
		public void onMovieEnd(IGeometry geometry, String name)
		{
			MetaioDebug.log("movie ended" + name);
		}
		
		@Override
		public void onNewCameraFrame(ImageStruct cameraFrame)
		{
			MetaioDebug.log("a new camera frame image is delivered" + cameraFrame.getTimestamp());
		}
		
		@Override 
		public void onCameraImageSaved(String filepath)
		{
			MetaioDebug.log("a new camera frame image is saved to" + filepath);
		}
		
		@Override
		public void onScreenshotImage(ImageStruct image)
		{
			MetaioDebug.log("screenshot image is received" + image.getTimestamp());
		}
		
		@Override
		public void onScreenshotSaved(String filepath)
		{
			MetaioDebug.log("screenshot image is saved to" + filepath);
		}
		
		@Override
		public void onTrackingEvent(TrackingValuesVector trackingValues)
		{
			MetaioDebug.log("The tracking time is:" + trackingValues.get(0).getTimeElapsed());
		}

		@Override
		public void onInstantTrackingEvent(boolean success, String file)
		{
			if (success)
			{
				MetaioDebug.log("Instant 3D tracking is successful");
			}
		}
	}
	
	final class VisualSearchCallbackHandler extends IVisualSearchCallback
	{

		@Override
		public void onVisualSearchResult(VisualSearchResponseVector response, int errorCode)
		{
			if (errorCode == 0)
			{
				MetaioDebug.log("Visual search is successful");
			}
		}

		@Override
		public void onVisualSearchStatusChanged(EVISUAL_SEARCH_STATE state) 
		{
			MetaioDebug.log("The current visual search state is: " + state);
		}
	}
	
}
