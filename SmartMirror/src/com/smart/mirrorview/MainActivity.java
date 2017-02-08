package com.smart.mirrorview;

/**
 * 为了尽快完成此次代码比较杂乱，且没有注释。不值得参考。引用了GPUImage库 ，改库涉及到的关于Camera类调用手机摄像头
 * 并且可以实时添加滤镜等效果，可以深入研究改库。
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.smart.mirrorview.utils.CameraHelper;
import com.smart.mirrorview.utils.GPUImageTools;
import com.smart.mirrorview.utils.CameraHelper.CameraInfo2;
import com.smart.mirrorview.utils.GPUImageFilterTools.FilterAdjuster;
import com.smart.mirrorview.utils.GPUImageTools.FilterType;
import com.smart.view.VerticalSeekBar;

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity implements
		OnSeekBarChangeListener, OnClickListener {
	private GLSurfaceView glSurfaceView;
	private GPUImage mGPUImage;
	private static Camera mCamera;
	private CameraHelper mHelper;
	private int mCurrentCameraId = 1;
	private GPUImageFilter mFilter;
	private VerticalSeekBar vSeekBar;
	private SeekBar mSeekBar;
	private Parameters parameters;

	private Button lj_bt, pz_bt, fz_bt;
	private Button nor_bt, invert_bt, hue_bt, sepia_bt, gray_bt, shar_bt,
			sobel_bt, convo_bt, emboss_bt, posterize_bt, group_bt, mono_bt;
	private Button remember_bt;
	
	private ImageView jk_img;
	

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private float start_x;
	private float start_y;
	private float stop_x;
	private float stop_y;

	private int hProgress = 480;
	private int vprogress;
	private int jk=0;
	private static boolean flipHorizontal = true;
	private static boolean seekbarisVisible = true;
	private static boolean flag=false;
	private static boolean scrollViewisVisible = false;
	private Timer timer = null;
	private Handler handler = new Handler();

	private HorizontalScrollView scrollView;

	private ViewGroup advg;
	private AdView mAdView;

	private SharedPreferences sp;

	private PowerManager powerManager;
	
	private WakeLock wakeLock;
	
	//定义一个传感器
//	private SensorManager sensorManager;
	
	//定义一个boolean 类型的参数来表明用户是否已经评论过
	private boolean iscomment=false;
	
	private ImageView block_img;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setScreenBrightness(1.0f);
		
		setContentView(R.layout.activity_main);
		initAppsflyer();
		initView();
		loadMobAd();
//		sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.post(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						mSeekBar.setVisibility(View.GONE);
						vSeekBar.setVisibility(View.GONE);
						block_img.setVisibility(View.GONE);
						seekbarisVisible = false;
					}
				});
			}
		}, 5000);
		// mListView.setAdapter(adapter);
		lj_bt.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (scrollViewisVisible) {
					scrollView.setVisibility(View.GONE);
					pz_bt.setVisibility(View.VISIBLE);
					fz_bt.setVisibility(View.VISIBLE);
					scrollViewisVisible = false;
				} else {
					scrollView.setVisibility(View.VISIBLE);
					pz_bt.setVisibility(View.GONE);
					fz_bt.setVisibility(View.GONE);
					scrollViewisVisible = true;
				}

				// if(listViewVisible){
				// mListView.setVisibility(View.GONE);
				// listViewVisible=false;
				// }else{
				// mListView.setVisibility(View.VISIBLE);
				// listViewVisible=true;
				// }
			}
		});

		pz_bt.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				// takePicture();
				if (mCamera
						.getParameters()
						.getFocusMode()
						.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
					// Log.e("TGA", "0");
					takePicture();
				} else {
					// Log.e("TGA", "1");
					mCamera.autoFocus(new Camera.AutoFocusCallback() {

						public void onAutoFocus(final boolean success,
								final Camera camera) {
							takePicture();
						}
					});
				}
			}
		});
		fz_bt.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				// mCamera.setDisplayOrientation(90);
				// setUpCamera();
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
				// mCurrentCameraId = (mCurrentCameraId + 1) %
				// mHelper.getNumberOfCameras();
//				mCamera = mHelper.openCamera(mCurrentCameraId);
				mCamera = Camera.open(mCurrentCameraId);
				// mCamera.setDisplayOrientation(180);
				if (flipHorizontal) {
					flipHorizontal = false;
				} else {
					flipHorizontal = true;
				}
				setUpCamera();

				// Rotate3d rotate = new Rotate3d();
				// rotate.setDuration(180);
				// rotate.setCenter(glSurfaceView.getMeasuredWidth() / 2,
				// glSurfaceView.getMeasuredHeight() / 2);
				// rotate.setFillAfter(true); //
				// 使动画结束后定在最终画面，如果不设置为true，则将会回到初始画面
				// glSurfaceView.startAnimation(rotate);
				// renderer.setCurrentEffect(R.id.fisheye);

				// Log.e("TGA", renderer.getRotation()+"");

				glSurfaceView.requestRender();
			}
		});
		
		block_img.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
				switch (jk) {
				case 0:
					jk=R.drawable.jk1;
					break;
				case R.drawable.jk1:
					jk=R.drawable.jk2;
					break;
				case R.drawable.jk2:
					jk=R.drawable.jk3;
					break;
				case R.drawable.jk3:
					jk=R.drawable.jk4;
					break;
				case R.drawable.jk4:
					jk=R.drawable.jk5;
					break;
				case R.drawable.jk5:
					jk=0;
					break;

				default:
					break;
				}
				
				if(jk==0){
					jk_img.setBackground(null);
				}else{
					jk_img.setBackgroundResource(jk);
				}
				timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						handler.post(new Runnable() {

							public void run() {
								// TODO Auto-generated method stub
								mSeekBar.setVisibility(View.GONE);
								vSeekBar.setVisibility(View.GONE);
								block_img.setVisibility(View.GONE);
								seekbarisVisible = false;
							}
						});
					}
				}, 5000);
			}
		});
		// mListView.setOnItemClickListener(new OnItemClickListener() {
		//
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// // TODO Auto-generated method stub
		// switchFilterTo(GPUImageTools.createFilterForType(MainActivity.this,
		// filter.filters.get(position)));
		// }
		// });

		// glSurfaceView.setOnClickListener(new OnClickListener() {
		//
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// if(seekbarisVisible){
		// mSeekBar.setVisibility(View.GONE);
		// vSeekBar.setVisibility(View.GONE);
		// seekbarisVisible=false;
		// if(timer!=null){
		// timer.cancel();
		// timer=null;
		// }
		// }else{
		// mSeekBar.setVisibility(View.VISIBLE);
		// vSeekBar.setVisibility(View.VISIBLE);
		// seekbarisVisible=true;
		// timer=new Timer();
		// timer.schedule(new TimerTask() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// handler.post(new Runnable() {
		//
		// public void run() {
		// // TODO Auto-generated method stub
		// mSeekBar.setVisibility(View.GONE);
		// vSeekBar.setVisibility(View.GONE);
		// seekbarisVisible=false;
		// }
		// });
		// }
		// }, 10000);
		// }
		// }
		// });
		glSurfaceView.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// Log.e("TGA", "GLDOWN");
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
					 if(!seekbarisVisible){
						 flag=false;
						 mSeekBar.setVisibility(View.VISIBLE);
						 vSeekBar.setVisibility(View.VISIBLE);
						 block_img.setVisibility(View.VISIBLE);
						 seekbarisVisible=true;
					 }else{
						 flag=true;
					 }
					
					start_x = event.getX();
					start_y = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					// Log.e("TGA", "GLMOVE");
//					flag=false;
					stop_x = event.getX();
					stop_y = event.getY();
					float dx = stop_x - start_x;
					float dy = stop_y - start_y;
					float absdx = Math.abs(dx);
					float absdy = Math.abs(dy);
					
//					mSeekBar.setVisibility(View.VISIBLE);
//					vSeekBar.setVisibility(View.VISIBLE);
//					block_img.setVisibility(View.VISIBLE);
//					seekbarisVisible = true;
					
					if(absdx>=0.4f||absdy>=0.4f){
						flag=false;
						if (absdx > absdy) {
							hProgress=(int) (hProgress+dx);
							mSeekBar.setProgress(hProgress);
							
						} else {
							vprogress=(int) (vprogress-dy);
							vSeekBar.setMoveProgress(vprogress);
							
						}
						start_x=stop_x;
						start_y=stop_y;
					}
					break;
				case MotionEvent.ACTION_UP:
					// Log.e("TGA", "GLUP");
//					if(!flag){
//						if (!seekbarisVisible) {
//							mSeekBar.setVisibility(View.VISIBLE);
//							vSeekBar.setVisibility(View.VISIBLE);
//							block_img.setVisibility(View.VISIBLE);
//							seekbarisVisible = true;
//						} else {
//							mSeekBar.setVisibility(View.GONE);
//							vSeekBar.setVisibility(View.GONE);
//							block_img.setVisibility(View.GONE);
//							seekbarisVisible = false;
//						}
//					}
//					Log.e("TGA", ""+flag);
					if (!flag) {
						timer = new Timer();
						timer.schedule(new TimerTask() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								handler.post(new Runnable() {

									public void run() {
										// TODO Auto-generated method stub
										mSeekBar.setVisibility(View.GONE);
										vSeekBar.setVisibility(View.GONE);
										block_img.setVisibility(View.GONE);
										seekbarisVisible = false;
									}
								});
							}
						}, 5000);
					}else{
						mSeekBar.setVisibility(View.GONE);
						vSeekBar.setVisibility(View.GONE);
						block_img.setVisibility(View.GONE);
						seekbarisVisible = false;
					}
					break;
				default:
					break;
				}
				return true;
			}
		});
	}

	/**
	 * 功能说明： 日期: 2016-9-8 开发者: cyd
	 * 
	 */
	private void loadMobAd() {
		// TODO Auto-generated method stub
		mAdView = new AdView(this);
		mAdView.setAdSize(AdSize.SMART_BANNER);
		mAdView.setAdUnitId("ca-app-pub-2802581248984684/3518811252");
		mAdView.setAdListener(new AdListener() {

			@Override
			public void onAdClosed() {
				// TODO Auto-generated method stub
				super.onAdClosed();
				advg.setVisibility(View.GONE);
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				// TODO Auto-generated method stub
				super.onAdFailedToLoad(errorCode);
			}

			@Override
			public void onAdLeftApplication() {
				// TODO Auto-generated method stub
				super.onAdLeftApplication();
			}

			@Override
			public void onAdLoaded() {
				// TODO Auto-generated method stub
				super.onAdLoaded();
				advg.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAdOpened() {
				// TODO Auto-generated method stub
				super.onAdOpened();
			}

		});

		AdRequest ar = new AdRequest.Builder().build();
		mAdView.loadAd(ar);
		advg.addView(mAdView);
	}

	/**
	 * 功能说明： 日期: 2016-8-4 开发者: cyd
	 * 
	 */
	protected void takePicture() {
		// TODO Auto-generated method stub
		// parameters.setPictureSize(1280, 960);
		if (flipHorizontal) {
			parameters.setRotation(90);
		} else {
			parameters.setRotation(270);
		}

		mCamera.setParameters(parameters);
		mCamera.takePicture(null, null, new PictureCallback() {

			public void onPictureTaken(byte[] data, final Camera camera) {
				// TODO Auto-generated method stub
				final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
				if (pictureFile == null) {
					Toast.makeText(MainActivity.this,
							"Failed to save photo, please try again!",
							Toast.LENGTH_SHORT).show();
					// Log.e("ASDF","Error creating media file, check storage permissions");
					return;
				}
				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
				} catch (FileNotFoundException e) {
					// Log.e("ASDF", "File not found: " + e.getMessage());
				} catch (IOException e) {
					// Log.e("ASDF", "Error accessing file: " + e.getMessage());
				}
				data = null;
				Bitmap bitmap = BitmapFactory.decodeFile(pictureFile
						.getAbsolutePath());
				// mGPUImage.setImage(bitmap);
//				Bitmap jk_bp=BitmapFactory.decodeResource(getResources(), id)
				final GLSurfaceView view = (GLSurfaceView) findViewById(R.id.surfaceview);
				view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
				mGPUImage.saveToPictures(bitmap, "GPUImage",
						System.currentTimeMillis() + ".jpg",
						new OnPictureSavedListener() {

							public void onPictureSaved(final Uri uri) {
								pictureFile.delete();
								mCamera.startPreview();
								view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
								Toast.makeText(MainActivity.this,
										"Photo already saved to album", Toast.LENGTH_SHORT)
										.show();
							}
						});
				
			}
		});

	}
	/*将像框和图片进行融合，返回一个Bitmap*/
	public Bitmap montageBitmap(Bitmap frame, Bitmap src, int x, int y){
	int w = src.getWidth();
	int h = src.getHeight();
	Bitmap sizeFrame = Bitmap.createScaledBitmap(frame, w, h, true);

	Bitmap newBM = Bitmap.createBitmap(w, h, Config.ARGB_8888);
	Canvas canvas = new Canvas(newBM);
	canvas.drawBitmap(src, x, y, null);
	canvas.drawBitmap(sizeFrame, 0, 0, null);
	return newBM;
	}
	/**
	 * 功能说明： 日期: 2016-8-4 开发者: cyd
	 * 
	 * @param mediaTypeImage
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	protected File getOutputMediaFile(int type) {
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Toast.makeText(MainActivity.this,
						"Failed to create a file, please try again!",
						Toast.LENGTH_SHORT).show();
				// Log.e("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	/**
	 * 功能说明： 日期: 2016-8-1 开发者: cyd
	 * 
	 */
	private void initView() {
		// TODO Auto-generated method stub
		mGPUImage = new GPUImage(this);
		mHelper = new CameraHelper(this);
		new GPUImageRenderer(new GPUImageFilter());
		glSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceview);
		
		mGPUImage.setGLSurfaceView(glSurfaceView);
		mSeekBar = ((SeekBar) findViewById(R.id.seekbar));
		sp = getSharedPreferences("Mirror", Activity.MODE_PRIVATE);
		hProgress = sp.getInt("hProgress", 480);
		mSeekBar.setProgress(hProgress);
		
		mSeekBar.setOnSeekBarChangeListener(this);

		vSeekBar = (VerticalSeekBar) findViewById(R.id.verticalSeekBar);
		vSeekBar.setOnSeekBarChangeListener(this);
		
		jk_img=(ImageView) findViewById(R.id.jk_img);
		jk=sp.getInt("jk_img", 0);
		if(jk!=0){
			jk_img.setBackgroundResource(jk);
		}
		lj_bt = (Button) findViewById(R.id.img_lj);
		fz_bt = (Button) findViewById(R.id.img_fz);
		pz_bt = (Button) findViewById(R.id.img_pz);

		nor_bt = (Button) findViewById(R.id.normal);
		nor_bt.setOnClickListener(this);

		invert_bt = (Button) findViewById(R.id.invert);
		invert_bt.setOnClickListener(this);

		hue_bt = (Button) findViewById(R.id.hue);
		hue_bt.setOnClickListener(this);

		sepia_bt = (Button) findViewById(R.id.sepia);
		sepia_bt.setOnClickListener(this);

		gray_bt = (Button) findViewById(R.id.gray);
		gray_bt.setOnClickListener(this);

		shar_bt = (Button) findViewById(R.id.shar);
		shar_bt.setOnClickListener(this);

		sobel_bt = (Button) findViewById(R.id.sobel);
		sobel_bt.setOnClickListener(this);

		convo_bt = (Button) findViewById(R.id.convolution);
		convo_bt.setOnClickListener(this);

		emboss_bt = (Button) findViewById(R.id.emboss);
		emboss_bt.setOnClickListener(this);

		posterize_bt = (Button) findViewById(R.id.postersize);
		posterize_bt.setOnClickListener(this);

		group_bt = (Button) findViewById(R.id.grouped);
		group_bt.setOnClickListener(this);

		mono_bt = (Button) findViewById(R.id.monochrome);
		mono_bt.setOnClickListener(this);
		remember_bt = new Button(this);

		
		scrollView = (HorizontalScrollView) findViewById(R.id.scroll);
		
		block_img=(ImageView) findViewById(R.id.block);
		

		advg = (ViewGroup) findViewById(R.id.ad_fra);
		
		iscomment=sp.getBoolean("iscomment", iscomment);
		
		try {
//			mCamera = mHelper.openCamera(mCurrentCameraId);
			mCamera = Camera.open(mCurrentCameraId);
			
			setUpCamera();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			if(mCamera==null){
				mCamera=Camera.open(1);
			}
//			Toast.makeText(
//					MainActivity.this,
//					"The camera is being used, please close other applications first!",
//					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		powerManager=(PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK  
                | PowerManager.ON_AFTER_RELEASE, "My lOCK");
		wakeLock.acquire();
		
		//注册传感器
