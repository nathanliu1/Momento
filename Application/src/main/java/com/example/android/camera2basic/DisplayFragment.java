package com.example.android.camera2basic;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;


/**
 * Shows the quote detail page.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class DisplayFragment extends Fragment implements View.OnClickListener {

    String title;
    String pageid;
    String extract;
    String lat;
    String longitude;
    Double distance;
    String pathUrl;
    MapView mMapView;
    GoogleMap googleMap;
    FloatingActionButton mFloatingAction;
    CollapsingToolbarLayout collapsingToolbar;
    Toolbar toolbar;
    String savedFavorite;
    Boolean isSaved = false;


    public static DisplayFragment newInstance() {
        return new DisplayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_display, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        collapsingToolbar = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((DisplayActivity) getActivity()).setToolbar(toolbar);

        title = getArguments().getString("title");
        pageid = getArguments().getString("pageid");
        extract = getArguments().getString("extract");
        distance = getArguments().getDouble("distance");
        pathUrl = getArguments().getString("image");
        longitude = getArguments().getString("long");
        lat = getArguments().getString("lat");

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng locationMapped = new LatLng(Double.parseDouble(lat), Double.parseDouble(longitude));
                googleMap.addMarker(new MarkerOptions().position(locationMapped).title(title).snippet("You are "+distance+" meters away from here"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(locationMapped).zoom(15).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });


        return rootView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        TextView titleView = (TextView)view.findViewById(R.id.author);
        titleView.setText(title);
        mFloatingAction = (FloatingActionButton) view.findViewById(R.id.appbarbutton);
        collapsingToolbar.setTitle(title);
        TextView extractView = (TextView)view.findViewById(R.id.quote);
        extractView.setText(extract);
        ImageView backdropImg = (ImageView) view.findViewById(R.id.backdrop);
        Glide.with(this).load(pathUrl).centerCrop().into(backdropImg);
        view.findViewById(R.id.appbarbutton).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.appbarbutton: {
                if (!isSaved) {
                    isSaved = true;
                    mFloatingAction.setImageResource(R.drawable.exit_drawable_icon);
                    SharedPreferences sharedPref = getActivity().getSharedPreferences("savedLocations", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    savedFavorite = title + "||" + extract + "||" + pathUrl + "||" + lat + "||" + longitude;
                    int savedCount = sharedPref.getInt("countSaved", 0);
                    Log.i("saved value", savedCount + "");
                    savedCount++;
                    editor.putString("" + (savedCount - 1), savedFavorite);
                    editor.putInt("countSaved", savedCount);
                    editor.commit();
                    Toast.makeText(getActivity(), "This location has been saved", Toast.LENGTH_SHORT).show();
                    Log.i("saved value", savedFavorite);
                    break;
                } else {
                    isSaved = false;
                    mFloatingAction.setImageResource(R.drawable.heart_drawable_icon);
                    SharedPreferences sharedPref = getActivity().getSharedPreferences("savedLocations", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    int savedCount = sharedPref.getInt("countSaved", 0);
                    savedCount--;
                    editor.remove(savedCount+"");
                    editor.putInt("countSaved", savedCount);
                    editor.commit();
                    Toast.makeText(getActivity(), "This location has been removed", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

        }
    }
}
