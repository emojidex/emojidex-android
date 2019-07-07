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
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emojidex.emojidexandroid.view.HScrollView;
import com.emojidex.emojidexandroid.view.VScrollView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * PhotoEditorActivity
 * Created by Yoshida on 2017/12/13.
 */

public class PhotoEditorActivity extends Activity implements ColorPickerDialogListener {
    private static final int NONE = -1;
    private static final int MOVE = 0;
    private static final int SCALE = 1;
    private static final int ROLL = 2;
    private static final int V_FLIP = 3;
    private static final int H_FLIP = 4;
    private int mode = NONE;

    private static final int DIALOG_TEXT = 2000;
    private static final int DIALOG_SHADOW = 2001;

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
    private int maximumTextureSize;

    private ImageButton moveButton;
    private ImageButton scaleButton;
    private ImageButton rollButton;
    private ImageButton vFlipButton;
    private ImageButton hFlipButton;

    private EditText editText;
    private TextWatcher textWatcher;

    private Emojidex emojidex;

    private ColorMatrixColorFilter currentFilter;

    private PopupWindow popupWindow;
    private EditText previewEditText;
    private TextView sizeTextView;
    private TextView colorTextView;
    private Button pickerButton;
    private TextView shadowSizeTextView;
    private TextView shadowColorTextView;
    private Button shadowPickerButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);
        setTheme(R.style.Theme_AppCompat);

        initialize();
        imeEnableCheck();
        getMaxTextureSize();
        setBaseImage(getIntent());
    }

    private void initialize()
    {
        emojidex = Emojidex.getInstance();
        emojidex.initialize(getApplicationContext());

        vScrollView = findViewById(R.id.photo_editor_vscroll);
        hScrollView = findViewById(R.id.photo_editor_hscroll);
        frameLayout = findViewById(R.id.photo_editor_frame);
        baseImageView = findViewById(R.id.photo_editor_base_image);

        moveButton = findViewById(R.id.photo_editor_move_button);
        scaleButton = findViewById(R.id.photo_editor_scale_button);
        rollButton = findViewById(R.id.photo_editor_roll_button);
        vFlipButton = findViewById(R.id.photo_editor_v_flip_button);
        hFlipButton = findViewById(R.id.photo_editor_h_flip_button);

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
        editText = findViewById(R.id.photo_editor_text);
        editText.addTextChangedListener(textWatcher);

        currentFilter = new ColorMatrixColorFilter(new ColorMatrix());
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
            intent.removeExtra("android.intent.extra.STREAM");
        }
        else
        {
            intent.setData(null);
        }

        try
        {
            InputStream is = getContentResolver().openInputStream(uri);

            // exif
            assert (is) != null;
            ExifInterface exifInterface = new ExifInterface(is);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            is.close();

            // Set image to imageView.
            is = getContentResolver().openInputStream(uri);
            Bitmap originBitmap = BitmapFactory.decodeStream(is);
            Bitmap bitmap = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(),
                                                originBitmap.getHeight(), getMatrix(orientation), true);
            if (bitmap.getWidth() > maximumTextureSize || bitmap.getHeight() > maximumTextureSize)
            {
                showToast(getString(R.string.too_large));
            }
            else
            {
                baseImageView.setImageBitmap(bitmap);
                baseImageView.setColorFilter(currentFilter);
            }
            if (is != null) is.close();
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
        if (baseImageView.getDrawable() == null)
        {
            showToast(getString(R.string.select_base_image));
            return;
        }

        // Get emoji code from invisible edit text.
        final String emojiName = emojidex.codify(editText.getText()).toString().replaceAll(":", "");
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

        addImageToFrame(bitmap);
    }

    /**
     * Add emoji image to photo editor.
     * @param bitmap image
     */
    public void addImageToFrame(Bitmap bitmap)
    {
        final GestureImageView image = new GestureImageView(getApplicationContext());
        image.setImageBitmap(bitmap);
        image.setX(100);
        image.setY(100);
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        image.setLayoutParams(params);
        image.setColorFilter(currentFilter);

        frameLayout.addView(image);
    }

    /**
     * Show emojidex keyboard.
     * @param v button.
     */
    public void showEmojidexKeyboard(View v)
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
     * Set mode - V_FLIP
     * @param v button.
     */
    public void setModeToVerticalFlip(View v)
    {
        mode = mode == V_FLIP ? NONE : V_FLIP;
        setButtonBackGround();
    }

    /**
     * Set mode - H_FLIP
     * @param v button.
     */
    public void setModeToHorizontalFlip(View v)
    {
        mode = mode == H_FLIP ? NONE : H_FLIP;
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
        vFlipButton.setBackgroundColor(Color.TRANSPARENT);
        hFlipButton.setBackgroundColor(Color.TRANSPARENT);
        if (mode == MOVE) moveButton.setBackgroundColor(getResources().getColor(R.color.primary));
        if (mode == SCALE) scaleButton.setBackgroundColor(getResources().getColor(R.color.primary));
        if (mode == ROLL) rollButton.setBackgroundColor(getResources().getColor(R.color.primary));
        if (mode == V_FLIP) vFlipButton.setBackgroundColor(getResources().getColor(R.color.primary));
        if (mode == H_FLIP) hFlipButton.setBackgroundColor(getResources().getColor(R.color.primary));
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
        Paint paint = new Paint();
        paint.setColorFilter(baseImageView.getColorFilter());
        Matrix matrix = new Matrix();
        matrix.postScale(baseImageView.getScaleX(), baseImageView.getScaleY(),
                         baseImageView.getWidth() / 2, baseImageView.getHeight() / 2);
        canvas.drawBitmap(((BitmapDrawable) baseImageView.getDrawable()).getBitmap(), matrix, paint);

        // Draw emoji images.
        for (int i = 1; i < frameLayout.getChildCount(); i++)
        {
            ImageView image = (ImageView) frameLayout.getChildAt(i);
            canvas.drawBitmap(((BitmapDrawable) image.getDrawable()).getBitmap(), image.getMatrix(), paint);
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
        baseImageView.setImageDrawable(null);
        baseImageView.setColorFilter(null);
        baseImageView.setScaleX(1);
        baseImageView.setScaleY(1);

        currentFilter = new ColorMatrixColorFilter(new ColorMatrix());

        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19)
        {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        else
        {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, null), SELECT_PHOTO);
    }

    /**
     * Add effect.
     * @param v button
     */
    public void addEffect(View v)
    {
        final CharSequence[] items = getResources().getStringArray(R.array.effect_items);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.select_effect);
        dialog.setItems(
                items,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i < 3 || i == 5 || i == 7)
                        {
                            ColorMatrix colorMatrix = new ColorMatrix();
                            colorMatrix.reset();

                            switch (i) {
                                case 0: // grayscale
                                    colorMatrix.setSaturation(0);
                                    break;
                                case 1: // sepia
                                    colorMatrix.setScale(0.69f, 0.47f, 0.27f, 1f);
                                    break;
                                case 2: // nega-posi
                                    colorMatrix.set(new float[]{-1, 0, 0, 0, 255,
                                            0, -1, 0, 0, 255,
                                            0, 0, -1, 0, 255,
                                            0, 0, 0, 1, 0});
                                    break;
                                case 5: // clear (color filter)
                                case 7: // clear (all)
                                    break;
                            }

                            currentFilter = new ColorMatrixColorFilter(colorMatrix);
                            baseImageView.setColorFilter(currentFilter);

                            for (int j = 1; j < frameLayout.getChildCount(); j++)
                            {
                                GestureImageView imageView = (GestureImageView) frameLayout.getChildAt(j);
                                imageView.setColorFilter(currentFilter);
                            }
                        }

                        if (i == 3 || i == 4 || i > 5)
                        {
                            float scaleX = baseImageView.getScaleX();
                            float scaleY = baseImageView.getScaleY();
                            float width = baseImageView.getWidth();
                            float height = baseImageView.getHeight();
                            boolean clearedX = false;
                            boolean clearedY = false;

                            switch (i) {
                                case 3: // flip (vertical)
                                    scaleY = scaleY * -1;
                                    break;
                                case 4: // flip (horizontal)
                                    scaleX = scaleX * -1;
                                    break;
                                case 6: // clear (flip)
                                case 7: // clear (all)
                                    if (scaleX == -1) clearedX = true;
                                    if (scaleY == -1) clearedY = true;
                                    scaleX = 1;
                                    scaleY = 1;

                                    break;
                            }

                            baseImageView.setScaleX(scaleX);
                            baseImageView.setScaleY(scaleY);

                            for (int j = 1; j < frameLayout.getChildCount(); j++)
                            {
                                GestureImageView imageView = (GestureImageView) frameLayout.getChildAt(j);
                                imageView.setScaleX(scaleX);
                                imageView.setScaleY(scaleY);

                                if (i == 3 || clearedY)
                                    imageView.setY(height - imageView.getY() - imageView.getHeight());
                                if (i == 4 || clearedX)
                                    imageView.setX(width - imageView.getX() - imageView.getWidth());
                            }
                        }
                    }
                }
        );
        dialog.show();
    }

    private void showToast(String text)
    {
        Toast.makeText(PhotoEditorActivity.this, text, Toast.LENGTH_LONG).show();
    }

    /**
     * Show help popup window.
     * @param v button
     */
    public void showHelp(View v)
    {
        popupWindow = new PopupWindow(PhotoEditorActivity.this);
        final View helpView = getLayoutInflater().inflate(R.layout.popup_photo_editor_help, null);
        helpView.findViewById(R.id.photo_editor_help_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow.isShowing()) popupWindow.dismiss();
            }
        });
        popupWindow.setContentView(helpView);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        final View rootView = getWindow().getDecorView().getRootView();
        popupWindow.setWidth(rootView.getWidth() - 100);
        popupWindow.setHeight(rootView.getHeight() - 100);

        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 50);
    }

    /**
     * Show create text popup.
     * @param v button.
     */
    public void showCreateTextPopup(View v)
    {
        if (baseImageView.getDrawable() == null)
        {
            showToast(getString(R.string.select_base_image));
            return;
        }

        popupWindow = new PopupWindow(PhotoEditorActivity.this);
        final View drawTextView = getLayoutInflater().inflate(R.layout.popup_photo_editor_text, null);
        drawTextView.findViewById(R.id.photo_editor_text_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow.isShowing()) popupWindow.dismiss();
            }
        });
        drawTextView.findViewById(R.id.photo_editor_text_create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createText();
                if (popupWindow.isShowing()) popupWindow.dismiss();
            }
        });

        previewEditText = drawTextView.findViewById(R.id.photo_editor_text_preview);
        sizeTextView = drawTextView.findViewById(R.id.photo_editor_text_size);
        colorTextView = drawTextView.findViewById(R.id.photo_editor_text_color);
        shadowSizeTextView = drawTextView.findViewById(R.id.photo_editor_shadow_size);
        shadowColorTextView = drawTextView.findViewById(R.id.photo_editor_shadow_color);

        pickerButton = drawTextView.findViewById(R.id.photo_editor_text_picker_button);
        pickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder().setColor(Color.BLACK).setDialogId(DIALOG_TEXT).show(PhotoEditorActivity.this);
            }
        });
        shadowPickerButton = drawTextView.findViewById(R.id.photo_editor_shadow_picker_button);
        shadowPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder().setColor(Color.WHITE).setDialogId(DIALOG_SHADOW).show(PhotoEditorActivity.this);
            }
        });

        final SeekBar seekBar = drawTextView.findViewById(R.id.photo_editor_text_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        setSize(seekBar.getProgress());

        final SeekBar shadowSeekBar = drawTextView.findViewById(R.id.photo_editor_shadow_seekbar);
        shadowSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setShadow(progress, previewEditText.getShadowColor());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        setShadow(shadowSeekBar.getProgress(), Color.WHITE);

        popupWindow.setContentView(drawTextView);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);

        final View rootView = getWindow().getDecorView().getRootView();
        popupWindow.setWidth(rootView.getWidth() - 100);
        popupWindow.setHeight(drawTextView.getHeight() - 100);

        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
    }

    private void setSize(int size)
    {
        sizeTextView.setText(String.valueOf(size));
        previewEditText.setTextSize(size);
    }

    private void setShadow(int radius, int color)
    {
        shadowSizeTextView.setText(String.valueOf(radius));
        previewEditText.setShadowLayer(radius, 0, 0, color);
    }

    /**
     * Create text.
     */
    public void createText()
    {
        String text = previewEditText.getText().toString();
        int size = (int) previewEditText.getTextSize();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(previewEditText.getCurrentTextColor());
        paint.setTextSize(size);
        paint.setShadowLayer(previewEditText.getShadowRadius(), 0, 0, previewEditText.getShadowColor());
        paint.getTextBounds(text, 0, text.length(), new Rect());

        Paint.FontMetrics matrix = paint.getFontMetrics();
        int width = (int) paint.measureText(text);
        int height = (int) (Math.abs(matrix.top) + matrix.bottom);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, 0, Math.abs(matrix.top), paint);

        addImageToFrame(bitmap);
    }

    /**
     * Show keyboard for create text.
     * @param v button
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

        if (currentIme.contains("Emojidex"))
        {
            showToast(getString(R.string.select_other_keyboard));
            inputMethodManager.showInputMethodPicker();
        }

        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        previewEditText.requestFocus();
    }

    @Override
    protected void onDestroy() {
        if (popupWindow != null && popupWindow.isShowing()) popupWindow.dismiss();

        super.onDestroy();
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
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) setBaseImage(data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND)) setBaseImage(intent);
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

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == DIALOG_TEXT) {
            colorTextView.setText("#" + Integer.toHexString(color).substring(2, 8));
            pickerButton.setBackgroundColor(color);
            previewEditText.setTextColor(color);
        } else {
            shadowColorTextView.setText("#" + Integer.toHexString(color).substring(2, 8));
            shadowPickerButton.setBackgroundColor(color);
            setShadow((int)previewEditText.getShadowRadius(), color);
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
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

                        case V_FLIP:
                            if (newY - oldY < 0)
                                setScaleY(Math.abs(getScaleY()));
                            else
                                setScaleY(-Math.abs(getScaleY()));

                            break;

                        case H_FLIP:
                            if (newX - oldX < 0)
                                setScaleX(Math.abs(getScaleX()));
                            else
                                setScaleX(-Math.abs(getScaleX()));

                            break;
                    }

                    break;
            }

            oldX = newX;
            oldY = newY;

            return true;
        }
    }

    /**
     * Get maximum texture size of bitmap.
     */
    private void getMaxTextureSize() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int[] version = new int[2];
        egl.eglInitialize(display, version);

        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

        int[] textureSize = new int[1];
        for (int i = 0; i < totalConfigurations[0]; i++) {
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);
            if (maximumTextureSize < textureSize[0]) maximumTextureSize = textureSize[0];
        }

        egl.eglTerminate(display);
    }
}
