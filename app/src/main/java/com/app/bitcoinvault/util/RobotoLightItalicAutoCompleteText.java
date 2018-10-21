package com.app.bitcoinvault.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by admin on 30-08-2017.
 */

public class RobotoLightItalicAutoCompleteText extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    public RobotoLightItalicAutoCompleteText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoLightItalicAutoCompleteText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoLightItalicAutoCompleteText(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Roboto-LightItalic.ttf");
        setTypeface(tf);
    }

}

