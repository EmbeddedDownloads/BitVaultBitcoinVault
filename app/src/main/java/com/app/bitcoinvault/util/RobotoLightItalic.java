package com.app.bitcoinvault.util;

/**
 * Font file used for displaying the text in RobotoLightItalic font.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class RobotoLightItalic extends android.support.v7.widget.AppCompatTextView {

    public RobotoLightItalic(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoLightItalic(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoLightItalic(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Roboto-LightItalic.ttf");
        setTypeface(tf);
    }

}

