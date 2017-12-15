package com.emojidex.emojidexandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.emojidex.emojidexandroid.view.HScrollView;
import com.emojidex.emojidexandroid.view.VScrollView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Yoshida on 2017/12/13.
 */

public class PhotoEditorActivity extends Activity {
    private static final int NONE = -1;
    private static final int MOVE = 0;
    private static final int SCALE = 1;
    private static final int ROLL = 2;
    private int mode = NONE;

    private static final int REQUEST_EXTERNAL_STORAGE = 1000;
    private static final int SELECT_PHOTO = 1001;
    private static final String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_APN_SETTINGS
    };

    private VScrollView vScrollView;
    private HScrollView hScrollView;
    private FrameLayout frameLayout;
    private ImageView baseImageView;

    private ImageButton moveButton;
    private ImageButton scaleButton;
    private ImageButton rollButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);

        initViews();
        setBaseImage(getIntent().getData());
    }

    private void initViews()
    {
        vScrollView = (VScrollView) findViewById(R.id.photo_editor_vscroll);
        hScrollView = (HScrollView) findViewById(R.id.photo_editor_hscroll);
        frameLayout = (FrameLayout) findViewById(R.id.photo_editor_frame);
        baseImageView = (ImageView) findViewById(R.id.photo_editor_base_image);

        moveButton = (ImageButton) findViewById(R.id.photo_editor_move_button);
        scaleButton = (ImageButton) findViewById(R.id.photo_editor_scale_button);
        rollButton = (ImageButton) findViewById(R.id.photo_editor_roll_button);
    }

    /**
     * Set base image.
     * @param data photo data.
     */
    private void setBaseImage(Uri data)
    {
        if (data == null) return;

        try
        {
            InputStream is = getContentResolver().openInputStream(data);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            baseImageView.setImageBitmap(bitmap);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(PhotoEditorActivity.this, getString(R.string.open_error), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Set emoji to photo editor frame.
     * @param v button.
     */
    public void setEmoji(View v)
    {
        final GestureImageView image = new GestureImageView(getApplicationContext());

        // TODO: test
        final Emoji emoji = Emojidex.getInstance().getEmoji("heart");
        final Drawable drawable = emoji.getDrawable(EmojiFormat.toFormat(getString(R.string.emoji_format_catalog)));
        image.setImageDrawable(drawable);
        image.setX(100);
        image.setY(100);

        frameLayout.addView(image);
    }

    /**
     * Set mode - MOVE
     * @param v button.
     */
    public void setModeToMove(View v)
    {
        mode = mode == MOVE ? NONE : MOVE;
        setButtonBackGround();
    }

    /**
     * Set mode - SCALE
     * @param v button.
     */
    public void setModeToScale(View v)
    {
        mode = mode == SCALE ? NONE : SCALE;
        setButtonBackGround();
    }

    /**
     * Set mode - ROLL
     * @param v button.
     */
    public void setModeToRoll(View v)
    {
        mode = mode == ROLL ? NONE : ROLL;
        setButtonBackGround();
    }

    /**
     * Set buttons background color.
     */
    private void setButtonBackGround()
    {
        moveButton.setBackgroundColor(Color.TRANSPARENT);
        scaleButton.setBackgroundColor(Color.TRANSPARENT);
        rollButton.setBackgroundColor(Color.TRANSPARENT);
        if (mode == MOVE) moveButton.setBackgroundColor(getResources().getColor(R.color.primary));
        if (mode == SCALE) scaleButton.setBackgroundColor(getResources().getColor(R.color.primary));
        if (mode == ROLL) rollButton.setBackgroundColor(getResources().getColor(R.color.primary));
    }

    /**
     * Prepare save image.
     * @param v button.
     */
    public void prepareSaveImage(View v)
    {

        if (checkExternalStoragePermission())
        {
            saveImage();
        }
        else
        {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * Save image.
     */
    public void saveImage()
    {
        // Image has not been added.
        if (frameLayout.getChildCount() == 1) return;

        // Prepare canvas.
        Bitmap bitmap = Bitmap.createBitmap(baseImageView.getWidth(), baseImageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw base image.
        canvas.drawBitmap(((BitmapDrawable) baseImageView.getDrawable()).getBitmap(),
                          baseImageView.getImageMatrix(), null);

        // Draw emoji image.
        for (int i = 1; i < frameLayout.getChildCount(); i++)
        {
            ImageView image = (ImageView) frameLayout.getChildAt(i);
            canvas.drawBitmap(((BitmapDrawable) image.getDrawable()).getBitmap(), image.getMatrix(), null);
        }

        // Create save folder.
        File saveFolder = new File(EmojidexFileUtils.getLocalSealkitSaveFolder());
        if (!saveFolder.exists())
        {
            if (!saveFolder.mkdir())
            {
                Toast.makeText(PhotoEditorActivity.this,
                               getResources().getString(R.string.failed_make_folder), Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Save image.
        String filePath = EmojidexFileUtils.getLocalSealkitSaveFolder() + "/" + System.currentTimeMillis() + ".png";
        try
        {
            OutputStream os = getContentResolver().openOutputStream(Uri.fromFile(new File(filePath)));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            if (os != null) os.close();

            Toast.makeText(PhotoEditorActivity.this,
                           getResources().getString(R.string.save_image_success) + "\n" + filePath,
                           Toast.LENGTH_LONG).show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(PhotoEditorActivity.this,
                           getResources().getString(R.string.save_image_failed), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Clear the added images.
     * @param v button.
     */
    public void clearImage(View v)
    {
        if (frameLayout.getChildCount() == 1) return;

        for (int i = frameLayout.getChildCount(); i > 1; i--)
        {
            frameLayout.removeViewAt(i - 1);
        }
    }

    /**
     * prepare new image.
     * @param v button.
     */
    public void prepareNewImage(View v)
    {
        if (baseImageView.getDrawable() != null)
        {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(PhotoEditorActivity.this);
            dialog.setTitle(R.string.new_image);
            dialog.setMessage(R.string.new_image_message);
            dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    newImage();
                }
            });
            dialog.setNegativeButton(R.string.no, null);
            dialog.show();
        }
        else
        {
            newImage();
        }
    }

    /**
     * Remove all image and set new image.
     */
    private void newImage()
    {
        clearImage(null);
        baseImageView.setImageBitmap(null);

        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PHOTO);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (vScrollView.isContains((int)event.getX(), (int)event.getY()))
        {
            vScrollView.onTouch(event);
            hScrollView.onTouch(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            saveImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != SELECT_PHOTO || resultCode != Activity.RESULT_OK || data == null)
        {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        setBaseImage(data.getData());
    }

    /**
     * Check permission for save image.
     * @return true or false
     */
    private boolean checkExternalStoragePermission()
    {
        int permission = ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    public class GestureImageView extends android.support.v7.widget.AppCompatImageView
    {
        private float oldX, oldY;

        public GestureImageView(Context context)
        {
            super(context);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            final int action = event.getAction();
            vScrollView.requestDisallowInterceptTouchEvent(true);
            hScrollView.requestDisallowInterceptTouchEvent(true);

            final float newX = event.getRawX();
            final float newY = event.getRawY();

            switch (action)
            {
                case MotionEvent.ACTION_MOVE:
                    final float x = newX - oldX;
                    final float y = newY - oldY;

                    switch (mode)
                    {
                        case MOVE:
                            setX(getX() + x);
                            setY(getY() + y);

                            break;

                        case SCALE:
                            final float scale = Math.abs(x) > Math.abs(y) ? x : y;
                            setScaleX(getScaleX() + (scale * 0.01f));
                            setScaleY(getScaleY() + (scale * 0.01f));

                            break;

                        case ROLL:
                            final float roll = Math.abs(x) > Math.abs(y) ? x : y;
                            setRotation(getRotation() + (roll * 0.1f));

                            break;
                    }
            }

            oldX = newX;
            oldY = newY;

            return true;
        }
    }
}
