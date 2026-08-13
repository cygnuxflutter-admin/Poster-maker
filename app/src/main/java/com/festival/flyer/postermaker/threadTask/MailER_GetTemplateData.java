package com.festival.flyer.postermaker.threadTask;

import android.os.AsyncTask;

import com.festival.flyer.postermaker.model.MailER_PosterModel;
import com.festival.flyer.postermaker.model.MailER_PosterImage;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MailER_GetTemplateData extends AsyncTask<Void, Void, String> {

    private final MailER_PreferenceClass preferenceClass;
    private final JSONArray jsonArray;
    private final ArrayList<MailER_PosterModel> posterCoArrayList = new ArrayList<>();
    private final OnGetCatDataListener onGetDataListener;

    public interface OnGetCatDataListener {
        void onGetDataComplete(ArrayList<MailER_PosterModel> posterModels);

        void onError();
    }

    public MailER_GetTemplateData(MailER_PreferenceClass preferenceClass, JSONArray jsonArray, OnGetCatDataListener onGetDataListener) {
        this.preferenceClass = preferenceClass;
        this.jsonArray = jsonArray;
        this.onGetDataListener = onGetDataListener;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject arrayJSONObject = jsonArray.getJSONObject(i);
                String field_46 = arrayJSONObject.getString(preferenceClass.getDataType("field_46"));
                String field_6 = arrayJSONObject.getString(preferenceClass.getDataType("field_6"));
                String field_5 = arrayJSONObject.getString(preferenceClass.getDataType("field_5"));
                ArrayList<MailER_PosterImage> posterThumbFullArrayList = getPostThumb(arrayJSONObject);
                if (posterThumbFullArrayList != null && posterThumbFullArrayList.size() > 0)
                    posterCoArrayList.add(new MailER_PosterModel(field_46, field_6, field_5, posterThumbFullArrayList));
            }
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private ArrayList<MailER_PosterImage> getPostThumb(JSONObject arrayJSONObject) {
        ArrayList<MailER_PosterImage> posterThumbFullArrayList = new ArrayList<>();
        try {
            JSONArray field_7 = arrayJSONObject.getJSONArray(preferenceClass.getDataType("field_7"));
            for (int j = 0; j < field_7.length(); j++) {
                JSONObject jsonObject = field_7.getJSONObject(j);
                String field_10 = jsonObject.getString(preferenceClass.getDataType("field_10"));
                String field_11 = jsonObject.getString(preferenceClass.getDataType("field_11"));
                String field_8 = jsonObject.getString(preferenceClass.getDataType("field_8"));

//                if (j != 0) {
//                    if ((j) % preferenceClass.getInt("rv_count", 4) == 0) {
//                        posterThumbFullArrayList.add(null);
//                    }
//                }
//                if (j != 0) {
//                    if (!isFirstShow) {
//                        if ((j) % preferenceClass.getInt("First_rv_count", 4) == 0) {
//                            posterThumbFullArrayList.add(null);
//                            isFirstShow = true;
//                        }
//                    } else {
//                        if ((j) % preferenceClass.getInt("rv_count", 4) == 0) {
//                            posterThumbFullArrayList.add(null);
//                        }
//                    }
//                }

                posterThumbFullArrayList.add(new MailER_PosterImage(field_10, field_11, field_8));
            }
            return posterThumbFullArrayList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result.equals("success")) {
            onGetDataListener.onGetDataComplete(posterCoArrayList);
        } else {
            onGetDataListener.onError();
        }
    }
}
