package com.fariza.mpezexpenses.ui.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fariza.mpezexpenses.Adapters.AdapterMemberList;
import com.fariza.mpezexpenses.Model.ModelUser;
import com.fariza.mpezexpenses.R;
import com.fariza.mpezexpenses.ui.managegroup.ManageGroupFragment;
import com.fariza.mpezexpenses.ui.profile.ProfileFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfo extends AppCompatActivity {

    private String groupId;

    private ActionBar actionBar;

    private ImageView IvGroupIcon;
    private TextView TvCreatedBy, TvEditGroup, TvAddMember, TvLeaveGroup, TvMembers;
    private RecyclerView membersRv;
    private String myGroupRole = "";
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelUser> userList;
    private AdapterMemberList adapterMemberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        IvGroupIcon = findViewById(R.id.groupIconIv);
        TvCreatedBy = findViewById(R.id.TvcreatedBy);
        TvEditGroup = findViewById(R.id.TveditGroup);
        TvAddMember = findViewById(R.id.TvaddMember);
        TvLeaveGroup = findViewById(R.id.TvleaveGroup);
        TvMembers = findViewById(R.id.TvMembers);
        membersRv = findViewById(R.id.membersRv);


        //get id of the group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGroupRole();

        //button to go to add member page
        TvAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfo.this, AddMember.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        //button to go to edit group page
        TvEditGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfo.this, GroupEdit.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        //button to show alert dialog to leave or delete group
        TvLeaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";
                if (myGroupRole.equals("Creator")){
                    dialogTitle= "Delete Group";
                    dialogDescription= "Delete this group permanently?";
                    positiveButtonTitle= "DELETE";
                }
                else {
                    dialogTitle= "Leave Group";
                    dialogDescription= "Leave this group permanently?";
                    positiveButtonTitle= "LEAVE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfo.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (myGroupRole.equals("Creator")){
                                    deleteGroup();
                                }
                                else {
                                    leaveGroup();
                                }

                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

    }

    //if the user is creator, can delete the group
    private void deleteGroup(){
       DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
       reference.child(groupId)
               .removeValue()
               .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       Toast.makeText(GroupInfo.this, "Delete group successfully", Toast.LENGTH_SHORT).show();
                       startActivity(new Intent(GroupInfo.this, ManageGroupFragment.class));
                        finish();
                   }
               })
               .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(GroupInfo.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               });
    }

    //if the user member or admin, they can leave group
    private void leaveGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Members").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfo.this, "Left group successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfo.this, ManageGroupFragment.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfo.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //retrieved data from database ("Groups") to retrieved group name and group icon
    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupName = ""+ds.child("groupName").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();

                    //convert timestamp to time
                    Calendar c = Calendar.getInstance(Locale.ENGLISH);
                    c.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", c).toString();

                    loadCreatorInfo(dateTime, createdBy);
                    
                    actionBar.setTitle(groupName);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(IvGroupIcon);
                    }
                    catch (Exception e) {
                        IvGroupIcon.setImageResource(R.drawable.ic_group_primary);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //retrieved creator name from database ("Users")
    private void loadCreatorInfo(final String dateTime, final String createdBy) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    TvCreatedBy.setText("Created by " + name + " on " + dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //retrieved member's role from database("Members")
    private void loadMyGroupRole() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Members").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            myGroupRole = ""+ds.child("Role").getValue();
                            //actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail() + "("+myGroupRole+")");

                            if (myGroupRole.equals("Member")){
                                TvEditGroup.setVisibility(View.GONE);
                                TvAddMember.setVisibility(View.GONE);
                                TvLeaveGroup.setText("Leave Group");
                            }

                            else if (myGroupRole.equals("Admin")){
                                TvEditGroup.setVisibility(View.GONE);
                                TvAddMember.setVisibility(View.VISIBLE);
                                TvLeaveGroup.setText("Leave Group");
                            }

                            else if (myGroupRole.equals("Creator")){
                                TvEditGroup.setVisibility(View.VISIBLE);
                                TvAddMember.setVisibility(View.VISIBLE);
                                TvLeaveGroup.setText("Delete Group");
                            }
                        }
                        loadMembers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    //retrieved members data from database ("User") by compare it with uid from ("Users") and ("Members")
    private void loadMembers() {
        userList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get uid from group members
                    String uid = "" + ds.child("uid").getValue();

                    //get info from user info
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                ModelUser model = ds.getValue(ModelUser.class);

                                userList.add(model);
                            }
                            adapterMemberList = new AdapterMemberList(GroupInfo.this, userList, groupId, myGroupRole);
                            membersRv.setAdapter(adapterMemberList);
                            TvMembers.setText("Members (" + userList.size() + ")");
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

    //back button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
