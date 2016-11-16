/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2basic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.dto.prediction.Prediction;

import static android.R.attr.path;

public class CameraActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AsyncResponse, AsyncResponseClarifai {

    File imageData;
    Bitmap imageBitmap;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Boolean isBitmap;
    String imagePath;

    Boolean wikiDone = false;
    Boolean clarifaiDone = false;

    runWikiLocationAPI wikilocation =new runWikiLocationAPI();
    RunClarifaiAPI clarifaitask =new RunClarifaiAPI();

    ArrayList<HashMap<String, String>> returnedWikiData = new ArrayList<HashMap<String, String>>();
    List<HashMap<String,String>> allClarifaiValuesOutput = new ArrayList<HashMap<String, String>>();

    private static final int MY_PERMISSION_VALUES = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestAllPermissions();
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
    }

    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA}
                ,MY_PERMISSION_VALUES);
    }

    public void setImageData(File imageData) {
        this.imageData = imageData;
    }

    public File getImageData() {
        return imageData;
    }

    public void setImageDataBM(Bitmap imageData) {
        this.imageBitmap = imageData;
    }

    public Bitmap getImageDataBM() {
        return imageBitmap;
    }

    public void setImageDataPath(String imageData) {
        this.imagePath = imageData;
    }

    public String getImageDataPath() {
        return imagePath;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "You're connected!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed to connect...", Toast.LENGTH_SHORT).show();
    }

    public void getLocationPerformWikiAPI(boolean isBitmap) {
        this.isBitmap = isBitmap;
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED )
        {
            if (!isBitmap) {
                try {
                    byte[] byteArray = FileUtils.readFileToByteArray(getImageData());
                    clarifaitask.delegate = this;
                    clarifaitask.execute(byteArray);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }  else {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap bmp = getImageDataBM();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                clarifaitask.delegate = this;
                clarifaitask.execute(byteArray);
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            wikilocation.delegate = this;
            wikilocation.execute(mLastLocation);
        }
    }

    @Override
    public void processFinish(ArrayList<HashMap<String, String>> output)  {
        returnedWikiData = output;
        wikiDone = true;
        if (clarifaiDone && !returnedWikiData.isEmpty() && !allClarifaiValuesOutput.isEmpty()) {
            HashMap<String,String> decision = new HashMap<String,String>();
            decision = MatchingToLocation.sendForMatching(returnedWikiData,allClarifaiValuesOutput);
            Log.i("decision",decision.toString());
            Intent i = new Intent(this, DisplayActivity.class);
            i.putExtra("title",decision.get("title").toString());
            i.putExtra("pageid",decision.get("pageid").toString());
            i.putExtra("extract",decision.get("extract").toString());
            i.putExtra("lat", decision.get("lat").toString());
            i.putExtra("long", decision.get("long").toString());
            i.putExtra("distance",Double.parseDouble(decision.get("distance").toString()));
            if (!isBitmap) {
                Log.i("iamgelocation",getImageData().getAbsolutePath());
                i.putExtra("image", getImageData().getAbsolutePath());
            } else {
                i.putExtra("image", getImageDataPath());
            }
            this.startActivity(i);
        }
    }

    @Override
    public void processFinish(List<HashMap<String, String>> output) {
        allClarifaiValuesOutput = output;
        clarifaiDone = true;
        if (wikiDone && !returnedWikiData.isEmpty() && !allClarifaiValuesOutput.isEmpty()) {
            HashMap<String,String> decision = new HashMap<String,String>();
            decision = MatchingToLocation.sendForMatching(returnedWikiData,allClarifaiValuesOutput);
            Intent i = new Intent(this, DisplayActivity.class);
            i.putExtra("title",decision.get("title"));
            i.putExtra("pageid",decision.get("pageid"));
            i.putExtra("extract",decision.get("extract"));
            i.putExtra("lat", decision.get("lat"));
            i.putExtra("long", decision.get("long"));
            i.putExtra("distance",Double.parseDouble(decision.get("distance")));
            if (!isBitmap) {
                i.putExtra("image", getImageData().getAbsolutePath());
            } else {
                i.putExtra("image", getImageDataPath());
            }
            this.startActivity(i);
        }
    }
}
