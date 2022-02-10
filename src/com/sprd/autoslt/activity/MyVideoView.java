package com.sprd.autoslt.activity;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

public class MyVideoView extends VideoView {
	private static final String TAG = "VideoTestActivity";

	private int videoRealW = 1;
	private int videoRealH = 1;
	private int mWIDTH = 0;
	private int mHEIGHT = 0;
	private int mVideoWidth = 0;
	private int mVideoHeight = 0;

	public MyVideoView(Context context) {
		super(context);
	}

	public MyVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setWIDTH(int mWIDTH) {
		this.mWIDTH = mWIDTH;
	}

	public void setHEIGHT(int mHEIGHT) {
		this.mHEIGHT = mHEIGHT;
	}

	@Override
	public void setVideoURI(Uri uri) {
		// TODO Auto-generated method stub
		super.setVideoURI(uri);
		MediaMetadataRetriever retr = new MediaMetadataRetriever();
		retr.setDataSource(uri.getPath());
		String height = retr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
		String width = retr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
		try {
			videoRealH = Integer.parseInt(height);
			videoRealW = Integer.parseInt(width);
			Log.d(TAG, "videoRealH = " + width + "; videoRealW = " + height);
		} catch (NumberFormatException e) {
			Log.e("----->" + "VideoView", "setVideoPath:" + e.toString());
		}
		retr.release();
	}

	// this interface is critical.
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(mWIDTH, widthMeasureSpec);
		int height = getDefaultSize(mHEIGHT, heightMeasureSpec);
		// setMeasuredDimension(width, height);
		Log.d(TAG, " width = " + width + "; height = " + height);
		if (height > width) {
			if (videoRealH > videoRealW) {

				mVideoHeight = height;
				mVideoWidth = width;
			} else {
				mVideoWidth = width;
				float r = videoRealH / (float) videoRealW;
				mVideoHeight = (int) (mVideoWidth * r);
			}
		} else {
			if (videoRealH > videoRealW) {
				mVideoHeight = height;
				float r = videoRealW / (float) videoRealH;
				mVideoWidth = (int) (mVideoHeight * r);
			} else {
				mVideoHeight = height;
				mVideoWidth = width;
			}
		}
		if (videoRealH == videoRealW && videoRealH == 1) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		} else {
			setMeasuredDimension(mVideoWidth, mVideoHeight);
		}
	}
}


