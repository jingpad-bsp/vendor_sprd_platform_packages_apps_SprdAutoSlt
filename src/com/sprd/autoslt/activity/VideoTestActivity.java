package com.sprd.autoslt.activity;

import com.sprd.autoslt.R;
import com.sprd.autoslt.SltBaseActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoTestActivity extends SltBaseActivity {

    private static final String TAG = "VideoTestActivity";
    private MediaController mMediaController = null;
    private MyVideoView mVideoView = null;
    private AudioManager mAm;
    private int mWIDTH = 0;
    private int mHEIGHT = 0;
    private WindowManager mWindowManager;
    private int playCount = 0;
    private static final int PLAY_COUNT = 500;
    private Uri mVideoUri;
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    
    public static VideoTestActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        mWindowManager = getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mHEIGHT = dm.widthPixels;
        mWIDTH = dm.heightPixels;
        //mVideoView = new MyVideoView(this);
        mMediaController = new MediaController(this);
        mAm = (AudioManager) getSystemService("audio");
        setContentView(/*mVideoView*/R.layout.video_play_view);
        mVideoView = (MyVideoView)findViewById(R.id.vv);
        playCount = 0;
        mVideoUri = getIntent().getData();
        Log.d("huasong", "mVideoUri:" + mVideoUri);
        Log.d(TAG, "mHEIGHT =" + mHEIGHT +"; mWIDTH = "+ mWIDTH);
        startVideoTest();
        instance = this;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        if(intent.getBooleanExtra("stop", false)) {
            //stopVideoTest();
        }
    }

    private void stopVideoTest() {
        if(mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
        this.finish();
    }

    private void startVideoTest() {
        Log.d(TAG, "startVideoTest");
        if (mVideoView != null) {
            mVideoView.setVideoURI(mVideoUri);

            mVideoView.setMediaController(mMediaController);
            mVideoView.requestFocus();
            mVideoView.setOnErrorListener(mErrorListner);

            mVideoView.setOnCompletionListener(mCompleteListner);

            // set volume to MAX value.
            mAm.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    AudioManager.FLAG_PLAY_SOUND);

            mVideoView.start();
            Log.d(TAG, "mVideoView start play");
        }
    }

    // mediaplayer error listener
    private OnErrorListener mErrorListner = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(TAG, "Error occurred while playing video:what:" + what);
            // mVideoView.stopPlayback();
            Dialog dialog = new AlertDialog.Builder(VideoTestActivity.this)
                    .setTitle(
                            "play video error at " + (playCount + 1)
                                    + " times.")
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                    VideoTestActivity.this.finish();
                                }
                            }).create();
            dialog.show();
            return true;
        }
    };

    // mediaplayer comple listener
    private OnCompletionListener mCompleteListner = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            Toast.makeText(VideoTestActivity.this, "Video test Success!", Toast.LENGTH_SHORT).show();
            VideoTestActivity.this.finish();
        }
    };

    // videoview used to display the video
   /* public class MyVideoView extends VideoView {
        private int videoRealW=1;
        private int videoRealH=1;
        public MyVideoView(Context context) {
            super(context);
        }

        public MyVideoView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MyVideoView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

		@Override
		public void setVideoURI(Uri uri) {
			// TODO Auto-generated method stub
			super.setVideoURI(uri);
			MediaMetadataRetriever retr = new MediaMetadataRetriever();
	        retr.setDataSource(uri.getPath());
	        String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
	        String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
	        try {
	            videoRealH=Integer.parseInt(height);
	            videoRealW=Integer.parseInt(width);
				Log.d(TAG, "videoRealH = "+ width +"; videoRealW = "+height); 
	        } catch (NumberFormatException e) {
	            Log.e("----->" + "VideoView", "setVideoPath:" + e.toString());
	        }
		}
        // this interface is critical.
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = getDefaultSize(mWIDTH, widthMeasureSpec);
            int height = getDefaultSize(mHEIGHT, heightMeasureSpec);
            //setMeasuredDimension(width, height);
			Log.d(TAG, " width = "+ width +"; height = "+height);
			if(height>width){
                if(videoRealH>videoRealW){
                    mVideoHeight=height;
                    mVideoWidth=width;
                }else {
                    mVideoWidth=width;
                    float r=videoRealH/(float)videoRealW;
                    mVideoHeight= (int) (mVideoWidth*r);
                }
            }else {
                if(videoRealH>videoRealW){
                    mVideoHeight=height;
                    float r=videoRealW/(float)videoRealH;
                    mVideoWidth= (int) (mVideoHeight*r);
                }else {
                    mVideoHeight=height;
                    mVideoWidth=width;
                }
            }
            if(videoRealH==videoRealW&&videoRealH==1){                
                super.onMeasure(widthMeasureSpec,heightMeasureSpec);
            }else {
                setMeasuredDimension(mVideoWidth, mVideoHeight);
            }
        }
    }*/
}
