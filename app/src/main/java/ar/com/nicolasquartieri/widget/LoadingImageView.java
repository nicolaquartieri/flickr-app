package ar.com.nicolasquartieri.widget;

import java.util.Locale;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import ar.com.nicolasquartieri.R;
import ar.com.nicolasquartieri.ui.utils.AnimationUtils;

/**
 * This view provides an image view and a loading.
 */
public class LoadingImageView extends FrameLayout {
    /** Array to map scale type and index. */
    private static final ImageView.ScaleType[] sScaleTypeArray = {
            ImageView.ScaleType.MATRIX,
            ImageView.ScaleType.FIT_XY,
            ImageView.ScaleType.FIT_START,
            ImageView.ScaleType.FIT_CENTER,
            ImageView.ScaleType.FIT_END,
            ImageView.ScaleType.CENTER,
            ImageView.ScaleType.CENTER_CROP,
            ImageView.ScaleType.CENTER_INSIDE
    };
    /** Image view. */
    private ImageView mImageView;
    /** Loading view. */
    private ProgressBar mLoadingView;

    /** Constructor. */
    public LoadingImageView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    /** Constructor. */
    public LoadingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    /** Constructor. */
    public LoadingImageView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    /** Constructor. */
    @SuppressWarnings("unused")
    @TargetApi(21)
    public LoadingImageView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /** Initialize the view. */
    private void init(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        // Get attributes.
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LoadingImageView, defStyleAttr, defStyleRes);
        final int scaleTypeIndex = a.getInt(
                R.styleable.LoadingImageView_scaleType, -1);
        final Drawable srcDrawable = a.getDrawable(
                R.styleable.LoadingImageView_src);
        a.recycle();

        // Create image view.
        mImageView = new ImageView(context);
        mImageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mImageView);

        // Set attributes.
        if (scaleTypeIndex >= 0) {
            mImageView.setScaleType(sScaleTypeArray[scaleTypeIndex]);
        } else {
            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        if (srcDrawable != null) {
            mImageView.setImageDrawable(srcDrawable);
        }

        // Create loading view.
        mLoadingView = new ProgressBar(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mLoadingView.setLayoutParams(params);
        mLoadingView.setIndeterminate(true);
        mLoadingView.setVisibility(GONE);
        addView(mLoadingView);
    }

    public void setImageUrl(String url, boolean isRounded) {
        mLoadingView.setVisibility(VISIBLE);
        mImageView.setVisibility(INVISIBLE);

        BitmapTypeRequest<String> requestCreator = Glide.with(getContext())
                .load(url).asBitmap();

        switch (mImageView.getScaleType()) {
            case FIT_CENTER:
                requestCreator.fitCenter();
                break;
            case CENTER_CROP:
                requestCreator.centerCrop();
                break;
        }

        requestCreator.listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                AnimationUtils.fadeInView(mImageView);
                AnimationUtils.fadeOutView(mLoadingView);
                Log.d("GLIDE", String.format(Locale.ROOT,
                        "onException(%s, %s, %s, %s)", e, model, target, isFirstResource), e);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                AnimationUtils.fadeInView(mImageView);
                AnimationUtils.fadeOutView(mLoadingView);
                Log.d("GLIDE", String.format(Locale.ROOT,
                        "onResourceReady(%s, %s, %s, %s, %s)", resource, model, target,
                        isFromMemoryCache, isFirstResource));
                return false;
            }
        });

        if (isRounded) {
            requestCreator.centerCrop().into(new BitmapImageViewTarget(mImageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    mImageView.setImageDrawable(circularBitmapDrawable);
                }
            });
        } else {
            requestCreator.into(mImageView);
        }
    }

    /**
     * Load the image from given url.
     * @param url the image url.
     */
    public void setImageUrl(String url) {
        setImageUrl(url, false);
    }

    /**
     * @see ImageView#setImageDrawable(Drawable)
     */
    public void setImageDrawable(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }
}
