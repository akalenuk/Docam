package p.namespace;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.content.DialogInterface;
import p.namespace.CameraPreview;
import p.namespace.DocamView;
import p.namespace.DocamView.Mode;
import p.namespace.R.id;

public class DocamActivity extends Activity {
    private Camera mCamera;		// not sure, if I want them here
    private CameraPreview mPreview;
    private DocamView mDrawView;
    private Bitmap mBitmap;
    private int mProcessProgress = 0;	// ? refactor
    private Random mRandom = new Random();
    
    public static final int MEDIA_TYPE_IMAGE = 1; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(id.camera_preview);
        preview.addView(mPreview);

        mDrawView = new DocamView(this);
        mDrawView.setBackgroundColor(Color.TRANSPARENT);
        preview.addView(mDrawView);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_MENU)
        {
        	if(mDrawView.mMode == Mode.SHOT){
	        	Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
	        	    public void onShutter() {
	        	    	return;
	        	    }
	        	};
	            mCamera.takePicture(shutterCallback, null, mPicture);
	            return true;
        	}else if(mDrawView.mMode == Mode.TOUCH){
        		mDrawView.TurnOnProcessMode();
        		new Thread(new Runnable() {
        		    public void run() {
        		      ProcessAndSave();
        		    }
        		  }).start();

        		final Handler handler = new Handler();
        		Runnable invalidation = new Runnable(){
        			public void run() {
        				if(mProcessProgress==102){
        					System.exit(0);
        				}else{
        					mDrawView.mProcessProgress = mProcessProgress;
        					mDrawView.invalidate();
        					handler.postDelayed(this, 100);
        				}
        			}
        		};
        		invalidation.run();
        	}
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK){
        	System.exit(0); 
        }
        return false;
    }
   
    public int D(Point a, Point b){
    	return (int)Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y)); 
    }
    
    private double jitter(){
    	return mRandom.nextDouble() * 0.001;
    }
    
    @SuppressLint("SimpleDateFormat")	// this might be revised
	public void ProcessAndSave(){
    	int new_w = (D(mDrawView.mX1, mDrawView.mX2) + D(mDrawView.mX3, mDrawView.mX4))/2;    	
    	int new_h = (D(mDrawView.mX1, mDrawView.mX4) + D(mDrawView.mX2, mDrawView.mX3))/2; 
    	new_w = new_w * mBitmap.getWidth() / mDrawView.getWidth();
    	new_h = new_h * mBitmap.getHeight() / mDrawView.getHeight();
    	Bitmap out;

    	double x1 = mDrawView.mX1.x;
    	double x2 = mDrawView.mX2.x;
    	double x3 = mDrawView.mX3.x;
    	double x4 = mDrawView.mX4.x;
    	
    	double y1 = mDrawView.mX1.y;
    	double y2 = mDrawView.mX2.y;
    	double y3 = mDrawView.mX3.y;
    	double y4 = mDrawView.mX4.y;
    	
    	if(	x1==0 && y1==0 
  		&& x2==mDrawView.getWidth()-1 && y2==0 
   		&& x3==mDrawView.getWidth()-1 && y3==mDrawView.getHeight()-1
   		&& x4==0 && y4==mDrawView.getHeight()-1){	// bypass transformation
    		out = mBitmap;	
    	}else{
    		// jitter a bit to mitigate 0 == determintant
    		x1 += jitter();
        	x2 -= jitter();
        	x3 -= jitter();
        	x4 += jitter();
        	
        	y1 += jitter();
        	y2 += jitter();
        	y3 -= jitter();
        	y4 -= jitter();
        	
        	// magic
	    	double s1 = y1*x2-x1*y2;
	    	double s2 = y1*x4-x1*y4;
	    	double s3 = x3*x2*x1+x3*x4*x1-x2*x4*x1-x3*x2*x4;
	    	double s4 = y3*y2*y1+y3*y4*y1-y2*y4*y1-y3*y2*y4;
	    	double a3 = x2*x4-x3*x4;
	    	double b3 = x2*x4-x3*x2;
	    	double d4 = y2*y4-y3*y4;
	    	double e4 = y2*y4-y3*y2;
    	
	    	double dB = ((y2*b3)*(x4*d4)-(x2*a3)*(y4*e4));
	    	double B =(((x4*d4)*(y2*s3-s1*a3)-(x2*a3)*(s2*e4+s4*x4)))/  dB;
	    	
	    	double dD = ((x2*a3)*(y4*e4)-(y2*b3)*(x4*d4));
			double D =(((y4*e4)*(y2*s3-s1*a3)-(y2*b3)*(s2*e4+s4*x4)))/ dD;
	    	
	    	double A = (s3-b3*B) / a3;
	    	double E = (s4-d4*D) / e4;
	    	
	    	double a = (A+x1-x2) / x2;
	    	double b = (B+x1-x4) / x4;
	    	
	    	double C = x1;
	    	double F = y1;
	    	double c = 1.0;
	    	
	    	double dx = (double)mBitmap.getWidth() / (double)mDrawView.getWidth();
	    	double dy = (double)mBitmap.getHeight() / (double)mDrawView.getHeight();
	    	
	    	int[] new_colors = new int[new_w*new_h];
	    	
	    	for(int i = 0; i<new_h; i++){
	    		double y = i / (double)(new_h);
	    		mProcessProgress = (int)(100*y);
	    		for(int j = 0; j<new_w; j++){
	    			double x = j / (double)(new_w);
	    			
	    			double d = 1.0 / (a*x+b*y+c);
	    			double xn = (A*x + B*y + C) * d;
	    			double yn = (D*x + E*y + F) * d;
	    			double rx = xn * dx;
	    			double ry = yn * dy;
	    			int co = 0;
	    			// catch IllegalArgumentException omitted because of algorithm invariant 	
    				co = mBitmap.getPixel((int)rx, (int)ry);
	    			new_colors[i*new_w+j] = co;  			
	    		}
	    	}
	    	out = Bitmap.createBitmap( new_colors, 0, new_w, new_w, new_h, Bitmap.Config.ARGB_8888);
    	}
    	
    	mProcessProgress = 101;	// ready for saving
    	
    	SimpleDateFormat format = new SimpleDateFormat("yyMMdd_HHmmss");
    	String fileName = format.format( new Date() );
   	
    	File docamDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/Docam/");    	
    	docamDirectory.mkdirs();  	

        File pictureFile = new File(docamDirectory, fileName+".jpg");
        try {
			pictureFile.createNewFile();
		} catch (IOException e1) {
			Alert("Error creating the file, check storage permissions. " + e1.getMessage());
        	return;
		}

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            out.compress(CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
        	Alert("File not found.");
        } catch (IOException e) {
        	Alert("Error accessing file.");
        }
        mProcessProgress = 102;	// saved and ready to quit
    }
    
    public static void Alert(String msg){	// this is for debug purpose mostly, but not entirely. Let it be for now
	    AlertDialog ad = new AlertDialog.Builder(null).create();  
	    ad.setCancelable(false);  
	    ad.setMessage(msg);  
	    ad.setButton("OK", new DialogInterface.OnClickListener() {  
	         public void onClick(DialogInterface dialog, int which) {  
	             dialog.dismiss();                      
	         }  
	    });  
	    ad.show(); 
    }
    
    private PictureCallback mPicture = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            mBitmap = BitmapFactory.decodeByteArray(data, 0 , data.length);
            mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);          
            mDrawView.TurnOnTouchMode(mDrawView.getWidth(), mDrawView.getHeight());
        }
    };

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){      
        	Alert("Camera screwed :-(");
        }
        return c;
    }   
}