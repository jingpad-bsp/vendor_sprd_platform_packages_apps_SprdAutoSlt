package com.sprd.autoslt.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.KeyEvent;

import com.sprd.autoslt.util.TestResultUtil;
import com.sprd.autoslt.util.NavigationUtil;

public class TpPatternSquareActivity extends Activity {

    private static final String TAG = "TpFieldTest";

    private static int RECT_WIDTH = 70;
    private static int RECT_HEIGHT = 60;

    private static final int OFFSET = 3;

    private int mScreenWidth;
    private int mScreenHeight;
    private PointF mCentPoint;

    private List<RectInfo> mRectInfoList;

    private final Paint mGesturePaint = new Paint();
    private final Path mPath = new Path();

    private int mRectSize;
    private int mPassNum;
    private boolean mTouchDown;
    private boolean mIsRun;
    private int mNavigationBarHeight = 0;

    public static TpPatternSquareActivity instance;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (NavigationUtil.getNavigationBarShowState(this)) {
            Log.d(TAG, "TpPatternSquareActivity, hideNavigationBar...");
            NavigationUtil.hideNavigationBar(this);
            mNavigationBarHeight = NavigationUtil.getRealHeight(this) -
                    NavigationUtil.getHeight(this);
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Log.d(TAG, "getHeight()=:" + NavigationUtil.getHeight(this) +
                ",getRealHeight()=:" + NavigationUtil.getRealHeight(this));
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels + mNavigationBarHeight;
        Log.d(TAG, "mScreenWidth:" + mScreenWidth + "   mScreenHeight:" + mScreenHeight);
        if (mScreenWidth == 540) {
            RECT_WIDTH = 70;
        }
        if (mScreenWidth == 480) {
            RECT_WIDTH = 65;
            RECT_HEIGHT = 65;
        }
        if (mScreenWidth == 720) {
            RECT_WIDTH = 76;
            RECT_HEIGHT = 59;
        }

        mCentPoint = new PointF(mScreenWidth / 2, mScreenHeight / 2);
        Log.d(TAG, "mCentPoint:" + mCentPoint.toString());
        initView();
        setContentView(new MySurfaceView(this));
        instance = this;
        TestResultUtil.getInstance().reset();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "TpPatternSquareActivity, onKeyDown, keyCode=:" + keyCode);
        if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            mIsRun = false;
            finish();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        mIsRun = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        mIsRun = false;
        super.onDestroy();
    }
    private void initView() {
        mRectInfoList = new ArrayList<RectInfo>();
        int centLeft = (int) (mCentPoint.x - RECT_WIDTH / 2);
        int centTop = (int) (mCentPoint.y - RECT_HEIGHT / 2);
        int centRight = centLeft + RECT_WIDTH;
        int centBottom = centTop + RECT_HEIGHT;
        Rect centRect = new Rect(centLeft, centTop, centRight, centBottom);

        Paint centPaint = new Paint();
        centPaint.setColor(Color.GRAY);

        mRectInfoList.add(new RectInfo(centRect, false, centPaint));

        int leftNum = (mScreenWidth / 2 - RECT_WIDTH / 2) / RECT_WIDTH;
        int topNum = centTop / RECT_HEIGHT;

        Log.d(TAG, "leftNum:" + leftNum);
        Log.d(TAG, "topNum:" + topNum);

        drawLeft(centRect, leftNum);
        Rect rect = mRectInfoList.get(mRectInfoList.size() - 1).rect;
        drawTop(rect, topNum);
        drawRight(mRectInfoList.get(mRectInfoList.size() - 1).rect,
                leftNum * 2 - 1);
        drawBottom(rect, topNum);
        drawRight(mRectInfoList.get(mRectInfoList.size() - 1).rect,
                leftNum * 2 - 1);

        drawRight(centRect, leftNum);

        rect = mRectInfoList.get(mRectInfoList.size() - 1).rect;
        drawTop(rect, topNum);
        drawBottom(rect, topNum);

        drawTop(centRect, topNum - 1);
        drawBottom(centRect, topNum - 1);

        mRectSize = mRectInfoList.size();

    }

    private void drawLeft(Rect r, int num) {
        for (int i = 1; i <= num; i++) {

            int left = r.left - OFFSET * i - RECT_WIDTH * i;
            int top = r.top;
            int right = r.right - OFFSET * i - RECT_WIDTH * i;
            int bottom = r.bottom;
            Rect rect = new Rect(left, top, right, bottom);

            Paint paint = new Paint();
            paint.setColor(Color.GRAY);

            mRectInfoList.add(new RectInfo(rect, false, paint));
        }
    }

    private void drawRight(Rect r, int num) {
        for (int i = 1; i <= num; i++) {

            int left = r.left + OFFSET * i + RECT_WIDTH * i;
            int top = r.top;
            int right = r.right + OFFSET * i + RECT_WIDTH * i;
            int bottom = r.bottom;
            Rect rect = new Rect(left, top, right, bottom);

            Paint paint = new Paint();
            paint.setColor(Color.GRAY);

            mRectInfoList.add(new RectInfo(rect, false, paint));
        }
    }

    private void drawTop(Rect r, int num) {
        for (int i = 1; i <= num; i++) {
            int left = r.left;
            int top = r.top - OFFSET * i - RECT_HEIGHT * i;
            int right = r.right;
            int bottom = r.bottom - OFFSET * i - RECT_HEIGHT * i;
            Rect rect = new Rect(left, top, right, bottom);

            Paint paint = new Paint();
            paint.setColor(Color.GRAY);

            mRectInfoList.add(new RectInfo(rect, false, paint));
        }
    }

    private void drawBottom(Rect r, int num) {

        for (int i = 1; i <= num; i++) {
            int left = r.left;
            int top = r.top + OFFSET * i + RECT_HEIGHT * i;
            int right = r.right;
            int bottom = r.bottom + OFFSET * i + RECT_HEIGHT * i;
            Rect rect = new Rect(left, top, right, bottom);

            Paint paint = new Paint();
            paint.setColor(Color.GRAY);

            mRectInfoList.add(new RectInfo(rect, false, paint));
        }
    }

    static class RectInfo {

        Rect rect;
        boolean isTouched;
        Paint paint;

        public RectInfo(Rect r, boolean b, Paint p) {
            this.rect = r;
            this.isTouched = b;
            this.paint = p;
        }
    }

    class SurfaceViewRunnable implements Runnable {
        private SurfaceHolder holder;

        private Paint paint;

        public SurfaceViewRunnable(SurfaceHolder holder) {
            this.holder = holder;
            paint = new Paint();
            paint.setColor(Color.GRAY);
        }

        public void run() {
            Canvas canvas = null;
            while (mIsRun) {
                try {
                    canvas = holder.lockCanvas();
                    if (canvas == null) {
                        return;
                    }
                    canvas.drawColor(Color.WHITE);
                    drawView(canvas);

                    if (mTouchDown) {
                        canvas.drawPath(mPath, mGesturePaint);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "draw exception", e);
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }

        }
    }

    private void drawView(Canvas canvas) {
        for (int i = 0; i < mRectInfoList.size() && mIsRun; i++) {
            RectInfo ri = mRectInfoList.get(i);
            canvas.drawRect(ri.rect, ri.paint);
        }
    }

    private class MySurfaceView extends SurfaceView implements
            SurfaceHolder.Callback, OnTouchListener {
        private SurfaceHolder mHolder;
        private SurfaceViewRunnable mRunnable;

        private float mX;
        private float mY;

        public MySurfaceView(Context context) {
            super(context);
            mHolder = this.getHolder();
            mHolder.addCallback(this);

            mGesturePaint.setAntiAlias(true);
            mGesturePaint.setStyle(Style.STROKE);
            mGesturePaint.setStrokeWidth(5);
            mGesturePaint.setColor(Color.BLUE);

            this.setOnTouchListener(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mRunnable = new SurfaceViewRunnable(mHolder);
            mIsRun = true;
            new Thread(mRunnable).start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsRun = false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDown = true;
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                mTouchDown = false;
                Log.d(TAG, "mPassNum:" + mPassNum + "  mRectSize:" + mRectSize);
                if (mPassNum == mRectSize) {
                    TestResultUtil.getInstance().setCurrentStepStatus("pass");
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "mPassNum:" + mPassNum + "  mRectSize:" + mRectSize);
                if (mPassNum == mRectSize) {
                    TestResultUtil.getInstance().setCurrentStepStatus("pass");
                }
                break;
            }

            return true;
        }

        private void touchDown(MotionEvent event) {

            mPath.reset();
            float x = event.getX();
            float y = event.getY();

            mX = x;
            mY = y;

            mPath.moveTo(x, y);
        }

        private void touchMove(MotionEvent event) {
            final float x = event.getX();
            final float y = event.getY();

            final float previousX = mX;
            final float previousY = mY;

            final float dx = Math.abs(x - previousX);
            final float dy = Math.abs(y - previousY);

            if (dx >= 10 || dy >= 10) {

                float cX = (x + previousX) / 2;
                float cY = (y + previousY) / 2;

                mPath.quadTo(previousX, previousY, cX, cY);

                for (RectInfo ri : mRectInfoList) {
                    if (ri.rect.contains((int) (x), (int) (y)) && !ri.isTouched) {
                        ri.paint.setColor(Color.GREEN);
                        ri.isTouched = true;
                        mPassNum++;
                    }
                }

                mX = x;
                mY = y;
            }
        }
    }
}
