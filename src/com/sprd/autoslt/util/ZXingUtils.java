
package com.sprd.autoslt.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

/**
 * Éú³ÉÌõÐÎÂëºÍ¶þÎ¬ÂëµÄ¹¤¾ß
 */
public class ZXingUtils {
    /**
     * Éú³É¶þÎ¬Âë Òª×ª»»µÄµØÖ·»ò×Ö·û´®,¿ÉÒÔÊÇÖÐÎÄ
     * 
     * @param url
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createQRImage(String url, final int width, final int height) {
        try {
            // ???URL?????
        	Log.d("createQRImage", "url = "+ url);
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width,
                    height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Éú³ÉÌõÐÎÂë
     * 
     * @param context
     * @param contents ÐèÒªÉú³ÉµÄÄÚÈÝ
     * @param desiredWidth Éú³ÉÌõÐÎÂëµÄ¿í´ø
     * @param desiredHeight Éú³ÉÌõÐÎÂëµÄ¸ß¶È
     * @param displayCode ÊÇ·ñÔÚÌõÐÎÂëÏÂ·½ÏÔÊ¾ÄÚÈÝ
     * @return
     */
    public static Bitmap creatBarcode(Context context, String contents, int desiredWidth,
            int desiredHeight, boolean displayCode) {
        Bitmap ruseltBitmap = null;
        /**
         * Í¼Æ¬Á½¶ËËù±£ÁôµÄ¿Õ°×µÄ¿í¶È
         */
        int marginW = 20;
        /**
         * ÌõÐÎÂëµÄ±àÂëÀàÐÍ
         */
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

        if (displayCode) {
            Bitmap barcodeBitmap = encodeAsBitmap(contents, barcodeFormat, desiredWidth,
                    desiredHeight);
            Bitmap codeBitmap = creatCodeBitmap(contents, desiredWidth + 2 * marginW,
                    desiredHeight, context);
            ruseltBitmap = mixtureBitmap(barcodeBitmap, codeBitmap, new PointF(0, desiredHeight));
        } else {
            ruseltBitmap = encodeAsBitmap(contents, barcodeFormat, desiredWidth, desiredHeight);
        }

        return ruseltBitmap;
    }

    /**
     * Éú³ÉÌõÐÎÂëµÄBitmap
     * 
     * @param contents ÐèÒªÉú³ÉµÄÄÚÈÝ
     * @param format ±àÂë¸ñÊ½
     * @param desiredWidth
     * @param desiredHeight
     * @return
     * @throws WriterException
     */
    protected static Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int desiredWidth,
            int desiredHeight) {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        try {
            result = writer.encode(contents, format, desiredWidth, desiredHeight, null);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * Éú³ÉÏÔÊ¾±àÂëµÄBitmap
     * 
     * @param contents
     * @param width
     * @param height
     * @param context
     * @return
     */
    protected static Bitmap creatCodeBitmap(String contents, int width, int height, Context context) {
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
        tv.setHeight(height);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setWidth(width);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(Color.BLACK);
        tv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

        tv.buildDrawingCache();
        Bitmap bitmapCode = tv.getDrawingCache();
        return bitmapCode;
    }

    /**
     * ½«Á½¸öBitmapºÏ²¢³ÉÒ»¸ö
     * 
     * @param first
     * @param second
     * @param fromPoint µÚ¶þ¸öBitmap¿ªÊ¼»æÖÆµÄÆðÊ¼Î»ÖÃ£¨Ïà¶ÔÓÚµÚÒ»¸öBitmap£©
     * @return
     */
    protected static Bitmap mixtureBitmap(Bitmap first, Bitmap second, PointF fromPoint) {
        if (first == null || second == null || fromPoint == null) {
            return null;
        }
        int marginW = 20;
        Bitmap newBitmap = Bitmap.createBitmap(first.getWidth() + second.getWidth() + marginW,
                first.getHeight() + second.getHeight(), Config.ARGB_4444);
        Canvas cv = new Canvas(newBitmap);
        cv.drawBitmap(first, marginW, 0, null);
        cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();

        return newBitmap;
    }

}
