package com.example.sjsucampus.map;

import android.location.Location;

import com.example.sjsucampus.BaseApplication;
import com.example.sjsucampus.R;
import com.example.sjsucampus.util.Log;
import com.example.sjsucampus.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by chitoo on 10/22/16.
 */

public class MapControllor {
    MapConfig mapConfig;
    Map<String, MapConfig.Building> buildings;
    OkHttpClient client;

    private static MapControllor controllor;

    public static MapControllor get() {
        if (controllor == null) {
            controllor = new MapControllor();
        }
        return controllor;
    }

    private MapControllor() {
        mapConfig = MapConfig.getConfig();
        buildings = new HashMap<>();
        for (MapConfig.Building building : mapConfig.buildings) {
            buildings.put(building.abbr, building);
        }
        client = new OkHttpClient();
    }

    public MapConfig getMapConfig() {
        return mapConfig;
    }

    public Map<String, MapConfig.Building> getBuildings() {
        return buildings;
    }

    public Point logicalPointToPhysical(Point logical) {
        Segment leftBottom = new Segment(mapConfig.corners.bottom.logical, mapConfig.corners.left.logical);
        Segment rightBottom = new Segment(mapConfig.corners.bottom.logical, mapConfig.corners.right.logical);
        Segment segments = new Segment(logical, mapConfig.corners.bottom.logical);
        return Calculator.logicalToPhysical(mapConfig.corners.left.physical, mapConfig.corners.bottom.physical,
                mapConfig.corners.right.physical, leftBottom.distance(), rightBottom.distance(), segments.dx(), segments.dy());
    }

    public Point physicalPointToLogical(Point physical) {
        //TODO convert lat,lng to image offset
//        System.out.println(Math.toDegrees(Math.sinh(0.5)));
//        System.out.println( Math.sin(Math.toRadians(30)) ) ;
        //eng
        //physical = new Point(37.337012, -121.881595);
        //library
        //physical = new Point(37.335337, -121.884950);
        //YUHYoshihiro Uchida Hall
        //physical = new Point(37.333637, -121.883756);
        //south garage
        //physical = new Point(37.333068, -121.880787);
        //student union
        //physical = new Point(37.336446, -121.881152);
        //bbc
        //physical = new Point(37.336667, -121.878631);

        double lat0 = 37.335882;
        double lng0 = -121.886022;

        double lat2 = 37.330735;
        double lng2 = -121.879041;

        double lat1 = physical.x;
        double lng1 = physical.y;

        double latResult = lat0;
        double lngResult = lng0;
        double distance = Math.sqrt((lat1 - lat0)*(lat1 - lat0) + (lng1 - lng0)*(lng1 - lng0));
        if(lat1 > lat0){
            double angle1 = Math.toDegrees(Math.sinh((lat1 - lat0)/distance));
            double angle2 = 30 - angle1;
            lngResult = lng0 + distance * Math.cos(Math.toRadians(angle2));
            latResult = lat0 - distance * Math.sin(Math.toRadians(angle2));
        }else if(lat1 < lat0){
            double angle1 = Math.toDegrees(Math.sinh((lat0 - lat1)/distance));
            double angle2 = 30 + angle1;
            lngResult = lng0 + distance * Math.cos(Math.toRadians(angle2));
            latResult = lat0 - distance * Math.sin(Math.toRadians(angle2));
        }else{
            latResult = lat0 - distance * Math.sin(Math.toRadians(30));
            lngResult = lng0 + distance * Math.cos(Math.toRadians(30));
        }

        double x = 500*(lngResult - lng0)/(lng2-lng0) + 50;
        double y = 440*(latResult - lat0)/(lat2-lat0) + 160;
        Point point = new Point(x,y);
        return point;
//        return buildings.get("Yoshihiro Uchida Hall").center;
    }

    public MapConfig.Building getClickedBuilding(Point point) {
        for (Map.Entry<String, MapConfig.Building> entry : buildings.entrySet()) {
            MapConfig.Building building = entry.getValue();
            if (Calculator.isInside(building.vertexes, point)) {
                return building;
            }
        }
        return null;
    }

    public RouteInfo.Leg getRouteInfo(Location myLocation, MapConfig.Building building) {
        HttpUrl url = getWalkingRequestUrl(new Point(myLocation.getLatitude(), myLocation.getLongitude())
                , new Point(building.center.x, building.center.y));
        Log.i(url);
        String json = getContentAsString(url);
        RouteInfo routeInfo = Util.jsonToObj(json, RouteInfo.class);
        Log.i("route.leg = ", Util.objToJson(routeInfo));
        if (routeInfo == null || routeInfo.routes == null || routeInfo.routes.get(0).legs == null) {
            return null;
        }
        return routeInfo.routes.get(0).legs.get(0);
    }

    public String getPlacePhoto(Point location) {
        HttpUrl url = getPlaceInfoUrl(new Point(location.x, location.y));
        Log.i(url);
        String json = getContentAsString(url);
        String photoRef = Util.getJsonValueAs(json, "/results/0/photos/0/photo_reference", String.class);
        Log.i(photoRef);
        return getPlacePhotoUrl(photoRef).toString();
    }

    private HttpUrl getWalkingRequestUrl(Point original, Point destination) {
        Point dst = logicalPointToPhysical(destination);
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("directions")
                .addPathSegment("json")
                .addQueryParameter("origin", Util.getString(original.x, ",", original.y))
                .addQueryParameter("destination", Util.getString(dst.x, ",", dst.y))
                .addQueryParameter("key", BaseApplication.get().getString(R.string.map_api_key))
                .addQueryParameter("mode", "walking")
                .build();
        return url;
    }

    private HttpUrl getPlaceInfoUrl(Point location) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("place")
                .addPathSegment("nearbysearch")
                .addPathSegment("json")
                .addQueryParameter("location", Util.getString(location.x, ",", location.y))
                .addQueryParameter("key", BaseApplication.get().getString(R.string.place_api_key))
                .addQueryParameter("rankby", "distance")
                .build();
        return url;
    }

    private HttpUrl getPlacePhotoUrl(String photoRef) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("place")
                .addPathSegment("photo")
                .addQueryParameter("maxwidth", String.valueOf(600))
                .addQueryParameter("key", BaseApplication.get().getString(R.string.place_api_key))
                .addQueryParameter("photoreference", photoRef)
                .build();
        return url;
    }

    public String getContentAsString(HttpUrl url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(e.toString());
        }
        return "";
    }

    public int getDefaultImage(String abbr){
        return 0;
    }
}
