package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.emojidex.emojidexandroid.view.HScrollView;
import com.emojidex.emojidexandroid.view.VScrollView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Yoshida on 2017/12/13.
 */

public class PhotoEditorActivity extends Activity {
    private VScrollView vScrollView;
    private HScrollView hScrollView;
    private FrameLayout frameLayout;
    private ImageView baseImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);

        setViews();
        setPhoto();
    }

    private void setViews() {
        vScrollView = (VScrollView) findViewById(R.id.photo_editor_vscroll);
        hScrollView = (HScrollView) findViewById(R.id.photo_editor_hscroll);
        frameLayout = (FrameLayout) findViewById(R.id.photo_editor_frame);
        baseImageView = (ImageView) findViewById(R.id.photo_editor_base_image);
    }

    private void setPhoto() {
        Intent intent = getIntent();
        if (intent.getData() == null) showError();

        try
        {
            InputStream is = getContentResolver().openInputStream(intent.getData());
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            baseImageView.setImageBitmap(bitmap);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showError();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (vScrollView.isContains((int)event.getX(), (int)event.getY())) {
            vScrollView.onTouch(event);
            hScrollView.onTouch(event);
        }
        return super.onTouchEvent(event);
    }

    private void showError() {
        Toast.makeText(getApplicationContext(), getString(R.string.open_error), Toast.LENGTH_LONG).show();
        finish();
    }
}