//   	 Sensor sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        /*
         * 第二个参数 传感器类型
         * 第三个参数 传感器监听频率
         * */
//   	sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		
	}

	/**
	 * 功能说明： 日期: 2016-8-1 开发者: cyd
	 * 
	 */
	@SuppressLint("InlinedApi")
	private void setUpCamera() {
		// TODO Auto-generated method stub
		parameters = mCamera.getParameters();
		vprogress = sp.getInt("vprogress", 0);
		parameters.setZoom(vprogress / 10);
		parameters.setExposureCompensation((hProgress/4 - 120) / 10);
		// vprogress=parameters.getZoom();
		vSeekBar.setProgress(vprogress);
		// parameters.setPreviewSize(720, 480);
		// parameters.set("rotation", 180);
		// parameters.setRotation(180);
		if (parameters.getSupportedFocusModes().contains(
				Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			parameters
					.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}

		mCamera.setParameters(parameters);
		// mCamera.setDisplayOrientation(90);
		int orientation = mHelper.getCameraDisplayOrientation(this,
				mCurrentCameraId);
		CameraInfo2 cameraInfo = new CameraInfo2();
		mHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
		

		mGPUImage.setUpCamera(mCamera, orientation, flipHorizontal, false);
		// mGPUImage.setUpCamera(mCamera,270,false,false);
		// glSurfaceView.star
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
		
		SharedPreferences.Editor se = sp.edit();
		se.putInt("hProgress", hProgress);
		se.putInt("vprogress", vprogress);
		se.putInt("jk_img", jk);
		se.putBoolean("iscomment", iscomment);
		se.commit();
		wakeLock.release();
//		sensorManager.unregisterListener(this);
//		System.exit(0);
	}

	/**
	 * 功能说明： 日期: 2016-8-1 开发者: cyd
	 * 
	 * @param filter
	 */
	protected void switchFilterTo(GPUImageFilter filter) {
		// TODO Auto-generated method stub
		if (mFilter == null
				|| (filter != null && !mFilter.getClass().equals(
						filter.getClass()))) {
			mFilter = filter;
			mGPUImage.setFilter(mFilter);
			new FilterAdjuster(mFilter);
		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		// if(timer!=null){
		// timer.cancel();
		// timer=null;
		// }
		if (seekBar instanceof VerticalSeekBar) {
			vprogress = seekBar.getProgress();
			if (vprogress > 900) {
				// Toast.makeText(this, "It can't be enlarged anymore!",
				// Toast.LENGTH_SHORT).show();
			} else if (vprogress < 0) {
				// Toast.makeText(this, "It can't be reduced anymore!",
				// Toast.LENGTH_SHORT).show();
			} else {
				setCameraZoom(vprogress);
			}
			// vSeekBar.setProgress(vprogress);
			// if(){}
			// vSeekBar.setThumbOffset(90);
		} else {
			// SetLightness(this, progress);
			hProgress = seekBar.getProgress();
			if (hProgress >= 960) {
				// Toast.makeText(this, "It can't be any brighter than this!",
				// Toast.LENGTH_SHORT).show();
			} else if (hProgress <= 0) {
				// Toast.makeText(this, "It can't be any darker than this!",
				// Toast.LENGTH_SHORT).show();
			} else {
				// setScreenBrightness((float)hProgress/255f);
				setCamerExpose(hProgress/4 - 120);
			}

		}

		// if (mFilterAdjuster != null) {
		// mFilterAdjuster.adjust(progress);
		// }
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		// Log.e("TGA", "START");
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		// Log.e("TGA", "STOP");
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.post(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						mSeekBar.setVisibility(View.GONE);
						vSeekBar.setVisibility(View.GONE);
						seekbarisVisible = false;
					}
				});
			}
		}, 5000);
	}

	// 改变亮度
	// @SuppressWarnings("unused")
	// private void SetLightness(Activity act, int value) {
	// try {
	// Settings.System.putInt(act.getContentResolver(),
	// Settings.System.SCREEN_BRIGHTNESS, value);
	// WindowManager.LayoutParams lp = act.getWindow().getAttributes();
	// lp.screenBrightness = (value <= 0 ? 1 : value/100) / 255f;
	// act.getWindow().setAttributes(lp);
	// } catch (Exception e) {
	// Toast.makeText(act, "无法改变亮度", Toast.LENGTH_SHORT).show();
	// }
	// }
	// 设置屏幕亮度的函数
	private void setScreenBrightness(float num) {
		try {
			WindowManager.LayoutParams layoutParams = super.getWindow()
					.getAttributes();
			layoutParams.screenBrightness = num;// 设置屏幕的亮度
			getWindow().setAttributes(layoutParams);

		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(MainActivity.this,
					"Unable to change the brightness!", Toast.LENGTH_SHORT)
					.show();
		}

	}

	/**
	 * 功能说明： 日期: 2016-10-16 开发者: cyd
	 * 
	 */
	private void setCamerExpose(int value) {
		// TODO Auto-generated method stub
		parameters.setExposureCompensation(value / 10);
		mCamera.setParameters(parameters);
	}

	/**
	 * 功能说明： 日期: 2016-8-3 开发者: cyd
	 * 
	 */
	// private int getLightness() {
	// // TODO Auto-generated method stub
	// return Settings.System.getInt(getContentResolver(),
	// Settings.System.SCREEN_BRIGHTNESS,255);
	// }

	/**
	 * 功能说明： 日期: 2016-8-3 开发者: cyd
	 * 
	 */
	private void setCameraZoom(int value) {
		// TODO Auto-generated method stub
		try {
			// parameters = mCamera.getParameters();
			parameters.setZoom(value / 10);
			mCamera.setParameters(parameters);

		} catch (Exception e) {
			// TODO: handle exception
			// Log.e("TGA", value+"");
			Toast.makeText(MainActivity.this, "It can't be enlarged anymore!",
					Toast.LENGTH_SHORT).show();
		}
	}

	// 3D动画旋转效果
	// class Rotate3d extends Animation {
	// private float mCenterX = 0;
	// private float mCenterY = 0;
	//
	// public void setCenter(float centerX, float centerY) {
	// mCenterX = centerX;
	// mCenterY = centerY;
	// }
	//
	// @Override
	// protected void applyTransformation(float interpolatedTime, Transformation
	// t) {
	// Matrix matrix = t.getMatrix();
	// android.graphics.Camera camera = new android.graphics.Camera();
	// camera.save();
	// camera.rotateY(180 * interpolatedTime);
	// camera.getMatrix(matrix);
	// camera.restore();
	// matrix.preTranslate(-mCenterX, -mCenterY);
	// matrix.postTranslate(mCenterX, mCenterY);
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.invert:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(invert_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(invert_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.INVERT));
			invert_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.hue:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(hue_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(hue_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.HUE));
			hue_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.sepia:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(sepia_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(sepia_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.SEPIA));
			sepia_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.gray:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(gray_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(gray_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.GRAYSCALE));
			gray_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.shar:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(shar_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(shar_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.SHARPEN));
			shar_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.sobel:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(sobel_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(sobel_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.SOBEL_EDGE_DETECTION));
			sobel_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.convolution:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(convo_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(convo_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.THREE_X_THREE_CONVOLUTION));
			convo_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.emboss:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(emboss_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(emboss_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.EMBOSS));
			emboss_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.postersize:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(posterize_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(posterize_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.POSTERIZE));
			posterize_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.grouped:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(group_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(group_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.FILTER_GROUP));
			group_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.monochrome:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(mono_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(mono_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.MONOCHROME));
			mono_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;
		case R.id.normal:
			if (remember_bt.getTag() == null) {
				remember_bt.setTag(nor_bt);
			} else {
				((Button) remember_bt.getTag()).setTextColor(Color
						.parseColor("#ffffff"));
				remember_bt.setTag(nor_bt);
			}
			switchFilterTo(GPUImageTools.createFilterForType(this,
					FilterType.PIXELATION));
			nor_bt.setTextColor(Color.parseColor("#a3a3a3"));
			break;

		default:
			break;
		}
	}

	private void initAppsflyer() {
		AppsFlyerLib.getInstance().setImeiData(getDeviceImei());
		AppsFlyerLib.getInstance().setAndroidIdData(getAndroid_Id());
		AppsFlyerLib.getInstance().startTracking(this.getApplication(),
				"rPRvtwEJo6LhykDx4hHwPC");
		AppsFlyerLib.getInstance().setGCMProjectNumber("768125846820");
		AppsFlyerLib.getInstance().setGCMProjectID("smart-mirror-150310");
		AppsFlyerLib.getInstance().sendPushNotificationData(this);
		AppsFlyerLib.getInstance().sendDeepLinkData(this);
//		AppsFlyerLib.getInstance().setDeviceTrackingDisabled(true);
		AppsFlyerLib.getInstance().reportTrackSession(this);

	}
	/**
	 * 功能说明： 日期: 2016-1-20 开发者: cyd
	 * 
	 */
	public String getAndroid_Id() {
		// TODO Auto-generated method stub
		return Settings.Secure.getString(getContentResolver(),
				Settings.Secure.ANDROID_ID);

	}
	
	/**
	 * 功能说明：获取设备的Imei 日期: 2016-1-19 开发者: cyd
	 * 
	 */
	public String getDeviceImei() {
		// TODO Auto-generated method stub
		return ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}
}
