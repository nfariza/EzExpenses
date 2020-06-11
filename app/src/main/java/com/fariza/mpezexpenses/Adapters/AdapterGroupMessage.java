package com.fariza.mpezexpenses.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fariza.mpezexpenses.R;
import com.fariza.mpezexpenses.Model.ModelGroupList;
import com.fariza.mpezexpenses.Model.ModelGroupMessage;
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

public class AdapterGroupMessage extends RecyclerView.Adapter<AdapterGroupMessage.HolderGroupMessage>{

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelGroupMessage> modelGroupMessageList;
    private String groupId;

    private FirebaseAuth firebaseAuth;

    public AdapterGroupMessage(Context context, ArrayList<ModelGroupMessage> modelGroupMessageList){
        this.context = context;
        this.modelGroupMessageList = modelGroupMessageList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.model_group_message_right, parent, false);
            return new HolderGroupMessage(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.model_group_message_left, parent, false);
            return new HolderGroupMessage(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupMessage holder, final int position) {
        //get data
        ModelGroupMessage model = modelGroupMessageList.get(position);
        String timestamp = model.getTimestamp();
        String message = model.getMessage();
        String senderUid = model.getSender();
        String dataType = model.getType();

        //convert timestamp to time
        Calendar c = Calendar.getInstance(Locale.ENGLISH);
        c.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", c).toString();

        //set data
        if (dataType.equals("text")) {
            holder.image.setVisibility(View.GONE);
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setText(message);
        }
        else {
            holder.image.setVisibility(View.VISIBLE);
            holder.message.setVisibility(View.GONE);
            try{
                Picasso.get().load(message).placeholder(R.drawable.ic_gallery_black).into(holder.image);
            } catch (Exception e) {
                holder.image.setImageResource(R.drawable.ic_gallery_black);
            }
        }
        holder.time.setText(dateTime);
        setUserName(model, holder);
    }

    private void setUserName(ModelGroupMessage model, final HolderGroupMessage holder) {
        //get sender info from uid in model
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(model.getSender())
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

    @Override
    public int getItemCount() {
        return modelGroupMessageList.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (modelGroupMessageList.get(position).getSender().equals(firebaseAuth.getUid())){
            //if the sender is the user, the message wll appear at the right side
            return MSG_TYPE_RIGHT;
        }
        else {
            //the other housemates' message in the house group
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupMessage extends RecyclerView.ViewHolder{

        private TextView name, message, time;
        private ImageView image;

        public HolderGroupMessage(@NonNull View itemView){
            super(itemView);

            name = itemView.findViewById(R.id.TvName);
            message = itemView.findViewById(R.id.TvMessage);
            time = itemView.findViewById(R.id.TvTime);
            image = itemView.findViewById(R.id.IvMessage);
        }
    }
}
