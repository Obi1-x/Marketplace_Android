package com.example.obi1.a3ade;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.obi1.a3ade.Dashboard.inCartList;
import static com.example.obi1.a3ade.Dashboard.dashboardFeeds;
import static com.example.obi1.a3ade.Dashboard.favourites;
import static com.example.obi1.a3ade.Dashboard.incomingCart;
import static com.example.obi1.a3ade.Dashboard.myProductsList;
import static com.example.obi1.a3ade.Dashboard.outCartList;
import static com.example.obi1.a3ade.FirebaseUtil.userName;

public class ProductViewActivity extends AppCompatActivity implements Serializable {
    public static int OPENED = 4;
    public static int CLOSED = 8;
    public static int rowClickPos;
    public static String mUser;
    private boolean isChecked = false;
    private StoreActivity.ProductInfo mProductInfo;
    public static StoreActivity.ProductInfo alternateProductInfo;
    private boolean mChanged = false;
    static int[] spareListIndex;
    ExpandableListView mExpandableListView;
    public static ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List> mExpandableListDetail;
    public static DatabaseReference mProducStockDatabase;
    public static DatabaseReference mProducRatingDatabase;
    public static String Route = "A";
    String quant = "AB", quantText = "Cb";
    public Button mAddValidateButton;
    public static String mThisProductId;
    public static int productQuantityInterger;
    public TextView mQuantity_stock;
    public int quantity;
    public static String countOfBoxedUnstocked;
    public static VariableChangeListener mTriggerStockLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);

        ViewPager productViewPager = (ViewPager)findViewById(R.id.productview_image_vp); // Instantiate a ViewPager and a PagerAdapter.
        PagerAdapter VpadapterForProductView = new VpScreenSlidePagerAdapter(ProductViewActivity.this, savedInstanceState);
        productViewPager.setAdapter(VpadapterForProductView);
        mQuantity_stock = (TextView)findViewById(R.id.quantity_tv);
        //final TextInputEditText itemNo = (TextInputEditText)findViewById(R.id.item_no_et); //Item number reference
        Button stockUp = (Button)findViewById(R.id.stockup_b);
        final ImageButton favouriteButton = (ImageButton)findViewById(R.id.favorite_ib);
        mAddValidateButton = (Button)findViewById(R.id.add_validate_b);
        mExpandableListView = (ExpandableListView) findViewById(R.id.productviewitems_elv); //Expandable list view reference.

        mTriggerStockLabels = new VariableChangeListener();
        mTriggerStockLabels.set(CLOSED);
        Intent intent = getIntent(); //Receives intent from a product click.
        //Receives all Extras
        Route = intent.getStringExtra("Route");
        mProductInfo = (StoreActivity.ProductInfo)intent.getSerializableExtra("Selected product"); //(StoreActivity.ProductInfo) DasRecyclerAdapter.SPInfo.get(rowClickPos);
        //Gets the user who created the product.
        mUser = mProductInfo.getUserName(); //An error occurs here. Save mProductInfo in a persistant state at on Pause, after moving it to on Resume
        //This is used to store the product id used in cart.

        mThisProductId = String.valueOf(mProductInfo.getProductId());
        alternateProductInfo = mProductInfo;
        if(Route.equals("Cart")){ //Used to convert the cart product Id back to productId
            alternateProductInfo.setProductId(String.valueOf(extractDefaultID(alternateProductInfo)));
        }

        StockUpProductActivity.initializeArrayLists(); //Used to get Stock Array list ready.
        mProducRatingDatabase = FirebaseUtil.mydatabase.child("appData").child("productRatings").child(alternateProductInfo.getProductId());
        mProducRatingDatabase.addChildEventListener(Dashboard.mChildListener);
        mProducRatingDatabase.child("Status").setValue("Retrieving");  //Used to trigger child listiner to acquire values

        String[] displayImgaes = {mProductInfo.getImageUriOne(), mProductInfo.getImageUriTwo(), mProductInfo.getImageUriThree(), mProductInfo.getImageUriFour()}; //Gets all Product images
        int i, numberOfPhotos = 0;
        for(i=0; i<displayImgaes.length && displayImgaes[i] != null; i++){ //Convert them to Uri
            StoreActivity.mImageSliderUri[i] = Uri.parse(displayImgaes[i]);
            if(!displayImgaes[i].equals("A")){ //Gets the number recieved images and uses it as the ViewPager size
                numberOfPhotos++;
            }
        }
        if(numberOfPhotos == 0 ){numberOfPhotos++;} //To avoid errors
        StoreActivity.NUM_PAGES = numberOfPhotos;
        VpadapterForProductView.notifyDataSetChanged();
        productQuantityInterger = Integer.parseInt(mProductInfo.getProductNo());
        quant = String.valueOf(productQuantityInterger);
        //quant = mProductInfo.getProductNo();  //quantity number.
        quantText = mQuantity_stock.getText().toString();  //quantity text.
        ExpandableListAdapter.xListAdapterProductName = mProductInfo.getProductName();
        ExpandableListAdapter.xListAdapterProductPrice = "N" + mProductInfo.getPrice();
        ExpandableListAdapter.xListAdapterProductDescription = mProductInfo.getProductDescription();
        //Select the initial purpose of opening stock up activity.

        if(mUser.equals(FirebaseUtil.userName)){ //If this is the user that created the product
            mProducStockDatabase = FirebaseUtil.mUserDataReference.child("products").child(alternateProductInfo.getProductId());
            mProducStockDatabase.addChildEventListener(Dashboard.mChildListener);
            mProducStockDatabase.child("Status").setValue("Opened");  //Used to trigger child listiner to acquire values

            stockUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {  //Similar to what we have in expandable List Adapter
                    if(Route.equals("Cart")){
                        StockingRecyclerAdapter.accessingStockTo = StockUpProductActivity.UNSTOCK;
                    }else StockingRecyclerAdapter.accessingStockTo = StockUpProductActivity.STOCK; //Select the initial purpose of opening stock up activity.
                    Intent stockUpIntent = new Intent(ProductViewActivity.this, StockUpProductActivity.class);
                    //stockUpIntent.putExtra("Products Specs", alternateProductInfo);
                    startActivity(stockUpIntent);
                }
            });
            favouriteButton.setVisibility(View.GONE);
            if(Route.equals("Cart")){
                quantText = quantText + " purchased";
                quantity = productQuantityInterger;
                productQuantityInterger = StockUpProductActivity.stockPhaseGrouping("Stocked", "non", null); //Total quantity of stocked products.
                quant = String.valueOf(quantity) + " of " + String.valueOf(productQuantityInterger) ;
                //Multiple concatination for unstocked and boxed status was here.
                if(mProductInfo.getProcessingPhase().equals("3") || mProductInfo.getProcessingPhase().equals("4")){  //If a product has been delivered,  ut the buyer is yet to receive.
                    stockUp.setVisibility(View.GONE);
                    mAddValidateButton.setVisibility(View.GONE);
                }else {
                    stockUp.setText("UNSTOCK");
                    mAddValidateButton.setText(getString(R.string.Deliver));
                    //adjustButton(StockUpProductActivity.stockPhaseGrouping("Boxed"));
                    if(StockUpProductActivity.stockPhaseGrouping("Boxed", mProductInfo.getBuyerUsername(), mProductInfo.getCartId()) == 0){
                        mAddValidateButton.setEnabled(false);  //Look for a way to avoid making Button static
                    }else mAddValidateButton.setEnabled(true);
                }
            }else mAddValidateButton.setText(getString(R.string.Validate_button));
            ExpandableListDataGen.productEListData = alternateProductInfo;
            mExpandableListDetail = ExpandableListDataGen.getVendorGroup(Route, "Non"); //Gets the respective group titles to be viewed by a vendor, passing in the point in which productview activity was accessed from.
            ExpandableListAdapter.productdescriptionAccess = true;
        }else{ //If this isnt the user who created the product
            stockUp.setVisibility(View.GONE);
            favouriteButton.setVisibility(View.VISIBLE);
            if(doesArrayListHaveAttribute(favourites, alternateProductInfo.getProductId())){ //Checks to see if the product os already within the favourites list.  favourites.contains(DasRecyclerAdapter.SPInfo.get(rowClickPos))
                isChecked = true;  //Was previous from mProductInfo.getProductId()
                favouriteButton.setImageResource(R.drawable.ic_favorite_checked);
            }
            if(Route.equals("Cart")){
                quantText = quantText + " purchased";
                if(mProductInfo.getProcessingPhase() != null){
                    if(mProductInfo.getProcessingPhase().equals("4")){ //If the product has been received.
                        mAddValidateButton.setVisibility(View.GONE);
                    }else mAddValidateButton.setText(getString(R.string.Recieve));
                }
            }else mAddValidateButton.setText(getString(R.string.Add_to_cart_button));
            //itemNo.setEnabled(false);
            ExpandableListAdapter.productdescriptionAccess = false; //ProductDescription.setEnabled(false); //Text colour has to be set in the xml file

            favouriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mChanged = true; //The favourite button was clicked
                    if(isChecked){
                        isChecked = false;
                        favouriteButton.setImageResource(R.drawable.ic_favorite_border_unchecked);
                        //favourites.remove(mProductInfo);   //Removes product of our vendors from favourite list.
                        favourites.remove(ExpandableListDataGen.makeFavoriteProductLink(mProductInfo.getProductId(), mProductInfo.getProductName()));
                    } else if(!isChecked){
                        isChecked = true;
                        favouriteButton.setImageResource(R.drawable.ic_favorite_checked);
                        //favourites.add(mProductInfo); //Adds to favourite array list in Dashbord.
                        favourites.add(ExpandableListDataGen.makeFavoriteProductLink(mProductInfo.getProductId(), mProductInfo.getProductName())); //Adds to favourite array list in Dashbord.
                    }
                }
            });
            Dashboard.ratingPerProduct.add("Active rating bar");  //Used to indirectly add the ratings bar list entry to the expandable List
            ExpandableListDataGen.productEListData = alternateProductInfo;
            String cartStatusOfProduct = "A";
            doesArrayListHaveAttribute(inCartList, alternateProductInfo.getCartId());
            int cartStatus = inCartList.get(spareListIndex[0]).getCartStatus();
            if(cartStatus == CLOSED){
                cartStatusOfProduct = "CLOSED";
            }
            mExpandableListDetail = ExpandableListDataGen.getCustomerGroup(Route, cartStatusOfProduct); //Gets the respective group titles to be viewed by a customer, passing in the point in which productview activity was accessed from.
        }

        mQuantity_stock.setText(quantText + ": " + quant);  //itemNo.setText(mProductInfo.getProductNo());
        expandableListTitle = new ArrayList<String>(mExpandableListDetail.keySet());
        expandableListAdapter = new ExpandableListAdapter(this, expandableListTitle, mExpandableListDetail);
        mExpandableListView.setAdapter(expandableListAdapter);

        mAddValidateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUser.equals(FirebaseUtil.userName)){
                    if(Route.equals("Not cart")){
                        mProductInfo.setProductNo(quant);  //mProductInfo.setProductNo(itemNo.getText().toString());
                        doesArrayListHaveAttribute(myProductsList, mProductInfo.getProductName());
                        myProductsList.remove(spareListIndex[0]);
                        myProductsList.add(spareListIndex[0], mProductInfo); //Replaces product in myProductsList
                        DatabaseReference productDatabase = FirebaseUtil.mUserDataReference.child("products").child(mProductInfo.getProductId());
                        productDatabase.setValue(mProductInfo);   //Saves to DB
                        doesArrayListHaveAttribute(dashboardFeeds, mProductInfo.getProductName());
                        dashboardFeeds.remove(spareListIndex[0]);//remove(indexOfDBF);
                        dashboardFeeds.add(spareListIndex[0], mProductInfo);  //Replaces product in dashboardfeeds
                        DatabaseReference dashboardFeedsDB = FirebaseUtil.mAppDataReference.child("dashboardfeeds").child(mProductInfo.getProductId());
                        dashboardFeedsDB.setValue(mProductInfo); //Saves to DB
                 /*       int i;
                        for(i=0;i<Dashboard.ratingPerProduct.size();i++){ //Replaces the deleted rating after the product's info has been updated.
                            StoreActivity.RatingInfo ratingInfo = (StoreActivity.RatingInfo)Dashboard.ratingPerProduct.get(i);
                            DatabaseReference ratingDatabase = mProducStockDatabase.child("ratings").child(ratingInfo.getUsername());
                            ratingDatabase.setValue(ratingInfo);
                        }*/
                        Intent intent = new Intent(ProductViewActivity.this, Dashboard.class);
                        startActivity(intent);
                        Toast.makeText(ProductViewActivity.this, "Product details updated!", Toast.LENGTH_LONG).show();
                    }else if(Route.equals("Cart")){
                        //Check if logistics info is empty and notify the Vendor to enter a value if so.
                        View anotherView = mExpandableListView.getFocusedChild();
                        EditText logData = (EditText)anotherView.findViewById(R.id.auth_logvalue_et);
                        mProductInfo.setLogisticsInfo(logData.getText().toString());//Get log value if not empty and associate it with the productinfo
                        if(mProductInfo.getProcessingPhase().equals("2")){
                            mProductInfo.setProcessingPhase("3");//Jump to phase 3 for both vendor and customer
                            mProductInfo.setProductId(mThisProductId);
                            Dashboard.outgoingCart.remove(mProductInfo);
                            Dashboard.outgoingCart.add(mProductInfo);
                            FirebaseUtil.mUserDataReference.child("outgoingcart").child(mThisProductId).setValue(mProductInfo);
                            CartActivity.buildTaskJDForIncoming(mThisProductId, mProductInfo, logData.getText().toString());
                            Toast.makeText(ProductViewActivity.this, "Sending...", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }else{
                    if(Route.equals("Not cart")){

                        boolean existsAgain = false;
                        int cartOfPreviouslyOtheredProduct = 0;
                        boolean exists = doesArrayListHaveAttribute(incomingCart, mProductInfo.getProductName()); //From here
                        if(exists){ //If product exists in incoming cart.
                            int[] indices = spareListIndex;
                            int s = 0;
                            while (s < indices.length && cartOfPreviouslyOtheredProduct != OPENED){
                                StoreActivity.ProductInfo previouslyOtheredProduct = Dashboard.incomingCart.get(indices[s]);
                                existsAgain = doesArrayListHaveAttribute(inCartList, previouslyOtheredProduct.getCartId());
                                cartOfPreviouslyOtheredProduct = inCartList.get(spareListIndex[0]).getCartStatus(); //to here is used to check if the product to be added to incoming cart, already exists in an Opened cart or not.
                                if(cartOfPreviouslyOtheredProduct == OPENED){ s = indices.length;}
                                s++;
                            }
                        }

                        //The above code tries to assign the variables in the condition below.
                        if(existsAgain && cartOfPreviouslyOtheredProduct == OPENED){ //If cart id is in cartLIst and that cart is Oened.
                            Toast.makeText(ProductViewActivity.this, "Product exists in incomingCart", Toast.LENGTH_LONG).show();
                        }else {
                            mProductInfo.setCartId(getIdOfOpenedCart()); //Associates product with cart, through cartId
                            String prepProductId = mProductInfo.getProductId() + "_" + mProductInfo.getCartId();
                            mProductInfo.setProductId(StoreActivity.generateProductId(StoreActivity.CART_LEVEL, prepProductId));

                            if(incomingCart == null){
                                incomingCart = new ArrayList<StoreActivity.ProductInfo>();
                            }
                            mProductInfo.setProductNo("1"); //Sets number of products to 1 first, so it can be set later in user's cart
                            incomingCart.add(mProductInfo);

                            DatabaseReference incomingcartDatabase = FirebaseUtil.mUserDataReference.child("incomingcart").child(mProductInfo.getProductId());
                            incomingcartDatabase.setValue(mProductInfo);  //Remember to add a new object named product id for a clear distinction between products or orders.

                            Toast.makeText(ProductViewActivity.this, "Product has been added to incomingCart", Toast.LENGTH_LONG).show();
                          /*  Intent intent = new Intent(ProductViewActivity.this, Dashboard.class);
                            startActivity(intent);
                            */
                            finish();
                        }
                    }else if(Route.equals("Cart")){
                        //Find a way to later change the code below to Launch activity for results.
                        StockingRecyclerAdapter.accessingStockTo = StockUpProductActivity.RECEIVER; //Select the initial purpose of opening stock up activity.
                        Intent stockUpIntent = new Intent(ProductViewActivity.this, StockUpProductActivity.class);
                        stockUpIntent.putExtra("Products Specs", alternateProductInfo);
                        startActivity(stockUpIntent);
                    }
                }
            }
        });

    }

    public static String extractDefaultID(StoreActivity.ProductInfo alternateProductInfo) {
        int endpoint = alternateProductInfo.getUserName().length() + alternateProductInfo.getStoreName().length() + alternateProductInfo.getProductName().length() + 2;
        String modifiedProductID = (String) alternateProductInfo.getProductId().subSequence(0,endpoint);
        //alternateProductInfo.setProductId(String.valueOf(modifiedProductID));
        return modifiedProductID;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        int newQuantity;
        newQuantity = StockUpProductActivity.stockPhaseGrouping("Stocked", "non", null);
        if(productQuantityInterger != newQuantity && StockUpProductActivity.uploadedProductStockInfo.size()>0){
            productQuantityInterger = newQuantity;
            if(Route.equals("Cart") && mUser.equals(FirebaseUtil.userName)){
                quant = String.valueOf(quantity) + " of " + String.valueOf(productQuantityInterger) ;
                mQuantity_stock.setText(quantText + ": " + quant);
                quant = String.valueOf(productQuantityInterger);
                refreshStockStatusLabels(expandableListAdapter);
            }else {
                quant = String.valueOf(newQuantity);
                mQuantity_stock.setText(quantText + ": " + quant);
            }
            //At this point, quant is the value of stocked products.
            mProductInfo.setProductNo(quant);
            doesArrayListHaveAttribute(Dashboard.dashboardFeeds, mProductInfo.getProductName());
            Dashboard.dashboardFeeds.get(spareListIndex[0]).setProductNo(quant);
            doesArrayListHaveAttribute(Dashboard.myProductsList, mProductInfo.getProductName());
            Dashboard.myProductsList.get(spareListIndex[0]).setProductNo(quant);
            FirebaseUtil.mAppDataReference.child("dashboardfeeds").child(mProductInfo.getProductId()).child("productNo").setValue(mProductInfo.getProductNo());
            FirebaseUtil.mUserDataReference.child("products").child(mProductInfo.getProductId()).child("productNo").setValue(mProductInfo.getProductNo());
        }


        mTriggerStockLabels.setOnIntegerChangeListener(new VariableChangeListener.OnIntegerChangeListener() {
            @Override
            public void onIntegerChanged(int newValue) {
                if(newValue == OPENED){  //If stocks has finished downloading.
                    if(Route.equals("Cart") && mUser.equals(FirebaseUtil.userName)){
                        if(StockUpProductActivity.stockPhaseGrouping("Boxed", mProductInfo.getBuyerUsername(), mProductInfo.getCartId()) == 0){  //
                            mAddValidateButton.setEnabled(false);
                        }else mAddValidateButton.setEnabled(true);
                        productQuantityInterger = StockUpProductActivity.stockPhaseGrouping("Stocked", "non", null);
                        String valueOfQuantity = (String) mQuantity_stock.getText();
                        int endIndex = valueOfQuantity.indexOf('f') + 1;
                        valueOfQuantity = valueOfQuantity.substring(0,endIndex);
                        valueOfQuantity = valueOfQuantity.concat(" " + String.valueOf(productQuantityInterger));
                        mQuantity_stock.setText(valueOfQuantity);
                        refreshStockStatusLabels(expandableListAdapter);
                        View searchView = new View(mExpandableListView.getContext());
                        View returnedView = expandableListAdapter.getChildView(1,4,false, searchView, null);
                        TextView expanded = returnedView.findViewById(R.id.expandedListItem_tv);
                        expanded.setText(countOfBoxedUnstocked);  //Manually set the text.
                        expandableListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void refreshStockStatusLabels(ExpandableListAdapter expandableListAdapter) {
        //Items unstocked: Unstocked of Bought. Items boxed: Boxed of Unstocked.  Items unstocked: 0 of 0. Items boxed: 0 of 0.
        //Was "Items unstocked: Unstocked of Stocked" before.
        int unstocked, boxed, received;
        unstocked = StockUpProductActivity.stockPhaseGrouping("Unstocked for boxing", mProductInfo.getBuyerUsername(), mProductInfo.getCartId());
        boxed = StockUpProductActivity.stockPhaseGrouping("Boxed", mProductInfo.getBuyerUsername(), mProductInfo.getCartId());
        received = StockUpProductActivity.stockPhaseGrouping("Received", mProductInfo.getBuyerUsername(), mProductInfo.getCartId());
        countOfBoxedUnstocked = getString(R.string.unstockedStatusLabel);
        countOfBoxedUnstocked = countOfBoxedUnstocked.concat(" ");
        countOfBoxedUnstocked = countOfBoxedUnstocked.concat(String.valueOf(unstocked + boxed) + " of ");
        countOfBoxedUnstocked = countOfBoxedUnstocked.concat(String.valueOf(quantity) + "." + "\n"); //End of Items unstocked. Was "String.valueOf(productQuantityInterger)" before.
        countOfBoxedUnstocked = countOfBoxedUnstocked.concat(getString(R.string.boxededStatusLabel) + " ");
        countOfBoxedUnstocked = countOfBoxedUnstocked.concat(String.valueOf(boxed + received) + " of ");
        countOfBoxedUnstocked = countOfBoxedUnstocked.concat(String.valueOf(unstocked) + ".");
        expandableListAdapter.notifyDataSetChanged();
    }

    public static boolean doesArrayListHaveAttribute(ArrayList arrayList, String attribute) {
        int[] indexes = new int[arrayList.size()];
        int i = 0; int h = 0;
        String pIdInFavourites = "A";
        boolean search = false;
        while(i<arrayList.size()){
            if(arrayList == favourites){
                //StoreActivity.ProductInfo producing = (StoreActivity.ProductInfo) arrayList.get(i);
                HashMap<String, String> producing = (HashMap<String, String>) arrayList.get(i);
                //pIdInFavourites = producing.getProductId();
                Object[] keyCup = producing.keySet().toArray();
                pIdInFavourites = (String) keyCup[0];
            }else if(arrayList == incomingCart || arrayList == myProductsList || arrayList == dashboardFeeds){
                StoreActivity.ProductInfo producing = (StoreActivity.ProductInfo) arrayList.get(i);
                pIdInFavourites = producing.getProductName();
            }else if(arrayList == inCartList || arrayList == outCartList){
                StoreActivity.CartListInfo cartFish = (StoreActivity.CartListInfo) arrayList.get(i);
                pIdInFavourites = cartFish.getCartId();
            }else if(arrayList == StockUpProductActivity.uploadedProductStockInfo){
                StockUpProductActivity.StockUpInfo stockFish = (StockUpProductActivity.StockUpInfo)arrayList.get(i);
                pIdInFavourites = stockFish.getUniqueCode();
            }else if(arrayList == Dashboard.ratingPerProduct){
                Object objective = arrayList.get(i);
                if(!objective.equals("Active rating bar")){
                    StoreActivity.RatingInfo sniffOutRating = (StoreActivity.RatingInfo) arrayList.get(i);
                    pIdInFavourites = sniffOutRating.getUsername();
                }else pIdInFavourites = (String) objective;
            }

            if(pIdInFavourites.equals(attribute)){
                indexes[h] = i;
                h++;
                search = true; //True in this case might not be accurate.
                if(arrayList != incomingCart){
                    //spareListIndex = i;
                    i = arrayList.size(); //Used to terminate the loop
                }
            }
            i++;
        }
        spareListIndex = indexes;
        return search;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mChanged){
            DatabaseReference favouritesDatabase = FirebaseUtil.mUserDataReference.child("favourites").child(mProductInfo.getProductId());
            if(isChecked){
                //favouritesDatabase.setValue(mProductInfo);  //("Checked");
                favouritesDatabase.setValue(mProductInfo.getProductName());  //("Checked");
                updateFavouriteCountDB("add", alternateProductInfo.getProductId());
            }else if(!isChecked){
                favouritesDatabase.removeValue();
                updateFavouriteCountDB("remove", alternateProductInfo.getProductId());
            }
        }
        if(mUser.equals(FirebaseUtil.userName)){ //This condition is here because location of ratings data for customers has moved.
            mProducStockDatabase.child("Status").setValue("Closed");
            mProducStockDatabase.removeEventListener(Dashboard.mChildListener);
        }
        mProducRatingDatabase.child("Status").removeValue();
        mProducRatingDatabase.removeEventListener(Dashboard.mChildListener);
        Dashboard.ratingPerProduct.clear();
        StockUpProductActivity.uploadedProductStockInfo.clear(); //Gets these Array Lists ready to be repopulated.
        StockUpProductActivity.productStockInfo.clear();
        mTriggerStockLabels.set(CLOSED); // Reset Stock Label triggers for next time
    }

    public static void updateFavouriteCountDB(final String action, final String productId) {
        final DatabaseReference favoriteCount = FirebaseUtil.mydatabase.child("appData").child("productFavouriteCount").child(productId);
        favoriteCount.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalfavouritesforThisProduct;

                if (dataSnapshot.getValue() == null || dataSnapshot.getValue() == "0") {
                    totalfavouritesforThisProduct = 0;
                }else{
                    String stringValue = (String) dataSnapshot.getValue();
                    totalfavouritesforThisProduct = Integer.parseInt(stringValue);
                }

                if(action.equals("add")){
                    totalfavouritesforThisProduct++;
                }else if(action.equals("remove")){
                    totalfavouritesforThisProduct--;
                }

                if(totalfavouritesforThisProduct > 0){
                    favoriteCount.setValue(String.valueOf(totalfavouritesforThisProduct));
                    FirebaseUtil.mydatabase.child("appData").child("dashboardfeeds").child(productId).child("FavouriteCount").setValue(totalfavouritesforThisProduct);
                }else {
                    favoriteCount.removeValue();
                    FirebaseUtil.mydatabase.child("appData").child("dashboardfeeds").child(productId).child("FavouriteCount").removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.productviewmenu, menu);
        if(mUser.equals(FirebaseUtil.userName) && Route.equals("Not cart")){
            MenuItem editProduct = menu.findItem(R.id.editproduct_mi);
            editProduct.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.editproduct_mi){
            Intent intent = new Intent(ProductViewActivity.this, StoreActivity.class);
            intent.putExtra("Editing Product", mProductInfo);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private String getIdOfOpenedCart() {
        String openedCartId = null;
        int cartTally = 0;
        int count = 0;
        int range = inCartList.size();

        while (count < range){
            StoreActivity.CartListInfo scaningCart = inCartList.get(count);
            if(scaningCart.getCartStatus() == OPENED){ //Look for an open cart
                openedCartId = scaningCart.getCartId();
            }
            count++;
        }
        if(openedCartId == null){  //If non opened, create new cart and give it an id.
            String sample = inCartList.get(inCartList.size()-1).getCartId();  //Gets the cart_id of the last cart
            cartTally = (int)sample.charAt(sample.length()-1);  //Gets the last carts Tally. -49 is a byte conversion of char to string
            cartTally = cartTally - 47; //- 48 + 1;
            openedCartId = userName.toLowerCase() + "_" + cartTally;  //Concartenate username, _ and cart Tally
            //inCartList.add(new StoreActivity.CartListInfo(openedCartId, OPENED));
            StoreActivity.CartListInfo newCart = new StoreActivity.CartListInfo(openedCartId, OPENED);
            inCartList.add(newCart);
            DatabaseReference cartDatabase = FirebaseUtil.mUserDataReference.child("carts").child(openedCartId);
            cartDatabase.setValue(newCart);
        }
        return openedCartId;
    }

    public static void uploadProductRating(final StoreActivity.RatingInfo rating_entry){
        //DatabaseReference ratingDatabase = mProducStockDatabase.child("ratings").child(rating_entry.getUsername());
        //ratingDatabase.setValue(rating_entry);76

        ValueEventListener totalratingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StoreActivity.RatingInfo recievedRating = rating_entry;
                float totalRatingforThisProduct, previousRating;
                if (dataSnapshot.getValue() == null || dataSnapshot.getValue() == "0") {
                    totalRatingforThisProduct = 0;
                }else{
                    String stringValue = (String) dataSnapshot.getValue();
                    totalRatingforThisProduct = Float.parseFloat(stringValue);
                }

                if(Dashboard.ratingPerProduct.size() > 0){
                    boolean isRatedbefore = doesArrayListHaveAttribute(Dashboard.ratingPerProduct, recievedRating.getUsername());
                    if(isRatedbefore){
                        StoreActivity.RatingInfo found = (StoreActivity.RatingInfo) Dashboard.ratingPerProduct.get(spareListIndex[0]);
                        previousRating = Float.parseFloat(found.getRatingToStore()) ;
                    }else previousRating = 0;
                }else previousRating = 0;



                totalRatingforThisProduct -= previousRating;
                float currentRating = Float.parseFloat(recievedRating.getRatingToStore());
                totalRatingforThisProduct += currentRating;

                mProducRatingDatabase.child("totalRating").setValue(String.valueOf(totalRatingforThisProduct));
                mProducRatingDatabase.child(FirebaseUtil.userName).setValue(rating_entry);  //Sets rating.
                FirebaseUtil.mydatabase.child("appData").child("dashboardfeeds").child(alternateProductInfo.getProductId()).child("TotalRating").setValue(totalRatingforThisProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Operation Status", "OPERATION FAILED!!!");
            }
        };
        mProducRatingDatabase.child("totalRating").addListenerForSingleValueEvent(totalratingsListener);
    }
}
