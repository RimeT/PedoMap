package vivo.learn.rime.pedomap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {

    //view
    TextView mBaiduLocationText;
    TextView mSensorText;

    //sensor
    SensorManager mSensorManager;
    Sensor mStepCountSensor;
    Sensor mStepDetectSensor;
    SensorEventListener mSensorEventListener;

    //map
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private LocationClient mLocationClient = null;

    //location listener
    private BDLocationListener baiduLocationListener;

    //first open app
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        //view
        mBaiduLocationText = (TextView) findViewById(R.id.text_baidu_location);
        mSensorText = (TextView) findViewById(R.id.text_step_count);

        //Sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mSensorEventListener = new MyListener();

        //baidumap
        mMapView = (MapView) findViewById(R.id.map_view_baidu);
        mBaiduMap = mMapView.getMap();
        initMap();

        //location
        mLocationClient = new LocationClient(getApplicationContext());
        mBaiduMap.setMyLocationEnabled(true);

        //location listener
        baiduLocationListener = new BaiduLocationListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //baidu map
        mLocationClient.registerLocationListener(baiduLocationListener);
        initLocation();
        mLocationClient.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        mSensorManager.registerListener(mSensorEventListener, mStepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorEventListener, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    @Override
    protected void onStop() {
        mLocationClient.stop();
        mLocationClient.unRegisterLocationListener(baiduLocationListener);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
    }

    /*
     * baidu map initialize
     */
    private void initMap() {
        mMapView.showZoomControls(false);
    }

    /*
     * baidu location client initialize
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);

        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    /*
     * map move to a location
     */
    private void navigateTo(BDLocation bdLocation) {
        if (isFirstLocate) {
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            float zoomLevel = 19f;
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(latLng, zoomLevel);
            mBaiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(bdLocation.getLatitude());
        locationBuilder.longitude(bdLocation.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        mBaiduMap.setMyLocationData(locationData);
    }

    /*
     * sensor listener
     */
    private class MyListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            int sensorType = sensorEvent.sensor.getType();
            switch (sensorType) {
                case Sensor.TYPE_STEP_COUNTER:
                    mSensorText.setText(String.valueOf((int) sensorEvent.values[0]));
                    break;
                case Sensor.TYPE_STEP_DETECTOR:
                    break;
                default:
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    /*
     * Baidu location listener
     */
    private class BaiduLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    double longitude = bdLocation.getLongitude();
                    double latitude = bdLocation.getLatitude();
                    navigateTo(bdLocation);
                    String lon = String.format("%.6f", longitude);
                    String lat = String.format("%.6f", latitude);
                    mBaiduLocationText.setText("baidu-(" + lon + "," + lat + ")\n");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(bdLocation.getLocationDescribe());
                    stringBuilder.append(bdLocation.getDistrict() + "\n");
                    stringBuilder.append(bdLocation.getLocTypeDescription() + "\n");
                    stringBuilder.append(String.valueOf(bdLocation.getSatelliteNumber()));
                    mBaiduLocationText.append(stringBuilder.toString());
                }
            });
        }
    }
}
