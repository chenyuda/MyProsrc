/*******************************************************************************
 * Copyright (c) 2016 by Ohio Corporation all right reserved.
 * 2016-8-1 
 * 
 *******************************************************************************/ 
package com.smart.mirrorview.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

import com.smart.mirrorview.utils.CameraHelper.CameraHelperImpl;
import com.smart.mirrorview.utils.CameraHelper.CameraInfo2;

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
@SuppressWarnings("deprecation")
public class CameraHelperBase implements CameraHelperImpl {

	private final Context mContext;

    public CameraHelperBase(final Context context) {
        mContext = context;
    }
    
	public int getNumberOfCameras() {
		// TODO Auto-generated method stub
		return hasCameraSupport() ? 1 : 0;
	}

	/**
	 * 功能说明：
	 * 日期:	2016-8-1
	 * 开发者: cyd
	 *
	 * @return
	 */
	private boolean hasCameraSupport() {
		// TODO Auto-generated method stub
		return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	
	public Camera openCamera(int id) {
		// TODO Auto-generated method stub
		return Camera.open();
	}


	public Camera openDefaultCamera() {
		// TODO Auto-generated method stub
		return Camera.open();
	}

	
	public Camera openCameraFacing(int facing) {
		// TODO Auto-generated method stub
		if (facing == CameraInfo.CAMERA_FACING_BACK) {
            return Camera.open();
        }
        return null;
	}

	
	public boolean hasCamera(int cameraFacingFront) {
		// TODO Auto-generated method stub
		 if (cameraFacingFront == CameraInfo.CAMERA_FACING_BACK) {
	            return hasCameraSupport();
	        }
	        return false;
	}


	@SuppressLint("InlinedApi")
	public void getCameraInfo(int cameraId, CameraInfo2 cameraInfo) {
		// TODO Auto-generated method stub
		cameraInfo.facing = Camera.CameraInfo.CAMERA_FACING_BACK;
        cameraInfo.orientation = 90;
	}

}
