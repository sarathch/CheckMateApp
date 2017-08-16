package com.example.syennamani.checkmate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.syennamani.checkmate.Database.Friend;
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

        // Add Friend Floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
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
        public FriendsListAdapter(Context context,
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

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // First let's verify the convertView is not null
            if (convertView == null) {
                // This a new view we inflate the new layout
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.adapter_friendslist, parent, false);
            }
            TextView tvTextBottom = (TextView)convertView.findViewById(R.id.card_item_textBottom);
            tvTextBottom.setText(friends.get(position).getF_email());

            TextView tvTextBottom1 = (TextView)convertView.findViewById(R.id.card_item_textBottom1);
            tvTextBottom1.setText(friends.get(position).getF_phone());

            ImageView imageView = (ImageView)convertView.findViewById(R.id.iv_search);
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    myFirebaseMethods.addTracker(friends.get(position).getF_uid());
                }
            });
            return convertView;
        }

    }

    protected ArrayList<Friend> fetchFriendsList(){
        final ArrayList<Friend> friendsList = new ArrayList<>();
        DatabaseReference ref = mDatabase.child(mAuth.getCurrentUser().getUid()).child("friends");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.v("Count " ,""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Friend friend = postSnapshot.getValue(Friend.class);
                    Log.v(TAG, friend.getF_email());
                    friendsList.add(friend);
                    FriendsListAdapter adapter = new FriendsListAdapter(context, friendsList);
                    // FriendsList List View
                    ListView listView = (ListView) findViewById(R.id.lv_friendsList);
                    listView.setAdapter(adapter);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        return friendsList;
    }

}
