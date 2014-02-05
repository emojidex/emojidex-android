package org.genshin.emojidexandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ViewFlipper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initEmojidexEditor();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                openSettings(null);
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (viewFlipper.getCurrentView() == findViewById(R.id.emoji_layout))
        {
            outState.putCharSequence("text", emojiEditText.getText());
            outState.putString("view", "center");
        }
        else if (viewFlipper.getCurrentView() == findViewById(R.id.text_layout))
        {
            outState.putCharSequence("text", textEditText.getText());
            outState.putString("view", "right");
        }
        else
        {
            outState.putCharSequence("text", emojiHalfEditText.getText());
            outState.putString("view", "left");
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String nowView = savedInstanceState.getString("view");

        if (nowView.equals("center"))
        {
            emojiEditText.setText(savedInstanceState.getCharSequence("text"));
        }
        else if (nowView.equals("right"))
        {
            viewFlipper.showNext();
            textEditText.setText(savedInstanceState.getCharSequence("text"));
        }
        else
        {
            viewFlipper.showPrevious();
            emojiHalfEditText.setText(savedInstanceState.getCharSequence("text"));
            textHalfEditText.setText(deEmojify(savedInstanceState.getCharSequence("text")));
        }
    }


    /**
     * Emojidex
     */
    private Emojidex emojidex = null;

    private EditText emojiEditText;
    private EditText textEditText;
    private EditText emojiHalfEditText;
    private EditText textHalfEditText;
    private CustomTextWatcher emojiTextWatcher;
    private CustomTextWatcher textTextWatcher;
    private ViewFlipper viewFlipper;

    private void initEmojidexEditor()
    {
        if (emojidex == null)
            emojidex = new Emojidex(getApplicationContext());

        emojiEditText = (EditText)findViewById(R.id.emoji_edittext);
        textEditText = (EditText) findViewById(R.id.text_edittext);
        emojiHalfEditText = (EditText)findViewById(R.id.emoji_half_edittext);
        textHalfEditText = (EditText)findViewById(R.id.text_half_edittext);

        // detects input
        emojiTextWatcher = new CustomTextWatcher(emojiHalfEditText.getId());
        textTextWatcher = new CustomTextWatcher(textHalfEditText.getId());
        emojiHalfEditText.addTextChangedListener(emojiTextWatcher);
        emojiHalfEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                emojiHalfEditText.removeTextChangedListener(emojiTextWatcher);
                emojiHalfEditText.addTextChangedListener(emojiTextWatcher);
                textHalfEditText.removeTextChangedListener(textTextWatcher);
                return false;
            }
        });
        textHalfEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                textHalfEditText.removeTextChangedListener(textTextWatcher);
                textHalfEditText.addTextChangedListener(textTextWatcher);
                emojiHalfEditText.removeTextChangedListener(emojiTextWatcher);
                return false;
            }
        });

        // set viewFlipper action
        viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper_editor);
        viewFlipper.setOnTouchListener(new FlickTouchListener());
        // set default view
        viewFlipper.showNext();
    }

    private CharSequence emojify(final CharSequence cs)
    {
        return emojidex.emojify(cs);
    }

    private CharSequence deEmojify(final CharSequence cs)
    {
        return emojidex.deEmojify(cs);
    }

    private CharSequence toUnicodeString(final CharSequence cs)
    {
        return emojidex.toUnicodeString(cs);
    }

    public void shareData(View v)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");

            // set share data
            if (viewFlipper.getCurrentView() == findViewById(R.id.emoji_layout))
            {
                intent.putExtra(Intent.EXTRA_TEXT, toUnicodeString(emojiEditText.getText()).toString());
            }
            else if (viewFlipper.getCurrentView() == findViewById(R.id.text_layout))
            {
                intent.putExtra(Intent.EXTRA_TEXT, textEditText.getText().toString());
            }
            else
            {
                intent.putExtra(Intent.EXTRA_TEXT, toUnicodeString(emojiHalfEditText.getText()).toString());
            }

            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share)));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.d("hoge", "send error.");
        }
    }

    public void moveViewToRight(View v)
    {
        // right edge of the screen
        if (viewFlipper.getCurrentView() == findViewById(R.id.text_layout))
            return;

        // move emoji
        if (viewFlipper.getCurrentView() == findViewById(R.id.emoji_text_layout))
        {
            emojiEditText.setText(emojiHalfEditText.getText());
        }
        // move text and toUnicodeString
        else
        {
            textEditText.setText(toUnicodeString(emojiEditText.getText()));
        }

        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out));
        viewFlipper.showNext();
}

    public void moveViewToLeft(View v)
    {
        // left edge of the screen
        if (viewFlipper.getCurrentView() == findViewById(R.id.emoji_text_layout))
            return;

        // move text and emojify
        if (viewFlipper.getCurrentView() == findViewById(R.id.text_layout))
        {
            emojiEditText.setText(emojify(deEmojify(textEditText.getText())));
        }
        // move emoji and deEmojify
        else
        {
            emojiHalfEditText.setText(emojiEditText.getText());
            textHalfEditText.setText(deEmojify(emojiEditText.getText()));
        }

        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out));
        viewFlipper.showPrevious();
    }

    private class FlickTouchListener implements View.OnTouchListener
    {
        private float lastTouchX;
        private float currentX;

        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    currentX = event.getX();
                    if (lastTouchX + 30 < currentX)
                        moveViewToLeft(null);
                    if (lastTouchX > currentX + 30)
                        moveViewToRight(null);
                    break;
            }
            return true;
        }
    }

    private class CustomTextWatcher implements TextWatcher
    {
        private int inputEditTextId;

        public CustomTextWatcher(int id)
        {
            inputEditTextId = id;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            // exclude while converting the Japanese
            final Object[] spans = s.getSpans(0, s.length(), Object.class);
            for(Object span : spans)
                if(span.getClass().getName().equals("android.view.inputmethod.ComposingText"))
                    return;

            // conversion while input emoji
            if (inputEditTextId == R.id.emoji_half_edittext )
            {
                int oldPos = emojiHalfEditText.getSelectionStart();
                int oldTextLength = emojiHalfEditText.getText().length();

                emojiHalfEditText.removeTextChangedListener(emojiTextWatcher);
                emojiHalfEditText.setText(emojify(s));
                textHalfEditText.setText(deEmojify(s));
                emojiHalfEditText.addTextChangedListener(emojiTextWatcher);

                // adjustment cursor position
                int addTextLength = emojiHalfEditText.getText().length() - oldTextLength;
                int newPos = oldPos + addTextLength;
                if (newPos > emojiHalfEditText.getText().length())
                    newPos = emojiHalfEditText.getText().length();
                emojiHalfEditText.setSelection(newPos);
            }
            // conversion while input text
            else
            {
                int oldPos = textHalfEditText.getSelectionStart();
                int oldTextLength = textHalfEditText.getText().length();

                textHalfEditText.removeTextChangedListener(textTextWatcher);
                emojiHalfEditText.setText(emojify(s));
                textHalfEditText.setText(deEmojify(s));
                textHalfEditText.addTextChangedListener(textTextWatcher);

                // adjustment cursor position
                int addTextLength = textHalfEditText.getText().length() - oldTextLength;
                int newPos = oldPos + addTextLength;
                if (newPos > textHalfEditText.getText().length())
                    newPos = textHalfEditText.getText().length();
                textHalfEditText.setSelection(newPos);
            }
        }
    }

    /**
     * open settings view
     * @param v
     */
    public void openSettings(View v)
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}