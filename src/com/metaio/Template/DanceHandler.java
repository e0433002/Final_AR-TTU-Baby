package com.metaio.Template;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IGeometryVector;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.Vector3d;

public class DanceHandler extends Handler{
	IGeometryVector mModelVector;
	int unit;
	
	float rightAngleConst = (float) 1.5707957651346171970720366937891;	// 90 degree
	
	public DanceHandler(int unit, IGeometryVector mModelVector){
		this.unit = unit;
		this.mModelVector = mModelVector;
	}
	
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
			case 5:		// move to some direction
				new MyThread(){
					public void run() {
						// circleModel(float cX, float cY, int id, int circleTimes);
						int id = bundle.getInt("id");
						float cX = bundle.getFloat("centerX");
						float cY = bundle.getFloat("centerY");
						float circleTimes = bundle.getFloat("circleTimes");
						boolean isCW = bundle.getBoolean("isCW");
						IGeometry tmpModel = mModelVector.get(id);
						Vector3d mTrans = tmpModel.getTranslation();
						float mX = mTrans.getX();
						float mY = mTrans.getY();
						//float mR = (float) Math.sqrt(Math.pow(Math.abs(cX-mX), 2) + Math.pow(Math.abs(cY-mY), 2));	// not precise
						float mR = (float) Math.sqrt(Math.pow(cX-mX, 2) + Math.pow(cY-mY, 2));	// not precise
						float moveUnit = (float) 1;
						float circleUpOrDown = (mY > 0) ? 1 : -1;
						circleUpOrDown = (isCW) ? circleUpOrDown : -circleUpOrDown;
						
						DecimalFormat df = new DecimalFormat("#.#");
						//float border = Float.valueOf(df.format(mR));
						float border = (int) mR;
						float i = mX;
						int count = 0;
						while(count < circleTimes){
							i += circleUpOrDown * moveUnit;	// if moveUnit is 0.1, the x increase in 0.1
							i = Float.valueOf(df.format(i));	// format as #.#
							if(i == border){
								circleUpOrDown *= -1;
							}
							else if(i == -border){
								circleUpOrDown *= -1;
							}
							float y = (float) Math.sqrt((double) ((mR * mR) - (i * i)));
							tmpModel.setTranslation(new Vector3d(i, circleUpOrDown * y, 0));
							//y = Float.valueOf(df.format(y));	// format as #.#
							if(i == mX) count++;
							try {
								if(i > mR-7)
									Thread.sleep(50);
								else if(i < -mR+7)
									Thread.sleep(50);
								else
									Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
				}.start();
				break;
			default:	
		}
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
}
