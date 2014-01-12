package com.metaio.Template;

import java.io.FileInputStream;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometryVector;
import com.metaio.tools.io.AssetsManager;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;

public class MusicScore {
	private MediaPlayer mMediaPlayer;
	MediaTimeThread mediaTimeControl;
	
	DanceHandler danceHandler;
	IGeometryVector mModelVector;
	int unit;
	
	public MusicScore(int unit, IGeometryVector mModelVector){
		this.unit = unit;
		this.mModelVector = mModelVector;
		danceHandler = new DanceHandler(unit, mModelVector);
	}
	
	public void playSound()
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
		
		startDance();
	}
	
	private void startDance(){
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
						Message[] msgList = new Message[5];
						for(int i = 0 ; i < 5 ; i++){
							mModelVector.get(i).startAnimation("tapd_start", true);	// change dance to tapd
							rotateModel(msgList[i], i, CW, 2);	// rotate id_0 ~ id_4
						}
						doOnce = second;
					}
					if(second == 460 && doOnce < second){
						Message[] msgList = new Message[5];
						for(int i = 0 ; i < msgList.length ; i++){
							if(i == 0) continue;	// don't do id 0
							circleModel(msgList[i], 0, 0, i, 4, true);	// 2 circle
						}
						doOnce = second;
					}
					if(second == 520 && doOnce < second){	// rotate id_0 ~ id_4
						Message[] msgList = new Message[5];
						for(int i = 0 ; i < 5 ; i++){
							if(i % 2 == 0)
								rotateModel(msgList[i], i, CCW, 4);	// notice 4 model
							else
								rotateModel(msgList[i], i, CW, 4);	// notice 4 model
						}
						doOnce = second;
					}
					if(second == 670 && doOnce < second){
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
		
		public void circleModel(Message msg, float cX, float cY, int id, float circleTimes, boolean isCW){
			msg = new Message();
			msg.what = 5;
			Bundle bundle = new Bundle();
			bundle.putInt("id", id);
			bundle.putFloat("centerX", cX);
			bundle.putFloat("centerY", cY);
			bundle.putFloat("circleTimes", circleTimes);
			bundle.putBoolean("isCW", isCW);
			msg.setData(bundle);
			danceHandler.sendMessage(msg);
		}
	}
}
