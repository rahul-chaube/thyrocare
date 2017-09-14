package com.demo.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.demo.model.FirebaseUserDetail;
import com.demo.model.UserDetail;
import com.demo.utitlity.DataAttributes;
import com.demo.R;
import com.demo.utitlity.FirebaseConstant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.client.androidlegacy.HelpActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

import io.realm.Realm;

public class    UserProfile extends AppCompatActivity {
    ImageView imageButton;
    String uid, name, gender, yearOfBirth, careOf, villageTehsil, postOffice, district, state, postCode;
    EditText editTextName, editTextEmail, editTextMobile, editTextDob, editTextAddress, editTextPinCode, editTextMedicalHistory;
    Button buttonSave;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userDetailRef = database.getReference(FirebaseConstant.USERDETIL);
    DatabaseReference testingNode = database.getReference("Test");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        imageButton = (ImageView) findViewById(R.id.imageViewScanner);
        editTextAddress = (EditText) findViewById(R.id.address);
        editTextName = (EditText) findViewById(R.id.name);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextMobile = (EditText) findViewById(R.id.mobileNumber);
        editTextDob = (EditText) findViewById(R.id.dob);
        editTextPinCode = (EditText) findViewById(R.id.pinCode);
        editTextMedicalHistory = (EditText) findViewById(R.id.medicalHistory);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openScanner();
            }
        });
        buttonSave = (Button) findViewById(R.id.btnSave);
        {
            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveOnFirebase();
                }
            });
        }
        String getkey=testingNode.push().getKey();
        testingNode.child(getkey).setValue(System.currentTimeMillis());
    }

    void openScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan a Aadharcard QR Code");
        integrator.setResultDisplayDuration(500);
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            //we have a result
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            // process received data
            if (scanContent != null && !scanContent.isEmpty()) {
                processScannedData(scanContent);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Scan Cancelled", Toast.LENGTH_SHORT);
                toast.show();
            }

        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    protected void processScannedData(String scanData) {
        Log.e("Rajdeol", scanData);
        XmlPullParserFactory pullParserFactory;

        try {
            // init the parserfactory
            pullParserFactory = XmlPullParserFactory.newInstance();
            // get the parser
            XmlPullParser parser = pullParserFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(scanData));

            // parse the XML
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d("Rajdeol", "Start document");
                } else if (eventType == XmlPullParser.START_TAG && DataAttributes.AADHAAR_DATA_TAG.equals(parser.getName())) {
                    // extract data from tag
                    //uid
                    uid = parser.getAttributeValue(null, DataAttributes.AADHAR_UID_ATTR);
                    //name
                    name = parser.getAttributeValue(null, DataAttributes.AADHAR_NAME_ATTR);
                    //gender
                    gender = parser.getAttributeValue(null, DataAttributes.AADHAR_GENDER_ATTR);
                    // year of birth
                    yearOfBirth = parser.getAttributeValue(null, DataAttributes.AADHAR_YOB_ATTR);
                    // care of
                    careOf = parser.getAttributeValue(null, DataAttributes.AADHAR_CO_ATTR);
                    // village Tehsil
                    villageTehsil = parser.getAttributeValue(null, DataAttributes.AADHAR_VTC_ATTR);
                    // Post Office
                    postOffice = parser.getAttributeValue(null, DataAttributes.AADHAR_PO_ATTR);
                    // district
                    district = parser.getAttributeValue(null, DataAttributes.AADHAR_DIST_ATTR);
                    // state
                    state = parser.getAttributeValue(null, DataAttributes.AADHAR_STATE_ATTR);
                    // Post Code
                    postCode = parser.getAttributeValue(null, DataAttributes.AADHAR_PC_ATTR);

                } else if (eventType == XmlPullParser.END_TAG) {
                    Log.d("Rajdeol", "End tag " + parser.getName());

                } else if (eventType == XmlPullParser.TEXT) {
                    Log.d("Rajdeol", "Text " + parser.getText());

                }
                // update eventType
                eventType = parser.next();
            }

            // display the data on screen
            displayScannedData();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

    }// EO function

    void displayScannedData() {
        Toast.makeText(this, name + "   and " + uid, Toast.LENGTH_SHORT).show();
        editTextName.setText(name);
        editTextDob.setText(yearOfBirth);
        editTextPinCode.setText(postOffice);
        editTextAddress.setText(careOf + "  " + villageTehsil + " \n " + postOffice + "  " + district + " " + state);
    }

    void saveOnFirebase() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            final String name = user.getDisplayName();
            final String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
            final String uid = user.getUid();
            FirebaseUserDetail userDetail=new FirebaseUserDetail();
            userDetail.setUserId(uid);
            userDetail.setUserName(editTextName.getText().toString());
            userDetail.setUserEmail(email);
            userDetail.setUserDOB(editTextDob.getText().toString());
            userDetail.setUserAddress(editTextAddress.getText().toString());
            userDetail.setUserGender("male");
            userDetail.setUserMobileNumber(editTextMobile.getText().toString());
            userDetail.setUserPinCode(editTextPinCode.getText().toString());
            userDetail.setUserMedicalHistory(editTextMedicalHistory.getText().toString());

//            Realm realm = Realm.getDefaultInstance();
////            final UserDetail userDetail=realm.where(UserDetail.class).findFirst();
//            realm.executeTransaction(new Realm.Transaction() {
//                @Override
//                public void execute(Realm realm) {
//
//                    realm.insertOrUpdate(userDetail);
//                }
//            });

            userDetailRef.child(uid).setValue(userDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(UserProfile.this, "User Detail Updated Success Fully", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}