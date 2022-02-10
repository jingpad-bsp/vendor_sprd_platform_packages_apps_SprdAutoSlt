package com.sprd.autoslt.activity;

import android.R.integer;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.autoslt.R;
import com.sprd.autoslt.util.TestResultUtil;

public class MutiTouchTestActivity extends Activity {
    private static final String TAG = "MutiTouchTestActivity";
    private MuiltImageView mImgView;
    private TextView mTextView;
    private DisplayMetrics mDisplayMetrics;
    private MainHandler mHandler;
    private Context mContext;
    public static MutiTouchTestActivity instance;
    private int distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mDisplayMetrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mDisplayMetrics);
        mHandler = new MainHandler();
        setContentView(createView());
        super.onCreate(savedInstanceState);
        TestResultUtil.getInstance().reset();
        instance = this;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

     private View createView(){
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
            LinearLayout view = new LinearLayout(this);
            view.setLayoutParams(lp);
            view.setOrientation(LinearLayout.VERTICAL);
            ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mTextView = new TextView(this);
            Intent intent = getIntent();
            Bundle bd = intent.getBundleExtra("bundle");
            distance = Integer.parseInt(bd.getString("distance"));
            Log.d(TAG, "distance = " +distance);
            mImgView = new MuiltImageView(this, mDisplayMetrics.widthPixels,
                    mDisplayMetrics.heightPixels, mHandler,distance);
            mTextView.setLayoutParams(vlp);
            mImgView.setLayoutParams(vlp);
            mTextView.setText(getString(R.string.muti_touchpoint_info));
            view.addView(mTextView);
            view.addView(mImgView);
            return view;
        }
    private static class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                TestResultUtil.getInstance().setCurrentStepStatus("pass");
            }else {
                TestResultUtil.getInstance().setCurrentStepStatus("fail");
            }
        }
    }

    private static class MuiltImageView extends View {
        private static final float RADIUS = 75f;
        private PointF pointf = new PointF();
        private PointF points = new PointF();
        private Handler mHandler;
        private boolean mPass = false;
        private int mWidth, mHeight,mDis;

        public MuiltImageView(Context context, int width, int height,
                Handler handler, int dis) {
            super(context);
            mWidth = width;
            mHeight = height;
            mHandler = handler;
            mDis = dis;
            initData();
        }

        private void initData() {
            int x1,x2;
            if (mWidth > mDis+RADIUS*2) {
                x1= (int)RADIUS;
                x2=mDis+x1;
                pointf.set(x2, mHeight/2);
                points.set(x1,mHeight/2 );
            }else {
                pointf.set(mWidth - RADIUS, mHeight/2);
                points.set(RADIUS,mHeight/2 /*mHeight - RADIUS - 150*/);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.YELLOW);
            if (mPass == false){
                canvas.drawCircle(pointf.x, pointf.y, RADIUS, paint);
                canvas.drawCircle(points.x, points.y, RADIUS, paint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getPointerCount() == 2) {
                pointf.set(event.getX(0), event.getY(0));
                points.set(event.getX(1), event.getY(1));
                double distance = Math.sqrt((pointf.x - points.x)
                        * (pointf.x - points.x) + (pointf.y - points.y)
                        * (pointf.y - points.y));
                /*if (distance < mWidth / 3 || distance > mWidth / 3 * 2) {
                    mPass = true;
                }*/
                if (distance < (mDis + RADIUS*2) || distance > RADIUS) {
                    mPass = true;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP && mPass) {
                mHandler.sendEmptyMessage(1);
            }else {
                mHandler.sendEmptyMessage(2);
            }
            invalidate();
            return true;
        }
    }
}
