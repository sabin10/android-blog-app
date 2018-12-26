package com.example.sabin.projectkcal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    android.support.v7.widget.Toolbar mToolbar;

    TextView mChoosePhoto;
    EditText mTitle;
    EditText mDesc;
    EditText mContent;
    Button mAddBtn;

    FirebaseAuth mAuth;
    StorageReference mStorageRef;
    FirebaseFirestore mFirestore;

    Uri postImageUri = null;

    String currUserId;

    Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.new_post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add New Post");

        mChoosePhoto = findViewById(R.id.choose_photo);
        mTitle = findViewById(R.id.title);
        mDesc = findViewById(R.id.description);
        mContent = findViewById(R.id.content);
        mAddBtn = findViewById(R.id.add_post_btn);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currUserId = mAuth.getCurrentUser().getUid();

        mChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        //.setMinCropResultSize(256, 128)
                        //.setAspectRatio(2, 1)
                        .start(NewPostActivity.this);
            }
        });

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (postImageUri == null) {
                    Toast.makeText(NewPostActivity.this, "You need to choose a photo", Toast.LENGTH_LONG).show();
                }


                final String title = mTitle.getText().toString();
                final String desc = mDesc.getText().toString();
                final String content = mContent.getText().toString();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc) && !TextUtils.isEmpty(content) && postImageUri != null) {

                    final String randomString = UUID.randomUUID().toString();


                    /*
                    File newImageFile = new File(postImageUri.getPath());
                    try {

                        compressedImageFile = new Compressor(NewPostActivity.this)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();

                    UploadTask uploadTask = mStorageRef.child("post_images").child(randomString + ".jpg").putBytes(imageData);
                    */

                    final StorageReference image_path = mStorageRef.child("post_images").child(currUserId).child(randomString + ".jpg");
                    UploadTask uploadTask = image_path.putFile(postImageUri);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            final Map<String, Object> postMap = new HashMap<>();

                            mStorageRef.child("post_images/" + currUserId + "/" + randomString + ".jpg").getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            //Map<String, Object> postMap = new HashMap<>();
                                            postMap.put("title", title);
                                            postMap.put("description", desc);
                                            postMap.put("content", content);
                                            postMap.put("image", uri.toString());
                                            postMap.put("user_id", currUserId);
                                            postMap.put("timestamp", FieldValue.serverTimestamp());


                                        }
                                    });

                            File newThumbFile = new File(postImageUri.getPath());
                            try {

                                compressedImageFile = new Compressor(NewPostActivity.this)
                                        .setMaxHeight(100)
                                        .setMaxWidth(100)
                                        .setQuality(1)
                                        .compressToBitmap(newThumbFile);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] thumbData = baos.toByteArray();

                            UploadTask uploadTaskThumbnail = mStorageRef.child("post_images").child(currUserId).child("thumbs")
                                    .child(randomString + ".jpg").putBytes(thumbData);

                            uploadTaskThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    mStorageRef.child("post_images/" + currUserId + "/thumbs/" + randomString + ".jpg").getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    postMap.put("thumb", uri.toString());

                                                    mFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if (task.isSuccessful()) {
                                                                //post saved, go to main
                                                                Toast.makeText(NewPostActivity.this, "Post was added", Toast.LENGTH_LONG).show();
                                                                sendToMain();


                                                            } else {
                                                                String err = task.getException().getMessage();
                                                                Toast.makeText(NewPostActivity.this, err, Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });

                                                }
                                            });

                                }
                            });

                        }
                    });

                }
            }
        });




    }

    private void sendToMain() {
        Intent toMain = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(toMain);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                mChoosePhoto.setText("Photo choosed");
                mChoosePhoto.setTypeface(mChoosePhoto.getTypeface(), Typeface.BOLD);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }
}
