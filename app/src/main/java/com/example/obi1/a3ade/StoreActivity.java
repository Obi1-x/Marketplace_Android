package com.example.obi1.a3ade;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StoreActivity extends AppCompatActivity {
    EditText mNameSet;
    EditText mDescripSet;
    StoreInfo storeinfo;
    static ProductInfo productinfo;
    private EditText mPriceSet;
    public static int rowClickPos;
    FirebaseDatabase myFirebase;
    DatabaseReference mydatabase;
    public static final int STORE_LEVEL = 3;
    public static final int CART_LEVEL = 7;
    private static final int PICTURE_RESULT = 42; //Upload image request code
    public static Uri mImageUri;
    public static int NUM_PAGES; //The number of pages (wizard steps) to show in this demo.
    //int NUM_PAGES;
    private ViewPager mImagePager; // The pager widget, which handles animation and allows swiping horizontally to access previous and next wizard steps.
    private PagerAdapter pagerAdapter; //The pager adapter, which provides the pages to the view pager widget.
    //public static int[] imageNumber = {0,0,0,0};
    public static Uri uri = Uri.parse("A");
    public static Uri[] mImageSliderUri = {uri, uri, uri, uri};
    private Button mCreateEditStore;
    private int mUsesnavItemId;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_info);
        /*
        Toolbar newStoreToolbar = findViewById(R.id.toolbar);
        setActionBar(newStoreToolbar);
        newStoreToolbar.setTitle("IN JESUS NAME.AMEN!");*/

        myFirebase = FirebaseUtil.myFirebase;
        mydatabase = FirebaseUtil.mydatabase; //"tradeData" DB reference

        mNameSet = (EditText) findViewById(R.id.Name_set_et);
        mDescripSet = (EditText)findViewById(R.id.Descrip_set_et);
        mPriceSet = (EditText)findViewById(R.id.Price_set_et);
        mCreateEditStore = findViewById(R.id.createStore_b);
        //mStoreImage = (ImageButton) findViewById(R.id.Store_image_ib);

        // Instantiate a ViewPager and a PagerAdapter.
        mImagePager = (ViewPager) findViewById(R.id.Product_image_slide_vp);
        pagerAdapter = new VpScreenSlidePagerAdapter(StoreActivity.this, savedInstanceState);
        mImagePager.setAdapter(pagerAdapter);
        mUsesnavItemId = Dashboard.navItemId;


        StoreInfo editingStore = (StoreInfo)getIntent().getSerializableExtra("Editing Store");
        if(editingStore != null){
            storeinfo = editingStore;
            mUsesnavItemId = R.id.stores;
            mNameSet.setText(storeinfo.getStoreName());
            mNameSet.setEnabled(false);
            mDescripSet.setText(storeinfo.getStoreDescription());
            mImageSliderUri[0] = Uri.parse(storeinfo.getImageUri());
            mCreateEditStore.setText(R.string.Replace);
        }else{
            storeinfo = new StoreInfo();
        }
        final ProductInfo editingProduct = (ProductInfo)getIntent().getSerializableExtra("Editing Product");
        if(editingProduct != null){
            productinfo = editingProduct;
            mUsesnavItemId = DasRecyclerAdapter.PRODUCT_NAV_MENU;
            mNameSet.setText(productinfo.getProductName());
            mDescripSet.setText(productinfo.getProductDescription());
            mPriceSet.setText(productinfo.getPrice());
            mImageSliderUri[0] = Uri.parse(productinfo.getImageUriOne());
            mImageSliderUri[1] = Uri.parse(productinfo.getImageUriTwo());
            mImageSliderUri[2] = Uri.parse(productinfo.getImageUriThree());
            mImageSliderUri[3] = Uri.parse(productinfo.getImageUriFour());
            mCreateEditStore.setText(R.string.Replace);
        }else{
            productinfo = new ProductInfo();
        }


        if(mUsesnavItemId == R.id.stores) { //If Store Activity was opened to or edit create a store.
            mPriceSet.setVisibility(View.GONE);
            NUM_PAGES = 1;
            pagerAdapter.notifyDataSetChanged();
        }else if(mUsesnavItemId == DasRecyclerAdapter.PRODUCT_NAV_MENU) {      //Its a product
            NUM_PAGES = 4;
            pagerAdapter.notifyDataSetChanged();
        }

  /*      //Handle Store Image
        mStoreImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // Intent of type get contents  used to the an image resource.
                intent.setType("image/jpeg"); //Data type expected
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true); //The source of the image should be local
                startActivityForResult(Intent.createChooser(intent, //Remember to ask for permission at the Manifest.
                        "Insert Image"), PICTURE_RESULT);
            }
        });*/

        //Handles button OnClick action
        mCreateEditStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comparison = mCreateEditStore.getText().toString().toUpperCase();
                if(mUsesnavItemId == 300){ //Create Product. Products Array has been displayed
                    productinfo.setProductName(mNameSet.getText().toString());
                    productinfo.setProductDescription(mDescripSet.getText().toString());
                    productinfo.setImageUriOne(String.valueOf(mImageSliderUri[0])); //Sets individual Images.
                    productinfo.setImageUriTwo(String.valueOf(mImageSliderUri[1]));
                    productinfo.setImageUriThree(String.valueOf(mImageSliderUri[2]));
                    productinfo.setImageUriFour(String.valueOf(mImageSliderUri[3]));
                    productinfo.setPrice(mPriceSet.getText().toString());
                    //Dashboard.storeProducts.add(productinfo);
                    if(comparison.equals("CREATE")){ //If a new product was created
                        productinfo.setStoreName(Dashboard.myStoresList.get(rowClickPos).storeName); //Sets the store name you are working in
                        productinfo.setProductId(generateProductId(STORE_LEVEL, "_"));
                        productinfo.setUserName(FirebaseUtil.userName); //Attaches the username of the seller to the product
                        productinfo.setProductNo("0");  //Number of items
                        //Create Cloud storage reference for the first image.
                        //Get url for the first image.
                        //Set the image url in productInfo.
                        Dashboard.myProductsList.add(productinfo); //All products are deposited to myStoreList array with its store name as an identifier.
                        Dashboard.dashboardFeeds.add(productinfo);  //All products are deposited to dashboardfeeds array with its store name as an identifier.
                        DatabaseReference productDatabase = FirebaseUtil.mUserDataReference.child("products").child(productinfo.getProductId()); //Saves to DB
                        productDatabase.setValue(productinfo);
                        DatabaseReference dashboardfeeds = FirebaseUtil.mAppDataReference.child("dashboardfeeds").child(productinfo.getProductId());
                        dashboardfeeds.setValue(productinfo);
                        dashboardfeeds.child("Tstamp").setValue(ServerValue.TIMESTAMP); //Sets the time stamp of this post.
                        uploadToCloudStore("Products"); //Upload images to Cloud Storage
                        Toast.makeText(StoreActivity.this, "Product created!", Toast.LENGTH_SHORT).show();
                        StoreActivity.StoreInfo store = Dashboard.myStoresList.get(rowClickPos);
                    }else if(comparison.equals("REPLACE")){          //If a product was edited
                        Dashboard.myProductsList.remove(ProductViewActivity.rowClickPos); //Remove it.
                        Dashboard.myProductsList.add(ProductViewActivity.rowClickPos, productinfo); //Replace it with the new item using the position of the previously deleted one.
                        Dashboard.dashboardFeeds.remove(editingProduct); //Remove the product as it was before updating. This was done this way to avoid indexing errors
                        Dashboard.dashboardFeeds.add(productinfo); //Replace it with the new item using the position of the previously deleted one.
                        DatabaseReference productDatabase = FirebaseUtil.mUserDataReference.child("products").child(productinfo.getProductId()); //Saves to DB
                        productDatabase.setValue(productinfo);
                        DatabaseReference dashboardfeeds = FirebaseUtil.mAppDataReference.child("dashboardfeeds").child(productinfo.getProductId()); //Saves to DB
                        dashboardfeeds.setValue(productinfo);
                        //REMEMBER YOU ALSO HAVE TO ADJUST EVERY OTHER THING THAT DEPENDS ON THE PRODUCT NAME AND SO ON.
                        Toast.makeText(StoreActivity.this, "Edit successful!", Toast.LENGTH_SHORT).show();
                        StoreActivity.ProductInfo product = Dashboard.myProductsList.get(ProductViewActivity.rowClickPos);
                    }
                } else{ //Create Store. Products array has not yet been displayed, at least once.
                    storeinfo.setStoreDescription(mDescripSet.getText().toString());
                    storeinfo.setImageUri(mImageSliderUri[0].toString()); //Convert to string first in other to avoid errors from firebase
                    DatabaseReference storeDatabase = FirebaseUtil.mUserDataReference.child("stores").child(mNameSet.getText().toString()); //Gets the store name and set it as the name of the child in db.
                    String message = "A";
                    if(comparison.equals("CREATE")){
                        storeinfo.setStoreName(mNameSet.getText().toString());
                        Dashboard.myStoresList.add(storeinfo);
                        message = "Store Created!";
                    }else if(comparison.equals("REPLACE")){
                        Dashboard.myStoresList.remove(rowClickPos);
                        Dashboard.myStoresList.add(rowClickPos, storeinfo);
                        message = "Edit successful!";
                    }
                    storeDatabase.setValue(storeinfo);
                    uploadToCloudStore("Stores"); //Upload image to Cloud Storage
                    Toast.makeText(StoreActivity.this, message, Toast.LENGTH_SHORT).show();
                }
                mImageSliderUri[0] = uri; mImageSliderUri[1] = uri; mImageSliderUri[2] = uri; mImageSliderUri[3] = uri;  //To prevent error
                finish(); //Back to previous activity.
            }
        });

    }

    private void uploadToCloudStore(String path) {
        if(FirebaseUtil.mStorageRef == null){
            FirebaseUtil.mStorageRef = FirebaseUtil.mStorage.getReference().child("Trade").child(FirebaseUtil.userName);
        }
        //StorageReference cloudStoreRef = FirebaseUtil.mStorageRef.child(path);
        if(path.equals("Products")){
            int i;
            for(i=0; i<mImageSliderUri.length; i++){
                String imageSurfix = productinfo.getProductName() + (Dashboard.navItemId + i);
                StorageReference cloudStoreRef = FirebaseUtil.mStorageRef.child(path).child(productinfo.getProductName() + " images").child(imageSurfix);
                cloudStoreRef.putFile(mImageSliderUri[i]);
            }
        } else {
            StorageReference cloudStoreRef = FirebaseUtil.mStorageRef.child(path).child(storeinfo.getStoreName());
            cloudStoreRef.putFile(mImageUri);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK){ //Checks if the picture was acquired successfully
            //mImageUri = data.getVendorGroup();
            //Store image on local database. Get location of the image in local storage and move it to App specific folder as named below.
            File imageFile = null;
            int storeOrProduct = mUsesnavItemId;//Dashboard.navItemId;
            if(storeOrProduct != 300){ //Creating Store.
                File storePath = new File(Dashboard.mAppDirectory, "Stores");//Creates the Stores directory. Find a neater way to do this
                storePath.mkdir();
                imageFile = new File(storePath, mNameSet.getText().toString() + ".jpg");// Prepares the destination folder and the new name of the image.
            }else {                    //Creating Product.
                File productsPath = new File(Dashboard.mAppDirectory, "Products");//Find a neater way to do this
                productsPath.mkdir();
                File specificProductPath = new File(productsPath, mNameSet.getText().toString() + " images");
                specificProductPath.mkdir();
                String imageSurfix = String.valueOf(storeOrProduct + mImagePager.getCurrentItem()); //300 + 0,1,2 or 3 (Pager position).
                imageFile = new File(specificProductPath, mNameSet.getText().toString() + imageSurfix + ".jpg");// Prepares the destination folder and the new name of the image.
            }
            ContentResolver contentResolver = getApplicationContext().getContentResolver(); //Create a reference to the content resolver so that the selected image can be acquired.

            try {
                imageFile.createNewFile(); //Creates an empty image with the store name.
                InputStream sourceStream = contentResolver.openInputStream(data.getData());
                OutputStream destinationStream = new FileOutputStream(imageFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = sourceStream.read(buf)) > 0){
                    destinationStream.write(buf, 0 ,len);
                }
                sourceStream.close();
                destinationStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mImageUri = Uri.fromFile(imageFile);

            //Save image to cloud database.

            //After image was uploaded successfully, set image, else post error message.
            //showImage(mImageUri); //mStoreImage.setImageURI(mImageUri);
            VpScreenSlidePagerAdapter.positioning = mImagePager.getCurrentItem();
            mImageSliderUri[VpScreenSlidePagerAdapter.positioning] = mImageUri;
            ImageView imageView = (ImageView)findViewById(R.id.slide_layout_iv);
            imageView.setImageURI(mImageUri);
        }
    }
