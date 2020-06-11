package com.fariza.mpezexpenses.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fariza.mpezexpenses.R;
import com.fariza.mpezexpenses.Model.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.HashMap;

public class AdapterMemberAdd extends RecyclerView.Adapter<AdapterMemberAdd.HolderParticipantAdd> {

    private Context context;
    private ArrayList<ModelUser> userList;
    private String groupId, myGroupRole; //creator/admin/member

    public AdapterMemberAdd(Context context, ArrayList<ModelUser> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.model_member_add, parent, false);

        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
        //get data
        final ModelUser modelUser = userList.get(position);
        String name = modelUser.getName();
        String email = modelUser.getEmail();
        String image = modelUser.getImage();
        final String uid = modelUser.getUid();

        //set data
        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_person_primary).into(holder.avatarIv);
        }
        catch (Exception e){
            holder.avatarIv.setImageResource(R.drawable.ic_person_primary);
        }
        checkIfAlreadyExists(modelUser, holder);

        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check user if already add or not
                //added - make admin/remove admin
                //not added = show option
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Members").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    //user exists/not-participant
                                    String hisPreviousRole = "" + dataSnapshot.child("Role").getValue();

                                    //options to display in dialog
                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Option");
                                    if (myGroupRole.equals("Creator")) {
                                        if (hisPreviousRole.equals("Admin")) {
                                            //i'm creator, he also an admin
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if (which == 0) {
                                                        //Remove Admin
                                                        removeAdmin(modelUser);
                                                    } else {
                                                        //Remove User
                                                        removeMember(modelUser);
                                                    }
                                                }
                                            }).show();
                                        } else if (hisPreviousRole.equals("Member")) {
                                            //i'm creator, he is participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if (which == 0) {
                                                        //Make Admin
                                                        makeAdmin(modelUser);
                                                    } else {
                                                        //Remove User
                                                        removeMember(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    } else if (myGroupRole.equals("Admin")) {
                                        if (hisPreviousRole.equals("Creator")) {
                                            //i'm admin, he is creator
                                            Toast.makeText(context, "Creator of Group", Toast.LENGTH_SHORT).show();
                                        } else if (hisPreviousRole.equals("Admin")) {
                                            //i'm admin, he is admin too
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if (which == 0) {
                                                        //Remove Admin
                                                        removeAdmin(modelUser);
                                                    } else {
                                                        //Remove User
                                                        removeMember(modelUser);
                                                    }
                                                }
                                            }).show();
                                        } else if (hisPreviousRole.equals("Member")) {
                                            //i'm admin, he is member
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if (which == 0) {
                                                        //Remove Admin
                                                        makeAdmin(modelUser);
                                                    } else {
                                                        //Remove User
                                                        removeMember(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                }
                                else{
                                    //user doesn't exist//not-participant.. add user
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Member")
                                            .setMessage("Add this user in this group")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //add user
                                                    addMember(modelUser);
                                                }
                                            })
                                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });
    }

    private void makeAdmin(ModelUser modelUser) {
        //setup user data - change role
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("Role", "Admin"); //role : member/admin/creator
        //update role in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(modelUser.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //made admin
                        Toast.makeText(context, "The user is now an admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeAdmin(ModelUser modelUser) {
        //remove admin
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("Role", "Member"); //change role
        //update role in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(modelUser.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //made admin
                        Toast.makeText(context, "The user is no longer an admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addMember(ModelUser modelUser) {
        //add user in group
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", modelUser.getUid());
        hashMap.put("name & email", modelUser.getName() + " " + modelUser.getEmail());
        hashMap.put("Role", "Member");
        hashMap.put("timestamp", ""+timestamp);

        //add the user in the group->groupId->Participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(modelUser.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                        Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed add user
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeMember(ModelUser modelUser) {
        //remove member from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(modelUser.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //removed successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed remove member
                    }
                });
    }


    private void checkIfAlreadyExists(ModelUser modelUser, final HolderParticipantAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(modelUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            //already exists
                            String hisRole = ""+dataSnapshot.child("Role").getValue();
                            holder.statusTv.setText(hisRole);
                        }
                        else {
                            //doesn't exists
                            holder.statusTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder{

        private ImageView avatarIv;
        private TextView nameTv, emailTv, statusTv;

        public HolderParticipantAdd(@NonNull View itemView){
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
