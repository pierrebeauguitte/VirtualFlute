package com.dit.pierre.virtualflute;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class TButton extends View {

    private int fingerValue;
    public boolean pressed;
    Paint paint;
    float dim;
    float density;
    float center;

    public TButton(Context context) {
        super(context);
        init();
    }

    public TButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        this.pressed = false;
        paint = new Paint();
        paint.setColor(0xfffed325);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;
        center = 350 * density;
        dim = 100 * density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, center, dim, paint);
    }

    public void setFingerValue(int v) {
        this.fingerValue = v;
    }

    public int getFingerValue() {
        return this.fingerValue;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

}
