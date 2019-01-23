package com.example.sabin.projectkcal;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class AnotherAccountFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    String userId;

    //recycler adapter
    RecyclerView userBlogListView;
    List<BlogPost> userBlogList;
    //AccountBlogRecycleAdapter accountBlogRecycleAdapter;
    AnotherAccountBlogRecycleAdapter anotherAccountBlogRecycleAdapter;

    public AnotherAccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_another_account, container, false);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userId = getArguments().getString("userId");

        userBlogListView = view.findViewById(R.id.another_user_blog_list);
        userBlogList = new ArrayList<>();
        //accountBlogRecycleAdapter = new AccountBlogRecycleAdapter(userBlogList);
        anotherAccountBlogRecycleAdapter = new AnotherAccountBlogRecycleAdapter(userBlogList);

        userBlogListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        userBlogListView.setAdapter(anotherAccountBlogRecycleAdapter);

        Query accountQuery = mFirestore.collection("Posts").whereEqualTo("user_id", userId);
        accountQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                    if (doc.getType() == DocumentChange.Type.ADDED) {

                        BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                        userBlogList.add(blogPost);
                        anotherAccountBlogRecycleAdapter.notifyDataSetChanged();

                    }
                }
            }
        });

        return view;
    }

}
