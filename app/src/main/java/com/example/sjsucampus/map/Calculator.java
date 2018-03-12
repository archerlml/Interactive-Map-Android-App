package com.example.sjsucampus.map;

import com.example.sjsucampus.util.Log;
import com.example.sjsucampus.util.Util;

import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.List;

/**
 * Created by chitoo on 10/22/16.
 */
public class Calculator {

    public static double distanceOf2Points(Point p1, Point p2) {
        return Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2));
    }

    public static Point logicalToPhysical(Point a, Point b, Point c, double lenAB, double lenBC, double lenX, double lenY) {
        Point point = new Point();
        point.x = new ExpressionBuilder("(Xa-Xb)*lenY/lenAB + (Xc-Xb)*lenX/lenBC + Xb")
                .variables("Xa", "Xb", "Xc", "lenAB", "lenBC", "lenX", "lenY")
                .build()
                .setVariable("Xa", a.x)
                .setVariable("Xb", b.x)
                .setVariable("Xc", c.x)
                .setVariable("lenAB", lenAB)
                .setVariable("lenBC", lenBC)
                .setVariable("lenX", lenX)
                .setVariable("lenY", lenY)
                .evaluate();
        point.y = new ExpressionBuilder("(Ya-Yb)*lenY/lenAB + (Yc-Yb)*lenX/lenBC + Yb")
                .variables("Ya", "Yb", "Yc", "lenAB", "lenBC", "lenX", "lenY")
                .build()
                .setVariable("Ya", a.y)
                .setVariable("Yb", b.y)
                .setVariable("Yc", c.y)
                .setVariable("lenAB", lenAB)
                .setVariable("lenBC", lenBC)
                .setVariable("lenX", lenX)
                .setVariable("lenY", lenY)
                .evaluate();
        return point;
    }

    public static boolean isInside(List<Point> vertexes, Point target) {

        Log.i("vertexes: ", Util.objToJson(vertexes));
        Log.i("target: ", Util.objToJson(target));
        boolean result = false;
        double x = target.x;
        double y = target.y;
        int i,j = vertexes.size() - 1;
        for(i = 0; i < vertexes.size(); i++){
            Point pi = vertexes.get(i);
            Point pj = vertexes.get(j);
            if((pi.y < y && pj.y >= y || pj.y < y && pi.y >= y) && (pi.x <= x || pj.x <= x)){
                if(pi.x + (y - pi.y)/(pj.y - pi.y) * (pj.x - pi.x) < x){
                    result = !result;
                }
            }
            j = i;
        }
        return result;
    }
}
