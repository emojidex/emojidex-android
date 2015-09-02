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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class TutorialActivity extends Activity {
    private int page;
    private ViewFlipper viewFlipper;
    private Animation rightInAnimation;
    private Animation rightOutAnimation;
    private Animation leftInAnimation;
    private Animation leftOutAnimation;
    private RadioGroup radioGroup;
    private GestureDetector gestureDetector;

    private TextView textView;
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
        // TODO: Dropboxの画像を追加、差し替え。それに伴ってヘッダーテキストも修正。
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

        rightInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in);
        rightOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
        rightOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                releaseImage((ImageView) viewFlipper.getChildAt(page + 1));
                setIndicator(page + 1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        leftInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
        leftOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out);
        leftOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                releaseImage((ImageView) viewFlipper.getChildAt(page - 1));
                setIndicator(page - 1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

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

        textView = (TextView)findViewById(R.id.tutorial_header);
        textView.setText(R.string.tutorial_header_1);

        // Instead of page indicator.
        radioGroup = (RadioGroup)findViewById(R.id.tutorial_indicator);
        for (int i = 0; i < 8; i++) {
            RadioButton button = new RadioButton(getApplicationContext());
            button.setId(i);
            button.setEnabled(false);
            radioGroup.addView(button);
        }
        radioGroup.check(0);
        radioGroup.getChildAt(0).setEnabled(true);
    }

    public void tutorialShowNext(View v) {
        if (page == 7) return;

        page++;
        setImage();
        setText();
        setButtonVisibility();

        viewFlipper.setInAnimation(rightInAnimation);
        viewFlipper.setOutAnimation(leftOutAnimation);
        viewFlipper.showNext();
    }

    public void tutorialShowPrevious(View v) {
        if (page == 0) return;

        page--;
        setImage();
        setText();
        setButtonVisibility();

        viewFlipper.setInAnimation(leftInAnimation);
        viewFlipper.setOutAnimation(rightOutAnimation);
        viewFlipper.showPrevious();
    }

    private void setImage() {
        ImageView imageView = (ImageView)viewFlipper.getChildAt(page);
        releaseImage(imageView);

        int resourceId = getResources().getIdentifier("tutorial_0" + (page + 1), "drawable", getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        imageView.setImageBitmap(bitmap);
    }

    private void setText() {
        int resourceId = getResources().getIdentifier("tutorial_header_" + (page + 1), "string", getPackageName());
        textView.setText(resourceId);
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

    private void setIndicator(int disablePage) {
        radioGroup.getChildAt(disablePage).setEnabled(false);
        radioGroup.getChildAt(page).setEnabled(true);
        radioGroup.check(page);
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
