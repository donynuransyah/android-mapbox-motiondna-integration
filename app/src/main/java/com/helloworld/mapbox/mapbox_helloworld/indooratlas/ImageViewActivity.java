package com.helloworld.mapbox.mapbox_helloworld.indooratlas;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.helloworld.mapbox.mapbox_helloworld.R;

public class ImageViewActivity extends FragmentActivity implements ListenerFloorPlan {

    private static final String TAG = "IndoorAtlasExample";

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

    // blue dot radius in meters
    private static final float dotRadius = 1.0f;
    FloorPlanListener floorPlanListener;
//    private IALocationManager mIALocationManager;
//    private IAFloorPlan mFloorPlan;

    private BlueDotView mImageView;
    private long mDownloadId;
    private DownloadManager mDownloadManager;

//    private IALocationListener mLocationListener = new IALocationListenerSupport() {
//        @Override
//        public void onLocationChanged(IALocation location) {
//            Log.d(TAG, "location is: " + location.getLatitude() + "," + location.getLongitude());
//            if (mImageView != null && mImageView.isReady()) {
//                IALatLng latLng = new IALatLng(location.getLatitude(), location.getLongitude());
//                PointF point = mFloorPlan.coordinateToPoint(latLng);
//                mImageView.setDotCenter(point);
//                mImageView.setUncertaintyRadius(
//                        mFloorPlan.getMetersToPixels() * location.getAccuracy());
//                mImageView.postInvalidate();
//            }
//        }
//    };

    private HeadingListenerInterface mHeadingListener = new HeadingListenerInterface() {
        @Override
        public void onHeadingChanged(float value) {
            mImageView.setHeading(value);
        }
    };



//    private IAOrientationListener mOrientationListener = new IAOrientationListener() {
//        @Override
//        public void onHeadingChanged(long timestamp, double heading) {
//            if (mFloorPlan != null) {
//                mImageView.setHeading(heading - mFloorPlan.getBearing());
//            }
//        }
//
//        @Override
//        public void onOrientationChange(long l, double[] doubles) {
//            // No-op
//        }
//    };

//    private IARegion.Listener mRegionListener = new IARegion.Listener() {
//
//        @Override
//        public void onEnterRegion(IARegion region) {
//            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
//                String id = region.getId();
//                Log.d(TAG, "floorPlan changed to " + id);
//                Toast.makeText(ImageViewActivity.this, id, Toast.LENGTH_SHORT).show();
//                fetchFloorPlan(region.getFloorPlan());
//            }
//        }
//
//        @Override
//        public void onExitRegion(IARegion region) {
//            // leaving a previously entered region
//        }
//
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoormap);
        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        mImageView = findViewById(R.id.imageView);

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//        mIALocationManager = IALocationManager.create(this);
        floorPlanListener = new FloorPlanListener(this);

        // Setup long click listener for sharing traceId
//        ExampleUtils.shareTraceId(findViewById(R.id.imageView), ImageViewActivity.this,
//                mIALocationManager);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mIALocationManager.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        ensurePermissions();
        // starts receiving location updates
        floorPlanListener.removeListener();
//        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mLocationListener);
//        mIALocationManager.registerRegionListener(mRegionListener);
//        IAOrientationRequest orientationRequest = new IAOrientationRequest(10f, 10f);
//        mIALocationManager.registerOrientationListener(orientationRequest, mOrientationListener);
//        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mIALocationManager.removeLocationUpdates(mLocationListener);
//        mIALocationManager.unregisterRegionListener(mRegionListener);
//        mIALocationManager.unregisterOrientationListener(mOrientationListener);
        unregisterReceiver(onComplete);
    }

    /**
     * Methods for fetching bitmap image.
     */

    /*  Broadcast receiver for floor plan image download */
    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != mDownloadId) {
                Log.w(TAG, "Ignore unrelated download");
                return;
            }
            Log.w(TAG, "Image download completed");
            Bundle extras = intent.getExtras();

            if (extras == null) {
                Log.w(TAG, "Extras null: can't show floor plan");
                return;
            }

            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = mDownloadManager.query(q);

            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    // process download
                    String filePath = c.getString(c.getColumnIndex(
                            DownloadManager.COLUMN_LOCAL_URI));
                    showFloorPlanImage(filePath);
                }
            }
            c.close();
        }
    };

    private void showFloorPlanImage(String filePath) {
        Log.w(TAG, "showFloorPlanImage: " + filePath);
//        mImageView.setDotRadius(mFloorPlan.getMetersToPixels() * dotRadius);
//        mImageView.setImage(ImageSource.uri(filePath));
    }

    /**
     * Fetches floor plan data from IndoorAtlas server. Some room for cleaning up!!
     */
//    private void fetchFloorPlan(IAFloorPlan floorPlan) {
//        mFloorPlan = floorPlan;
//        String fileName = mFloorPlan.getId() + ".img";
//        String filePath = Environment.getExternalStorageDirectory() + "/"
//                + Environment.DIRECTORY_DOWNLOADS + "/" + fileName;
//        File file = new File(filePath);
//        if (!file.exists()) {
//            DownloadManager.Request request =
//                    new DownloadManager.Request(Uri.parse(mFloorPlan.getUrl()));
//            request.setDescription("IndoorAtlas floor plan");
//            request.setTitle("Floor plan");
//            request.setDestinationInExternalPublicDir(Environment.
//                    DIRECTORY_DOWNLOADS, fileName);
//
//            mDownloadId = mDownloadManager.enqueue(request);
//        } else {
//            showFloorPlanImage(filePath);
//        }
//    }

//    private void ensurePermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
//        }
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
//            if (grantResults.length == 0
//                    || grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                Toast.makeText(this, R.string.storage_permission_denied_message,
//                        Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }
//    }

    @Override
    public void onFloorChanged(int floorplan) {

    }
}

