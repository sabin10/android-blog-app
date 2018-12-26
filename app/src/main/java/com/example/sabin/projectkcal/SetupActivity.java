package com.example.sabin.projectkcal;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    CircleImageView setupImage;
    Uri imageUri;
    EditText mFirstName;
    EditText mLastName;
    EditText mBio;
    Button mSaveBtn;

    FirebaseAuth mAuth;
    StorageReference mStorageRef;
    FirebaseFirestore mFirestore;

    String firstName;
    String lastName;
    String biography;
    String user_id;

    boolean isImageChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mFirstName = findViewById(R.id.firstname_field);
        mLastName = findViewById(R.id.lastname_field);
        mBio = findViewById(R.id.biography_field);
        mSaveBtn = findViewById(R.id.save_settings_btn);
        setupImage = findViewById(R.id.profile_image);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //retrieving initial data
        mFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        //data exist and fields will be completed
                        String firstN = task.getResult().getString("first");
                        String lastN = task.getResult().getString("last");
                        String bio = task.getResult().getString("bio");
                        String image = task.getResult().getString("image");

                        imageUri = Uri.parse(image);

                        mFirstName.setText(firstN);
                        mLastName.setText(lastN);
                        mBio.setText(bio);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.profileimage);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);

                    }

                } else {
                    String err = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore GET: " + err, Toast.LENGTH_LONG).show();
                }
            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //daca e mai mare decat Marshmello, e nevoie de request permision aici
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {
                        popUpImagePicker();
                    }
                } else {
                    popUpImagePicker();
                }
            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null) {
                    Toast.makeText(SetupActivity.this, "You need to choose a photo", Toast.LENGTH_LONG).show();
                }

                firstName = mFirstName.getText().toString();
                lastName = mLastName.getText().toString();
                biography = mBio.getText().toString();

                if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(biography)){

                    if (isImageChanged) {
                        final StorageReference image_path = mStorageRef.child("profile_images").child(user_id + ".jpg");
                        UploadTask uploadTask = image_path.putFile(imageUri);
                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                String errorMsg = exception.getMessage();
                                Toast.makeText(SetupActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                                //Toast.makeText(SetupActivity.this, "Image upload succesfully", Toast.LENGTH_LONG).show();

                                mStorageRef.child("profile_images/" + user_id + ".jpg").getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put("first", firstName);
                                                userMap.put("last", lastName);
                                                userMap.put("bio", biography);
                                                userMap.put("image", uri.toString());

                                                mFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(SetupActivity.this, "Succes in Firestore", Toast.LENGTH_LONG).show();
                                                            sendToMain();
                                                        } else {
                                                            String err = task.getException().getMessage();
                                                            Toast.makeText(SetupActivity.this, err, Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SetupActivity.this, "Try again", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                    } else {
                        //daca poza nu e schimbata se vor update doar fieldurile text

                        WriteBatch batch = mFirestore.batch();
                        DocumentReference userRef = mFirestore.collection("Users").document(user_id);
                        batch.update(userRef, "first", firstName);
                        batch.update(userRef, "last", lastName);
                        batch.update(userRef, "bio", biography);

                        // Commit the batch
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(SetupActivity.this, "Succes batch update", Toast.LENGTH_LONG).show();
                                sendToMain();
                            }
                        });
                    }

                } else {
                    Toast.makeText(SetupActivity.this, "All fields are required to be completed", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                setupImage.setImageURI(imageUri);

                isImageChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void popUpImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }

    private void sendToMain() {
        Intent toMain = new Intent(SetupActivity.this, MainActivity.class);
        startActivity(toMain);
        finish();
    }

}
