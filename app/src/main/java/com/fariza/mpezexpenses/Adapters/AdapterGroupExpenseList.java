package com.fariza.mpezexpenses.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fariza.mpezexpenses.Model.ModelGroupList;
import com.fariza.mpezexpenses.R;
import com.fariza.mpezexpenses.ui.expenses.ExpenseHistory;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterGroupExpenseList extends RecyclerView.Adapter<AdapterGroupExpenseList.HolderGroupExpenseList>{

    private Context context;
    private ArrayList<ModelGroupList> groupLists;

    public AdapterGroupExpenseList(Context context, ArrayList<ModelGroupList> groupLists) {
        this.context = context;
        this.groupLists = groupLists;
    }

    @NonNull
    @Override
    public AdapterGroupExpenseList.HolderGroupExpenseList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.model_group_expenses_list, parent, false);
        return new AdapterGroupExpenseList.HolderGroupExpenseList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterGroupExpenseList.HolderGroupExpenseList holder, int position) {

        //getdata
        ModelGroupList model = groupLists.get(position);
        final String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupName = model.getGroupName();


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
                Intent intent = new Intent(context, ExpenseHistory.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return groupLists.size();
    }

    //view holder class
    class HolderGroupExpenseList extends RecyclerView.ViewHolder {

        //ui views
        private ImageView groupIconIv;
        private TextView groupName;

        public HolderGroupExpenseList(@NonNull View itemView) {
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupName = itemView.findViewById(R.id.groupName);
        }
    }
}
