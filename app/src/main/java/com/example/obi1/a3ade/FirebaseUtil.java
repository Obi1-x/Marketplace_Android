package com.example.obi1.a3ade;

import android.app.Activity;
import android.graphics.Path;
import android.net.Uri;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FirebaseUtil {
    public static FirebaseDatabase myFirebase;
    public static DatabaseReference mydatabase;
    private static FirebaseUtil firebaseUtil; //Reference to firebase
    public static FirebaseAuth myFirebaseAuth; //FB authentication variable
    public static FirebaseAuth.AuthStateListener myAuthListener; //FB authentication listener variable
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageRef;
    public static final int RC_SIGN_IN = 123;
    private static Activity caller; // Caller activity variable
    public static String userName;
    public static String userEmail;
    public static DatabaseReference mUserDataReference;
    public static String userStatus = "Off";
    public static DatabaseReference mAppDataReference;

    private FirebaseUtil(){}//Private constructor to avoid this class from getting instantiated

    public static void openFbReference(String ref, final Activity activity) {
        if (firebaseUtil == null){
            firebaseUtil = new FirebaseUtil(); //New firebase util instance
            //Create Firebase db refernce.
            myFirebase = FirebaseDatabase.getInstance(); //Instance of Firebase DB
            //mydatabase = myFirebase.getReference("message");
            myFirebaseAuth = FirebaseAuth.getInstance(); //FB auth object initialization
            caller = activity; //Starts the login activity

            myAuthListener = new FirebaseAuth.AuthStateListener() {  //FB auth LIstener object.
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null) { //This checks if the current user is not signed in
                        signIn(); //Signin if not

                        Log.d("LogIn", "User Logging In");
                    } else {
                        String userId = firebaseAuth.getUid(); //Retrieves the user id of the current user
                        //checkAdmin(userId); //Checks if the user is an admin with the acquired id
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        userName = user.getDisplayName(); //Used to get username.
                        userEmail = user.getEmail();
                        mUserDataReference = mydatabase.child("userData").child(userName); //Gets username database reference.
                        mAppDataReference = mydatabase.child("appData");//Gets dasboardfeeds database reference.
                        if(userStatus.equals("Off")){ //Used to avoid repeating a database read and therefore populating the Arrays.
                            userStatus = "Online";
                            mUserDataReference.addChildEventListener(Dashboard.mChildListener);
                            mUserDataReference.child("status").setValue(userStatus);
                            //mAppDataReference.addChildEventListener(Dashboard.mChildListener);  //orderByChild("Tstamp")
                            mAppDataReference.child("dashboardfeeds").orderByChild("VSOrank").addChildEventListener(Dashboard.mChildListener); //Was on Tstamp.
                            mAppDataReference.child("Synced with").child(userName.toUpperCase()).setValue(userName);
                        }
                    }
                }
            };
            mStorage = FirebaseStorage.getInstance(); //Instance of firebase storage
        }

        //mDeals = new ArrayList<TravelDealNG>(); //New Empty array
        mydatabase = myFirebase.getReference().child(ref); //Opens the path that was passed in as an argument (parameter) tradeData
    }

    static void downloadImages() {
        if(mStorageRef == null) { //Recover images.
            mStorageRef = mStorage.getReference().child("Trade").child(userName);

        }

        //Downloading Images related to your stores and products.
        //Comparing the images.
        int i;
        File tradeStoreFiles = new File(Dashboard.mAppDirectory, "Stores");  //Get the file directory for Stores.
        for(i=0; i<Dashboard.myStoresList.size(); i++){
            StoreActivity.StoreInfo store = Dashboard.myStoresList.get(i);
            File fileCursor = new File(tradeStoreFiles, store.getStoreName() + ".jpg"); //Get the store name and add to the file uri
            if(!fileCursor.exists() && i>0){  //Checks if that image exists;
                StorageReference imageWebReference = mStorageRef.child("Stores").child(store.getStoreName());
                imageWebReference.getFile(Uri.parse(store.getImageUri())); //Getting the image.
            }
        }

        downloadNeededProductImages(Dashboard.myProductsList, 0); //Downloading Images related to your products.
        downloadNeededProductImages(Dashboard.dashboardFeeds, -1); //Downloading Images related to other vendor's products.
    }

    private static void downloadNeededProductImages(ArrayList<StoreActivity.ProductInfo> aProductsList, int offset) {
        int i,j;
        File tradeProductFiles = new File(Dashboard.mAppDirectory, "Products");  //Get the file directory for Products.
        tradeProductFiles.mkdir();
        for(j=0; j<aProductsList.size(); j++){
            StoreActivity.ProductInfo product = aProductsList.get(j);
            for(i=0; i<4; i++){
                String productSurffix = product.getProductName() + (300 + i);  //Get the store name and add 300++ surffix.
                File productsFileCursorFolder = new File(tradeProductFiles, product.getProductName() + " images"); //Get the store name and add to the file uri
                productsFileCursorFolder.mkdir();
                File productsFileCursor = new File(productsFileCursorFolder, productSurffix + ".jpg");
                if(!productsFileCursor.exists() && j>offset){  //&& product.getUserName().equals(userName)
                    String url = "Trade/" + product.getUserName() + "/Products/" + product.getProductName() + " images/" + productSurffix;
                    StorageReference imageWebReference = mStorage.getReference().child(url);
                    imageWebReference.getFile(productsFileCursor).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Dashboard.dasRecyclerAdapter.notifyDataSetChanged();
                        }
                    });/*.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Failed message", "Image download failed ");
                        }
                    });*/
                }
            }
        }
    }

    public static void signOut() {
        FirebaseUtil.userStatus = "Off";
        FirebaseUtil.mUserDataReference.child("status").setValue("Offline");
        Dashboard.dashboardFeeds.clear(); //Clear array for
        Dashboard.myStoresList.clear();
        Dashboard.myProductsList.clear();
        Dashboard.inCartList.clear();
        Dashboard.incomingCart.clear();
        Dashboard.outgoingCart.clear();
        Dashboard.favourites.clear();
        Dashboard.ratingPerProduct.clear(); // the next logged user.

        Log.d("TO TEST OFFLINE", " Dashboard stopped, Offline.");
        AuthUI.getInstance()
                .signOut(caller)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    private static void signIn() { //Sign in method You can change back to static
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

// Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        //  .setLogo(R.drawable.travel_mantics_icon_64dp)      // Set logo drawable
                        .setTheme(R.style.AppTheme)      // Set theme
                        .build(),
                RC_SIGN_IN);

    }

    public static void detachAuthListener() {
        myFirebaseAuth.removeAuthStateListener(myAuthListener);//detach auth statelistener when not needed.
    }

    public static void attachAuthListener() {
        myFirebaseAuth.addAuthStateListener(myAuthListener); //Attach auth statelistener when needed.
    }
}
