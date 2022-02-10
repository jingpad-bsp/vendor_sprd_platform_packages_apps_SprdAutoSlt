package com.sprd.autoslt.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class LoadView extends View {
    private static final String TAG = "LoadView";
    private static final boolean DEBUG = true;
    String mLoadText = null;
    int mLoadWidth = 0;

    private Paint mLoadPaint;
    private Paint mShadowPaint;
    private Paint mShadow2Paint;
    private float mAscent;
    private int mFH;
    private int mNeededWidth = 0;
    private int mNeededHeight = 0;
    private static final int TEXT_SIZE = 50;

    public LoadView(Context c) {
        super(c);
        Log.d(TAG, " loadView ! + ");

        mLoadText = "pass";
        // setPadding(4, 4, 4, 4);

        // Need to scale text size by density... but we won't do it
        // linearly, because with higher dps it is nice to squeeze the
        // text a bit to fit more of it. And with lower dps, trying to
        // go much smaller will result in unreadable text.
        int textSize = TEXT_SIZE;

        float density = c.getResources().getDisplayMetrics().density;
        if (density < 1) {
            textSize = TEXT_SIZE;
        } else {
            textSize = (int) (TEXT_SIZE * density);
            if (textSize < TEXT_SIZE) {
                textSize = TEXT_SIZE;
            }
        }

        setPadding(4, (int) (30 * density), 4, 4);

        mLoadPaint = new Paint();
        mLoadPaint.setAntiAlias(true);
        mLoadPaint.setTextSize(textSize);
        mLoadPaint.setARGB(255, 255, 0, 0);

        mShadowPaint = new Paint();
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setTextSize(textSize);
        mShadowPaint.setARGB(192, 0, 0, 0);
        mLoadPaint.setShadowLayer(4, 0, 0, 0xff000000);

        mShadow2Paint = new Paint();
        mShadow2Paint.setAntiAlias(true);
        mShadow2Paint.setTextSize(textSize);
        mShadow2Paint.setARGB(192, 0, 0, 0);
        mLoadPaint.setShadowLayer(2, 0, 0, 0xff000000);

        mAscent = mLoadPaint.ascent();
        float descent = mLoadPaint.descent();
        mFH = (int) (descent - mAscent + .5f);

        boolean result = isCalibration();
        if(result) {
            mLoadText = "pass";
            mLoadPaint.setARGB(255, 0, 255, 0);
        } else {
            if(isUntest()) {
                mLoadText = "";
            } else {
                mLoadText = "fail";
            }
            mLoadPaint.setARGB(255, 255, 0, 0);
        }
        mLoadWidth = (int) mLoadPaint.measureText(mLoadText);
        updateDisplay();
        if (DEBUG)
            Log.d(TAG, " loadView ! - ");
    }

    private boolean isCalibration() {
        /*String result = mEngSqlite.queryHistoryData();
        if("null".contains(result) || TextUtils.isEmpty(result)) return false;
        if(result.toLowerCase().contains("pass")) return true;*/
        return false;
    }

    private boolean isUntest() {
       /* String result = mEngSqlite.queryHistoryData();
        if("null".contains(result) || TextUtils.isEmpty(result)) return true;*/
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSize(mNeededWidth, widthMeasureSpec),
                resolveSize(mNeededHeight, heightMeasureSpec));
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (DEBUG)
            Log.d(TAG, " onDraw ! + ");
        super.onDraw(canvas);
        final int RIGHT = getWidth() - 1;
        int paddingRight = getPaddingRight();
        int y = getPaddingTop() - (int) mAscent;

        canvas.drawText(mLoadText, RIGHT - paddingRight - mLoadWidth - 1,
                y - 1, mShadowPaint);
        canvas.drawText(mLoadText, RIGHT - paddingRight - mLoadWidth - 1,
                y + 1, mShadowPaint);
        canvas.drawText(mLoadText, RIGHT - paddingRight - mLoadWidth + 1,
                y - 1, mShadow2Paint);
        canvas.drawText(mLoadText, RIGHT - paddingRight - mLoadWidth + 1,
                y + 1, mShadow2Paint);

        canvas.drawText(mLoadText, RIGHT - paddingRight - mLoadWidth, y,
                mLoadPaint);
        if (DEBUG)
            Log.d(TAG, " onDraw ! - ");
    }

    void updateDisplay() {
        if (DEBUG)
            Log.d(TAG, " updateDisplay! + ");
        int maxWidth = mLoadWidth;

        int neededWidth = getPaddingLeft() + getPaddingRight() + maxWidth;
        int neededHeight = getPaddingTop() + getPaddingBottom() + mFH;

        if (neededWidth != mNeededWidth || neededHeight != mNeededHeight) {
            mNeededWidth = neededWidth;
            mNeededHeight = neededHeight;
            Log.d(TAG, " requestLayout ! + ");
            requestLayout();
        } else {
            invalidate();
        }
        if (DEBUG)
            Log.d(TAG, " updateDisplay ! - ");
    }
}
