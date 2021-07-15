package com.example.obi1.a3ade;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static com.example.obi1.a3ade.FirebaseUtil.userName;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CheckoutFragment.OnFragmentInteractionListener, CartFragment.OnFragmentInteractionListener{
    private RecyclerView mdas_rv;
    public static DasRecyclerAdapter dasRecyclerAdapter;
    public static ArrayList<StoreActivity.StoreInfo> myStoresList;  //Holds list of all stores of a particular vendor
    public static ArrayList<StoreActivity.ProductInfo> dashboardFeeds; //Holds sectioned list of all products ever created in all stores in the app
    public static ArrayList<StoreActivity.ProductInfo> myProductsList; //Holds sectioned list of all products of a particular vendor.
    public static ArrayList<StoreActivity.ProductInfo> storeProducts;  //Holds list of all product in a particular store
    public static ArrayList<StoreActivity.CartListInfo> inCartList;    //Holds list of all incoming carts.
    public static ArrayList<StoreActivity.CartListInfo> outCartList;   //Holds list of all outgoing carts (incoming carts of customers that purchased a vendor's product).
    public static ArrayList<StoreActivity.ProductInfo> outgoingCart; //Holds list of all products ever bought by a different user
    public static ArrayList<StoreActivity.ProductInfo> incomingCart; //Holds list of all products ever added to cart by the user
    public static ArrayList<StoreActivity.ProductInfo> loadingCart;  //Holds list of all products in a particular cart. Was also repurposed to tempoarily hold favorites.
    //public static ArrayList<StoreActivity.ProductInfo> favourites;   //Holds list of all products of other vendors ever favourited
    public static ArrayList<HashMap<String, String>> favourites;   //Holds list of all products of other vendors ever favourited
    public static ArrayList<StoreActivity.ProfileInfo> userProfile; //Holds the profile info for this user.
    public static ArrayList ratingPerProduct; //Holds rating of a product.
    public static ChildEventListener mChildListener;
    public static boolean isDeleting = false;
    TextView selectedStoreName;
    TextView selectedSDescription;
    public static int navItemId;
    private FrameLayout mstoreInfoDisplay;
    //private MenuItem mcheckedMenuItem;
    public static StoreActivity.ProductInfo newProductTemplate;
    private StoreActivity.StoreInfo mselectedStoreInfoFromIntent;
    public static FragmentManager mFragmentManager;
    public static int number = 2;
    public static MenuItem mDeleteMenuItem;
    public static String mAppDirectory;
    private ImageView mSelectedStoreLogo;
    private ViewStub mViewStub;
    private MenuItem mEditStoreMenuItem;
    public static ProgressBar mProgressBar; //Attend to this later
    private String mInstance_is_for;
    public Toolbar mToolbar;
    private static String cameFromHome;
    public String mNavigationIntentExtra; //Used to handle online/offline flow
    //public static Fragment mSelectedFragment;


    @Override
    protected void onResume() {
        super.onResume();
        if(mInstance_is_for.equals("Dashboard")){
            String pie = FirebaseUtil.userStatus;
            int p = 0;
            if(FirebaseUtil.userStatus.equals("Online") && cameFromHome != null && cameFromHome.equals("Yes")){ //Used to handle the online/Offline status after a user previously pressed back.
                FirebaseUtil.mUserDataReference.child("status").setValue("Online");
                FirebaseUtil.mAppDataReference.child("Synced with").child(userName.toUpperCase()).setValue(userName);
                Log.d("TO TEST OFFLINE", " Dashboard  not stopped, Online.");
                cameFromHome = "NO";
            }
            FirebaseUtil.openFbReference("tradeData",Dashboard.this);
            FirebaseUtil.attachAuthListener();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if(navigationView != null){
            navigationView.setCheckedItem(R.id.dashboard);
        }

        if(navItemId == R.id.cart && mInstance_is_for.equals("Carts") || navItemId == DasRecyclerAdapter.OUTGOING_CART_ID && mInstance_is_for.equals("Carts")){  //mInstance_is_for.equals("Carts")
            updateFrame(navItemId); //Prepares the Cart Frame Layout for display;
            Fragment selectedFragment = CartFragment.newInstance("My", "Checkout");
            mFragmentManager = getSupportFragmentManager();
            FragmentTransaction fragTransaction = mFragmentManager.beginTransaction();
            fragTransaction.add(R.id.forfragment_fl, selectedFragment);
            //fragTransaction.addToBackStack("CartFragment");
            fragTransaction.commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle((CharSequence)mInstance_is_for);
        }else{
            navItemId = getIntent().getIntExtra("Selected navMenu", 0); //Intent from store activity to revive Store or products list Rv after creating new store.
            mselectedStoreInfoFromIntent = (StoreActivity.StoreInfo) getIntent().getSerializableExtra("Selected store"); //Selected store intent from StoreActivity to Open a store.
            updateAdapter(); //Creates recycler adaprter instance and associates with RV.
            handleStoreLabel(); //Handles what gets displayed on the store label.
            dasRecyclerAdapter.notifyDataSetChanged();
        }
        //FirebaseUtil.attachAuthListener();
    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FirebaseUtil.RC_SIGN_IN && resultCode == RESULT_OK){ //Handles Login message
            Toast.makeText(Dashboard.this, "Welcome back!!!", Toast.LENGTH_LONG).show();
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mViewStub = (ViewStub)findViewById(R.id.deleting_vs);
        mViewStub.inflate().setVisibility(View.GONE);
        mProgressBar = (ProgressBar)findViewById(R.id.loading_pb);
        selectedStoreName = (TextView)findViewById(R.id.storename_tv);
        selectedSDescription = (TextView)findViewById(R.id.storedes_tv);
        mSelectedStoreLogo = (ImageView)findViewById(R.id.storelogo_iv);
        mstoreInfoDisplay = (FrameLayout)findViewById(R.id.storeinfo_fl); //Frame layout reference for store name display

        mInstance_is_for = getIntent().getStringExtra("instance is for");
        if(mInstance_is_for == null || mInstance_is_for.equals("Dashboard")){
            mInstance_is_for = "Dashboard";
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

                }

                @Override
                public void onDrawerOpened(@NonNull View drawerView) {
                    TextView userName = (TextView)findViewById(R.id.navhearder_UN_tv);
                    userName.setText(FirebaseUtil.userName);  //Used to set username on the navView hearder
                }

                @Override
                public void onDrawerClosed(@NonNull View drawerView) {

                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            });

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            //toggle.setDrawerIndicatorEnabled(false);//Used to remove drawer toggle button.
            navigationView.setCheckedItem(R.id.dashboard); //Handles the indication of selected navigational menu Item

            Intent intentToService = new Intent(getApplicationContext(), CleanupService.class);
            startService(intentToService);
        }

        if(dashboardFeeds == null){
            dashboardFeeds = new ArrayList<StoreActivity.ProductInfo>();
        }

        if(myProductsList == null){
            myProductsList = new ArrayList<StoreActivity.ProductInfo>();
        }

        newProductTemplate = new StoreActivity.ProductInfo("Non","New Product", "New P","0", R.drawable.ic_das_add_black_24dp," ", userName);
        if(storeProducts == null) { //Products
            storeProducts = new ArrayList<StoreActivity.ProductInfo>();
            storeProducts.add(newProductTemplate); //New product element
        }

        if (myStoresList == null){ //Stores
            myStoresList = new ArrayList<StoreActivity.StoreInfo>();
            myStoresList.add(new StoreActivity.StoreInfo("New Store", "New S", R.drawable.ic_das_add_black_24dp)); //New store element
        }

        if(favourites == null){ //Favourites
            //favourites = new ArrayList<StoreActivity.ProductInfo>();
            favourites = new ArrayList<HashMap<String, String>>();
        }

        if(ratingPerProduct == null){
            ratingPerProduct = new ArrayList();
        }

        if(outgoingCart == null){
            outgoingCart = new ArrayList<StoreActivity.ProductInfo>();
        }
        if( userProfile == null){
            userProfile = new ArrayList<StoreActivity.ProfileInfo>();
        }

        if(mChildListener == null){
            mChildListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Iterable<DataSnapshot> familyDataSnapshot = dataSnapshot.getChildren(); //Gets Children of a particular node in an array list.
                    String route = dataSnapshot.getKey(); //Gets the key of the immediate child.
                    String referee = dataSnapshot.getRef().getParent().getParent().getKey();
                    switch (route){
                        case "stores":
                            for (DataSnapshot familyLoop : familyDataSnapshot) {
                                myStoresList.add(familyLoop.getValue(StoreActivity.StoreInfo.class));
                            }
                            break;
                        case "products":
                            for (DataSnapshot familyLoop : familyDataSnapshot) {
                                myProductsList.add(familyLoop.getValue(StoreActivity.ProductInfo.class));
                            }
                            break;
           /*             case "dashboardfeeds":
                            for (DataSnapshot familyLoop : familyDataSnapshot) {
                                dashboardFeeds.add(familyLoop.getValue(StoreActivity.ProductInfo.class));
                            }
                            break;*/
                        case "carts":
                            for (DataSnapshot familyLoop : familyDataSnapshot) {
                                inCartList.add(familyLoop.getValue(StoreActivity.CartListInfo.class));
                            }
                            break;
                        case "incomingcart":
                            for (DataSnapshot familyLoop : familyDataSnapshot) {
                                incomingCart.add(familyLoop.getValue(StoreActivity.ProductInfo.class));
                            }
                            break;
                        case "outgoingcart":
                            for (DataSnapshot familyLoop : familyDataSnapshot) {
                                outgoingCart.add(familyLoop.getValue(StoreActivity.ProductInfo.class));
                            }
                            break;
                        case "favourites":
                            for (DataSnapshot familyLoop : familyDataSnapshot) {
                                //favourites.add(familyLoop.getKey());
                                //favourites.add(familyLoop.getValue(StoreActivity.ProductInfo.class));
                                favourites.add(ExpandableListDataGen.makeFavoriteProductLink(familyLoop.getKey(), (String) familyLoop.getValue()));
                            }
                            break;
                    /*    case "ratings":
                            for (DataSnapshot familyLoop : familyDataSnapshot) {
                                ratingPerProduct.add(familyLoop.getValue(StoreActivity.RatingInfo.class));
                            }
                            break;*/
                        case "stocks":
                            for (DataSnapshot familyLoop : familyDataSnapshot) {
                                StockUpProductActivity.uploadedProductStockInfo.add(familyLoop.getValue(StockUpProductActivity.StockUpInfo.class));
                            }
                            ProductViewActivity.mTriggerStockLabels.set(ProductViewActivity.OPENED); //Signifies Stocks has finished downloading
                            break;
                        case "profile":
                            for (DataSnapshot familyLoop : familyDataSnapshot) {
                                userProfile.add(familyLoop.getValue(StoreActivity.ProfileInfo.class));
                            }
                            break;
                            default:
                                if(!Objects.equals(route, "Status") && !Objects.equals(route, "totalRating")){
                                    if(Objects.equals(referee, "productRatings")){
                                        ratingPerProduct.add(dataSnapshot.getValue(StoreActivity.RatingInfo.class));  //This is different because retrieving value of productview activity was set only one parent above, unlike others that are two parents (grandparent)
                                    }else{
                                        String dashboardfeeds = dataSnapshot.getRef().getParent().getKey();
                                        if(Objects.equals(dashboardfeeds, "dashboardfeeds")){
                                            dashboardFeeds.add(dataSnapshot.getValue(StoreActivity.ProductInfo.class));
                                        }
                                    }
                                }
                                break;
                    }
                    dasRecyclerAdapter.notifyDataSetChanged();
                    //mUserDataReference.removeEventListener(mChildListener);
                    FirebaseUtil.downloadImages();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //Toast.makeText(Dashboard.this, "Child Changed", Toast.LENGTH_SHORT).show();
                    String route = dataSnapshot.getKey(); //Gets the key of the immediate child.
                    String referee = dataSnapshot.getRef().getParent().getParent().getKey();
                    if(!Objects.equals(route, "Status") && !Objects.equals(route, "totalRating")){
                        if(Objects.equals(referee, "productRatings")){
                            boolean hasRatedbefore = ProductViewActivity.doesArrayListHaveAttribute(Dashboard.ratingPerProduct, FirebaseUtil.userName); // Used to check if this user has rated before.
                            if(hasRatedbefore){
                                StoreActivity.RatingInfo ratingBefore = (StoreActivity.RatingInfo) Dashboard.ratingPerProduct.get(ProductViewActivity.spareListIndex[0]);
                                ratingPerProduct.remove(ratingBefore);
                                //ratingPerProduct.add(ProductViewActivity.spareListIndex, dataSnapshot.getValue(StoreActivity.RatingInfo.class));
                            }
                            ratingPerProduct.add(dataSnapshot.getValue(StoreActivity.RatingInfo.class));  //This is different because retrieving value of productview activity was set only one parent above, unlike others that are two parents (grandparent)
                        }
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
        File file = getExternalFilesDir("Trade"); //Creates the app's directory
        mAppDirectory = file.getPath();

        // Recycler view reference
        mdas_rv = findViewById(R.id.dashboard_rv);
        final GridLayoutManager dasLayoutManager = new GridLayoutManager(this,2); //Grid Layout reference
        //dasLayoutManager.setReverseLayout(true);
        mdas_rv.setLayoutManager(dasLayoutManager); //Connects RV and a layout manager
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mInstance_is_for.equals("Dashboard")){ //Used to handle the online/Offline status after a user previously pressed back.
            cameFromHome = "Yes";
            //FirebaseUtil.userStatus = "Off";
            FirebaseUtil.mUserDataReference.child("status").setValue("Offline");
            Log.d("TO TEST OFFLINE", " Dashboard stopped, Offline.");
        }
    }

    @Override
    protected void onStop() {
        super.onStop(); /*
        if(mInstance_is_for.equals("Dashboard") && navItemId == R.id.dashboard || mInstance_is_for.equals("Dashboard") && navItemId == 0){
            FirebaseUtil.userStatus = "Off";
            FirebaseUtil.mUserDataReference.child("status").setValue("Offline");
            int p = 0;
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachAuthListener();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if(mNavigationIntentExtra == null || mNavigationIntentExtra.equals("Dashboard")){
            cameFromHome = "Yes";
            //FirebaseUtil.userStatus = "Off";
            FirebaseUtil.mUserDataReference.child("status").setValue("Offline");
            Log.d("TO TEST OFFLINE", " Stopped to Home, Offline.");
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        MenuItem refreshProducts = menu.findItem(R.id.refreshlist_mi);
        if(navItemId == DasRecyclerAdapter.PRODUCT_NAV_MENU){
            mDeleteMenuItem = menu.findItem(R.id.action_delete);
            mDeleteMenuItem.setVisible(true); // Make the delete MenuItem Visible.
            mEditStoreMenuItem = menu.findItem(R.id.editstore_mi);
            mEditStoreMenuItem.setVisible(true);
        }
        if(mInstance_is_for.equals("Dashboard")){
            refreshProducts.setVisible(true);
        }else refreshProducts.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.action_delete){ //Delete Menu Item pressed
            if(!isDeleting){ //isDeleting == false
                isDeleting = true;
                item.setIcon(R.drawable.ic_delete_checked_24dp);  //Used to change the delete icon color by replacing it entirely
                if(DasRecyclerAdapter.SPInfo.size() == 1){ //If all the products within a store has been deleted.
                    deletingViewStub(); //Delete the store.
                }else {                 //Else Select The products to delete.
                    updateAdapter();
                    handleStoreLabel();
                }
            }else {
                isDeleting = false;
                item.setIcon(R.drawable.ic_delete_unchecked_24dp);
                updateAdapter();
                handleStoreLabel();
            }
        }
        if(id == R.id.editstore_mi){
            StoreActivity.StoreInfo editStore = (StoreActivity.StoreInfo) myStoresList.get(StoreActivity.rowClickPos);
            Intent intent = new Intent(Dashboard.this, StoreActivity.class);
            intent.putExtra("Editing Store", editStore);
            startActivity(intent);
        }
        if(id == R.id.refreshlist_mi){
            refreshDashboard();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deletingViewStub() {
        mdas_rv.setVisibility(View.GONE);
        mstoreInfoDisplay.setVisibility(View.GONE);
        mDeleteMenuItem.setVisible(false);
        mViewStub.setVisibility(View.VISIBLE);
        Button cancelButton = (Button)findViewById(R.id.cancel_b);
        Button okButton = (Button)findViewById(R.id.ok_b);

        okButton.setOnClickListener(new View.OnClickListener() { //Not yet perfect
            @Override
            public void onClick(View v) {
                StoreActivity.StoreInfo store = myStoresList.get(StoreActivity.rowClickPos);
                File storeLocation = new File(mAppDirectory, "Stores");
                File imageLocation = new File(storeLocation, store.getStoreName() + ".jpg");
                if(imageLocation.exists()){imageLocation.delete();} //Delete store image from Local storage.
                FirebaseUtil.mStorageRef.child("Stores").child(store.getStoreName()).delete(); //Delete store image from Cloud File Storage.
                FirebaseUtil.mUserDataReference.child("stores").child(store.getStoreName()).removeValue(); //Delete store from Database tree
                myStoresList.remove(StoreActivity.rowClickPos); //Delete store from ArrayList.
                isDeleting = false;
                Toast.makeText(Dashboard.this, "Store deleted!!!", Toast.LENGTH_SHORT).show();
                mViewStub.setVisibility(View.GONE);
                mdas_rv.setVisibility(View.VISIBLE);
                navItemId = R.id.stores;
                updateFrame(navItemId);
                updateAdapter();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDeleting = false;
                mViewStub.setVisibility(View.GONE);
                mdas_rv.setVisibility(View.VISIBLE);
                mstoreInfoDisplay.setVisibility(View.VISIBLE);
                mDeleteMenuItem.setVisible(true);
                mDeleteMenuItem.setIcon(R.drawable.ic_delete_unchecked_24dp);
                updateAdapter();
                handleStoreLabel();
            }
        });
    }

    private void handleStoreLabel() {
        if(mselectedStoreInfoFromIntent != null){//Used to populate the store name and description TExtviews
            mstoreInfoDisplay.setVisibility(View.VISIBLE);
            selectedStoreName.setText(mselectedStoreInfoFromIntent.getStoreName());
            selectedSDescription.setText(mselectedStoreInfoFromIntent.getStoreDescription());
            Uri uri = Uri.parse(mselectedStoreInfoFromIntent.getImageUri());
            mSelectedStoreLogo.setImageURI(uri);
            mSelectedStoreLogo.setAdjustViewBounds(true);
            mSelectedStoreLogo.setMaxHeight(200);
            mSelectedStoreLogo.setMaxWidth(200);

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        //mcheckedMenuItem = item;
        int id = navItemId = item.getItemId();
        if(mDeleteMenuItem != null){
            mDeleteMenuItem.setVisible(false); // Makes the delete MenuItem Invisible.
        }
        if(mEditStoreMenuItem != null){
            mEditStoreMenuItem.setVisible(false); // Makes the edit store MenuItem Invisible.
        }


        if (id == R.id.profile) {
            Intent intent = new Intent(Dashboard.this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.stores) {
            Intent intent = new Intent(this, Dashboard.class);
            intent.putExtra("instance is for", "My stores"); //Find a way to remove launching animation.
            intent.putExtra("Selected navMenu", navItemId);
            mNavigationIntentExtra = intent.getStringExtra("instance is for");
            startActivity(intent);
        } else if (id == R.id.assets) {

        } else if (id == R.id.dashboard) {
            updateFrame(navItemId);
            updateAdapter();
        }else if (id == R.id.favorites) {
            //updateAdapter();
            Intent intent = new Intent(this, Dashboard.class);
            intent.putExtra("instance is for", "Favorites"); //Find a way to remove launching animation.
            intent.putExtra("Selected navMenu", navItemId);
            mNavigationIntentExtra = intent.getStringExtra("instance is for");
            startActivity(intent);
        } else if (id == R.id.cart) {
            Intent intent = new Intent(this, Dashboard.class);
            intent.putExtra("instance is for", "Carts"); //Find a way to remove launching animation.
            intent.putExtra("Selected navMenu", navItemId);
            mNavigationIntentExtra = intent.getStringExtra("instance is for");
            startActivity(intent);
        } else if (id == R.id.logout) {
            Toast.makeText(Dashboard.this, "Goodbye!", Toast.LENGTH_LONG).show();
            FirebaseUtil.signOut();
            Log.d("Logout", "User Logged Out");
            //FirebaseUtil.attachAuthListener(); //
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        //updateAdapter(); //Commenting this out might be the cause of several errors.
        return true;
    }

    public static void prepareInOutCart() {  //shorten this method by removing inCartList array from type cartListInfo
        if(incomingCart == null || incomingCart.size() == 0){
            incomingCart = new ArrayList<StoreActivity.ProductInfo>();
            incomingCart.add(new StoreActivity.ProductInfo("Non","Empty",
                    "Fascinating", "2", 0, "0", userName));
        }
        String inEmptyName = incomingCart.get(0).getProductName();
        if(incomingCart.size() >= 2 && inEmptyName.equals("Empty")){
            incomingCart.remove(0);
        }

        if(outgoingCart == null || outgoingCart.size() == 0){ //Might remove.
            outgoingCart = new ArrayList<StoreActivity.ProductInfo>();
            outgoingCart.add(new StoreActivity.ProductInfo("Non","Empty",
                    "Fascinating", "2", 0, "0", userName));
        }
        String outEmptyName = outgoingCart.get(0).getProductName();
        if(outgoingCart.size() >= 2 && outEmptyName.equals("Empty")){
            outgoingCart.remove(0);
        }

        if(inCartList == null || inCartList.size() == 0){
            inCartList = new ArrayList<StoreActivity.CartListInfo>();
            inCartList.add(new StoreActivity.CartListInfo("Empty_0",8));
        }
        String inCartEmptyName = inCartList.get(0).getCartId();
        if(inCartList.size() >= 2 && inCartEmptyName.equals("Empty_0")){
            inCartList.remove(0);
        }

        if(outCartList == null || outCartList.size() == 0){
            outCartList = new ArrayList<StoreActivity.CartListInfo>();
            outCartList.add(new StoreActivity.CartListInfo("Empty_0",8));
        }
        String outCartEmptyName = outCartList.get(0).getCartId();
        if(outCartList.size() >= 2 && outCartEmptyName.equals("Empty_0")){
            outCartList.remove(0);
        }

        if(loadingCart == null || loadingCart.size() == 0){
            loadingCart = new ArrayList<StoreActivity.ProductInfo>();
            loadingCart.add(new StoreActivity.ProductInfo("Non","Empty",
                    "Fascinating", "2", 0, "0", userName));
        }
        String loadCartEmptyName = loadingCart.get(0).getProductName();
        if(loadingCart.size() >= 2 && loadCartEmptyName.equals("Empty_0")){
            loadingCart.remove(0);
        }
    }

    private void updateFrame(int navItemId) {
        FrameLayout mainFrameLayout = (FrameLayout)findViewById(R.id.dasp_fl);
        FrameLayout fragFrameLayout = (FrameLayout)findViewById(R.id.forfragment_fl);
        if(navItemId == R.id.cart){
            mainFrameLayout.setVisibility(View.GONE);
            fragFrameLayout.setVisibility(View.VISIBLE);
        } else{
            mainFrameLayout.setVisibility(View.VISIBLE);
            fragFrameLayout.setVisibility(View.GONE);
        }
    }

    private void updateAdapter() {
        mstoreInfoDisplay.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle((CharSequence) mInstance_is_for);  //Used to Set the toolbar's title.
        if(navItemId == DasRecyclerAdapter.PRODUCT_NAV_MENU){ //==300
            //mDeleteMenuItem.setVisible(true); // Make the delete MenuItem Visible.
            //navigationView.setCheckedItem(R.id.stores);
            storeProducts.clear(); //Gets it ready for below.
            storeProducts = porpulateListWithProducts(mselectedStoreInfoFromIntent.getStoreName(), myProductsList, R.id.dashboard);//Populate storeProducts array ahead of time with products from a particular store
            toolbar.setTitle("Store products");
        } else if(navItemId == R.id.favorites){
            loadingCart.clear();
            DasRecyclerAdapter.expectedFarvouriteTally = "Complete";
            int q = 0 ;
            while(q < favourites.size()){
                HashMap needed = favourites.get(q);
                Object[] pure = needed.keySet().toArray();
                String hashProductId = (String) pure[0]; //productId;
                StoreActivity.ProductInfo favProduct = (StoreActivity.ProductInfo) Dashboard.porpulateListWithProducts(hashProductId, dashboardFeeds, R.id.favorites).get(0);
                if(favProduct != null){
                    loadingCart.add(favProduct);
                }else if(favProduct == null){
                    DasRecyclerAdapter.expectedFarvouriteTally = "Incomplete"; //If null is returned, it might mean the product was deleted by the vendor.
                    //favourites.remove(ExpandableListDataGen.makeFavoriteProductLink(hashProductId, (String) needed.get(hashProductId)));
                    //FirebaseUtil.mUserDataReference.child("favourites").child(hashProductId).removeValue(); //Completely remove the product from favourites if missing.
                }
                q++;
            }
        }

        dasRecyclerAdapter = new DasRecyclerAdapter(this,navItemId);
        mdas_rv.setAdapter(dasRecyclerAdapter);  //Dreated a new adapter to handle The store list data items
        dasRecyclerAdapter.notifyDataSetChanged();
    }

    public static ArrayList porpulateListWithProducts(String searchParameter, ArrayList searchLocation, int selection) {
        ArrayList shufflingList = new ArrayList(); //Creates the result ArrayLIst
        shufflingList.add(newProductTemplate); //+ New product element
        int range = searchLocation.size();
        int count = 0;
        while(count < range){
            StoreActivity.ProductInfo storeProducts = (StoreActivity.ProductInfo) searchLocation.get(count);
            if(selection == R.id.dashboard){
                if(Objects.equals(storeProducts.getStoreName(), searchParameter) || Objects.equals(storeProducts.getCartId(), searchParameter)){
                    shufflingList.add(storeProducts);
                }
            }else if(selection == R.id.favorites){
                if(Objects.equals(storeProducts.getProductId(), searchParameter)){
                    shufflingList.add(storeProducts);
                }
            }

            count++;
        }
        if(searchLocation == incomingCart || searchLocation == outgoingCart || searchLocation == dashboardFeeds){
            shufflingList.remove(0); //Remove newProductTemplate.
        }
        return shufflingList;
    }

    public void refreshDashboard(){
        FirebaseUtil.mAppDataReference.child("dashboardfeeds").orderByChild("VSOrank").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String dashboardfeeds = dataSnapshot.getKey();
                if(Objects.equals(dashboardfeeds, "dashboardfeeds")){
                    Dashboard.dashboardFeeds.clear();
                    Iterable<DataSnapshot> refreshingDataSnapshot = dataSnapshot.getChildren(); //Gets Children of a particular node in an array list.
                    for (DataSnapshot refreshedFamilyLoop : refreshingDataSnapshot) {
                        dashboardFeeds.add(refreshedFamilyLoop.getValue(StoreActivity.ProductInfo.class));
                    }
                    updateAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
