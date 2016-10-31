package com.example.android.camera2basic;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jiliu on 10/20/2016.
 */

public interface AsyncResponseClarifai {
    void processFinish(List<HashMap<String,String>> output);
}