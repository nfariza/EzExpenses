package com.fariza.mpezexpenses.ui.group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.fariza.mpezexpenses.Adapters.AdapterGroupMessage;
import com.fariza.mpezexpenses.Model.ModelGroupMessage;
import com.fariza.mpezexpenses.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;


public class GroupHomepage extends AppCompatActivity {

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static int IMAGE_PICK_CAMERA_CODE = 300;
    private static int IMAGE_PICK_GALLERY_CODE = 400;
    //permission arrays
    String[] cameraPermissions;
    String[] storagePermissions;

    //picked image url

    Uri uri = null;

    private FirebaseAuth firebaseAuth;
    private String groupId, myGroupRole="";
    private Toolbar toolbar;
    private ImageView groupIconIv;
    private TextView groupNameTv;
    private EditText message;
    private ImageButton attach, send;
    private RecyclerView chatRv;

    private ArrayList<ModelGroupMessage> groupMessageList;
    private AdapterGroupMessage adapterGroupMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_homepage);

        toolbar = findViewById(R.id.toolbar);
        groupIconIv = findViewById(R.id.groupIcon);
        groupNameTv = findViewById(R.id.groupName);
        message = findViewById(R.id.ETmessage);
        attach = findViewById(R.id.attachBtn);
        send = findViewById(R.id.sendBtn);
        chatRv = findViewById(R.id.chatRv);

        setSupportActionBar(toolbar);

        //get id of the group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupMessage();
        loadMyGroupRole();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enter message
                String groupMessage = message.getText().toString().trim();
                //validate
                if (TextUtils.isEmpty(groupMessage)){
                    Toast.makeText(GroupHomepage.this, "Can't send empty message", Toast.LENGTH_SHORT).show();
                }
                else{
                    //send message
                    sendMessage(groupMessage);
                }
            }
        });

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageMessageDialog();
            }
        });
    }

    //choose image source
    private void showImageMessageDialog() {
        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //camera clicked
                    if (!checkCameraPermissions()) {
                        requestCameraPermissions();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if (which == 1){
                    //gallery clicked
                    if (!checkStoragePermissions()) {
                        requestStoragePermissions();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    //access device gallery
    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    //access device camera
    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Group Image Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Description");
        uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    //check storage permission for gallery
    private boolean checkStoragePermissions(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    //request storage permission
    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    //check camera and storage permission for camera
    private boolean checkCameraPermissions(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //request camera permission
    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //send image message
    private void sendImageMessage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Please wait");
        pd.setMessage("Sending Message...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        //save in firebase storage
        String fileNameAndPath = "Chat_Images/" + ""+System.currentTimeMillis();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
        storageReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUrl = uriTask.getResult();

                        if (uriTask.isSuccessful()) {
                            //timestamp
                            String timestamp = ""+System.currentTimeMillis();

                            //message info
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", ""+ firebaseAuth.getUid());
                            hashMap.put("message", ""+ downloadUrl);
                            hashMap.put("timestamp", ""+timestamp);
                            hashMap.put("type", "" + "image"); //any data

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                            reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            message.setText("");
                                            pd.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(GroupHomepage.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupHomepage.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });
    }

    //if accepted redirect to function for pick image source
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //handle permission result
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //permission allowed
                        pickFromCamera();
                    }
                    else {
                        //both or either is denied
                        Toast.makeText(this, "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length >0){
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission allowed
                        pickFromGallery();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    //select file function
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //handle image pick result
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //was picked from gallery
                uri = data.getData();
                sendImageMessage();
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //was picked from camera
                sendImageMessage();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //retrieved group message in database ("Message)" to display all message in the group
    private void loadGroupMessage() {
        //init list
        groupMessageList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        groupMessageList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelGroupMessage model = ds.getValue(ModelGroupMessage.class);
                            groupMessageList.add(model);
                        }
                        adapterGroupMessage = new AdapterGroupMessage(GroupHomepage.this, groupMessageList);
                        chatRv.setAdapter(adapterGroupMessage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    //store group message that the user sent in database ("Messages")
    private void sendMessage(String groupMessage) {
        //timestamp
        String timestamp = ""+System.currentTimeMillis();

        //message info
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", ""+ firebaseAuth.getUid());
        hashMap.put("message", ""+groupMessage);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("type", "" + "text"); //any data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        message.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupHomepage.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //used for menu(onPrepareOptionsMenu) to identify member's role and the menu will be refreshed, every time the user click the action bar
    //to ensure that the menu will be updated according to the member's role and if there will be changes in member's role
    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            myGroupRole = ""+ds.child("Role").getValue();
                            //refresh menu item
                            invalidateOptionsMenu();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    //retrieved data from database ("Groups") to retrieved group name and group icon to display at action bar
    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String groupName = ""+ds.child("groupName").getValue();
                            String groupIcon = ""+ds.child("groupIcon").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String createdBy = ""+ds.child("createdBy").getValue();

                            groupNameTv.setText(groupName);
                            try {
                                Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_white).into(groupIconIv);
                            }
                            catch (Exception e){
                                groupIconIv.setImageResource(R.drawable.ic_group_white);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    //create condition for the menu, if the user admin and creator, add member menu will appear
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (myGroupRole.equals("Creator") || myGroupRole.equals("Admin")) {
            //admin/creator - show add person option
            menu.findItem(R.id.addMember).setVisible(true);

        }
        else {
            menu.findItem(R.id.addMember).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //create menu group at action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group, menu);
            return super.onCreateOptionsMenu(menu);
        }

    //select menu option
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addMember){
            Intent intent = new Intent(this, AddMember.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        else if (id == R.id.infoGroup){
            Intent intent = new Intent(this, GroupInfo.class);
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
