package com.example.sjsucampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sjsucampus.map.MapConfig;
import com.example.sjsucampus.map.MapControllor;
import com.example.sjsucampus.map.Point;
import com.example.sjsucampus.util.Log;
import com.example.sjsucampus.util.Util;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class DetailActivity extends BaseActivity {
    MapConfig.Building building;
    TextView name;
    TextView info;
    TextView address;
    TextView time;
    TextView distance;
    ImageView image;
    MapControllor map;
    View fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initActionbar("");

        map = MapControllor.get();
        building = Util.jsonToObj(getIntent().getStringExtra(MapConfig.Building.class.getName()), MapConfig.Building.class);

        name = findViewById(R.id.name, TextView.class);
        info = findViewById(R.id.info, TextView.class);
        address = findViewById(R.id.address, TextView.class);
        time = findViewById(R.id.time, TextView.class);
        distance = findViewById(R.id.distance, TextView.class);
        image = findViewById(R.id.image, ImageView.class);
        fab = findViewById(R.id.fab);

        loadImage();
        name.setText(building.name);
        info.setText("");
        address.setText(building.address);
        time.setText(building.time);
        distance.setText(building.distance);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, StreetViewActivity.class);
                intent.putExtra(MapConfig.Building.class.getName(), Util.objToJson(building));
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
    }


    private void loadImage() {
        Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Point location = building.physicalLocation();
                String photoUrl = MapControllor.get().getPlacePhoto(building.physicalLocation());
                Log.i("photoUrl = ", photoUrl);
                return photoUrl;
            }
        }).onSuccess(new Continuation<String, Void>() {
            @Override
            public Void then(Task<String> task) throws Exception {
                int defaultRes = Util.getRes(building.defaultImage);
                if (defaultRes == 0) {
                    defaultRes = R.drawable.image_not_available;
                }
                Picasso.with(DetailActivity.this).load(task.getResult())
                        .error(defaultRes).into(image);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }
}
