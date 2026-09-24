package com.festival.flyer.postermaker.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

public class CurvedBottomNavView extends RelativeLayout {

    public CurvedBottomNavView(Context context) {
        super(context);
        init();
    }

    public CurvedBottomNavView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurvedBottomNavView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        
        // Corner radius for top left and top right of the nav bar (20dp)
        float cr = 20 * density;
        
        // FAB margin (distance between FAB and the cutout)
        float fabMargin = 8 * density;
        
        // FAB corner radius (23dp for a 46dp circular FAB)
        float fabCornerRadius = 23 * density;
        
        // Vertical offset of the cradle. 
        // Our FAB overlaps by 10dp. Total FAB is 46dp. 
        // So the FAB center is 23 - 10 = 13dp below the top edge.
        float cradleVerticalOffset = 13 * density;

        BottomAppBarTopEdgeTreatment topEdge = new BottomAppBarTopEdgeTreatment(
                fabMargin,
                fabCornerRadius,
                cradleVerticalOffset
        );
        topEdge.setFabDiameter(46 * density);

        ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel.Builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, cr)
                .setTopRightCorner(CornerFamily.ROUNDED, cr)
                .setTopEdge(topEdge)
                .build();

        MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
        shapeDrawable.setTint(Color.WHITE);
        shapeDrawable.setPaintStyle(android.graphics.Paint.Style.FILL);
        
        // Enable shadows
        shapeDrawable.setShadowCompatibilityMode(MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
        shapeDrawable.setElevation(16 * density);
        shapeDrawable.setShadowColor(Color.parseColor("#33000000")); // Subtle shadow

        setBackground(shapeDrawable);
        
        // Remove hardware clipping to ensure shadow and custom edges draw perfectly
        setClipChildren(false);
        setClipToPadding(false);
    }
}
