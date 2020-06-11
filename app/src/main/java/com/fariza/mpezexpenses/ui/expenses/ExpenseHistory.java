package com.fariza.mpezexpenses.ui.expenses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.fariza.mpezexpenses.Adapters.AdapterExpenseHistory;
import com.fariza.mpezexpenses.Model.ModelExpenses;
import com.fariza.mpezexpenses.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ExpenseHistory extends AppCompatActivity {

    private RecyclerView expenseRv;
    private String expenseId, groupId;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelExpenses> expenseList;
    private AdapterExpenseHistory adapterExpenseHistory;
    private TextView expenseHistory, expenseAmount, expenseDate, payer;
    private int countMembers;
    private TextView payAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_history);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        actionBar = getSupportActionBar();
        actionBar.setTitle("Expenses History");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        //init views
        expenseRv = findViewById(R.id.expenseRv);

        expenseHistory = findViewById(R.id.expenseType);
        payer = findViewById(R.id.payerName);
        expenseAmount = findViewById(R.id.expenseAmount);
        expenseDate = findViewById(R.id.expenseDate);
        payAmount = findViewById(R.id.payAmount);

        expensesList();
    }

    //retrieved data from database ("Expenses") to display expenses history
    private void expensesList () {
        expenseList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("Expenses");
           reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    expenseList.clear();
                    if(dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelExpenses model = ds.getValue(ModelExpenses.class);
                            //if (ds.child("expenseId").child(expenseId).exists()) {
                                expenseList.add(model);
                        }
                        adapterExpenseHistory = new AdapterExpenseHistory(ExpenseHistory.this, expenseList);
                        expenseRv.setAdapter(adapterExpenseHistory);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    //search expenses using search function by enter expenses type
    //retrieved data from database ("Expenses") for search function
    private void searchExpenseList(final String query) {
        expenseList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("Expenses");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                expenseList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        //search by expense name
                        if (ds.child("expenses").toString().toLowerCase().contains(query.toLowerCase())) {
                            ModelExpenses model = ds.getValue(ModelExpenses.class);
                            expenseList.add(model);
                        }
                        ModelExpenses model = ds.getValue(ModelExpenses.class);
                    }
                adapterExpenseHistory = new AdapterExpenseHistory(ExpenseHistory.this, expenseList);
                expenseRv.setAdapter(adapterExpenseHistory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //create menu expenses at action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_expenses, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if (!TextUtils.isEmpty(s.trim())){
                    searchExpenseList(s);
                }
                else{
                    expensesList();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchExpenseList(s);
                }
                else{
                    expensesList();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //select menu option
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addExpenses){
            //redirect to add expenses page
            Intent intent = new Intent(this, AddExpense.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //back button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}
