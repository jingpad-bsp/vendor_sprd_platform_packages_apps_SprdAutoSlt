package com.sprd.autoslt.action.impl;

import java.util.Date;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.autoslt.R;
import com.sprd.autoslt.action.AbstractAction;
import com.sprd.autoslt.common.SLTConstant;

public class GpsAction extends AbstractAction {

    private static final String TAG = "SLTGpsAction";

    private LocationManager mLocationManager;

    private static GpsAction instance;
    private static String mType;
    /** location update min time */
    private static final long UPDATE_MIN_TIME = 1000;
    private StringBuilder locationInfoResult = new StringBuilder();
    private StringBuilder satelliteInfoResult = new StringBuilder();
    /** location listener object */
    private LocationListener locationListener = null;
    private static final String GPS_EXTRA_POSITION = "position";
    private static final String GPS_EXTRA_EPHEMERIS = "ephemeris";
    private static final String GPS_EXTRA_TIME = "time";
    private static final String GPS_EXTRA_IONO = "iono";
    private static final String GPS_EXTRA_UTC = "utc";
    private static final String GPS_EXTRA_HEALTH = "health";
    private static final String GPS_EXTRA_ALL = "all";
    private static final String GPS_EXTRA_RTI = "rti";
    private static final String GPS_EXTRA_A1LMANAC = "almanac";
    private long mTtffValue = 0;
    private long startTime = 0;

    public GpsAction(StatusChangedListener listener) {
        super(listener);
        mLocationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status,
                    Bundle extras) {
            }
        };
    }

    public static GpsAction getInstance(StatusChangedListener listener,
            String type) {
        mType = type;
        if (instance == null) {
            instance = new GpsAction(listener);
        }
        return instance;
    }

    @Override
    public void start(String param) {
        if (SLTConstant.ACTION_TYPE_START_GPS_LOCATION.equals(mType)) {
            Log.d(TAG, "start() addGpsStatusListener && open GPS");
            // open GPS
            /*Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);*/
			 if(!isGpsOpen()) {
                    // open GPS
                	Settings.Secure.setLocationProviderEnabled(
    						mContext.getContentResolver(),
    						LocationManager.GPS_PROVIDER, true);
                    /*Settings.Secure.putInt(mContext.getContentResolver(),
                            Settings.Secure.LOCATION_MODE,
                            Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);*/
            }
            mTtffValue = 0;
            startTime = System.currentTimeMillis();
            try {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        UPDATE_MIN_TIME, 0, locationListener);
                mLocationManager.addGpsStatusListener(mListener);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, "error", Toast.LENGTH_SHORT).show();
            }
            ok();
        } else if (SLTConstant.ACTION_TYPE_DEL_GPS_AID_DATA.equals(mType)) {
            if (mLocationManager != null) {
                if(!isGpsOpen()) {
                    // open GPS
					Settings.Secure.setLocationProviderEnabled(
    						mContext.getContentResolver(),
    						LocationManager.GPS_PROVIDER, true);
                    /*Settings.Secure.putInt(mContext.getContentResolver(),
                            Settings.Secure.LOCATION_MODE,
                            Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);*/
                }
                Bundle extras = new Bundle();
                extras.putBoolean(GPS_EXTRA_EPHEMERIS, true);
                extras.putBoolean(GPS_EXTRA_POSITION, true);
                extras.putBoolean(GPS_EXTRA_TIME, true);
                extras.putBoolean(GPS_EXTRA_IONO, true);
                extras.putBoolean(GPS_EXTRA_UTC, true);
                extras.putBoolean(GPS_EXTRA_HEALTH, true);
                mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER,
                        "delete_aiding_data", extras);
            }
            ok();
        } else if (SLTConstant.ACTION_TYPE_GET_GPS_INFO.equals(mType)) {
            if (TextUtils.isEmpty(param)) {
                error("param error");
                return;
            }
            if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                error("gps close");
                return;
            }
            if ("locationinfo".equalsIgnoreCase(param)) {
                if(locationInfoResult.length() <= 0) {
                    end("fail");
                } else {
                    end(locationInfoResult.toString());
                }
            } else if ("satelliteinfo".equalsIgnoreCase(param)) {
                if(satelliteInfoResult.length() <= 0) {
                    end("fail");
                } else {
                    end(satelliteInfoResult.toString());
                }
            }
        } else if (SLTConstant.ACTION_TYPE_END_GPS_LOCATION.equals(mType)) {
        	if (mListener != null) {
        		mLocationManager.removeGpsStatusListener(mListener);
			}            
            if (locationListener != null) {
                mLocationManager.removeUpdates(locationListener);
            }
            mTtffValue = 0;
            if (isGpsOpen()) {
				// open GPS
				Settings.Secure.setLocationProviderEnabled(
						mContext.getContentResolver(),
						LocationManager.GPS_PROVIDER, false);
            	/*Settings.Secure.putInt(mContext.getContentResolver(),
                        Settings.Secure.LOCATION_MODE,
                        Settings.Secure.LOCATION_MODE_OFF);*/

			}
            ok();
        } else {
            error("invalid param!");
        }
    }

    @Override
    public void stop() {
    }

    GpsStatus.Listener mListener = new GpsStatus.Listener() {
        private void onFirstFix(int ttff) {
            Log.d(TAG, "Time stamp[onFirstFix]->" + new Date().getTime());
            Log.v(TAG, "Enter onFirstFix function: ttff = " + ttff);
        }

        @Override
        public void onGpsStatusChanged(int event) {
            Log.d(TAG, "onGpsStatusChanged event : " + event);
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                locationInfoResult = new StringBuilder();
                satelliteInfoResult = new StringBuilder();
                GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);

                Iterable<GpsSatellite> iterable = gpsStatus.getSatellites();
                Iterator<GpsSatellite> iterator = iterable.iterator();
                String provider = LocationManager.GPS_PROVIDER;
                Location location = mLocationManager
                        .getLastKnownLocation(provider);
                if(location != null) {
                    if(mTtffValue == 0) {                        
                        mTtffValue = System.currentTimeMillis() - startTime; // get default max count
                    }
                    Log.d(TAG, "gpsStatus.getTimeToFirstFix() -> " + mTtffValue);
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    locationInfoResult.append(mTtffValue);
                    locationInfoResult.append("^");
                    locationInfoResult.append(latitude);
                    locationInfoResult.append("^");
                    locationInfoResult.append(longitude);
                }

                while (iterator.hasNext()) {
                    GpsSatellite gpsStatellite = iterator.next();
                    int prn = gpsStatellite.getPrn();
                    float snr = gpsStatellite.getSnr();
                    Log.d(TAG, "gpsStatellite.getPrn() -> " + prn);
                    Log.d(TAG, "gpsStatellite.getSnr() -> " + snr);
                    if (satelliteInfoResult.length() > 0) {
                        satelliteInfoResult.append("^");
                    }
                    satelliteInfoResult.append(prn);
                    satelliteInfoResult.append("^");
                    satelliteInfoResult.append(snr);
                }
            } else if(event == GpsStatus.GPS_EVENT_FIRST_FIX) {   
                GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                onFirstFix(gpsStatus.getTimeToFirstFix());
            }
        }
    };
	 private boolean isGpsOpen() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // Weather gps is open or not
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return gpsStatus;
    }
}
