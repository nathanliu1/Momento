package com.example.android.camera2basic;

/**
 * Created by jiliu on 10/20/2016.
 */

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.*;

public class MatchingToLocation {
    public static HashMap<String,String> sendForMatching(ArrayList<HashMap<String, String>> articleData, List<HashMap<String, String>> clarifaiData) {
        List<HashMap<String, String>> predicted_locations = new ArrayList<HashMap<String, String>>();
        Double maxDist = Double.parseDouble(articleData.get(articleData.size()-1).get("distance"));
        for (int i = 0; i < articleData.size(); i++) {
            Double cnt = 0.0000;

            for (int j = 0; j < clarifaiData.size(); j++) {
                Pattern pattern = Pattern.compile("\\b" + clarifaiData.get(j).get("name") + "\\b");
                Matcher matcher = pattern.matcher(articleData.get(i).get("extract"));

                while (matcher.find()) {
                    cnt += Double.parseDouble(clarifaiData.get(j).get("value"));
                }
            }
            HashMap<String, String> predicted_data = new HashMap<String, String>();
            cnt *= (maxDist/Double.parseDouble(articleData.get(i).get("distance")));
            predicted_data.put("location", articleData.get(i).get("pageid"));
            predicted_data.put("likelihood", cnt.toString());
            predicted_locations.add(predicted_data);
        }

        Double ranking = 0.0000;
        int decision = 0;
        for (int i = 0; i < predicted_locations.size(); i++) {
            if (Double.parseDouble(predicted_locations.get(i).get("likelihood")) >= ranking) {
                ranking = Double.parseDouble(predicted_locations.get(i).get("likelihood"));
                decision = i;
            }
        }
        Log.i("ranks: ", predicted_locations.toString());
        return articleData.get(decision);
    }
}
