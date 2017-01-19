package ar.com.nicolasquartieri.widget.recyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Handle the space between {@link RecyclerView} columns and rows.
 *
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

    private int mItemOffset;

    /**
	 * Constructor.
	 *
	 * @param itemOffset The space distance between columns and rows. Could be 0 (zero).
	 */
    public ItemOffsetDecoration(int itemOffset) {
        mItemOffset = itemOffset;
    }

    /**
     * Constructor.
     *
     * @param context The {@link Context}, can't be null.
     * @param itemOffsetId The offset id.
     */
    public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
        this(context.getResources().getDimensionPixelSize(itemOffsetId));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
            RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
    }
}