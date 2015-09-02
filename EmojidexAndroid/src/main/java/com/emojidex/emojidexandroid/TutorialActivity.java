package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ViewFlipper;

public class TutorialActivity extends Activity {
    private int page;
    private ViewFlipper viewFlipper;
    private GestureDetector gestureDetector;

    private ImageButton nextButton;
    private ImageButton prevButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        createTutorial();
    }

    private void createTutorial() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        page = 0;

        nextButton = (ImageButton)findViewById(R.id.tutotial_next_button);
        prevButton = (ImageButton)findViewById(R.id.tutotial_prev_button);
        prevButton.setVisibility(View.INVISIBLE);

        // TODO: 英語版の時は？
        viewFlipper = (ViewFlipper)findViewById(R.id.tutorial_view_flipper);
        for (int i = 1; i <= 8; i++) {
            ImageView imageView = new ImageView(getApplicationContext());

            if (i <= 2) {
                int resourceId = getResources().getIdentifier("tutorial_0" + i, "drawable", getPackageName());
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
                imageView.setImageBitmap(bitmap);
            }

            viewFlipper.addView(imageView);
        }

        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float disX = e1.getX() - e2.getX();
                if (Math.abs(disX) < 100) return true;

                if (disX > 0) {
                    tutorialShowNext(null);
                } else {
                    tutorialShowPrevious(null);
                }

                return false;
            }
        });
    }

    public void tutorialShowNext(View v) {
        if (page == 7) return;

        page++;
        setImage();
        setButtonVisibility();

        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out));
        viewFlipper.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                releaseImage((ImageView) viewFlipper.getChildAt(page - 1));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        viewFlipper.showNext();
    }

    public void tutorialShowPrevious(View v) {
        if (page == 0) return;

        page--;
        setImage();
        setButtonVisibility();

        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out));
        viewFlipper.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                releaseImage((ImageView) viewFlipper.getChildAt(page + 1));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        viewFlipper.showPrevious();
    }

    private void setImage() {
        ImageView imageView = (ImageView)viewFlipper.getChildAt(page);
        releaseImage(imageView);

        int resourceId = getResources().getIdentifier("tutorial_0" + (page + 1), "drawable", getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        imageView.setImageBitmap(bitmap);
    }

    private void setButtonVisibility() {
        if (page == 0) {
            nextButton.setVisibility(View.VISIBLE);
            prevButton.setVisibility(View.INVISIBLE);
        } else if (page == 7) {
            nextButton.setVisibility(View.INVISIBLE);
            prevButton.setVisibility(View.VISIBLE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            prevButton.setVisibility(View.VISIBLE);
        }
    }

    public void closeTutorial(View v) {
        releaseImages();

        finish();
    }

    private void releaseImage(ImageView imageView) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
    }

    private void releaseImages() {
        for (int i = 0; i < 8; i++) {
            ImageView imageView = (ImageView) viewFlipper.getChildAt(i);
            releaseImage(imageView);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseImages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setImage();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return gestureDetector.onTouchEvent(event);
    }
}
