package p.namespace;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;



public class DocamView extends View {
    Paint paint1 = new Paint();
    Paint paint2 = new Paint();
    Paint paint3 = new Paint();
    
    public enum Mode { SHOT, TOUCH, PROCESS };
    public Mode mMode = Mode.SHOT;
    public Point mX1 = new Point();
    public Point mX2 = new Point();
    public Point mX3 = new Point();
    public Point mX4 = new Point();
    public int mW;
    public int mH;
     
    public int mProcessProgress;
    private int mBlink = 0;

    public DocamView(Context context) {
        super(context);
        paint1.setColor(Color.parseColor("#ded3a2"));
        paint2.setColor(Color.parseColor("#332b07"));
        paint2.setStyle(Style.STROKE);
        paint2.setPathEffect(new DashPathEffect(new float[] {5,10}, 0));
        paint3.setColor(Color.parseColor("#332b07"));
        paint3.setStyle(Style.STROKE);
        paint3.setPathEffect(new DashPathEffect(new float[] {5,10}, 0));
    }
    
    public void TurnOnTouchMode(int w, int h){
    	mW = w;
    	mH = h;
    	mX1 = new Point(0, 0);
    	mX2 = new Point(w-1, 0);
    	mX3 = new Point(w-1, h-1);
    	mX4 = new Point(0, h-1);
    	mMode = Mode.TOUCH;
    	invalidate();
    }
    
    public void TurnOnProcessMode(){    	
    	mMode = Mode.PROCESS;
    }
    
    public double D(Point a, Point b){
    	return Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y)); 
    }
    
    @SuppressLint("DrawAllocation")	// consider revising
	@Override
    public void onDraw(Canvas canvas) {
    	if (mMode == Mode.TOUCH){
    		canvas.drawLine(mX1.x, mX1.y, mX2.x, mX2.y, paint1);
    		canvas.drawLine(mX2.x, mX2.y, mX3.x, mX3.y, paint1);
    		canvas.drawLine(mX3.x, mX3.y, mX4.x, mX4.y, paint1);
    		canvas.drawLine(mX4.x, mX4.y, mX1.x, mX1.y, paint1);
    		
    		canvas.drawLine(mX1.x, mX1.y, mX2.x, mX2.y, paint2);
    		canvas.drawLine(mX2.x, mX2.y, mX3.x, mX3.y, paint2);
    		canvas.drawLine(mX3.x, mX3.y, mX4.x, mX4.y, paint2);
    		canvas.drawLine(mX4.x, mX4.y, mX1.x, mX1.y, paint2);
    	}else if (mMode == Mode.PROCESS){
    		paint3.setPathEffect(new DashPathEffect(new float[] {5,10}, -mBlink));
    		canvas.drawLine(mX1.x, mX1.y, mX2.x, mX2.y, paint1);
    		canvas.drawLine(mX2.x, mX2.y, mX3.x, mX3.y, paint1);
    		canvas.drawLine(mX3.x, mX3.y, mX4.x, mX4.y, paint1);
    		canvas.drawLine(mX4.x, mX4.y, mX1.x, mX1.y, paint1);
    		
    		canvas.drawLine(mX1.x, mX1.y, mX2.x, mX2.y, paint2);
    		canvas.drawLine(mX2.x, mX2.y, mX3.x, mX3.y, paint2);
    		canvas.drawLine(mX3.x, mX3.y, mX4.x, mX4.y, paint2);
    		canvas.drawLine(mX4.x, mX4.y, mX1.x, mX1.y, paint2);
    		
    		double f = (double)mProcessProgress / 101;
    		double t = 1-f;
    		int X1 = (int)(mX1.x*t + mX2.x*f);
    		int Y1 = (int)(mX1.y*t + mX2.y*f);
    		int X2 = (int)(mX4.x*t + mX3.x*f);
    		int Y2 = (int)(mX4.y*t + mX3.y*f);
    		canvas.drawLine(X1, Y1, X2, Y2, paint1);
   			canvas.drawLine(X1, Y1, X2, Y2, paint3);
			mBlink ++;
    	}
    }

    private void resetXs(float x, float y){
    	if (mMode == Mode.TOUCH){
    		double d1 = D(mX1, new Point((int)x, (int)y));
    		double d2 = D(mX2, new Point((int)x, (int)y));
    		double d3 = D(mX3, new Point((int)x, (int)y));
    		double d4 = D(mX4, new Point((int)x, (int)y));
    		if(d1 <= d2 && d1 <= d3 && d1 <= d4){
    			mX1 = new Point((int)x, (int)y);
    		}else if(d2 <= d1 && d2 <= d3 && d2 <= d4){
    			mX2 = new Point((int)x, (int)y);
    		}else if(d3 <= d1 && d3 <= d2 && d3 <= d4){
    			mX3 = new Point((int)x, (int)y);
    		}else{
    			mX4 = new Point((int)x, (int)y);
    		}
    		invalidate();		
    	}
    }
    
    public boolean onTouchEvent(MotionEvent event) {
    	float x = event.getX();
    	float y = event.getY();
    	resetXs(x, y);
    	return true;
    }
}
