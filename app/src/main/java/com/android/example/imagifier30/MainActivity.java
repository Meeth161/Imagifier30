package com.android.example.imagifier30;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_IMAGE = 1;
    ImageView ivMain;
    TextView tvRecognise;
    TextView tvSearch;
    Uri mImageUri;
    Bitmap mBitmap;
    ByteArrayOutputStream outputStream;

    DatabaseReference mRef;
    StorageReference mStorageRef;
    FirebaseAuth mAuth;

    String desc;

    Uri imageUri;
    String res;

    VisionServiceClient visionServiceClient = new VisionServiceRestClient("992df27e2e22444e84f2924f8e2df8e7", "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        imageUri = Uri.parse(intent.getStringExtra("uri"));
        if(imageUri == null) {
            Toast.makeText(this, "Null", Toast.LENGTH_SHORT).show();
        }

        tvRecognise = (TextView) findViewById(R.id.tv_recognise);
        tvRecognise.setVisibility(View.VISIBLE);
        tvSearch = (TextView) findViewById(R.id.tv_searchOnWeb);
        ivMain = (ImageView) findViewById(R.id.imageView_main);

        outputStream = new ByteArrayOutputStream();
        //mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bill);
        try {
            mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ivMain.setImageURI(imageUri);
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, res);
                startActivity(intent);
            }
        });

        tvRecognise.setOnClickListener(new View.OnClickListener() {
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

                        try {
                            AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Caption caption : result.description.captions) {
                                stringBuilder.append(caption.text);
                            }
                            desc = stringBuilder.toString();
                            TextView tvResult = (TextView) findViewById(R.id.tv_result);
                            res = String.valueOf(stringBuilder);
                            tvResult.setText(stringBuilder);
                            tvRecognise.setVisibility(View.GONE);
                            tvSearch.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        if(mAuth.getCurrentUser() != null) {
                            StorageReference sRef = mStorageRef.child("/images" + UUID.randomUUID() + ".jpg");
                            byte[] data = outputStream.toByteArray();

                            UploadTask uploadTask = sRef.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    Toast.makeText(MainActivity.this, "Search Not Saved", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    mRef.child("searches").child(mAuth.getCurrentUser().getUid()).push().setValue(new Search(downloadUrl.toString(), desc));
                                    progressDialog.dismiss();
                                }
                            });
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
