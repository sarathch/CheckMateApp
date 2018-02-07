package com.example.syennamani.checkmate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.syennamani.checkmate.Database.Friend;
import com.example.syennamani.checkmate.Database.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ConnectToMateActivity extends BaseActivity {

    private Context context;
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecttomate);
        context = this;
        // Fetch friends list
        fetchFriendsList();

        // Setting action bar logout
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayOptions(actionBar.getDisplayOptions()
                    | ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView imageView = new ImageView(actionBar.getThemedContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setImageResource(R.drawable.logout);
            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT, Gravity.END
                    | Gravity.CENTER_VERTICAL);
            layoutParams.rightMargin = 40;
            imageView.setLayoutParams(layoutParams);
            actionBar.setCustomView(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myFirebaseMethods.showAlertDialog("SIGN OUT", "Are you sure you want to logout?","SignOut");
                }
            });
        }

        // Add Friend Floating action button
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Enter your friend's email!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                showCustomDialog("ADD FRIEND", "FriendRequest");
            }
        });
        //Start LocationService
        context.startService(new Intent(context, LocationService.class));
    }


    private class FriendsListAdapter extends BaseAdapter {

        private Context context;
        private List<Friend> friends;
        private FriendsListAdapter(Context context,
                                  ArrayList<Friend> friends) {
            this.context = context;
            this.friends = friends;
        }

        @Override
        public int getCount() {
            return friends.size();
        }

        @Override
        public Object getItem(int i) {
            return friends.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Nullable
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // First let's verify the convertView is not null
            if (convertView == null) {
                // This a new view we inflate the new layout
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.adapter_friendslist, parent, false);
            }

            TextView tvTextBottom = convertView.findViewById(R.id.card_item_textBottom);
            tvTextBottom.setText(friends.get(position).getF_email());

            TextView tvTextBottom1 = convertView.findViewById(R.id.card_item_textBottom1);
            tvTextBottom1.setText(friends.get(position).getF_phone());

            ImageView imageView = convertView.findViewById(R.id.iv_search);
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    myFirebaseMethods.addTracker(friends.get(position).getF_uid());
                }
            });
            return convertView;
        }

    }

    protected void fetchFriendsList(){
        final ArrayList<Friend> friendsList = new ArrayList<>();
        if(mAuth.getCurrentUser()==null)
            return;
        DatabaseReference ref = mDatabase.child(mAuth.getCurrentUser().getUid()).child("friends");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.v("Count " ,""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Friend friend = postSnapshot.getValue(Friend.class);
                    Log.v(TAG, friend != null ? friend.getF_email() : "null");
                    friendsList.add(friend);
                    FriendsListAdapter adapter = new FriendsListAdapter(context, friendsList);
                    // FriendsList List View
                    ListView listView = findViewById(R.id.lv_friendsList);
                    listView.setAdapter(adapter);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

}
