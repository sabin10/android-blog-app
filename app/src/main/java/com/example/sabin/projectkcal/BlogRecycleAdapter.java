package com.example.sabin.projectkcal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecycleAdapter extends RecyclerView.Adapter<BlogRecycleAdapter.ViewHolder> {

    public List<BlogPost> blogList;
    public Context context;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    Boolean currUserLiked = false;

    public BlogRecycleAdapter(List<BlogPost> blogList) {
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);

        context = parent.getContext();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String currentUserId = mAuth.getCurrentUser().getUid();
        final String blogPostId = blogList.get(position).BlogPostId;

        String desc_data = blogList.get(position).getDescription();
        holder.setDescText(desc_data);

        String title_data = blogList.get(position).getTitle();
        holder.setTitleText(title_data);

        String image_url = blogList.get(position).getImage();
        String thumbnail_url = blogList.get(position).getThumb();
        holder.setBlogImage(image_url, thumbnail_url);


        try {
            long millisecond = blogList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("d/MM/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        String user_id = blogList.get(position).getUser_id();

        mFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    String firstName = task.getResult().getString("first");
                    String lastName = task.getResult().getString("last");
                    String profileImageUrl = task.getResult().getString("image");

                    holder.setUserData(firstName, lastName, profileImageUrl);
                } else {
                    //
                }
            }
        });


        //Likes


        mFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    int counter = queryDocumentSnapshots.size();
                    holder.updateLikeCounter(counter);
                } else {
                    holder.updateLikeCounter(0);
                }
            }
        });


        mFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    holder.likeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_btn_accent));
                } else {
                    holder.likeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_btn_gray));
                }

            }
        });


        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!currUserLiked) {
                    currUserLiked = true;
                    //holder.likeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_btn_accent));
                } else {
                    currUserLiked = false;
                    //holder.likeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_btn_gray));
                }

                mFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            mFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);


                        } else {

                            mFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                        }

                    }
                });
            }
        });


        holder.commBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blogPostId", blogPostId);
                context.startActivity(commentIntent);
            }
        });



    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView descView;
        private TextView titleView;
        private ImageView blogImageView;
        private TextView dateView;
        private TextView userNameView;
        private CircleImageView userProfileImageView;
        private ImageView likeBtn;
        private TextView likeCounter;
        private ImageView commBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            commBtn = mView.findViewById(R.id.blog_comm_btn);
            likeBtn = mView.findViewById(R.id.blog_like_btn);
        }

        public void setDescText(String descString) {
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descString);
        }

        public void setTitleText(String titleString) {
            titleView = mView.findViewById(R.id.blog_title);
            titleView.setText(titleString);

        }

        public void setBlogImage(String imageUrl, String thumbnailUrl) {
            blogImageView = mView.findViewById(R.id.blog_image);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.rectangle);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption)
                    .load(imageUrl)
                    .thumbnail(Glide.with(context).load(thumbnailUrl))
                    .into(blogImageView);
        }

        public void setTime(String dateString) {
            dateView = mView.findViewById(R.id.blog_date);
            dateView.setText(dateString);
        }

        public void setUserData(String firstName, String lastName, String profileImageUrl) {
            userNameView = mView.findViewById(R.id.blog_user_name);
            userProfileImageView = mView.findViewById(R.id.blog_user_image);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.ellipse);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(profileImageUrl).into(userProfileImageView);

            userNameView.setText(firstName + " " + lastName);
        }

        public void updateLikeCounter(int counter) {
            likeCounter = mView.findViewById(R.id.blog_like_count);

            if (counter > 0)
                likeCounter.setText(Integer.toString(counter));
            else
                likeCounter.setText(" ");

            if (currUserLiked)
                likeCounter.setTextColor(Color.parseColor("#263238"));
            else
                likeCounter.setTextColor(Color.parseColor("#90A4AE"));


        }
    }

}
