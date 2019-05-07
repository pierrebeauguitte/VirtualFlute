package com.dit.pierre.virtualflute;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TestButton extends View {

    private int fingerValue;
    public boolean pressed;
    Paint paint;
    float dim;
    float density;
    float center;

    public TestButton(Context context) {
        super(context);
        init();
    }

    public TestButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        this.pressed = false;
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
