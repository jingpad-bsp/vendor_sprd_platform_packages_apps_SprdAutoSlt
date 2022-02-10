
package com.sprd.autoslt.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;

import com.sprd.autoslt.util.NavigationUtil;
import com.sprd.autoslt.common.SLTConstant;
import com.sprd.autoslt.R;

public class LCDModeTestActivity extends Activity {
    private static final String TAG = "LCDModeTestActivity";
    public static LCDModeTestActivity instance;
    private String mFileName;
    private String mBrightness;
    private ImageView mContent;
    public static boolean isActivityInFront;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.lcd_mode);
        mContent = (ImageView) findViewById(R.id.imageview_lcd);
        String param = getIntent().getStringExtra("param");
        Bitmap bit = null;
        if(!TextUtils.isEmpty(param)) {
            if(param.contains("^")) {
                String[] keyValue = param.split("\\^");
                mFileName = keyValue[0];
                mBrightness = keyValue[1];
            } else {
                mFileName = param;
            }
            Log.d(TAG, "mFileName:" + mFileName);
            if (mFileName.contains("/")) {
                File image = new File(mFileName);
                bit = getBitmap(image.getAbsolutePath());
                if(image.exists()) {
                    mContent.setImageBitmap(bit);
                } else {
                    Toast.makeText(this, "Image File " + mFileName + " not exist!", Toast.LENGTH_SHORT).show();
                }
            }else {
                 Uri uri = getFileUri(mFileName);
                 Log.d(TAG, "uri = " +uri);
                 InputStream in = null;
                 if (uri != null) {
                     try {
                          in = getContentResolver().openInputStream(uri);
                          bit = BitmapFactory.decodeStream(in);
                     } catch (FileNotFoundException e1) {
                          // TODO Auto-generated catch block
                          e1.printStackTrace();
                     }finally{
                         try {
                             if (in != null) {
                                 in.close();
                             }
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                   mContent.setImageBitmap(bit);
                 }else {
                    Toast.makeText(this, "Image File not exist!", Toast.LENGTH_SHORT).show();
                 }
            }
            try {
                int brightness = Integer.parseInt(mBrightness);
                setWindowBrightness(brightness);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Brightness " + mBrightness + " Invalid!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        isActivityInFront = true;
        super.onResume();
        if (NavigationUtil.getNavigationBarShowState(this)) {
            Log.d(TAG, "LCDModeTestActivity, hideNavigationBar...");
            NavigationUtil.hideNavigationBar(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "LCDModeTestActivity, onKeyDown, keyCode=:" + keyCode);
        if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            isActivityInFront = false;
            finish();
        }
        return true;
    }

    @Override
    protected void onPause() {
        isActivityInFront = false;
        super.onPause();
    }

    private Bitmap getBitmap(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // ��ʼ���Ȱ�newOpts.inJustDecodeBounds ���true��
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts); // ��ʱ����bitmapΪnull

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // ��800*480�ֱ���Ϊ��
        float hh = 800f; // �������ø߶�Ϊ800f
        float ww = 480f; // �������ÿ��Ϊ480f
        // ���űȡ������ǹ̶��������ţ�ֻ�ø߻��߿�����һ����ݽ��м��㼴��
        int scale = 1; // be=1��ʾ������
        if (w > h && w > ww) { // ����ȴ�Ļ���ݿ�ȹ̶���С����
            scale = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) { // ���߶ȸߵĻ���ݿ�ȹ̶���С����
            scale = (int) (newOpts.outHeight / hh);
        }
        if (scale <= 0)
            scale = 1;
        newOpts.inSampleSize = scale; // �������ű��� //
                                      // ���¶���ͼƬ��ע���ʱ�Ѿ���options.inJustDecodeBounds
                                      // ���false��
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return bitmap;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String param = intent.getStringExtra("param");
        Bitmap bit1= null;
        if(!TextUtils.isEmpty(param)) {
            if(param.contains("^")) {
                String[] keyValue = param.split("\\^");
                mFileName = keyValue[0];
                mBrightness = keyValue[1];
            } else {
                mFileName = param;
            }
            Log.d(TAG, "onNewIntent ----> mFileName:" + mFileName);
            if (mFileName.contains("/")) {
                File image = new File(mFileName);
                bit1 = getBitmap(image.getAbsolutePath());
                if(image.exists()) {
                    mContent.setImageBitmap(bit1);
                } else {
                    Toast.makeText(this, "Image File " + mFileName + " not exist!", Toast.LENGTH_SHORT).show();
                }
            }else {
                Uri fileUri = getFileUri(mFileName);
                Log.d(TAG, "onNewIntent ---> uri = " +fileUri);
                InputStream in = null;
                if (fileUri != null) {
                    try {
                        in = getContentResolver().openInputStream(fileUri);
                        bit1 = BitmapFactory.decodeStream(in);
                    } catch (FileNotFoundException e1) {
                      // TODO Auto-generated catch block
                         e1.printStackTrace();
                    }finally{
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mContent.setImageBitmap(bit1);
                  }else {
                      Toast.makeText(this, "Image File not exist!", Toast.LENGTH_SHORT).show();
                   }
            }
          //  File image = new File(SLTConstant.SLT_SDCARD_PATH, mFileName);
            /*File image = new File(mFileName);
            Bitmap bit = BitmapFactory.decodeFile(image.getAbsolutePath());
            if(image.exists()) {
                mContent.setImageBitmap(bit);
            } else {
                Toast.makeText(this, "Image File " + mFileName + " not exist!", Toast.LENGTH_SHORT).show();
            }*/
            try {
                int brightness = Integer.parseInt(mBrightness);
                setWindowBrightness(brightness);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Brightness " + mBrightness + " Invalid!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setWindowBrightness(int brightness) {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness / 255.0f;
        window.setAttributes(lp);
    }

    private Uri getFileUri(String filename){
        Uri uri = null;
        if (filename.equalsIgnoreCase("b255.jpg")) {
            uri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.b255);
        }else if (filename.equalsIgnoreCase("g255.jpg")) {
            uri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.g255);
        }else if (filename.equalsIgnoreCase("r255.jpg")) {
            uri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.r255);
        }else if (filename.equalsIgnoreCase("l0.jpg")) {
            uri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.l0);
        }else if (filename.equalsIgnoreCase("l127.jpg")) {
            uri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.l127);
        }else if (filename.equalsIgnoreCase("l255.jpg")) {
            uri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.l255);
        }
      return uri;
    }
}
