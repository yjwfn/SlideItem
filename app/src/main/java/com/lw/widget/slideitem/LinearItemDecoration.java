package com.lw.widget.slideitem;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by yjwfn on 15-9-14.
 */
public class LinearItemDecoration extends RecyclerView.ItemDecoration{

    private Paint   mPaint;
    private int     mColor;

    public LinearItemDecoration(@ColorInt int color) {
        mPaint = new Paint();
        mPaint.setColor(color);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

    }

    public LinearItemDecoration() {
        this(0);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, parent.getResources().getDisplayMetrics());
        RecyclerView.LayoutManager  layoutManager = parent.getLayoutManager();
        View childView ;
        RecyclerView.LayoutParams   layoutParams;
        int childCount = layoutManager.getChildCount();
        Rect  drawRect = new Rect();
        int top,left,right,bottom;

        for(int childIndex = 0 ; childIndex < childCount - 1; childIndex++){
            childView = layoutManager.getChildAt(childIndex);
            layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();
            top = childView.getBottom() + layoutParams.bottomMargin;
            left = childView.getLeft() + layoutParams.leftMargin + childView.getPaddingLeft();
            right = childView.getRight() - childView.getPaddingRight() - layoutParams.rightMargin;
            bottom = top + height;
            drawRect.set(left,top,  right, bottom);
            c.drawRect(drawRect, mPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, parent.getResources().getDisplayMetrics());
        outRect.set(0, 0, 0,  height);
    }
}
