package ui.utils;

import android.os.Build;
import android.view.View;
import android.view.animation.Animation;

/**
 * Utility to manage animations in the seleted {@link View}.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class AnimationUtils {

    /**
     * Applies a fade in animation and set the visibility in
     * {@link View#VISIBLE}.
     * @param view view to animate.
     */
    public static void fadeInView(final View view) {
        if (view.getVisibility() != View.VISIBLE) {
            cancelAnimation(view);
            view.setVisibility(View.VISIBLE);
            Animation animation = android.view.animation.AnimationUtils
                    .loadAnimation(view.getContext(), android.R.anim.fade_in);
            animation.setFillEnabled(true);
            animation.setFillBefore(true);
            animation.setFillAfter(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            view.setAnimation(animation);
            animation.start();
        }
    }

    /**
     * Applies a fade out animation and set the visibility in
     * {@link View#GONE}.
     * @param view view to animate.
     */
    public static void fadeOutView(final View view) {
        if (view.getVisibility() == View.VISIBLE) {
            cancelAnimation(view);
            Animation animation = android.view.animation.AnimationUtils
                    .loadAnimation(view.getContext(), android.R.anim.fade_out);
            animation.setFillEnabled(true);
            animation.setFillBefore(true);
            animation.setFillAfter(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.GONE);
                    view.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            view.setAnimation(animation);
            animation.start();
        }
    }

    /**
     * Cancel any previous animation.
     * @param view the view.
     */
    public static void cancelAnimation(View view) {
        Animation animation = view.getAnimation();
        if (animation != null) {
            animation.reset();
            animation.cancel();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            view.animate().cancel();
        }
        view.clearAnimation();
    }

    /** This class cannot be instantiated. */
    private AnimationUtils() {
    }
}
