package com.fariza.mpezexpenses.ui.profile;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.fariza.mpezexpenses.BasicActivity.SignInActivity;

import com.fariza.mpezexpenses.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class ProfileFragment extends Fragment {

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static int IMAGE_PICK_CAMERA_CODE = 300;
    private static int IMAGE_PICK_GALLERY_CODE = 400;
    //permission arrays
    String[] cameraPermissions;
    String[] storagePermissions;

    //image url
    Uri image_uri;

    String UProfile;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    StorageReference storageReference;
    String storagePath = "Users_Profile_Image";

    FloatingActionButton btnEdit;
    ImageView avatar;
    TextView profile, profileName, pEmail, pPhone, pHobby;

    ProgressDialog pd;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        getActivity().setTitle("Profile");

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");
        storageReference = getInstance().getReference();

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        avatar = (ImageView) view.findViewById(R.id.avatarIv);
        profile = (TextView) view.findViewById(R.id.pName);
        profileName = (TextView) view.findViewById(R.id.nameP);
        pPhone = (TextView) view.findViewById(R.id.phone);
        pEmail = (TextView) view.findViewById(R.id.email);
        pHobby = (TextView) view.findViewById(R.id.hobby);
        btnEdit = (FloatingActionButton) view.findViewById(R.id.btn_edit);

        pd = new ProgressDialog(getActivity());

        //retrieved profile details from database ("Users")
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String pName = "" + ds.child("name").getValue();
                    String name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();
                    String phoneNumber = "" + ds.child("phone").getValue();
                    String email = "" + ds.child("email").getValue();
                    String hobby = "" + ds.child("hobby").getValue();

                    pEmail.setText(email);

                    if (pName.equals("")) {
                        profile.setText("Name");
                    }
                    else {
                        profile.setText(pName);
                    }

                    if (name.equals("")) {
                        profileName.setText("Name");
                    }
                    else {
                        profileName.setText(pName);
                    }
                    if (phoneNumber.equals("")) {
                        pPhone.setText("Phone Number");
                    }
                    else {
                        pPhone.setText(phoneNumber);
                    }

                    if (hobby.equals("")) {
                        pHobby.setText("Hobby");
                    }
                    else {
                        pHobby.setText(hobby);
                    }

                    try {
                        //if image is received then set
                        Picasso.get().load(image).into(avatar);
                    } catch (Exception e) {
                        //if there is any exception then set default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    //check storage permission for gallery
    private boolean checkStoragePermissions(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    //request storage permission
    private void requestStoragePermissions(){
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    //check camera and storage permission for camera
    private boolean checkCameraPermissions(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //request camera permission
    private void requestCameraPermissions(){
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
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
                        Toast.makeText(getActivity(), "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), "Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    //select file function and set imageview
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //handle image pick result
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //was picked from gallery
                //set to imageview
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
                //avatar.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //was picked from camera
                //set to imageview
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //update profile image at database ("Users")
    private void uploadProfileCoverPhoto(final Uri uri) {
        pd.show();

        String filePathAndName = "Profile_Images/" + storagePath+ "" + UProfile + "_"+ user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUrl = uriTask.getResult();

                        if (uriTask.isSuccessful()){
                            //image uploaded
                            HashMap<String, Object> results = new HashMap<>();

                            results.put(UProfile, downloadUrl.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating Image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Some error occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
        cv.put(MediaStore.Images.Media.TITLE, "Group Image Icon Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon Description");
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    //choose options to update profile
    private void showEditProfileDialog (){
        //show dialog containing option to edit profile
        String options[] = {"Edit Profile Picture", "Edit Name", "Edit Phone Number", "Edit Hobby"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //Edit Profile Picture
                    pd.setMessage("Updating Profile Picture");
                    UProfile = "image";
                    showImagePicDialog();
                }
                else if (which == 1) {
                    //Edit Name
                    pd.setMessage("Updating Name");
                    showNamePhoneHobbyUpdateDialog("name");
                }
                else if (which == 2) {
                    //Edit Phone Number
                    pd.setMessage("Updating Phone Number");
                    showNamePhoneHobbyUpdateDialog("phone");
                }
                else if (which == 3) {
                    pd.setMessage("Updating Hobby");
                    showNamePhoneHobbyUpdateDialog("hobby");
                }
            }
        });
        builder.create().show();
        }

    //update profile function and update at database ("Users")
    private void showNamePhoneHobbyUpdateDialog(final String key) {
        //update what the user want

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        //add button to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();
                //validate if user enter something or not
                if (!TextUtils.isEmpty(value)) {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else {
                    Toast.makeText(getActivity(), "Please enter " + key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //add button to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //create and show dialog
        builder.create().show();
    }

    //choose image source
    private void showImagePicDialog() {
        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    //to notify that this fragment has its own menu
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    //create menu profile at action bar
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //select menu option
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_logout){
            String dialogTitle= "Sign Out";
            String dialogDescription= "Do you want to sign out?";
            String positiveButtonTitle= "YES";
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(dialogTitle)
                    .setMessage(dialogDescription)
                    .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            Intent toMain = new Intent(getActivity(), SignInActivity.class);
                            toMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(toMain);
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
        return super.onOptionsItemSelected(item);
    }
}
