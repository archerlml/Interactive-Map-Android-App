package com.example.sjsucampus;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sjsucampus.map.MapConfig;
import com.example.sjsucampus.map.MapControllor;
import com.example.sjsucampus.map.Point;
import com.example.sjsucampus.map.RouteInfo;
import com.example.sjsucampus.util.Log;
import com.example.sjsucampus.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    final static int REQUEST_LOCATION_CODE = 1;
    final static String LOCATION_KEY = "location";

    boolean requestUpdateLocation = false;
    ImageView mapImageView;
    MapControllor map;
    RelativeLayout content;
    Map<String, ImageView> indicators;
    RelativeLayout summaryLayout;
    double heightOffset = -1;
    double scaleRatio = -1;
    Location mLastLocation;
    long lastUpdateTime = 0;
    long EXPIRE_PERIOD = 5 * 60 * 1000;// in ms
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    View fab;
    ImageView currentLocationIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i();
        super.onCreate(savedInstanceState);
        updateValuesFromBundle(savedInstanceState);
        setContentView(R.layout.activity_main);
        map = MapControllor.get();

        // init ui
        initActionbar(null);
        indicators = new HashMap<>();
        content = findViewById(R.id.content_main, RelativeLayout.class);
        summaryLayout = findViewById(R.id.summary, RelativeLayout.class);
        fab = findViewById(R.id.fab);

        // current location
        currentLocationIndicator = new ImageView(MainActivity.this);
        currentLocationIndicator.setImageResource(R.drawable.current);
        currentLocationIndicator.setVisibility(View.INVISIBLE);
        content.addView(currentLocationIndicator);

        // map related
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mapImageView = findViewById(R.id.map, ImageView.class);
        mapImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Point point = positionOnImage(new Point(event.getX(), event.getY()));
                        Point physicalPoint = map.logicalPointToPhysical(point);
                        Log.i(Util.objToJson(point));
                        Log.i(Util.objToJson(physicalPoint));

                        final MapConfig.Building building = map.getClickedBuilding(point);
                        Log.i("building: ", Util.objToJson(building));
                        if (building != null) {
                            highlightBuilding(building);
                        } else {
                            clearUp();
                        }
                        break;
                }
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestUpdateLocation = true;
                currentLocationIndicator.setVisibility(View.GONE);
            }
        });

    }

    private void clearUp() {
        removeIndicators();
        hideSummary();
    }

    private void highlightBuilding(MapConfig.Building building) {
        if (building == null) {
            Log.i("building is null");
            return;
        }
        Log.i(building.abbr);
        removeIndicators();
        getWalkingTime(building);
        addIndicator(building);
        showSummary(building);
    }

    private void getWalkingTime(final MapConfig.Building building) {
        Task.callInBackground(new Callable<RouteInfo.Leg>() {
            @Override
            public RouteInfo.Leg call() throws Exception {
                Log.i(building.abbr);
                RouteInfo.Leg leg = map.getRouteInfo(mLastLocation, building);
                building.time = leg.duration.text;
                building.distance = leg.distance.text;
                return leg;
            }
        }).onSuccess(new Continuation<RouteInfo.Leg, Void>() {
            @Override
            public Void then(Task<RouteInfo.Leg> task) throws Exception {
                // update ui if necessary
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void removeIndicators() {
        for (Map.Entry<String, ImageView> entry : indicators.entrySet()) {
            content.removeView(entry.getValue());
        }
        indicators.clear();
    }

    private void addIndicator(final MapConfig.Building building) {
        if (building == null) {
            return;
        }
        if (indicators.containsKey(building.abbr)) {
            return;
        }

        Log.i(building.abbr);
        final ImageView indicator = new ImageView(MainActivity.this);
        indicator.setImageResource(R.drawable.indicator);
        indicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDetail(building);
            }
        });

        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        Point screenPoint = logicalToScreen(new Point(building.center.x, building.center.y));
        params.leftMargin = (int) screenPoint.x;
        params.topMargin = (int) screenPoint.y;
        indicator.setVisibility(View.INVISIBLE);
        indicator.post(new Runnable() {
            @Override
            public void run() {
                params.leftMargin -= indicator.getWidth() / 2;
                params.topMargin -= indicator.getHeight();
                indicator.setLayoutParams(params);
                indicator.setVisibility(View.VISIBLE);
            }
        });
        indicator.setLayoutParams(params);

        content.addView(indicator);
        indicators.put(building.abbr, indicator);
    }

    private Point logicalToScreen(Point logical) {
        return new Point(logical.x / getScaleRatio(), logical.y / getScaleRatio() + getScreenHeightOffset());
    }

    private Point screenToLogical(Point screen) {
        return new Point(screen.x * getScaleRatio(), (screen.y - getScreenHeightOffset()) * getScaleRatio());
    }

    private void openDetail(MapConfig.Building building) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(MapConfig.Building.class.getName(), Util.objToJson(building));
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    private void showSummary(final MapConfig.Building building) {
        if (building == null) {
            return;
        }
        Log.i(building.abbr);
        TextView textView = findViewById(R.id.name, TextView.class);
        textView.setText(building.abbr);
        summaryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDetail(building);
            }
        });
        if (summaryLayout.getVisibility() != View.VISIBLE) {
            summaryLayout.setVisibility(View.VISIBLE);
            summaryLayout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_in));
            summaryLayout.animate();
        }
    }

    private void hideSummary() {
        if (summaryLayout.getVisibility() == View.VISIBLE) {
            summaryLayout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_out));
            summaryLayout.animate();
            summaryLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                String suggestion = getSuggestion(position);
                Log.i("name = ", suggestion);
                MapConfig.Building building = map.getBuildings().get(suggestion);
                highlightBuilding(building);
                return false;
            }

            private String getSuggestion(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(
                        position);
                String suggest1 = cursor.getString(cursor
                        .getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                return suggest1;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                String suggestion = getSuggestion(position);
                Log.i("name = ", suggestion);
                final MapConfig.Building building = map.getBuildings().get(suggestion);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        highlightBuilding(building);
                    }
                });
                return false;
            }
        });
        return true;
    }

    double getScreenHeightOffset() {
        if (heightOffset < 0) {
            double ratio = map.getMapConfig().size.width / map.getMapConfig().size.height;
            heightOffset = (mapImageView.getHeight() - mapImageView.getWidth() / ratio) / 2;
        }
        return heightOffset;
    }

    double getScaleRatio() {
        if (scaleRatio < 0) {
            scaleRatio = map.getMapConfig().size.width / mapImageView.getWidth();
        }
        return scaleRatio;
    }

    private Point positionOnImage(Point clickPoint) {
        Point point = new Point(clickPoint.x,
                clickPoint.y - getScreenHeightOffset());
        return new Point(point.x * getScaleRatio(), point.y * getScaleRatio());
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(savedInstanceState, outPersistentState);
        savedInstanceState.putParcelable(LOCATION_KEY, mLastLocation);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_CODE);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("requestCdoe = ", requestCode);

        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                boolean granted = true;
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        granted = false;
                    }
                }
                if (!granted) {
                    Toast.makeText(this, "Location permissions are required to get estimated time", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private static final double ACCURACY = 0.00001;

    private void getLocation(Location location) {
        if (location == null) {
            Log.i("location is null");
            return;
        }
        showCurrentLocation(location);
        lastUpdateTime = System.currentTimeMillis();
        mLastLocation = new Location(location);
    }

    private void showCurrentLocation(Location location) {

        Point logicalPoint = map.physicalPointToLogical(new Point(location.getLatitude(), location.getLongitude()));
        if (logicalPoint.x < 0 || logicalPoint.x > map.getMapConfig().size.width
                || logicalPoint.y < 0 || logicalPoint.y > map.getMapConfig().size.height) {
            Log.i("out of map");
            if (requestUpdateLocation) {
                requestUpdateLocation = false;
                currentLocationIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Out of campus", Toast.LENGTH_LONG).show();
            }
            return;
        }

        Log.i("logicalPoint: ", Util.objToJson(logicalPoint));
        Log.i("location: ", Util.objToJson(location));
        Point screenPoint = logicalToScreen(logicalPoint);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = (int) screenPoint.x;
        params.topMargin = (int) screenPoint.y;
        currentLocationIndicator.setLayoutParams(params);

        currentLocationIndicator.post(new Runnable() {
            @Override
            public void run() {
                params.leftMargin -= currentLocationIndicator.getWidth() / 2;
                params.topMargin -= currentLocationIndicator.getHeight() / 2;
                currentLocationIndicator.setLayoutParams(params);
                currentLocationIndicator.setVisibility(View.VISIBLE);
                requestUpdateLocation = false;
            }
        });


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("permission failed");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_CODE);
            return;
        }
        getLocation(LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient));
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        getLocation(location);
    }

}
