package com.fariza.mpezexpenses.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.fariza.mpezexpenses.R;

import com.fariza.mpezexpenses.Model.ModelExpenses;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class AdapterExpenseHistory extends RecyclerView.Adapter<AdapterExpenseHistory.HolderExpenseHistory>  {

    private Context context;
    private ArrayList<ModelExpenses> expensesHistory;

    public AdapterExpenseHistory(Context context, ArrayList<ModelExpenses> expensesHistory) {
        this.context = context;
        this.expensesHistory = expensesHistory;
    }

    @NonNull
    @Override
    public HolderExpenseHistory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.model_expense_history, parent, false);
        return new HolderExpenseHistory(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderExpenseHistory holder, int position) {

        //getdata
        ModelExpenses model = expensesHistory.get(position);
        final String expensesId = model.getExpenseId();
        String expenseType = model.getExpenses();
        String expenseAmount = model.getExpensesAmount();
        String expenseDate = model.getDate();
        String payer = model.getPayer();
        String eachPay = model.getEachPay();
        String countMembers = model.getTotalMembers();
        String attachFile = model.getExpensesFile();

        //set data
        holder.expenseHistory.setText(expenseType);
        holder.expenseAmount.setText(expenseAmount);
        holder.expenseDate.setText(expenseDate);
        holder.payer.setText(payer);
        holder.payAmount.setText(eachPay);
        holder.countMember.setText(countMembers);
        try {
            Picasso.get().load(attachFile).placeholder(R.drawable.ic_group_primary).into(holder.attachFile);
        }
        catch (Exception e) {
            holder.attachFile.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return expensesHistory.size();
    }

    //view holder class
    class HolderExpenseHistory extends RecyclerView.ViewHolder {

        //ui views
        private TextView expenseHistory, expenseAmount, payer, payAmount, expenseDate, countMember;
        private ImageView attachFile;

        public HolderExpenseHistory(@NonNull View itemView) {
            super(itemView);
            expenseHistory = itemView.findViewById(R.id.expenseHistory);
            expenseAmount = itemView.findViewById(R.id.expenseAmount);
            expenseDate = itemView.findViewById(R.id.expenseDate);
            payer = itemView.findViewById(R.id.payerName);
            payAmount = itemView.findViewById(R.id.payAmount);
            countMember = itemView.findViewById(R.id.countMembers);
            attachFile = itemView.findViewById(R.id.attachFile);
        }
    }
}
