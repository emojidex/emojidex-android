package com.emojidex.emojidexandroid;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by kou on 15/01/20.
 */
public class LoadingDialog extends AbstractDialog {
    private final int fadeDuration = 300;

    private View maskView;
    private View loadingView;

    /**
     * Construct object.
     * @param context       context.
     */
    public LoadingDialog(Context context) {
        super(context);
    }

    public void close()
    {
        if(loadingView.getVisibility() != View.INVISIBLE) {
            loadingView.setVisibility(View.INVISIBLE);

            final AlphaAnimation fadeOutAnim = new AlphaAnimation(1.0f, 0.0f);
            fadeOutAnim.setDuration(fadeDuration);
            fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // nop
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    LoadingDialog.super.dismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // nop
                }
            });
            maskView.startAnimation(fadeOutAnim);
        }
    }

    @Override
    public void dismiss()
    {
        // nop
    }

    @Override
    protected View createContentView(Context context) {
        maskView = new View(context);
        maskView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        maskView.setBackgroundColor(0xff000000);
        maskView.setAlpha(0.6f);

        final TextView textView = new TextView(context);
        textView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ));
        textView.setBackgroundColor(0xff000000);
        textView.setTextColor(0xffffffff);
        textView.setText("Loading...");
        textView.setPadding(20, 10, 20, 10);
        loadingView = textView;

        final FrameLayout contentView = new FrameLayout(context);
        contentView.addView(maskView);
        contentView.addView(loadingView);

        final AlphaAnimation fadeInAnim = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnim.setDuration(fadeDuration);
        maskView.startAnimation(fadeInAnim);

        return contentView;
    }
}
