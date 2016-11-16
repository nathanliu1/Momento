package com.example.android.camera2basic;


import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class PreviewFragment extends Fragment implements View.OnClickListener {

    private ImageView mImageView;

    private File imageData;

    private ViewSwitcher switcher;

    private ImageButton sendProcessing;

    private RelativeLayout picPreview;

    Boolean isBitmap=false;

    Location mLastLocation;



    public PreviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        view.findViewById(R.id.backToCamera).setOnClickListener(this);
        view.findViewById(R.id.sendForProcessing).setOnClickListener(this);
        mImageView = (ImageView) view.findViewById(R.id.image);
        sendProcessing = (ImageButton) view.findViewById(R.id.sendForProcessing);
        switcher = (ViewSwitcher) view.findViewById(R.id.switcher);
        picPreview = (RelativeLayout) view.findViewById(R.id.picturePreview);
        imageData =  ((CameraActivity) getActivity()).getImageData();
        if (imageData == null) {
            isBitmap = true;
            Bitmap b = ((CameraActivity) getActivity()).getImageDataBM();
            picPreview.setBackgroundColor(Color.parseColor("#000000"));
            mImageView.setImageBitmap(b);
        } else {
            isBitmap = false;
//            picPreview.setBackgroundColor(Color.parseColor("#000000"));
//            Bitmap b = BitmapFactory.decodeFile(((CameraActivity) getActivity()).getImageData().getPath());
//            mImageView.setImageBitmap(b);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backToCamera: {
                Camera2BasicFragment Camera2BasicFragment = new Camera2BasicFragment();
                FragmentManager transaction = getFragmentManager();
                transaction.beginTransaction().replace(R.id.picturePreview, Camera2BasicFragment)
                        .commit();
                break;
            }
            case R.id.sendForProcessing: {
                switcher.setDisplayedChild(1);
                mImageView.setVisibility(GONE);
                sendProcessing.setClickable(false);
                ((CameraActivity) getActivity()).getLocationPerformWikiAPI(isBitmap);
                break;
            }
        }
    }
}
