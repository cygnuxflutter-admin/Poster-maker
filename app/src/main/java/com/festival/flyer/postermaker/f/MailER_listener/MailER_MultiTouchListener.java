package com.festival.flyer.postermaker.f.MailER_listener;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

import com.festival.flyer.postermaker.components.MailER_AutofitTextRel;
import com.festival.flyer.postermaker.f.MailER_ResizableStickerView;

public class MailER_MultiTouchListener implements OnTouchListener {
    private static final int INVALID_POINTER_ID = -1;
    public boolean isRotateEnabled = true;
    public boolean isRotationEnabled = false;
    public boolean isTranslateEnabled = true;
    public float maximumScale = 8.0f;
    public float minimumScale = 0.5f;
    Bitmap bitmap;
    boolean bt = false;
    GestureDetector gd = null;
    private TouchCallbackListener listener = null;
    private int mActivePointerId = INVALID_POINTER_ID;
    private float mPrevX;
    private float mPrevY;
    private MailER_ScaleGestureDetector mScaleGestureDetector = new MailER_ScaleGestureDetector(new ScaleGestureListener());

    private static float adjustAngle(float degrees) {
        if (degrees > 180.0f) {
            return degrees - 360.0f;
        }
        if (degrees < -180.0f) {
            return degrees + 360.0f;
        }
        return degrees;
    }

    private static void adjustTranslation(View view, float deltaX, float deltaY) {
        float[] deltaVector = new float[]{deltaX, deltaY};
        view.getMatrix().mapVectors(deltaVector);
        view.setTranslationX(view.getTranslationX() + deltaVector[0]);
        view.setTranslationY(view.getTranslationY() + deltaVector[1]);
    }

    private static void computeRenderOffset(View view, float pivotX, float pivotY) {
        if (view.getPivotX() != pivotX || view.getPivotY() != pivotY) {
            float[] prevPoint = new float[]{0.0f, 0.0f};
            view.getMatrix().mapPoints(prevPoint);
            view.setPivotX(pivotX);
            view.setPivotY(pivotY);
            float[] currPoint = new float[]{0.0f, 0.0f};
            view.getMatrix().mapPoints(currPoint);
            float offsetY = currPoint[1] - prevPoint[1];
            view.setTranslationX(view.getTranslationX() - (currPoint[0] - prevPoint[0]));
            view.setTranslationY(view.getTranslationY() - offsetY);
        }
    }

    private void m28126a(View view, float f, float f2) {
        float f3;
        boolean z = true;
        float[] fArr = {f, f2};
        view.getMatrix().mapVectors(fArr);
        float translationY = view.getTranslationY() + fArr[1];
        view.setTranslationX(view.getTranslationX() + fArr[0]);
        view.setTranslationY(translationY);
        float mainWidth = 0.0f;
        float mainHeight = 0.0f;
        if (view instanceof MailER_ResizableStickerView) {
            MailER_ResizableStickerView resizableStickerView = (MailER_ResizableStickerView) view;
            mainWidth = resizableStickerView.getMainWidth();
            mainHeight = resizableStickerView.getMainHeight();
        }
        if (view instanceof MailER_AutofitTextRel) {
            MailER_AutofitTextRel autofitTextRel = (MailER_AutofitTextRel) view;
            mainWidth = autofitTextRel.getMainWidth();
            mainHeight = autofitTextRel.getMainHeight();
        }

        float width = (float) (view.getWidth() / 2);
        float height = (float) (view.getHeight() / 2);
        int y = (int) (view.getY() + height);
        float x = (float) ((int) (view.getX() + width));
        float f4 = mainWidth / 2.0f;
        float f5 = (float) ((int) (Resources.getSystem().getDisplayMetrics().density * 5.0f));
        if (x <= f4 - f5 || x >= f4 + f5) {
            z = false;
        } else {
            view.setX(f4 - width);
        }
        float f6 = (float) y;
        float f7 = mainHeight / 2.0f;
        if (f6 <= f7 - f5 || f6 >= f5 + f7) {
            f3 = 0.0f;
        } else {
            view.setY(f7 - height);
            f3 = Float.MIN_VALUE;
        }
        if (z && f3 != 0.0f) {
            if (listener != null) {
                listener.onMidXY(view);
            }
        } else if (z) {
            if (listener != null) {
                listener.onMidX(view);
            }
        } else if (f3 != 0.0f) {
            if (listener != null) {
                listener.onMidY(view);
            }
        } else {
            if (listener != null) {
                listener.onXY(view);
            }
        }
        float rotation = view.getRotation();
        if (Math.abs(90.0f - Math.abs(rotation)) <= 5.0f) {
            rotation = rotation > 0.0f ? 90.0f : -90.0f;
        }
        if (Math.abs(0.0f - Math.abs(rotation)) <= 5.0f) {
            rotation = rotation > 0.0f ? 0.0f : -0.0f;
        }
        if (Math.abs(180.0f - Math.abs(rotation)) <= 5.0f) {
            rotation = rotation > 0.0f ? 180.0f : -180.0f;
        }
        view.setRotation(rotation);
    }

