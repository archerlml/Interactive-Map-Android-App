package com.example.sjsucampus;

import android.os.Bundle;

import com.example.sjsucampus.map.MapConfig;
import com.example.sjsucampus.util.Log;
import com.example.sjsucampus.util.Util;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

public class StreetViewActivity extends BaseActivity {
    MapConfig.Building building;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        building = Util.jsonToObj(getIntent().getStringExtra(MapConfig.Building.class.getName()), MapConfig.Building.class);
        initActionbar(building.abbr);

        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        // Only set the panorama to SYDNEY on startup (when no panoramas have been
                        // loaded which is when the savedInstanceState is null).
                        Log.i(Util.objToJson(building));
                        panorama.setPosition(new LatLng(building.streetViewPoint().x, building.streetViewPoint().y));
                    }
                });

    }
}
