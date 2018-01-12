package com.emojidex.emojidexandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
 * PhotoEditorActivity
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

    private static final String LOCAL_SAVE_PATH =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/emojidex-photo-editor";

    private VScrollView vScrollView;
    private HScrollView hScrollView;
    private FrameLayout frameLayout;
    private ImageView baseImageView;

    private ImageButton moveButton;
    private ImageButton scaleButton;
    private ImageButton rollButton;

    private EditText editText;
    private TextWatcher textWatcher;

    private Emojidex emojidex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);

        initialize();
        imeEnableCheck();
        setBaseImage(getIntent());
    }

    private void initialize()
    {
        emojidex = Emojidex.getInstance();
        emojidex.initialize(getApplicationContext());

        vScrollView = (VScrollView) findViewById(R.id.photo_editor_vscroll);
        hScrollView = (HScrollView) findViewById(R.id.photo_editor_hscroll);
        frameLayout = (FrameLayout) findViewById(R.id.photo_editor_frame);
        baseImageView = (ImageView) findViewById(R.id.photo_editor_base_image);

        moveButton = (ImageButton) findViewById(R.id.photo_editor_move_button);
        scaleButton = (ImageButton) findViewById(R.id.photo_editor_scale_button);
        rollButton = (ImageButton) findViewById(R.id.photo_editor_roll_button);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                prepareEmoji();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        };
        editText = (EditText) findViewById(R.id.photo_editor_text);
        editText.addTextChangedListener(textWatcher);
    }

    /**
     * Check IME enable.
     */
    private void imeEnableCheck()
    {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null)
        {
            showToast(getString(R.string.no_input_method));
            return;
        }

        // Skip if ime enable.
        for(InputMethodInfo info : inputMethodManager.getEnabledInputMethodList())
        {
            if(info.getServiceName().equals(EmojidexIME.class.getName())) return;
        }

        // Show dialog and go to settings.
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.emojidex_keyboard_disabled);
        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                final Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    /**
     * Set base image.
     * @param intent photo data.
     */
    private void setBaseImage(Intent intent)
    {
        Uri uri = intent.getData();
        if (uri == null)
        {
            if (intent.getExtras() == null || intent.getExtras().get("android.intent.extra.STREAM") == null) return;
            //noinspection ConstantConditions
            uri = Uri.parse(intent.getExtras().get("android.intent.extra.STREAM").toString());
        }

        // exif
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;

        if (cursor != null)
        {
            cursor.moveToFirst();
            try
            {
                ExifInterface exifInterface = new ExifInterface(cursor.getString(0));
                orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            cursor.close();
        }

        try
        {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap originBitmap = BitmapFactory.decodeStream(is);
            Bitmap bitmap = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(),
                                                originBitmap.getHeight(), getMatrix(orientation), true);
            if (is != null) is.close();
            baseImageView.setImageBitmap(bitmap);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showToast(getString(R.string.open_error));
        }
    }

    /**
     * Get matrix from image exif.
     * @param orientation orientation
     * @return matrix
     */
    private Matrix getMatrix(int orientation)
    {
        Matrix matrix = new Matrix();
        matrix.reset();

        switch (orientation)
        {
            case ExifInterface.ORIENTATION_UNDEFINED:
                break;
            case ExifInterface.ORIENTATION_NORMAL:
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90f);
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(-90f);
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(-90f);
                break;
        }

        return matrix;
    }

    /**
     * Prepare emoji.
     */
    public void prepareEmoji()
    {
        // Get emoji code from invisible edit text.
        final String emojiName = emojidex.deEmojify(editText.getText()).toString().replaceAll(":", "");
        editText.removeTextChangedListener(textWatcher);
        editText.setText("");
        editText.addTextChangedListener(textWatcher);

        final Emoji emoji = emojidex.getEmoji(emojiName);

        if (emoji == null) return;

        final SealDownloader downloader = new SealDownloader(this);
        downloader.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                if (downloader.isCanceled()) return;

                final SealGenerator generator = new SealGenerator(PhotoEditorActivity.this);
                generator.setBackgroundColor(Color.TRANSPARENT);
                generator.generate(emojiName);

                if (generator.useLowQuality())
                {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(PhotoEditorActivity.this);
                    alertDialog.setMessage(R.string.send_seal_not_found);
                    alertDialog.setPositiveButton(R.string.send_seal_not_found_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog)
                        {
                            addEmoji(generator.getUri());
                        }
                    });
                    alertDialog.show();

                    return;
                }

                addEmoji(generator.getUri());
            }
        });

        downloader.download(
                emojiName,
                getString(R.string.send_seal_dialog_title),
                getString(R.string.send_seal_dialog_message),
                getString(R.string.send_seal_dialog_cancel)
        );
    }

    /**
     * Add emoji to photo editor frame.
     */
    private void addEmoji(Uri uri)
    {
        // Load emoji.
        Bitmap bitmap;
        try
        {
            InputStream is = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showToast(getString(R.string.load_emoji_error));
            return;
        }

        // Add emoji image to photo editor.
        final GestureImageView image = new GestureImageView(getApplicationContext());
        image.setImageBitmap(bitmap);
        image.setX(100);
        image.setY(100);
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        image.setLayoutParams(params);

        frameLayout.addView(image);
    }

    /**
     * Show keyboard.
     * @param v button.
     */
    public void showKeyboard(View v)
    {
        String currentIme = android.provider.Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD
        );

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null)
        {
            showToast(getString(R.string.no_input_method));
            return;
        }

        if (!currentIme.contains("Emojidex"))
        {
            showToast(getString(R.string.select_emojidex_keyboard));
            inputMethodManager.showInputMethodPicker();
        }

        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        editText.requestFocus();
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
        if (frameLayout.getChildCount() == 1 || baseImageView.getWidth() == 0) return;

        // Prepare canvas.
        Bitmap bitmap = Bitmap.createBitmap(baseImageView.getWidth(), baseImageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw base image.
        canvas.drawBitmap(((BitmapDrawable) baseImageView.getDrawable()).getBitmap(),
                          baseImageView.getImageMatrix(), null);

        // Draw emoji images.
        for (int i = 1; i < frameLayout.getChildCount(); i++)
        {
            ImageView image = (ImageView) frameLayout.getChildAt(i);
            canvas.drawBitmap(((BitmapDrawable) image.getDrawable()).getBitmap(), image.getMatrix(), null);
        }

        // Create save folder.
        File saveFolder = new File(LOCAL_SAVE_PATH);
        if (!saveFolder.exists())
        {
            if (!saveFolder.mkdir())
            {
                showToast(getString(R.string.save_image_failed));
                return;
            }
        }

        // Save image.
        String filePath = LOCAL_SAVE_PATH + "/" + System.currentTimeMillis() + ".png";
        try
        {
            OutputStream os = getContentResolver().openOutputStream(Uri.fromFile(new File(filePath)));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            if (os != null) os.close();

            MediaScannerConnection.scanFile(getApplicationContext(), new String[] { filePath },
                                            new String[] { "image/jpg" }, null);
            showToast(getString(R.string.save_image_success) + "\n" + filePath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showToast(getString(R.string.save_image_failed));
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
            frameLayout.removeViewAt(i - 1);
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
                public void onClick(DialogInterface dialogInterface, int i)
                {
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

    private void showToast(String text)
    {
        Toast.makeText(PhotoEditorActivity.this, text, Toast.LENGTH_LONG).show();
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
            saveImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != SELECT_PHOTO || resultCode != Activity.RESULT_OK || data == null)
        {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        setBaseImage(data);
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

                    break;
            }

            oldX = newX;
            oldY = newY;

            return true;
        }
    }
}
