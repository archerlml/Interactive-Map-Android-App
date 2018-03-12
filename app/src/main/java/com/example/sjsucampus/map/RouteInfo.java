package com.example.sjsucampus.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by chitoo on 10/23/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteInfo {
    public List<Route> routes;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Leg {
        public Distance distance;
        public Duration duration;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Distance {
        public String text;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Duration {
        public String text;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        List<Leg> legs;
    }
}
