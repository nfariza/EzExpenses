package com.fariza.mpezexpenses.ui.managegroup;

import androidx.core.view.MenuItemCompat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;
import com.fariza.mpezexpenses.Adapters.AdapterGroupList;
import com.fariza.mpezexpenses.R;
import com.fariza.mpezexpenses.ui.group.AddGroup;
import com.fariza.mpezexpenses.Model.ModelGroupList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageGroupFragment extends Fragment {

    private RecyclerView groupRv;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelGroupList> groupLists;
    private AdapterGroupList adapterGroupList;

    public ManageGroupFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_group, container, false);

        getActivity().setTitle("Manage Groups");

        groupRv = view.findViewById(R.id.listofgroup);

        firebaseAuth = FirebaseAuth.getInstance();
        //checkUserStatus();
        loadGroupList();

        return view;
    }

    //retrieved data from database ("Groups") to show list of groups
    private void loadGroupList() {
        groupLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupLists.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //if current user uid exists in participant list of group
                    if (ds.child("Members").child(firebaseAuth.getUid()).exists()) {
                        ModelGroupList model = ds.getValue(ModelGroupList.class);
                        groupLists.add(model);
                    }
                }
                adapterGroupList = new AdapterGroupList(getActivity(), groupLists);
                groupRv.setAdapter(adapterGroupList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //search groups using search function by enter group name
    //retrieved data from database ("Groups") for search function
    private void searchGroupList(final String query) {
        groupLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupLists.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //if current user uid exists in participant list of group then show the group
                    if (ds.child("Members").child(firebaseAuth.getUid()).exists()) {
                        //search by group name
                        if (ds.child("groupName").toString().toLowerCase().contains(query.toLowerCase())) {
                            ModelGroupList model = ds.getValue(ModelGroupList.class);
                            groupLists.add(model);
                        }
                        ModelGroupList model = ds.getValue(ModelGroupList.class);
                    }
                }
                adapterGroupList = new AdapterGroupList(getActivity(), groupLists);
                groupRv.setAdapter(adapterGroupList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //to notify that this fragment has its own menu
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    //create menu expenses at action bar
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create_group, menu);
        //menu.findItem(R.id.createGroup).setVisible(true);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if (!TextUtils.isEmpty(s.trim())){
                    searchGroupList(s);
                }
                else{
                    loadGroupList();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchGroupList(s);
                }
                else{
                    loadGroupList();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    //select menu option
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.createGroup){
            Intent intent = new Intent(getActivity(), AddGroup.class);

            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
