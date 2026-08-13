package com.festival.flyer.postermaker.controller;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.festival.flyer.postermaker.R;
import com.festival.flyer.postermaker.activities.MailER_MainSecurity;
import com.festival.flyer.postermaker.fragment.MailER_TemplateFragment;
import com.festival.flyer.postermaker.fragment.MailER_TemplatePagerAdapter;
import com.festival.flyer.postermaker.model.MailER_PosterModel;
import com.festival.flyer.postermaker.threadTask.MailER_GetTemplateData;
import com.festival.flyer.postermaker.utils.MailER_MaterialDialogUtils;
import com.festival.flyer.postermaker.utils.MailER_PreferenceClass;
import com.festival.flyer.postermaker.view.MailER_PagerSlidingTabStrip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MailER_TemplateSelectionController {

    private final Activity activity;
    private final FragmentManager supportFragmentManager;
    private final MailER_PreferenceClass preferenceClass;
    private MaterialDialog materialDialog;

    public MailER_TemplateSelectionController(Activity activity, FragmentManager supportFragmentManager, MailER_PreferenceClass preferenceClass) {
        this.activity = activity;
        this.supportFragmentManager = supportFragmentManager;
        this.preferenceClass = preferenceClass;
        loadTemplates();
    }

    private void loadTemplates() {
        startMaterialDialog();
        getTemplateThumb(preferenceClass.getDataType("field_0"));
    }

    public void getTemplateThumb(final String key) {
        String requestUrl = preferenceClass.getDataType("field_1") + preferenceClass.getDataType("field_34") + preferenceClass.getDataType("field_35");
        Log.e("---API_DATA---", "--- REQUEST START (Template Thumb) ---");
        Log.e("---API_DATA---", "URL: " + requestUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl, response -> {
            Log.e("---API_DATA---", "--- RESPONSE START (Template Thumb) ---");
            Log.e("---API_DATA---", "URL: " + requestUrl);
            Log.e("---API_DATA---", "Response: " + response);
            try {
                Log.d("xgdgdg", "getTemplateThumb: " + response);
                JSONObject jsonObject = new JSONObject(response);
                int error = jsonObject.getInt(preferenceClass.getDataType("field_3"));
                if (error == 0 || error == 404) {
                    dismissMaterialDialog();
                    MailER_MaterialDialogUtils.getInstance().errorDialog2(activity, activity.getResources().getString(R.string.something_went_wrong));
                    return;
                }
                if (error == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray(preferenceClass.getDataType("field_4"));
                    if (jsonArray.length() == 0) {
                        MailER_MaterialDialogUtils.getInstance().errorDialog2(activity, activity.getResources().getString(R.string.something_went_wrong));
                    } else {
                        MailER_TemplateFragment.TempisFirstShow = false;
                        new MailER_GetTemplateData(preferenceClass, jsonArray, new MailER_GetTemplateData.OnGetCatDataListener() {
                            @Override
                            public void onGetDataComplete(ArrayList<MailER_PosterModel> posterDataLists) {
                                setPagerAdapter(posterDataLists);
                            }

                            @Override
                            public void onError() {
                                dismissMaterialDialog();
                                MailER_MaterialDialogUtils.getInstance().errorDialog(activity, activity.getResources().getString(R.string.something_went_wrong));
                            }
                        }).execute();
                    }
                }
            } catch (Exception e) {
                dismissMaterialDialog();

                try {

                    String text11 = null;
                    if (preferenceClass.getDecryptionType() == 0) {
                        byte[] octets = Base64.decode(response, Base64.URL_SAFE);
                        String text_base = new String(octets, StandardCharsets.UTF_8);
                        byte[] octets1 = Base64.decode(text_base, Base64.URL_SAFE);
                        text11 = new String(octets1, StandardCharsets.UTF_8);
                    } else if (preferenceClass.getDecryptionType() == 1) {
                        MailER_MainSecurity decrypted = MailER_MainSecurity.decrypt(preferenceClass.getDataType("main_key"), response);
                        text11 = decrypted.getData();
                    }

                    MailER_MaterialDialogUtils.getInstance().errorDialog(activity, text11);
                    e.printStackTrace();
                } catch (Exception e1) {
                    MailER_MaterialDialogUtils.getInstance().errorDialog(activity, e.getMessage());
                    e.printStackTrace();
                }
            }
        }, error -> {
            Log.e("---API_DATA---", "--- ERROR (Template Thumb) ---");
            Log.e("---API_DATA---", "URL: " + requestUrl);
            Log.e("---API_DATA---", "Error: " + error.getMessage());
            dismissMaterialDialog();
            MailER_MaterialDialogUtils.getInstance().errorDialog(activity, error.getMessage());
            error.printStackTrace();

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> postMap = new HashMap<>();
                postMap.put(preferenceClass.getDataType("field_47"), key);
                postMap.put(preferenceClass.getDataType("field_48"), "1");
                postMap.put(preferenceClass.getDataType("field_46"), "0");
                postMap.put(preferenceClass.getDataType("field_8"), "0");
                Log.e("---API_DATA---", "Params: " + postMap);
                return postMap;
            }
        };

        int MY_SOCKET_TIMEOUT_MS = 300000;

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(activity).add(stringRequest);
    }

    private void setPagerAdapter(ArrayList<MailER_PosterModel> posterModel) {
        MailER_TemplateFragment.TempisFirstShow = false;
        MailER_PagerSlidingTabStrip tabs = activity.findViewById(R.id.pagerSlidingTabStrip);
        ViewPager viewPager = activity.findViewById(R.id.viewPager);
        viewPager.setAdapter(new MailER_TemplatePagerAdapter(supportFragmentManager, posterModel));

        tabs.setViewPager(viewPager);
        dismissMaterialDialog();
    }

    public void startMaterialDialog() {
        materialDialog = MailER_MaterialDialogUtils.getInstance().createAnimationDialog(activity);
        Objects.requireNonNull(materialDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        materialDialog.setCancelable(false);
        materialDialog.show();
    }

    public void dismissMaterialDialog() {
        if (materialDialog != null && materialDialog.isShowing())
            materialDialog.dismiss();
    }

}
