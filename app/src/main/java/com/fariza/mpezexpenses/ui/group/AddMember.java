package com.fariza.mpezexpenses.ui.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.fariza.mpezexpenses.Adapters.AdapterMemberAdd;
import com.fariza.mpezexpenses.Model.ModelUser;
import com.fariza.mpezexpenses.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddMember extends AppCompatActivity {

    private RecyclerView userRv;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private String groupId;
    private String myGroupRole;

    private ArrayList<ModelUser> userList;
    private AdapterMemberAdd adapterMemberAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Member");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        //init views
        userRv = findViewById(R.id.userRv);
        groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();
    }

    //retrieved data from database ("Users") to display list of users
    private void getAllUsers() {
        //init list
        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //get all users accept currently signed in
                    if (!firebaseAuth.getUid().equals(modelUser.getUid())){
                        //not my uid
                        userList.add(modelUser);
                    }
                }
                //setup adapter
                adapterMemberAdd = new AdapterMemberAdd(AddMember.this, userList, ""+groupId, ""+myGroupRole);
                userRv.setAdapter(adapterMemberAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //retrieved data from database ("Groups") to identify the members that already in group and their roles
    private void loadGroupInfo() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");

        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
        ref1.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    final String groupName = ""+ds.child("groupName").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    actionBar.setTitle("Add Members");

                    ref.child(groupId).child("Members").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        myGroupRole = ""+dataSnapshot.child("Role").getValue();
                                        getAllUsers();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //search user using search function by enter email address or name
    //retrieved data from database ("Users") for search function
    private void searchِAllUsers(final String query) {
        //init list
        userList = new ArrayList<>();
        //load user from db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    if (!firebaseAuth.getUid().equals(modelUser.getUid())){
                    //search by expense name
                    if (ds.child("email").toString().toLowerCase().contains(query.toLowerCase())) {
                        ModelUser model = ds.getValue(ModelUser.class);
                        userList.add(model);
                    }
                    else if (ds.child("name").toString().toLowerCase().contains(query.toLowerCase())){
                        ModelUser model = ds.getValue(ModelUser.class);
                        userList.add(model);
                    }
                    }
                }
                //setup adapter
                adapterMemberAdd = new AdapterMemberAdd(AddMember.this, userList, ""+groupId, ""+myGroupRole);
                userRv.setAdapter(adapterMemberAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //create menu search at action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if (!TextUtils.isEmpty(s.trim())){
                    searchِAllUsers(s);
                }
                else{
                    getAllUsers();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchِAllUsers(s);
                }
                else{
                    getAllUsers();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //back button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
