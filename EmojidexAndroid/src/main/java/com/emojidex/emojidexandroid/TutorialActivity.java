package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
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
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        createTutorial();
    }

    private void createTutorial() {
        page = 0;

        nextButton = (ImageButton)findViewById(R.id.tutotial_next_button);
        prevButton = (ImageButton)findViewById(R.id.tutotial_prev_button);
        prevButton.setVisibility(View.INVISIBLE);

        // TODO: 英語版の時の画像
        viewFlipper = (ViewFlipper)findViewById(R.id.tutorial_view_flipper);
        for (int i = 1; i <= 8; i++) {
            ImageView imageView = new ImageView(getApplicationContext());

            if (i == 1) {
                int resourceId = getResources().getIdentifier("tutorial_01", "drawable", getPackageName());
                bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
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
                if (Math.abs(disX) < 100)
                    return true;

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
        if (page < 7) {
            page++;
            setImage();
            setButtonVisibility();

            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in));
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out));
            viewFlipper.showNext();

            ((ImageView)viewFlipper.getChildAt(page - 1)).setImageBitmap(null);
        }
    }

    public void tutorialShowPrevious(View v) {
        if (page > 0) {
            page--;
            setImage();
            setButtonVisibility();

            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in));
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out));
            viewFlipper.showPrevious();

            ((ImageView) viewFlipper.getChildAt(page + 1)).setImageBitmap(null);
        }
    }

    private void setImage() {
        recycleBitmap();

        ImageView imageView = (ImageView)viewFlipper.getChildAt(page);
        int resourceId = getResources().getIdentifier("tutorial_0" + (page + 1), "drawable", getPackageName());
        bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
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
        recycleBitmap();

        // TODO: MainActivityに戻る
    }

    private void recycleBitmap() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        recycleBitmap();
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
