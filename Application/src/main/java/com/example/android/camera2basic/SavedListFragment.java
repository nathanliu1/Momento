package com.example.android.camera2basic;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Shows a list of all available quotes.
 * <p/>
 * Created by Andreas Schrade on 14.12.2015.
 */
public class SavedListFragment extends ListFragment {

    private Callback callback = dummyCallback;

    /**
     * A callback interface. Called whenever a item has been selected.
     */
    public interface Callback {
        void onItemSelected(String id);
    }

    /**
     * A dummy no-op implementation of the Callback interface. Only used when no active Activity is present.
     */
    private static final Callback dummyCallback = new Callback() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new MyListAdapter());
        setHasOptionsMenu(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.i("selectedValue", ""+position);
    }

    /**
     * onAttach(Context) is not called on pre API 23 versions of Android.
     * onAttach(Activity) is deprecated but still necessary on older devices.
     */
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    /**
     * Deprecated on API 23 but still necessary for pre API 23 devices.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /**
     * Called when the fragment attaches to the context
     */
    protected void onAttachToContext(Context context) {
        if (!(context instanceof Callback)) {
            throw new IllegalStateException("Activity must implement callback interface.");
        }

        callback = (Callback) context;
    }

    private class MyListAdapter extends BaseAdapter {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("savedLocations",Context.MODE_PRIVATE);
        int savedCount = sharedPref.getInt("countSaved", 0);

        @Override
        public int getCount() {
            return savedCount;
        }

        @Override
        public Object getItem(int position) {
            return sharedPref.getString(position+"","");
        }

        @Override
        public long getItemId(int position) {return 2;}

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_saved_list, container, false);
            }
            String columnVal = sharedPref.getString(position+"", "");
            Log.i("columnVal", columnVal);
            String[] colVals = columnVal.split("\\|\\|");
            ((TextView) convertView.findViewById(R.id.article_title)).setText(colVals[0]);
            ((TextView)convertView.findViewById(R.id.article_subtitle)).setText(colVals[1]);
            Log.i("columnVal", colVals[2]);
            final ImageView img = (ImageView) convertView.findViewById(R.id.thumbnail);
            try {
                String afterDecode = URLDecoder.decode(colVals[2], "UTF-8");
                if (afterDecode.contains("content://")) {
                    Uri imagePicker = Uri.parse(afterDecode);
                    Glide.with(getActivity()).load(imagePicker).asBitmap().fitCenter().into(new BitmapImageViewTarget(img) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            img.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                } else {
                    Glide.with(getActivity()).load(afterDecode).asBitmap().fitCenter().into(new BitmapImageViewTarget(img) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            img.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    public SavedListFragment() {
    }
}
