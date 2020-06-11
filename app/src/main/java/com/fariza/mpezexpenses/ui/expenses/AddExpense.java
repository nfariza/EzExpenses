package com.fariza.mpezexpenses.ui.expenses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fariza.mpezexpenses.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AddExpense extends AppCompatActivity {

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static int IMAGE_PICK_CAMERA_CODE = 300;
    private static int IMAGE_PICK_GALLERY_CODE = 400;
    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image url
    Uri uri;

    //actionbar
    private ActionBar actionBar;

    private String groupId, date, expenseId;
    private EditText expenseAmount;
    private TextView pickDate, fileSelected;
    private Button btnDate, selectFile, uploadFile;
    private Spinner spinnerExpense, spinnerPaid;
    private FloatingActionButton btnAddExpense;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog pd;
    private int countMembers;
    private String countMem;
    Double divide;
    String eachPay;
    String fileUpload;
    String storagePath = "Groups_Expenses_File";
    Calendar c;
    DatePickerDialog dpd;
    private ImageView attachFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        actionBar = getSupportActionBar();
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("Add Expenses");
        }

        expenseAmount = (EditText)findViewById(R.id.amount);
        spinnerExpense = (Spinner)findViewById(R.id.spinnerExpenses);
        spinnerPaid = (Spinner)findViewById(R.id.spinnerPaid);
        pickDate = (TextView)findViewById(R.id.datepick);
        btnDate = (Button)findViewById(R.id.btndate);
        selectFile = (Button)findViewById(R.id.selectFile);
        fileSelected = (TextView)findViewById(R.id.fileSelected);
        btnAddExpense = (FloatingActionButton)findViewById(R.id.btn_addexpenses);
        attachFile = (ImageView) findViewById(R.id.attachFile);

        //get id of the group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        //checkUser();

        spinnerExpenses();
        getPaidData();
        pickDate();

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilePickDialog();

            }
        });

        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddExpense();
            }
        });

    }

    //list of expenses type
    //spinner expenses function
    private void spinnerExpenses(){
        List<String>ExpensesCategories = new ArrayList<>();
        ExpensesCategories.add(0, "Please select an option");
        ExpensesCategories.add("Rent");
        ExpensesCategories.add("Electricity");
        ExpensesCategories.add("Water");
        ExpensesCategories.add("Gas");
        ExpensesCategories.add("Groceries");
        ExpensesCategories.add("Entertainment");
        ExpensesCategories.add("Internet");
        ExpensesCategories.add("Maintenance");
        ExpensesCategories.add("Others");

        ArrayAdapter<String> ExpensesAdapter = new ArrayAdapter<String>(AddExpense.this, android.R.layout.simple_spinner_item, ExpensesCategories);
        ExpensesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpense.setAdapter(ExpensesAdapter);

        spinnerExpense.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).equals("Please select an option")) {
                    //do nothing
                } else {
                    String item = parent.getItemAtPosition(position).toString();
                    Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(parent.getContext(), "Please select an option", Toast.LENGTH_LONG).show();
            }
        });
    }

    //add expenses function
    private void startAddExpense() {
        pd = new ProgressDialog(this);
        pd.setMessage("Add Expenses");

        final String g_timestamp = System.currentTimeMillis() + "";
        final String expensesType = spinnerExpense.getSelectedItem().toString();
        final String payerName = spinnerPaid.getSelectedItem().toString();

        if (expensesType.equals("Please select an option")){
            Toast.makeText(getApplicationContext(), "Please choose expenses type", Toast.LENGTH_SHORT).show();
        }
        else {
            if (TextUtils.isEmpty(expenseAmount.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            } else {

                if (date == null) {
                    Toast.makeText(getApplicationContext(), "Please enter expenses date", Toast.LENGTH_SHORT).show();
                } else {
                    if (payerName.equals("Please choose an option")){
                        Toast.makeText(getApplicationContext(), "Please choose a payer", Toast.LENGTH_LONG).show();
                    }
                    else {
                        final Double expAmount = Double.parseDouble(expenseAmount.getText().toString().trim());
                        divide = expAmount / countMembers;
                        eachPay = Double.toString(divide);

                        //add expenses without image
                        if (uri == null) {
                            addExpense("" + g_timestamp, "" + expensesType, expAmount, "" + date, "" + payerName, "" + countMembers, "" + eachPay, "");
                        } else {
                            //add expenses with image
                            //image name and path
                            String fileNameAndPath = "Expenses_Images/" + storagePath + fileUpload + g_timestamp;
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
                            storageReference.putFile(uri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                            while (!uriTask.isSuccessful()) ;
                                            Uri downloadUrl = uriTask.getResult();
                                            if (uriTask.isSuccessful()) {
                                                addExpense("" + g_timestamp, "" + expensesType, expAmount, "" + date, "" + payerName, "" + countMembers, "" + eachPay, "" + downloadUrl);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //failed
                                            pd.dismiss();
                                            Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
            }
        }

    }

    //store group expenses in database ("Expenses")
    private void addExpense(final String timestamp, String expensesType, final Double expensesAmount, String date, String payerName, String countMem, String eachPay, String uriFile) {
        //setup expenses info
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("expenseId", "" + timestamp);
        hashMap.put("recordBy", "" + firebaseAuth.getUid());
        hashMap.put("expenses", "" + expensesType);
        hashMap.put("expensesAmount", "" + expensesAmount);
        hashMap.put("date", "" + date);
        hashMap.put("payer", ""+ payerName);
        hashMap.put("totalMembers", countMem);
        hashMap.put("eachPay", eachPay);
        hashMap.put("expensesFile", "" + uriFile);
        hashMap.put("timestamp", "" + timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);
        ref.child("Expenses").child(timestamp).setValue(hashMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "Expenses Added", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddExpense.this, ExpenseHistory.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        pd.dismiss();
                        Toast.makeText(AddExpense.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddExpense.this, AddExpense.class);
                        startActivity(intent);
                    }
                });
    }

    //choose image source
    private void showFilePickDialog() {
        //option to select image
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which==0) {
                            //camera clicked
                            if (!checkCameraPermissions()) {
                                requestCameraPermissions();
                            } else {
                                pickFromCamera();
                            }
                        }
                        else {
                            //gallery clicked
                            if (!checkStoragePermissions()) {
                                requestStoragePermissions();
                            }
                            else {
                                pickFromGallery();
                            }
                        }
                    }
                }).show();
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

    //if accepted redirect to function for pick image source
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

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
        }
    }

    //select file function and set textview and imageview
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //check user has selected or not
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                attachFile.setImageURI(uri);
                fileSelected.setText("One file selected");
            }
            else if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //was picked from gallery
                uri = data.getData();
                attachFile.setImageURI(uri);
                fileSelected.setText("One file selected");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //pick date of expenses using calendar
    private void pickDate() {
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c= Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                final int month = c.get(Calendar.MONTH);
                final int day = c.get(Calendar.DAY_OF_MONTH);

                dpd = new DatePickerDialog(AddExpense.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int mYear, int mMonth, int mDay) {
                        date = mDay + "/" + (mMonth+1) + "/" + mYear;
                        pickDate.setText(date);
                    }
                }, day, month, year);
                dpd.show();

            }
        });
    }

    //get list of payers
    public void getPaidData(){
        final List<String> userName = new ArrayList<String>();
        userName.add(0, "Please choose an option");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Groups").child(groupId).child("Members");

        Query query = databaseReference.orderByChild("uid");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String name = userSnapshot.child("name & email").getValue(String.class);
                    userName.add(name);
                    if (dataSnapshot.exists()) {
                        countMembers = (int) dataSnapshot.getChildrenCount();
                        countMem = Integer.toString(countMembers);
                    }
                }

                ArrayAdapter<String> UsersAdapter = new ArrayAdapter<String>(AddExpense.this, android.R.layout.simple_spinner_item, userName);
                UsersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPaid.setAdapter(UsersAdapter);

                //addListenerOnButton();

                spinnerPaid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (parent.getItemAtPosition(position).equals("Please choose an option")) {
                     //do nothing
                        } else {
                            String item = parent.getItemAtPosition(position).toString();
                            Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
                        }
                        }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddExpense.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
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