package com.fariza.mpezexpenses.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fariza.mpezexpenses.R;
import com.fariza.mpezexpenses.ui.group.GroupHomepage;
import com.fariza.mpezexpenses.Model.ModelGroupList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupList extends RecyclerView.Adapter<AdapterGroupList.HolderGroupList>{

    private Context context;
    private ArrayList<ModelGroupList> groupLists;

    public AdapterGroupList(Context context, ArrayList<ModelGroupList> groupLists) {
        this.context = context;
        this.groupLists = groupLists;
    }

    @NonNull
    @Override
    public HolderGroupList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.model_group_list, parent, false);
        return new HolderGroupList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupList holder, int position) {

        //getdata
        ModelGroupList model = groupLists.get(position);
        final String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupName = model.getGroupName();

        holder.name.setText("");
        holder.time.setText("");
        holder.message.setText("");

        //load last message
        loadLastMessage(model, holder);
        
        //set data
        holder.groupName.setText(groupName);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(holder.groupIconIv);
        }
        catch (Exception e) {
            holder.groupIconIv.setImageResource(R.drawable.ic_group_primary);
        }

        //handle group click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open group
                Intent intent = new Intent(context, GroupHomepage.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });
    }

    //to show the last message details
    private void loadLastMessage(ModelGroupList model, final HolderGroupList holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //get data
                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();
                            String dataType = ""+ds.child("type").getValue();

                            //convert timestamp to time
                            Calendar c = Calendar.getInstance(Locale.ENGLISH);
                            c.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", c).toString();

                            if(dataType.equals("image")){
                                holder.message.setText("Sent Photo");
                            }
                            else{
                                holder.message.setText(message);
                            }
                            holder.time.setText(dateTime);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds: dataSnapshot.getChildren()){
                                                String name = ""+ds.child("name").getValue();
                                                holder.name.setText(name);
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

    @Override
    public int getItemCount() {
        return groupLists.size();
    }

    //view holder class
    class HolderGroupList extends RecyclerView.ViewHolder {

        //ui views
        private ImageView groupIconIv;
        private TextView groupName;
        private TextView name, message, time;

        public HolderGroupList(@NonNull View itemView) {
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupName = itemView.findViewById(R.id.groupName);
            name = itemView.findViewById(R.id.TvName);
            message = itemView.findViewById(R.id.TvMessage);
            time = itemView.findViewById(R.id.TvTime);

        }
    }
}
