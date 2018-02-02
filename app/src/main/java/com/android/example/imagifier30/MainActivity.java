package com.android.example.imagifier30;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_IMAGE = 1;
    ImageView ivMain;
    Button btnSelectImage;
    Button btnRecognise;
    Uri mImageUri;
    Bitmap mBitmap;
    ByteArrayOutputStream outputStream;

    VisionServiceClient visionServiceClient = new VisionServiceRestClient("992df27e2e22444e84f2924f8e2df8e7", "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRecognise = (Button) findViewById(R.id.button_recognise);
        ivMain = (ImageView) findViewById(R.id.imageView_main);
        btnSelectImage = (Button) findViewById(R.id.button_selectImage);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        outputStream = new ByteArrayOutputStream();
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sharan);
        ivMain.setImageBitmap(mBitmap);
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        btnRecognise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask<InputStream, String, String> visionTask = new AsyncTask<InputStream, String, String>() {

                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

                    @Override
                    protected String doInBackground(InputStream... inputStreams) {
                        try {
                            publishProgress("Recognising......");
                            String[] features = {"Description"};
                            String[] details = {};

                            AnalysisResult result = visionServiceClient.analyzeImage(inputStreams[0], features, details);

                            String strResult = new Gson().toJson(result);
                            return strResult;
                        } catch (Exception e) {
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        progressDialog.show();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        progressDialog.dismiss();

                        try {
                            AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Caption caption : result.description.captions) {
                                stringBuilder.append(caption.text);
                            }
                            TextView tvResult = (TextView) findViewById(R.id.tv_result);
                            tvResult.setText(stringBuilder);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        progressDialog.setMessage(values[0]);
                    }
                };

                visionTask.execute(inputStream);
            }
        });
    }

}
