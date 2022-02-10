/* 
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sprd.autoslt.activity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.sprd.autoslt.R;

public class Tester3dlesson08 extends Activity {

    public static final String TAG = "Tester3dlesson08";

    private int MSG_BEGIN_GRAPHICS_3D_LESSON08_TEST = 100;
    private int MSG_STOP_GRAPHICS_3D_LESSON08_TEST = 101;
    private int MSG_STOP_GRAPHICS_3D_LESSON08_TEST_FROM_NOTIFY = 102;

    private static Handler mCommHandler = null;
    private Lesson08 lesson08 = null;

    private boolean mbUserPressBack = false;

    private static String ACTION_STOP_3DLESSON08TEST = "com.sprd.runtime.3dlesson08test.stop";

    private Handler mInnerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_BEGIN_GRAPHICS_3D_LESSON08_TEST) {
            } else if (msg.what == MSG_STOP_GRAPHICS_3D_LESSON08_TEST) {
                finish();
            } else if (msg.what == MSG_STOP_GRAPHICS_3D_LESSON08_TEST_FROM_NOTIFY) {
                finish();
            }
        }
    };

    // processing event sent form runtimetest service(mostly for battery test
    // failed,all other test should be stopped).
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "mReceiver: action:" + action);

            if (action.equals(ACTION_STOP_3DLESSON08TEST)) {
                mInnerHandler
                        .sendEmptyMessage(MSG_STOP_GRAPHICS_3D_LESSON08_TEST_FROM_NOTIFY);
            }
        }

    };

    public static void setCommHandler(Handler handler) {
        mCommHandler = handler;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Make sure to create a TRANSLUCENT window. This is required
        // for SurfaceView to work. Eventually this'll be done by
        // the system automatically.
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        // We don't need a title either.
        // requestWindowFeature(Window.FEATURE_NO_TITLE);

        setTitle(TAG);

        // register filter for processing stop event sent from
        // runtimetestservice(mostly for battery test failed)
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP_3DLESSON08TEST);

        registerReceiver(mReceiver, filter);

        lesson08 = new Lesson08(this);
        lesson08.setSpeedAndTester(1, 1);
        // Set the lesson as View to the Activity
        setContentView(lesson08);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mbUserPressBack == true) {
            // finish activity
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // only process user back key
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int keycode = event.getKeyCode();
        Log.d(TAG, "onKeyDown:keycode:" + keycode);

        if (keycode == KeyEvent.KEYCODE_BACK) {
            mbUserPressBack = true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sprd.runtime.Testerthread.TesterInterface
     */
    public void onstartTest() {
        mInnerHandler.sendEmptyMessage(MSG_BEGIN_GRAPHICS_3D_LESSON08_TEST);
    }

    public void onstopTest() {
        // TODO Auto-generated method stub
        mInnerHandler.sendEmptyMessage(MSG_STOP_GRAPHICS_3D_LESSON08_TEST);
    }

    public int onsleepbeforestart() {
        // TODO Auto-generated method stub
        return 5000;
    }

    public int sleepBeforeStart() {
        return 1000; // 1 second
    }

    public int sleepBetweenRound() {
        return 0;
    }

    static class Lesson08 extends GLSurfaceView implements Renderer {
        /** Cube instance */
        private Cube cube;

        /* Rotation values */
        private float xrot; // X Rotation
        private float yrot; // Y Rotation

        /* Rotation speed values */
        private float xspeed; // X Rotation Speed
        private float yspeed; // Y Rotation Speed

        private float z = -5.0f; // Depth Into The Screen

        private int filter = 0; // Which texture filter?

        /** Is light enabled */
        private boolean light = true;
        /** Is blending enabled ( NEW ) */
        private boolean blend = true;

        /* The initial light values */
        private float[] lightAmbient = { 0.5f, 0.5f, 0.5f, 1.0f };
        private float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
        private float[] lightPosition = { 0.0f, 0.0f, 2.0f, 1.0f };

        /* The buffers for our light values */
        private FloatBuffer lightAmbientBuffer;
        private FloatBuffer lightDiffuseBuffer;
        private FloatBuffer lightPositionBuffer;

        /* Variables and factor for the input handler */
        private float oldX;
        private float oldY;
        private static final float TOUCH_SCALE = 0.2f; // Proved to be good for normal
                                                // rotation

        /** The Activity Context */
        private Context context;

        /**
         * Instance the Cube object and set the Activity Context handed over.
         * Initiate the light buffers and set this class as renderer for this
         * now GLSurfaceView. Request Focus and set if focusable in touch mode
         * to receive the Input from Screen and Buttons
         * 
         * @param context
         *            - The Activity Context
         */
        public Lesson08(Context context) {
            super(context);

            // Set this as Renderer
            this.setRenderer(this);
            // Request focus, otherwise buttons won't react
            this.requestFocus();
            this.setFocusableInTouchMode(true);

            //
            this.context = context;

            //
            ByteBuffer byteBuf = ByteBuffer
                    .allocateDirect(lightAmbient.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            lightAmbientBuffer = byteBuf.asFloatBuffer();
            lightAmbientBuffer.put(lightAmbient);
            lightAmbientBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            lightDiffuseBuffer = byteBuf.asFloatBuffer();
            lightDiffuseBuffer.put(lightDiffuse);
            lightDiffuseBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(lightPosition.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            lightPositionBuffer = byteBuf.asFloatBuffer();
            lightPositionBuffer.put(lightPosition);
            lightPositionBuffer.position(0);

            //
            cube = new Cube();
        }

        public void setSpeedAndTester(int speedX, int speedY) {
            xspeed = speedX;
            yspeed = speedY;
        }

        /**
         * The Surface is created/init()
         */
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // And there'll be light!
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer); // Setup
                                                                               // The
                                                                               // Ambient
                                                                               // Light
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer); // Setup
                                                                               // The
                                                                               // Diffuse
                                                                               // Light
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer); // Position
                                                                                 // The
                                                                                 // Light
            gl.glEnable(GL10.GL_LIGHT0); // Enable Light 0

            // Blending
            gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f); // Full Brightness. 50% Alpha
                                                  // ( NEW )
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE); // Set The Blending
                                                            // Function For
                                                            // Translucency (
                                                            // NEW )

            // Settings
            gl.glDisable(GL10.GL_DITHER); // Disable dithering
            gl.glEnable(GL10.GL_TEXTURE_2D); // Enable Texture Mapping
            gl.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // Black Background
            gl.glClearDepthf(1.0f); // Depth Buffer Setup
            gl.glEnable(GL10.GL_DEPTH_TEST); // Enables Depth Testing
            gl.glDepthFunc(GL10.GL_LEQUAL); // The Type Of Depth Testing To Do

            // Really Nice Perspective Calculations
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

            // Load the texture for the cube once during Surface creation
            cube.loadGLTexture(gl, this.context);
        }

        /**
         * Here we do our drawing
         */
        public void onDrawFrame(GL10 gl) {
            // Clear Screen And Depth Buffer
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity(); // Reset The Current Modelview Matrix

            // Check if the light flag has been set to enable/disable lighting
            if (light) {
                gl.glEnable(GL10.GL_LIGHTING);
            } else {
                gl.glDisable(GL10.GL_LIGHTING);
            }

            // Check if the blend flag has been set to enable/disable blending
            if (blend) {
                gl.glEnable(GL10.GL_BLEND); // Turn Blending On ( NEW )
                gl.glDisable(GL10.GL_DEPTH_TEST); // Turn Depth Testing Off (
                                                  // NEW )

            } else {
                gl.glDisable(GL10.GL_BLEND); // Turn Blending On ( NEW )
                gl.glEnable(GL10.GL_DEPTH_TEST); // Turn Depth Testing Off ( NEW
                                                 // )
            }

            // Drawing
            gl.glTranslatef(0.0f, 0.0f, z); // Move z units into the screen
            gl.glScalef(0.8f, 0.8f, 0.8f); // Scale the Cube to 80 percent,
                                           // otherwise it would be too large
                                           // for the screen

            // Rotate around the axis based on the rotation matrix (rotation, x,
            // y, z)
            gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f); // X
            gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f); // Y

            cube.draw(gl, filter); // Draw the Cube
            gl.glFinish();
            // Change rotation factors
            xrot += xspeed;
            yrot += yspeed;
        }

        /**
         * If the surface changes, reset the view
         */
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            if (height == 0) { // Prevent A Divide By Zero By
                height = 1; // Making Height Equal One
            }

            gl.glViewport(0, 0, width, height); // Reset The Current Viewport
            gl.glMatrixMode(GL10.GL_PROJECTION); // Select The Projection Matrix
            gl.glLoadIdentity(); // Reset The Projection Matrix

            // Calculate The Aspect Ratio Of The Window
            GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
                    100.0f);

            gl.glMatrixMode(GL10.GL_MODELVIEW); // Select The Modelview Matrix
            gl.glLoadIdentity(); // Reset The Modelview Matrix
        }

        /* ***** Listener Events ***** */
        /**
         * Override the key listener to receive keyUp events.
         * 
         * Check for the DPad presses left, right, up, down and middle. Change
         * the rotation speed according to the presses or change the texture
         * filter used through the middle press.
         */
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            //
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                yspeed -= 0.1f;

            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                yspeed += 0.1f;

            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                xspeed -= 0.1f;

            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                xspeed += 0.1f;

            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                filter += 1;
                if (filter > 2) {
                    filter = 0;
                }
            }

            // We handled the event
            return true;
        }

        /**
         * Override the touch screen listener.
         * 
         * React to moves and presses on the touchscreen.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            //
            float x = event.getX();
            float y = event.getY();

            // If a touch is moved on the screen
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                // Calculate the change
                float dx = x - oldX;
                float dy = y - oldY;
                // Define an upper area of 10% on the screen
                int upperArea = this.getHeight() / 10;

                // Zoom in/out if the touch move has been made in the upper
                if (y < upperArea) {
                    z -= dx * TOUCH_SCALE / 2;

                    // Rotate around the axis otherwise
                } else {
                    xrot += dy * TOUCH_SCALE;
                    yrot += dx * TOUCH_SCALE;
                }

                // A press on the screen
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // Define an upper area of 10% to define a lower area
                int upperArea = this.getHeight() / 10;
                int lowerArea = this.getHeight() - upperArea;

                //
                if (y > lowerArea) {
                    // Change the blend setting if the lower area left has been
                    // pressed ( NEW )
                    if (x < ((float)this.getWidth() / 2)) {
                        if (blend) {
                            blend = false;
                        } else {
                            blend = true;
                        }

                        // Change the light setting if the lower area right has
                        // been pressed
                    } else {
                        if (light) {
                            light = false;
                        } else {
                            light = true;
                        }
                    }
                }
            }

            // Remember the values
            oldX = x;
            oldY = y;

            // We handled the event
            return true;
        }
    }

    public static class Cube {

        /** The buffer holding the vertices */
        private FloatBuffer vertexBuffer;
        /** The buffer holding the texture coordinates */
        private FloatBuffer textureBuffer;
        /** The buffer holding the indices */
        private ByteBuffer indexBuffer;
        /** The buffer holding the normals */
        private FloatBuffer normalBuffer;

        /** Our texture pointer */
        private int[] textures = new int[3];

        /** The initial vertex definition */
        private float vertices[] = {
                // Vertices according to faces
                -1.0f,
                -1.0f,
                1.0f, // v0
                1.0f,
                -1.0f,
                1.0f, // v1
                -1.0f,
                1.0f,
                1.0f, // v2
                1.0f,
                1.0f,
                1.0f, // v3

                1.0f,
                -1.0f,
                1.0f, // ...
                1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,

                1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,

                -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,

                -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,

                -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, };

        /** The initial normals for the lighting calculations */
        private float normals[] = {
                // Normals
                0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f,

                0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f,

                0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f,

                0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f,

                0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f,

                0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, };

        /** The initial texture coordinates (u, v) */
        private float texture[] = {
                // Mapping coordinates for the vertices
                0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, };

        /** The initial indices definition */
        private byte indices[] = {
                // Faces definition
                0, 1, 3, 0, 3,
                2, // Face front
                4, 5, 7, 4, 7,
                6, // Face right
                8, 9, 11, 8, 11,
                10, // ...
                12, 13, 15, 12, 15, 14, 16, 17, 19, 16, 19, 18, 20, 21, 23, 20,
                23, 22, };

        /**
         * The Cube constructor.
         * 
         * Initiate the buffers.
         */
        public Cube() {
            //
            ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            vertexBuffer = byteBuf.asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);

            //
            byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            textureBuffer = byteBuf.asFloatBuffer();
            textureBuffer.put(texture);
            textureBuffer.position(0);

            //
            byteBuf = ByteBuffer.allocateDirect(normals.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            normalBuffer = byteBuf.asFloatBuffer();
            normalBuffer.put(normals);
            normalBuffer.position(0);

            //
            indexBuffer = ByteBuffer.allocateDirect(indices.length);
            indexBuffer.put(indices);
            indexBuffer.position(0);
        }

        /**
         * The object own drawing function. Called from the renderer to redraw
         * this instance with possible changes in values.
         * 
         * @param gl
         *            - The GL Context
         * @param filter
         *            - Which texture filter to be used
         */
        public void draw(GL10 gl, int filter) {
            // Bind the texture according to the set texture filter
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[filter]);

            // Enable the vertex, texture and normal state
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

            // Set the face rotation
            gl.glFrontFace(GL10.GL_CCW);

            // Point to our buffers
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
            gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);

            // Draw the vertices as triangles, based on the Index Buffer
            // information
            gl.glDrawElements(GL10.GL_TRIANGLES, indices.length,
                    GL10.GL_UNSIGNED_BYTE, indexBuffer);

            // Disable the client state before leaving
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        }

        /**
         * Load the textures
         * 
         * @param gl
         *            - The GL Context
         * @param context
         *            - The Activity context
         */
        public void loadGLTexture(GL10 gl, Context context) {
            // Get the texture from the Android resource directory
            InputStream is = context.getResources().openRawResource(
                    R.drawable.glass);
            Bitmap bitmap = null;
            try {
                // BitmapFactory is an Android graphics utility for images
                bitmap = BitmapFactory.decodeStream(is);

            } finally {
                // Always clear and close
                try {
                    is.close();
                    is = null;
                } catch (IOException e) {
                }
            }

            // Generate there texture pointer
            gl.glGenTextures(3, textures, 0);

            // Create Nearest Filtered Texture and bind it to texture 0
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                    GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                    GL10.GL_NEAREST);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

            // Create Linear Filtered Texture and bind it to texture 1
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[1]);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                    GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                    GL10.GL_LINEAR);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

            // Create mipmapped textures and bind it to texture 2
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[2]);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                    GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                    GL10.GL_LINEAR_MIPMAP_NEAREST);
            /*
             * This is a change to the original tutorial, as buildMipMap does
             * not exist anymore in the Android SDK.
             * 
             * We check if the GL context is version 1.1 and generate MipMaps by
             * flag. Otherwise we call our own buildMipMap implementation
             */
            if (gl instanceof GL11) {
                gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP,
                        GL11.GL_TRUE);
                GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

                //
            } else {
                buildMipmap(gl, bitmap);
            }

            // Clean up
            bitmap.recycle();
        }

        /**
         * Our own MipMap generation implementation. Scale the original bitmap
         * down, always by factor two, and set it as new mipmap level.
         * 
         * Thanks to Mike Miller (with minor changes)!
         * 
         * @param gl
         *            - The GL Context
         * @param bitmap
         *            - The bitmap to mipmap
         */
        private void buildMipmap(GL10 gl, Bitmap bitmap) {
            //
            int level = 0;
            //
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();

            //
            while (height >= 1 || width >= 1) {
                // First of all, generate the texture from our bitmap and set it
                // to the according level
                GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bitmap, 0);

                //
                if (height == 1 || width == 1) {
                    break;
                }

                // Increase the mipmap level
                level++;

                //
                height /= 2;
                width /= 2;
                Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width,
                        height, true);

                // Clean up
                bitmap.recycle();
                bitmap = bitmap2;
            }
        }
    }

}
