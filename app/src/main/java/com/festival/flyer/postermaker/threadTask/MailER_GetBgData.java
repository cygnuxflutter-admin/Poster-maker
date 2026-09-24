package com.festival.flyer.postermaker.threadTask;

import android.os.AsyncTask;

import com.festival.flyer.postermaker.model.MailER_BgImage;
import com.festival.flyer.postermaker.model.MailER_BgModel;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MailER_GetBgData extends AsyncTask<Void, Void, String> {

    private final MailER_PreferenceClass preferenceClass;
    private final JSONArray jsonArray;
    private final ArrayList<MailER_BgModel> posterCoArrayList = new ArrayList<>();
    private final OnGetCatDataListener onGetDataListener;
    public interface OnGetCatDataListener {
        void onGetDataComplete(ArrayList<MailER_BgModel> posterDataLists);

        void onError();
    }

    public MailER_GetBgData(MailER_PreferenceClass preferenceClass, JSONArray jsonArray, OnGetCatDataListener onGetDataListener) {
        this.preferenceClass = preferenceClass;
        this.jsonArray = jsonArray;
        this.onGetDataListener = onGetDataListener;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject arrayJSONObject = jsonArray.getJSONObject(i);
                String field_40 = arrayJSONObject.getString(preferenceClass.getDataType("field_40"));
                String field_41 = arrayJSONObject.getString(preferenceClass.getDataType("field_41"));
                ArrayList<MailER_BgImage> posterThumbFullArrayList = getPostThumb(arrayJSONObject);
                posterCoArrayList.add(new MailER_BgModel(field_40, field_41, posterThumbFullArrayList));
            }
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private ArrayList<MailER_BgImage> getPostThumb(JSONObject arrayJSONObject) {
        ArrayList<MailER_BgImage> bgImageArrayList = new ArrayList<>();
        try {
            JSONArray field_42 = arrayJSONObject.getJSONArray(preferenceClass.getDataType("field_42"));
            for (int j = 0; j < field_42.length(); j++) {
                JSONObject textJSONObject = field_42.getJSONObject(j);
                int field_43 = textJSONObject.getInt(preferenceClass.getDataType("field_43"));
                String field_44 = textJSONObject.getString(preferenceClass.getDataType("field_44"));
                String field_45 = textJSONObject.getString(preferenceClass.getDataType("field_45"));

                boolean isPremium = (j % preferenceClass.getInt("PremiumPostCount", 3) == 0);
                bgImageArrayList.add(new MailER_BgImage(field_43, field_44, field_45, isPremium));
            }
            return bgImageArrayList;
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
