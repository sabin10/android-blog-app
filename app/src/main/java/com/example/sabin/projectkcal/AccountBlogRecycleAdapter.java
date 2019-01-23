package com.example.sabin.projectkcal;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.PendingIntent.getActivity;
import static com.example.sabin.projectkcal.MainActivity.blogPostFragment;

public class AccountBlogRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public List<BlogPost> userBlogList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String currUserId;
    public Context context;

    public AccountBlogRecycleAdapter(List<BlogPost> userBlogList) {
        this.userBlogList = userBlogList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currUserId = mAuth.getCurrentUser().getUid();

        View viewBlog = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_blog_list_item, parent, false);
        View viewUser = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_user_data_item, parent, false);

        switch (viewType) {
            case 0: return new ViewHolderUserData(viewUser);
            case 1: return new ViewHolderBlog(viewBlog);
            default: return new ViewHolderBlog(viewBlog);
        }

       // return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        switch (holder.getItemViewType()) {
            case 0:
                final ViewHolderUserData viewHolderUserData = (ViewHolderUserData) holder;
                //...

                DocumentReference userDocRef = mFirestore.collection("Users").document(currUserId);
                userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);

                        String firstName = user.getFirst();
                        String lastName = user.getLast();
                        String profileImageUrl = user.getImage();
                        String bio = user.getBio();

                        viewHolderUserData.setUserData(firstName, lastName, profileImageUrl, bio);
                    }
                });

                viewHolderUserData.setUserReadersCounter(Integer.toString(0));

                Query readerQuery = mFirestore.collection("Users/" + currUserId + "/Readers");

                readerQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            int counter = 0;
                            counter = documentSnapshots.size();
                            viewHolderUserData.setUserReadersCounter(Integer.toString(counter));
                        }
                    }
                });

                break;

            case 1:
                ViewHolderBlog viewHolderBlog = (ViewHolderBlog) holder;
                //...

                String titleString = userBlogList.get(position - 1).getTitle();
                String descString = userBlogList.get(position - 1).getDescription();
                String imageUrl = userBlogList.get(position - 1).getImage();
                viewHolderBlog.setBlogData(titleString, descString, imageUrl);
                final String blogPostId = userBlogList.get(position).BlogPostId;


                viewHolderBlog.blogTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "Am apasat pe titlu", Toast.LENGTH_SHORT).show();
                        //goToBlogPostFragment(blogPostId, currUserId, view);
                    }
                });

                break;
        }


    }

    //ar fi trebuit +1
    /*DE REVENIT*/
    @Override
    public int getItemCount() {
        return userBlogList.size();
    }



    public class ViewHolderBlog extends RecyclerView.ViewHolder {

        private View mView;

        private TextView blogTitleView;
        private ImageView blogImageView;
        private TextView blogDescView;

        public ViewHolderBlog(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setBlogData(String titleString, String descString, String imageUrl) {
            blogTitleView = mView.findViewById(R.id.acc_blog_title);
            blogTitleView.setText(titleString);

            blogDescView = mView.findViewById(R.id.acc_blog_desc);
            blogDescView.setText(descString);

            blogImageView = mView.findViewById(R.id.acc_blog_image);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.rectangle);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(imageUrl).into(blogImageView);
        }

    }


    public class ViewHolderUserData extends RecyclerView.ViewHolder {
        private View mView;

        private CircleImageView userImageView;
        private TextView userNameView;
        private TextView userBioView;
        private TextView userReadersCounter;

        public ViewHolderUserData(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserData(String firstName, String lastName, String profileImageUrl, String bio) {
            userNameView = mView.findViewById(R.id.account_user_name);
            userNameView.setText(firstName + " " + lastName);

            userImageView = mView.findViewById(R.id.account_user_image);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.ellipse);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(profileImageUrl).into(userImageView);

            userBioView = mView.findViewById(R.id.account_user_bio);
            userBioView.setText(bio);
        }

        public void setUserReadersCounter(String counterString) {
            userReadersCounter = mView.findViewById(R.id.account_user_readers_counter);
            userReadersCounter.setText(counterString);
        }
    }


    public void goToBlogPostFragment(String blogPostId, String user_id, View view) {
        Bundle args = new Bundle();
        args.putString("blogPostId", blogPostId);
        args.putString("userId", user_id);

        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        blogPostFragment.setArguments(args);
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_container, blogPostFragment).addToBackStack(null).commit();
    }

}
