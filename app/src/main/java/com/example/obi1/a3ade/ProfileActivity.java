package com.example.obi1.a3ade;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

public class ProfileActivity extends AppCompatActivity {

    private StoreActivity.ProfileInfo mUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView profilePic = (ImageView)findViewById(R.id.profile_pic_iv);
        TextView profileUsername = (TextView)findViewById(R.id.username_tv);
        TextView profileEmail = (TextView)findViewById(R.id.user_email_tv);
        final EditText profileFullName = (EditText)findViewById(R.id.fullname_et);
        final EditText profileAddress = (EditText)findViewById(R.id.address_et);
        final EditText profilePhoneNumber = (EditText)findViewById(R.id.phone_number_et);
        final EditText profileOtherInfo = (EditText)findViewById(R.id.otherinfo_et);
        Button button = (Button)findViewById(R.id.savedetails_b);

        if(Dashboard.userProfile.size() == 0){
            mUserProfile = new StoreActivity.ProfileInfo();
        }else mUserProfile = (StoreActivity.ProfileInfo) Dashboard.userProfile.get(0);

        profilePic.setImageResource(R.drawable.ic_launcher_foreground);
        profileUsername.setText(FirebaseUtil.userName);
        profileEmail.setText(FirebaseUtil.userEmail);
        if(mUserProfile != null){
            profileFullName.setText(mUserProfile.getFullName());
            profileAddress.setText(mUserProfile.getAddress());
            profilePhoneNumber.setText(mUserProfile.getPhoneNumber());
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserProfile.setFullName(profileFullName.getText().toString());
                mUserProfile.setAddress(profileAddress.getText().toString());
                mUserProfile.setPhoneNumber(profilePhoneNumber.getText().toString());
                //mUserProfile.setOtherInfo(profileOtherInfo.getText().toString());
                DatabaseReference profileReference = FirebaseUtil.mUserDataReference.child("profile").child("profile_info");
                profileReference.setValue(mUserProfile);
                Toast.makeText(ProfileActivity.this, "Details updated!", Toast.LENGTH_LONG).show();
                finish(); //Back to the previous activity.
            }
        });
    }
}