/*
    private void showImage(Uri uri) {
        if (uri != null) { //Resizes the image to match the screen width and 2/3 of the image view height, if the uri isnt empty
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(uri)//.load(uri)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(mStoreImage);
        }
    }*/

    public static String generateProductId(int idType, String cartId) {
        String productId = "_";
        if(idType == STORE_LEVEL){
            productId = FirebaseUtil.userName.toLowerCase() + "_" + productinfo.getStoreName().toLowerCase() + "_" + productinfo.getProductName().toLowerCase();
        }else if(idType == CART_LEVEL){
           // productId = productinfo.getProductId();
            productId = cartId + "_" + Dashboard.number++;
        }
        return productId;
    }

    public static class ProfileInfo implements Serializable{
        private String fullName;
        private String address;
        private String phoneNumber;

        public ProfileInfo(){}

        public ProfileInfo(String fullName){
            this.fullName = fullName;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    public static class StoreInfo implements Serializable {
        private String storeName;
        private String storeDescription;
        private int image; //Find a way to use only the id or uri later on
        private String imageUri;
        //private String UserName;

        public StoreInfo(){}

        public StoreInfo(String StoreName, String StoreDescription, int image){
            this.setImage(image);
            //this.UserName = userName;
            this.setStoreName(StoreName);
            this.setStoreDescription(StoreDescription);
        }

        private void setStoreDescription(String StoreDescription) {
            this.storeDescription = StoreDescription;
        }

        public String getStoreDescription() {
            return storeDescription;
        }

        private void setStoreName(String StoreName) {
            this.storeName = StoreName;
        }

        public String getStoreName() {
            return storeName;
        }

        public int getImage() {
            return image;
        }

        public void setImage(int image) {
            this.image = image;
        }

        public String getImageUri() {
            return imageUri;
        }

        public void setImageUri(String imageUri) {
            this.imageUri = imageUri;
        }

   /*     public String getUserName() {
            return UserName;
        }

        public void setUserName(String userName) {
            UserName = userName;
        }*/
    }

    public static class ProductInfo implements Serializable {
        private String storeName;
        private String productName;
        private String productDescription;
        private String productNo;
        private String stockNo;
        private int productIcon;
        private String ImageUriOne;
        private String ImageUriTwo;
        private String ImageUriThree;
        private String ImageUriFour;
        private String price;
        private String UserName;
        private String cartId;
        private String productId;
        private String buyerUsername;
        private String buyerFullName;
        private String buyerAddress;
        private String processingPhase;
        private String logisticsInfo;

        public ProductInfo(){}

        public ProductInfo(String StoreName, String ProductName, String ProductDescription, String ProductNo, int productIcon, String price, String UserName){
            this.setStoreName(StoreName);
            this.setProductName(ProductName);
            this.setProductDescription(ProductDescription);
            this.setProductNo(ProductNo);
            this.setProductIcon(productIcon);
            this.setPrice(price);
            this.setUserName(UserName);
        }

        private void setStoreName(String StoreName) {
            this.storeName = StoreName;
        }

        public String getStoreName() {
            return storeName;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductDescription() {
            return productDescription;
        }

        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }

        public String getProductNo() {
            return productNo;
        }

        public void setProductNo(String productNo) {
            this.productNo = productNo;
        }

        public int getProductIcon() {
            return productIcon;
        }

        public void setProductIcon(int productIcon) {
            this.productIcon = productIcon;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getPrice() {
            return price;
        }

        public String getUserName() {
            return UserName;
        }

        public void setUserName(String userName) {
            UserName = userName;
        }

        public String getCartId() {
            return cartId;
        }

        public void setCartId(String cartId) {
            this.cartId = cartId;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getImageUriOne() {
            return ImageUriOne;
        }

        public void setImageUriOne(String imageUriOne) {
            ImageUriOne = imageUriOne;
        }

        public String getImageUriTwo() {
            return ImageUriTwo;
        }

        public void setImageUriTwo(String imageUriTwo) {
            ImageUriTwo = imageUriTwo;
        }

        public String getImageUriThree() {
            return ImageUriThree;
        }

        public void setImageUriThree(String imageUriThree) {
            ImageUriThree = imageUriThree;
        }

        public String getImageUriFour() {
            return ImageUriFour;
        }

        public void setImageUriFour(String imageUriFour) {
            ImageUriFour = imageUriFour;
        }

        public String getBuyerUsername() {
            return buyerUsername;
        }

        public void setBuyerUsername(String buyerUsername) {
            this.buyerUsername = buyerUsername;
        }

        public String getBuyerFullName() {
            return buyerFullName;
        }

        public void setBuyerFullName(String buyerFullName) {
            this.buyerFullName = buyerFullName;
        }

        public String getBuyerAddress() {
            return buyerAddress;
        }

        public void setBuyerAddress(String buyerAddress) {
            this.buyerAddress = buyerAddress;
        }

        public String getProcessingPhase() {
            return processingPhase;
        }

        public void setProcessingPhase(String processingPhase) {
            this.processingPhase = processingPhase;
        }

        public String getLogisticsInfo() {
            return logisticsInfo;
        }

        public void setLogisticsInfo(String logisticsInfo) {
            this.logisticsInfo = logisticsInfo;
        }

        public String getStockNo() {
            return stockNo;
        }

        public void setStockNo(String stockNo) {
            this.stockNo = stockNo;
        }
    }

    public static class CartListInfo implements Serializable {
        private String cartId;
        private int cartStatus;

        public CartListInfo(){}

        public CartListInfo(String cartId, int cartStatus){
            this.setCartId(cartId);
            this.setCartStatus(cartStatus);
        }

        public String getCartId() {
            return cartId;
        }

        public void setCartId(String cartId) {
            this.cartId = cartId;
        }

        public int getCartStatus() {
            return cartStatus;
        }

        public void setCartStatus(int cartStatus) {
            this.cartStatus = cartStatus;
        }
    }

    public static class RatingInfo implements Serializable{
        private String username;
        private String dateTime;
        private String ratingToStore;

        public RatingInfo(String username, String dateTime, String ratingToStore){
            this.username = username;
            this.dateTime = dateTime;
            this.ratingToStore = ratingToStore;
        }

        public RatingInfo(){}

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }

        public String getRatingToStore() {
            return ratingToStore;
        }

        public void setRatingToStore(String ratingToStore) {
            this.ratingToStore = ratingToStore;
        }
    }

    public static class TaskInfo implements Serializable{ //This class was created to resolve an error in the admin end listener.
        private String processingPhase;
        private String logisticsInfo;
        private String uniqueCode;
        private String authType;
        private String soldTo;
        //private HashMap<String, List> stocks;

        public TaskInfo(String processingPhase){
            this.processingPhase = processingPhase;
        }

        public String getProcessingPhase() {
            return processingPhase;
        }

        public void setProcessingPhase(String processingPhase) {
            this.processingPhase = processingPhase;
        }

        public String getLogisticsInfo() {
            return logisticsInfo;
        }

        public void setLogisticsInfo(String logisticsInfo) {
            this.logisticsInfo = logisticsInfo;
        }

        public String getUniqueCode() {
            return uniqueCode;
        }

        public void setUniqueCode(String uniqueCode) {
            this.uniqueCode = uniqueCode;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getSoldTo() {
            return soldTo;
        }

        public void setSoldTo(String soldTo) {
            this.soldTo = soldTo;
        }
    }
}