    public MailER_MultiTouchListener setGestureListener(GestureDetector gd) {
        this.gd = gd;
        return this;
    }

    public MailER_MultiTouchListener setOnTouchCallbackListener(TouchCallbackListener l) {
        this.listener = l;
        return this;
    }

    public MailER_MultiTouchListener enableRotation(boolean b) {
        this.isRotationEnabled = b;
        return this;
    }

    public MailER_MultiTouchListener setMinScale(float f) {
        this.minimumScale = f;
        return this;
    }

    private void move(View view, TransformInfo info) {
        if (this.isRotationEnabled) {
            view.setRotation(adjustAngle(view.getRotation() + info.deltaAngle));
        }
    }

    public boolean handleTransparency(View view, MotionEvent event) {
        try {
            if (event.getAction() == 2 && this.bt) {
                return true;
            }
            if (event.getAction() == 1 && this.bt) {
                this.bt = false;
                if (this.bitmap != null) {
                    this.bitmap.recycle();
                }
                return true;
            }
            int[] posXY = new int[2];
            view.getLocationOnScreen(posXY);
            int rx = (int) (event.getRawX() - ((float) posXY[0]));
            int ry = (int) (event.getRawY() - ((float) posXY[1]));
            float r = view.getRotation();
            Matrix mat = new Matrix();
            mat.postRotate(-r);
            float[] point = new float[]{(float) rx, (float) ry};
            mat.mapPoints(point);
            rx = (int) point[0];
            ry = (int) point[1];
            if (event.getAction() == 0) {
                this.bt = false;
                view.setDrawingCacheEnabled(true);
                this.bitmap = Bitmap.createBitmap(view.getDrawingCache());
                rx = (int) (((float) rx) * (((float) this.bitmap.getWidth()) / (((float) this.bitmap.getWidth()) * view.getScaleX())));
                ry = (int) (((float) ry) * (((float) this.bitmap.getWidth()) / (((float) this.bitmap.getHeight()) * view.getScaleX())));
                view.setDrawingCacheEnabled(false);
            }
            if (rx < 0 || ry < 0 || rx > this.bitmap.getWidth() || ry > this.bitmap.getHeight()) {
                return false;
            }
            boolean b = this.bitmap.getPixel(rx, ry) == 0;
            if (event.getAction() != 0) {
                return b;
            }
            this.bt = b;
            return b;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        this.mScaleGestureDetector.onTouchEvent(view, event);
        RelativeLayout rl = (RelativeLayout) view.getParent();
        if (this.gd != null) {
            this.gd.onTouchEvent(event);
        }
        if (!this.isTranslateEnabled) {
            return true;
        }
        int action = event.getAction();
        int pointerIndex;
        switch (event.getActionMasked() & action) {
            case MotionEvent.ACTION_DOWN:
                if (rl != null) {
                    rl.requestDisallowInterceptTouchEvent(true);
                }
                if (this.listener != null) {
                    this.listener.onTouchCallback(view);
                }
//                view.bringToFront();
                if (view instanceof MailER_AutofitTextRel) {
                    ((MailER_AutofitTextRel) view).setBorderVisibility(true);
                }
                this.mPrevX = event.getX();
                this.mPrevY = event.getY();
                this.mActivePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_UP:
                this.mActivePointerId = INVALID_POINTER_ID;
                if (this.listener != null) {
                    this.listener.onTouchUpCallback(view);
                }
                float rotation = view.getRotation();
                if (Math.abs(90.0f - Math.abs(rotation)) <= 5.0f) {
                    if (rotation > 0.0f) {
                        rotation = 90.0f;
                    } else {
                        rotation = -90.0f;
                    }
                }
                if (Math.abs(0.0f - Math.abs(rotation)) <= 5.0f) {
                    if (rotation > 0.0f) {
                        rotation = 0.0f;
                    } else {
                        rotation = -0.0f;
                    }
                }
                if (Math.abs(180.0f - Math.abs(rotation)) <= 5.0f) {
                    if (rotation > 0.0f) {
                        rotation = 180.0f;
                    } else {
                        rotation = -180.0f;
                    }
                }
                view.setRotation(rotation);
                Log.i("testing", "Final Rotation : " + rotation);
                break;
            case MotionEvent.ACTION_MOVE:
                if (rl != null) {
                    rl.requestDisallowInterceptTouchEvent(true);
                }
                if (this.listener != null) {
                    this.listener.onTouchMoveCallback(view);
                }
                pointerIndex = event.findPointerIndex(this.mActivePointerId);
                if (pointerIndex != INVALID_POINTER_ID) {
                    float currX = event.getX(pointerIndex);
                    float currY = event.getY(pointerIndex);
                    if (!this.mScaleGestureDetector.isInProgress()) {
                        m28126a(view, currX - this.mPrevX, currY - this.mPrevY);
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                this.mActivePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                pointerIndex = (65280 & action) >> 8;
                if (event.getPointerId(pointerIndex) == this.mActivePointerId) {
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    this.mPrevX = event.getX(newPointerIndex);
                    this.mPrevY = event.getY(newPointerIndex);
                    this.mActivePointerId = event.getPointerId(newPointerIndex);
                    break;
                }
                break;
        }
        return true;
    }

    public interface TouchCallbackListener {
        void onTouchCallback(View view);

        void onTouchMoveCallback(View view);

        void onTouchUpCallback(View view);

        void onMidX(View view);

        void onMidXY(View view);

        void onMidY(View view);

        void onXY(View view);

    }

    private class ScaleGestureListener extends MailER_ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float mPivotX;
        private float mPivotY;
        private MailER_Vector2D mPrevSpanVector;

        private ScaleGestureListener() {
            this.mPrevSpanVector = new MailER_Vector2D();
        }

        public boolean onScaleBegin(View view, MailER_ScaleGestureDetector detector) {
            this.mPivotX = detector.getFocusX();
            this.mPivotY = detector.getFocusY();
            this.mPrevSpanVector.set(detector.getCurrentSpanVector());
            return true;
        }

        public boolean onScale(View view, MailER_ScaleGestureDetector detector) {
            float focusX;
            float f = 0.0f;
            TransformInfo info = new TransformInfo();
            info.deltaAngle = MailER_MultiTouchListener.this.isRotateEnabled ? MailER_Vector2D.getAngle(this.mPrevSpanVector, detector.getCurrentSpanVector()) : 0.0f;
            if (MailER_MultiTouchListener.this.isTranslateEnabled) {
                focusX = detector.getFocusX() - this.mPivotX;
            } else {
                focusX = 0.0f;
            }
            info.deltaX = focusX;
            if (MailER_MultiTouchListener.this.isTranslateEnabled) {
                f = detector.getFocusY() - this.mPivotY;
            }
            info.deltaY = f;
            info.pivotX = this.mPivotX;
            info.pivotY = this.mPivotY;
            info.minimumScale = MailER_MultiTouchListener.this.minimumScale;
            info.maximumScale = MailER_MultiTouchListener.this.maximumScale;
            MailER_MultiTouchListener.this.move(view, info);
            return false;
        }
    }

    private class TransformInfo {
        public float deltaAngle;
        public float deltaScale;
        public float deltaX;
        public float deltaY;
        public float maximumScale;
        public float minimumScale;
        public float pivotX;
        public float pivotY;

        private TransformInfo() {
        }
    }
}
