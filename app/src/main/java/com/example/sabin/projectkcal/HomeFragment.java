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
import com.google.firebase.firestore.DocumentSnapshot;
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
public class HomeFragment extends Fragment {

    RecyclerView homeListView;
    List<BlogPost> blogList;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;

    BlogRecycleAdapter blogRecycleAdapter;

    DocumentSnapshot lastVisible;
    Boolean isFirstPageFirstLoaded = true;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();

        homeListView = view.findViewById(R.id.home_blog_list);
        blogList = new ArrayList<>();

        blogRecycleAdapter = new BlogRecycleAdapter(blogList);

        homeListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        homeListView.setAdapter(blogRecycleAdapter);

        if (mAuth.getCurrentUser() != null) {

            mFirestore = FirebaseFirestore.getInstance();


            homeListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {
                        loadMorePost();
                    }
                }
            });



            Query firstQuery = mFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);

            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (isFirstPageFirstLoaded) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    }

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogPostId = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                            if (isFirstPageFirstLoaded) {
                                blogList.add(blogPost);
                            } else {
                                blogList.add(0, blogPost);
                            }

                            blogRecycleAdapter.notifyDataSetChanged();
                        }
                    }

                    isFirstPageFirstLoaded = false;

                }
            });
        }


        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost() {
        if (mAuth.getCurrentUser() != null) {

            Query nextQuery = mFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                blogList.add(blogPost);

                                blogRecycleAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                }
            });


        }
    }

}
