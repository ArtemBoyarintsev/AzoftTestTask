package com.example.kittydetector;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;
    private int REQUEST_CAMERA = 2;

    CustomClassifier customClassifier;
    ClassifierFloatMobileNet mobileClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button cameraButton = findViewById(R.id.cameraButton);
        Button galleryButton = findViewById(R.id.galeryButton);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_CAMERA);
                }
            }
        });

        mobileClassifier = new ClassifierFloatMobileNet(this);
        customClassifier = new CustomClassifier(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(uri,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap imageBitmap = BitmapFactory.decodeFile(picturePath);
            // String picturePath contains the path of selected Image
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(imageBitmap);

            addClassifiersInfo(imageBitmap);
        }
        else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(imageBitmap);

            addClassifiersInfo(imageBitmap);
        }
    }

    private void addClassifiersInfo(Bitmap imageBitmap)
    {
        CheckBox customNet = findViewById(R.id.checkBoxCustomNet);
        CheckBox mobileNet = findViewById(R.id.checkBoxMobileNet);

        boolean catFoundByCustom = false, catFoundByMobile = false;
        if (customNet.isChecked())
            catFoundByCustom = customClassifier.recognizeImage(imageBitmap);
        if (mobileNet.isChecked())
            catFoundByMobile = mobileClassifier.recognizeImage(imageBitmap);

        TextView textView = findViewById(R.id.resultText);
        String text = "";
        if (catFoundByCustom && catFoundByMobile)
        {
            text = "The cat was found by both networks";
        }
        else if (catFoundByCustom)
        {
            text = "The cat was found by custom network";
        }
        else if (catFoundByMobile)
        {
            text = "The cat was found by mobile network";
        }
        else
        {
            text = "The cat was found by neither of networks";
        }


        textView.setText(text.toCharArray(), 0, text.length());

    }
}
