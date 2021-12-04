package com.app.jussfun.external.imageview;

import android.content.Context;
import android.util.AttributeSet;

import com.app.jussfun.R;
import com.app.jussfun.external.imageview.shader.ShaderHelper;
import com.app.jussfun.external.imageview.shader.SvgShader;


public class DiamondImageView extends ShaderImageView {

    public DiamondImageView(Context context) {
        super(context);
    }

    public DiamondImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DiamondImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public ShaderHelper createImageViewHelper() {
        return new SvgShader(R.raw.imgview_diamond);
    }
}
