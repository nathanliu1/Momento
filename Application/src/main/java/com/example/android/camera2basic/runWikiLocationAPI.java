package com.example.android.camera2basic;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class runWikiLocationAPI extends AsyncTask<Location, Void, ArrayList<HashMap<String, String>>> {

    public AsyncResponse delegate = null;
    JSONArray articles = null;
    ArrayList<HashMap<String, String>> articleList = new ArrayList<HashMap<String, String>>();

    @Override
    protected ArrayList<HashMap<String, String>> doInBackground(Location... params) {
        return JSONload(params[0]);
    }

    public ArrayList<HashMap<String, String>> JSONload(Location thisloc) {

        String APIurl = "https://en.wikipedia.org/w/api.php?action=query&list=geosearch&gsradius=10000&gscoord="
                + thisloc.getLatitude() + "%7C" + thisloc.getLongitude()
                + "&format=json&gslimit=4";

        JSONParser jParser = new JSONParser();

        JSONObject json = jParser.getJSONFromUrl(APIurl);
        try {
            json = (JSONObject) json.get("query");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            articles = json.getJSONArray("geosearch");
            Log.i("Returned Data:", articles.toString());
            Log.i("Returned Data:", "it is size" + articles.getString(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < articles.length(); i++) {
            try {
                JSONObject value = articles.getJSONObject(i);
//              APIurl = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts|images&exintro=&explaintext=&pageids=" + value.getString("pageid").toString();
                APIurl = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&pageids=" + value.getString("pageid").toString();
                JSONObject dataReturn = jParser.getJSONFromUrl(APIurl);
                dataReturn = (JSONObject) dataReturn.get("query");
                dataReturn = (JSONObject) dataReturn.get("pages");
                dataReturn = (JSONObject) dataReturn.get(value.getString("pageid"));
                Log.i("extract", dataReturn.getString("extract"));
                HashMap<String,String> locationsaved = new HashMap<String, String>();
                locationsaved.put("lat",value.getString("lat"));
                locationsaved.put("long",value.getString("lon"));
                locationsaved.put("pageid",value.getString("pageid"));
                locationsaved.put("title", value.getString("title"));
                locationsaved.put("distance", value.getString("dist"));
                locationsaved.put("extract", dataReturn.getString("extract"));
//                JSONArray dataImages = (JSONArray) dataReturn.get("images");
//                for(int j=0; j<dataImages.length(); j++){
//                    String imageValue = dataImages.getJSONObject(j).getString("title");
//                    locationsaved.put(j+"", imageValue);
//                }
//                locationsaved.put("imageCount",dataImages.length()-1+"" );
                articleList.add(locationsaved);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return articleList;
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String,String>> articleList) {
        if (articleList == null || articleList.isEmpty()) {
            Log.e("WikiLocationAPI","the wikilocationapi failed");
            return;
        }
        delegate.processFinish(articleList);
    }
}