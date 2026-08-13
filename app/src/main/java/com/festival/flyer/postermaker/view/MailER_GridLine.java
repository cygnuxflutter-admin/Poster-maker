package com.festival.flyer.postermaker.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

public class MailER_GridLine extends AppCompatImageView {

    boolean gnvhmvh = false;

    boolean drthdh = false;

    public Matrix cgcdg = new Matrix();

    private Paint xfncn;

    private Paint gvncg;

    private Paint fvgncf;

    private Paint fhdffjhcf;

    private View xfdfh;

    public MailER_GridLine(Context context) {
        super(context);
        m28297a(context);
    }

    public MailER_GridLine(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        m28297a(context);
    }

    public MailER_GridLine(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        m28297a(context);
    }
    public int m27290a(Context context, float f) {
        context.getResources();
        return (int) (Resources.getSystem().getDisplayMetrics().density * f);
    }

    private void m28297a(Context context) {
        this.xfncn = new Paint();
        this.xfncn.setColor(-1);
        this.xfncn.setStrokeWidth((float) m27290a(context, 2.0f));
        this.xfncn.setPathEffect(new DashPathEffect(new float[]{5.0f, 5.0f}, 1.0f));
        this.xfncn.setStyle(Style.STROKE);
        this.gvncg = new Paint();
        this.gvncg.setAntiAlias(true);
        this.gvncg.setStrokeWidth(3.2f);
        this.gvncg.setColor(-65536);
        this.gvncg.setAlpha(255);
        this.gvncg.setStyle(Style.FILL_AND_STROKE);
        this.gvncg.setAntiAlias(true);
        this.fvgncf = new Paint();
        this.fvgncf.setAlpha(255);
        this.fvgncf.setStrokeWidth(2.0f);
        this.fvgncf.setColor(Color.argb(50, 74, 255, 255));
        this.fvgncf.setStyle(Style.FILL_AND_STROKE);
        this.fvgncf.setPathEffect(new DashPathEffect(new float[]{10.0f, 5.0f}, 0.0f));
        this.fhdffjhcf = new Paint();
        this.fhdffjhcf.setAlpha(255);
        this.fhdffjhcf.setStrokeWidth(2.0f);
        this.fhdffjhcf.setColor(-65536);
        this.fhdffjhcf.setStyle(Style.FILL_AND_STROKE);
        this.fhdffjhcf.setPathEffect(new DashPathEffect(new float[]{10.0f, 5.0f}, 0.0f));
    }

    private void m28298a(Canvas canvas) {
        int i = 0;
        if (this.gnvhmvh) {
            canvas.drawLine((float) (canvas.getWidth() / 2), 0.0f, (float) (canvas.getWidth() / 2), (float) canvas.getHeight(), this.gvncg);
            this.gnvhmvh = false;
        }
        if (this.drthdh) {
            canvas.drawLine(0.0f, (float) (canvas.getHeight() / 2), (float) canvas.getWidth(), (float) (canvas.getHeight() / 2), this.gvncg);
            this.drthdh = false;
        }
        float width = ((float) canvas.getWidth()) / 10.0f;
        float height = ((float) canvas.getHeight()) / 10.0f;
        int i2 = 0;
        while (true) {
            float f = (float) i2;
            if (f > 10.0f) {
                break;
            }
            float f2 = f * width;
            canvas.drawLine(f2, 0.0f, f2, (float) canvas.getHeight(), this.fvgncf);
            i2++;
        }
        while (true) {
            float f3 = (float) i;
            if (f3 <= 10.0f) {
                float f4 = f3 * height;
                canvas.drawLine(0.0f, f4, (float) canvas.getWidth(), f4, this.fvgncf);
                i++;
            } else {
                return;
            }
        }
    }

    public void setGuildLine(boolean z, boolean z2) {
        this.gnvhmvh = z;
        this.drthdh = z2;
        invalidate();
    }

    public Matrix getMatrix() {
        return this.cgcdg;
    }

    public View getViewRotation() {
        return this.xfdfh;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        getWidth();
        getHeight();
        m28298a(canvas);
    }

    public void setInRotate(boolean z) {
    }

    public void setMatrix(Matrix matrix) {
        this.cgcdg = matrix;
    }

    public void setViewRotation(View view) {
        this.xfdfh = view;
    }
}
