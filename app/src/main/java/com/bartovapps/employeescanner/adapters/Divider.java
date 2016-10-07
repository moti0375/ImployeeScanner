package com.bartovapps.employeescanner.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.bartovapps.employeescanner.R;


/**
 * Created by BartovMoti on 09/03/16.
 */
public class Divider extends RecyclerView.ItemDecoration {

    public static final String TAG = Divider.class.getSimpleName();
    private Drawable mDivider;
    int mOrientation;

    public Divider(Context context, int orientation) {
        mDivider = ContextCompat.getDrawable(context, R.drawable.divider);
        if (orientation != LinearLayoutManager.VERTICAL) {
            throw new IllegalArgumentException("This item decoration can be used in horizintal layout only");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        Log.i(TAG, "onDraw: ");
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawHorizontalDivider(c, parent, state);
        }
    }

    private void drawHorizontalDivider(Canvas c, RecyclerView parent, RecyclerView.State state) {
        Log.i(TAG, "drawHorizontalDivider");
            int left, top, right, bottom;

            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();

            int count = parent.getChildCount();

            for (int i = 0; i < count; i++) {
                View current = parent.getChildAt(i);

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) current.getLayoutParams();
                    top = current.getTop() - params.topMargin;
                    bottom = top + mDivider.getIntrinsicHeight();

                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);

                    Log.i(TAG, "drawHorizontalDivider done!");
            }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

    }
}
