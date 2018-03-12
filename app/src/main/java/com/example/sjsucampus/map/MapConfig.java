package com.example.sjsucampus.map;

import com.example.sjsucampus.BaseApplication;
import com.example.sjsucampus.R;
import com.example.sjsucampus.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by chitoo on 10/22/16.
 */

public class MapConfig {
    // see res/raw/map_info.json
    public Corners corners;
    public List<Building> buildings;
    public Size size;

    private static MapConfig config;

    public static MapConfig getConfig() {
        if (config == null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                InputStream in = BaseApplication.get().getResources().openRawResource(R.raw.map_info);
                config = mapper.readValue(in, MapConfig.class);
            } catch (IOException e) {
                Log.e(e);
            }
        }
        return config;
    }

    public static class Corner {
        public Point logical;
        public Point physical;
    }


    public static class Corners {
        public Corner left;
        public Corner right;
        public Corner bottom;
    }

    public static class Building {
        public String name;
        public String abbr;
        public String address;
        public Point center;
        public String info;
        public String time;
        public String distance;
        public String defaultImage;
        public Point location;
        public Point viewPoint;
        public List<Point> vertexes;

        public Map<String, Object> extras;

        public Point streetViewPoint() {
            if (viewPoint != null) {
                return viewPoint;
            }
            return physicalLocation();
        }

        public Point physicalLocation() {
            if (location != null) {
                return location;
            }
            return MapControllor.get().logicalPointToPhysical(center);
        }
    }

    public static class Size {
        public double width;
        public double height;
    }
}
