package com.example.lijuanjuan.videoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * 按比例显示的TextureView
 */
public class ScaleTextureView extends TextureView {

    private int mScale;//宽高比

    public ScaleTextureView(Context context) {
        super(context);
    }

    public ScaleTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mScale > 0) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);

            if (width > height) {

            }
        }

    }
}
