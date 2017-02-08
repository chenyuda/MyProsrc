/*******************************************************************************
 * Copyright (c) 2016 by Ohio Corporation all right reserved.
 * 2016-8-1 
 * 
 *******************************************************************************/ 
package com.smart.mirrorview.utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.Surface;

/**
 * <pre>
 * 功能说明: 
 * 日期:	2016-8-1
 * 开发者:	cyd
 * 
 * 历史记录
 *    修改内容：
 *    修改人员：
 *    修改日期： 2016-8-1
 * </pre>
 */
@SuppressLint("InlinedApi")
@SuppressWarnings("deprecation")
public class CameraHelper {
	
	private final CameraHelperImpl mImpl;
	
	
	/**
	 * 
	 */
	public CameraHelper(final Context context) {
		
		if(SDK_INT >= GINGERBREAD){
			mImpl=new CameraHelperGB();
			Log.e("TGA", "CameraHelperGB");
		}else{
			mImpl = new CameraHelperBase(context);
			Log.e("TGA", "CameraHelperBase");
		}
	}
	
	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 *
	 */
	public int getNumberOfCameras() {
		// TODO Auto-generated method stub
		return mImpl.getNumberOfCameras();
	}
	
	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 * @return 
	 *
	 */
	public Camera openCamera(int id) {
		// TODO Auto-generated method stub
		return mImpl.openCamera(id);
	}
	
	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 *
	 */
	public Camera openDefaultCamera() {
		// TODO Auto-generated method stub
		return mImpl.openDefaultCamera();
	}
	
	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 * @return 
	 *
	 */
	public Camera openFrontCamera() {
		// TODO Auto-generated method stub
		return mImpl.openCameraFacing(CameraInfo.CAMERA_FACING_FRONT);
	}
	
	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 *
	 */
	public Camera openBackCamera() {
		// TODO Auto-generated method stub
		return mImpl.openCameraFacing(CameraInfo.CAMERA_FACING_BACK);
	}
	
	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 *
	 */
	@SuppressLint("InlinedApi")
	public boolean hasFrontCamera() {
		// TODO Auto-generated method stub
		return mImpl.hasCamera(CameraInfo.CAMERA_FACING_FRONT);
	}
	
	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 *
	 */
	@SuppressLint("InlinedApi")
	public boolean hasBackCamera() {
		// TODO Auto-generated method stub
		return mImpl.hasCamera(CameraInfo.CAMERA_FACING_BACK);
	}
	
	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 *
	 */
	@SuppressLint("InlinedApi")
	public void getCameraInfo(final int cameraId, final CameraInfo2 cameraInfo) {
		// TODO Auto-generated method stub
		mImpl.getCameraInfo(cameraId, cameraInfo);
	}
	
	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 *
	 */
	public void setCameraDisplayOrientation(final Activity activity,
            final int cameraId, final Camera camera) {
		// TODO Auto-generated method stub
		int result = getCameraDisplayOrientation(activity, cameraId);
        camera.setDisplayOrientation(result);
	}
	
	
	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 *
	 * @param activity
	 * @param cameraId
	 * @return
	 */
	public int getCameraDisplayOrientation(Activity activity, int cameraId) {
		// TODO Auto-generated method stub
		int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        CameraInfo2 info = new CameraInfo2();
        getCameraInfo(cameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
        	//result = (info.orientation - degrees + 360) % 360;
        } else { // back-facing
        	//result = (info.orientation + degrees) % 360;
            result = (info.orientation - degrees + 360) % 360;
        }
		return result;
	}


	public interface CameraHelperImpl{
		int getNumberOfCameras();

        Camera openCamera(int id);

        Camera openDefaultCamera();

        Camera openCameraFacing(int facing);

        boolean hasCamera(int cameraFacingFront);

        void getCameraInfo(int cameraId, CameraInfo2 cameraInfo);
	}
	
	
	public static class CameraInfo2 {
        public int facing;
        public int orientation;
    }
}
