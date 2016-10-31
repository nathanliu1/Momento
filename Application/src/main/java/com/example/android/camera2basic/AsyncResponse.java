package com.example.android.camera2basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jiliu on 10/20/2016.
 */

public interface AsyncResponse {
    void processFinish(ArrayList<HashMap<String,String>> output);
}