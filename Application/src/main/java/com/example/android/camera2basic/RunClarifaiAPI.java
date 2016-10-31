package com.example.android.camera2basic;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

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

/**
 * Created by jiliu on 10/20/2016.
 */

public class RunClarifaiAPI extends AsyncTask<byte[], Void, List<HashMap<String, String>>> {
    public AsyncResponseClarifai delegate = null;
    String appID = "9d9ZYpY5S3DpvFR8Dgj4qYXLRJBThU0L8hO_s_89";
    String appSecret = "15pB-4UI5YrfS9WaXollkZR5SuqI7A8ZDo6iYEpA";
    final ClarifaiClient client = new ClarifaiBuilder(appID, appSecret).buildSync();

    List<HashMap<String,String>> allClarifaiValuesOutput = new ArrayList<HashMap<String, String>>();
    @Nullable
    private Model<Concept> model = client.getDefaultModels().generalModel();

    @Override
    protected List<HashMap<String, String>> doInBackground(byte[]... params) {
        final ClarifaiResponse<List<ClarifaiOutput<Concept>>> predictions =
                model.predict()
                        .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(params[0])))
                        .executeSync();
        if(predictions.isSuccessful())

        {
            for (int i = 0; i < 15; i++) {
                final Concept prediction = predictions.get().get(0).data().get(i);
                if (prediction != null) {
                    final Concept concept = prediction;
                    final String name = concept.name();
                    HashMap<String, String> clarifaiOutput = new HashMap<String, String>();
                    clarifaiOutput.put("name", concept.name());
                    clarifaiOutput.put("value", String.valueOf(concept.value()));
                    allClarifaiValuesOutput.add(clarifaiOutput);
                }
            }
        }
        return allClarifaiValuesOutput;
    }

    @Override
    protected void onPostExecute(List<HashMap<String,String>> predictions) {
        if (predictions == null || predictions.isEmpty()) {
            Log.e("WikiLocationAPI","the wikilocationapi failed");
            return;
        }
        delegate.processFinish(allClarifaiValuesOutput);
    }
}
