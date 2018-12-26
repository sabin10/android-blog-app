package com.example.sabin.projectkcal;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentsActivity extends AppCompatActivity {

    android.support.v7.widget.Toolbar mToolbar;
    EditText commField;
    ImageView sendCommBtn;

    String blogPostId;
    String currUserId;

    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;

    RecyclerView commentsListView;
    CommentsRecyclerAdapter commentsRecyclerAdapter;
    List<Comment> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.comm_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Comments");

        commField = findViewById(R.id.comm_write);
        sendCommBtn = findViewById(R.id.comm_send_btn);
        commentsListView = findViewById(R.id.comm_list);

        //RecyclerView Firebase List
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        //comment_list.setHasFixedSize(true);
        commentsListView.setLayoutManager(new LinearLayoutManager(this));
        commentsListView.setAdapter(commentsRecyclerAdapter);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        currUserId = mAuth.getCurrentUser().getUid();

        blogPostId = getIntent().getStringExtra("blogPostId");

        mFirestore.collection("Posts/" + blogPostId + "/Comments")
                .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    Comment comment = doc.getDocument().toObject(Comment.class);
                                    commentsList.add(comment);
                                    commentsRecyclerAdapter.notifyDataSetChanged();
                                }
                            }


                        }

                    }
                });

        sendCommBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String commMessage = commField.getText().toString();

                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("message", commMessage);
                commentMap.put("user_id", currUserId);
                commentMap.put("timestamp", FieldValue.serverTimestamp());

                mFirestore.collection("Posts/" + blogPostId + "/Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (!task.isSuccessful())
                            Toast.makeText(CommentsActivity.this, "Error Comment: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        else
                            commField.setText("");

                    }
                });
            }
        });


    }
}
