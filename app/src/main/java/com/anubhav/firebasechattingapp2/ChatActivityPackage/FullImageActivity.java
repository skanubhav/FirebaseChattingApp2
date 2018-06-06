package com.anubhav.firebasechattingapp2.ChatActivityPackage;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.anubhav.firebasechattingapp2.GlideApp;
import com.anubhav.firebasechattingapp2.R;

import java.util.Date;

public class FullImageActivity extends AppCompatActivity {

    private static final float MIN_ZOOM = 1.f, MAX_ZOOM = 1.f;

    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1.f;

    private ImageView image_full;
    private String uri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullimageactivity);
        image_full = findViewById(R.id.image_full);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        uri = intent.getStringExtra("Image URL");

        showImage(uri);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
       // mScaleGestureDetector.onTouchEvent(event);

        image_full.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if(oldDist>5.f) {
                    savedMatrix.set(matrix);
                    midPoint(event, mid);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if(mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                }

                else if(mode == ZOOM) {
                    float newDist = spacing(event);
                    if(newDist>5.f) {
                        matrix.set(savedMatrix);
                        scale = newDist/oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        image_full.setImageMatrix(matrix);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.download) {
            Drawable drawable = image_full.getDrawable();
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            String fileName = String.valueOf(new Date().getTime());

            String imageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    bitmap,
                    "FCA2" + fileName,
                    "FCA2 Images"
            );

            Uri photoUri = Uri.parse(imageURL);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATE_ADDED, fileName);
            values.put(MediaStore.Images.Media.DATE_MODIFIED, fileName);
            values.put(MediaStore.Images.Media.DATE_TAKEN, fileName);

            getContentResolver().update(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values,
                    MediaStore.Images.Media._ID + "=?",
                    new String [] { ContentUris.parseId(photoUri) + "" });

            Log.d("Download",imageURL);

            Toast.makeText(this,
                    "Image Saved",
                    Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void showImage(String uri) {
        GlideApp.with(this)
                .load(uri)
                .into(image_full);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x*x + y*y);
    }

    private void midPoint(MotionEvent event, PointF pointF) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        pointF.set(x/2, y/2);
    }
}

