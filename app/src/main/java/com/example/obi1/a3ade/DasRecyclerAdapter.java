package com.example.obi1.a3ade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.obi1.a3ade.ProductViewActivity.CLOSED;
import static com.example.obi1.a3ade.ProductViewActivity.OPENED;

public class DasRecyclerAdapter extends RecyclerView.Adapter <DasRecyclerAdapter.DasViewHolder> {
    public static final int PRODUCT_NAV_MENU = 300;
    public static final int OUTGOING_CART_ID = 500;
    public static final int INCOMING_CART_CHECKOUT = 700;
    public static final int OUTGOING_CART_PROCESS = 900;
    public final Context viewHolderContext;
    private final LayoutInflater dasLayoutInflater;
    public static ArrayList SPInfo;
    public static int navMenuId;
    public static int rowclickPosition;
    public static int cost, check;
    public static int myPosition ;
    public static String expectedFarvouriteTally;
    Class myclass = ProductViewActivity.class;


    public DasRecyclerAdapter(Context viewHolderContext, int navMenuId) {
        this.viewHolderContext = viewHolderContext;  //An Inflater needs a context to function
        this.navMenuId = navMenuId;  //.getTitle();
        dasLayoutInflater = LayoutInflater.from(this.viewHolderContext); //Create an inflater with the context above
        switch (navMenuId){
            case R.id.stores:
                this.SPInfo = Dashboard.myStoresList; //Get store data
                break;
            case R.id.favorites:
                //this.SPInfo = Dashboard.favourites;  //Get favourite data
                this.SPInfo = Dashboard.loadingCart;  //Get favourite data
                if(expectedFarvouriteTally.equals("Incomplete")){
                    Toast.makeText(viewHolderContext, "Some products might have been deleted by the vendor.", Toast.LENGTH_LONG).show();
                    expectedFarvouriteTally = "Complete";
                }
                break;
            case R.id.dashboard:
                this.SPInfo = Dashboard.dashboardFeeds;  //myProductsList; //Get product data
                break;
            case R.id.cart:
                this.SPInfo = Dashboard.inCartList;  //Get List of cart data.
                break;
            case INCOMING_CART_CHECKOUT:
                this.SPInfo = Dashboard.loadingCart;  //Get a particular cart's data.
                check = 0;
                myPosition = 0;
                cost = getCost();
                break;
            case OUTGOING_CART_PROCESS:
                this.SPInfo = Dashboard.loadingCart;  //Get a particular cart's data.
                //this.SPInfo = Dashboard.outgoingCart;
                //check = 0;
                break;
            case OUTGOING_CART_ID:
                this.SPInfo = CartActivity.arrangeOutCartList(Dashboard.outgoingCart);  //Returns outCartList
                //this.SPInfo = Dashboard.outCartList;
                //this.SPInfo = Dashboard.outgoingCart;
                cost = 0;
                break;
            case PRODUCT_NAV_MENU:
                this.SPInfo = Dashboard.storeProducts; //Get product data
                break;
            default:
                this.SPInfo = Dashboard.dashboardFeeds; //myProductsList; //Get product data.
                break;
        }
        Dashboard.prepareInOutCart();
    }

    private static int getCost() {
        int counting = 0;
        int dynamicPrice = 0;
        while(counting < SPInfo.size()){
            StoreActivity.ProductInfo product = (StoreActivity.ProductInfo) SPInfo.get(counting);
            int price = Integer.parseInt(product.getPrice());
            int stock = Integer.parseInt(product.getProductNo());  //Value is gotten from incoming cart array instead
            dynamicPrice = dynamicPrice + price * stock;
            counting++;
        }
        return dynamicPrice;
    }

    //public DasRecyclerAdapter(ArrayList<StoreActivity.StoreInfo> storelist){}

    @NonNull
    @Override
    public DasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View dasItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.das_row_rv, parent, false);
        View dasItemView = dasLayoutInflater.inflate(R.layout.das_row_rv, parent, false); // Inflate the view using the a layout resource and the created inflater.
        DasViewHolder viewHolderInstance = new DasViewHolder(dasItemView, myPosition);
        //myPosition++;
        return viewHolderInstance; //New instance of DasViewHolder parsing in the inflated View.
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final DasViewHolder holder, final int position) {
        if(navMenuId == R.id.stores){ //Binds store data
            StoreActivity.StoreInfo store = (StoreActivity.StoreInfo) SPInfo.get(position);
            holder.storeName.setText(store.getStoreName());
            holder.storeImage.setImageResource(store.getImage());
            holder.productPrice.setVisibility(View.GONE);
            holder.favorites.setVisibility(View.GONE);
            if(store.getImageUri() != null){
                Uri uri = Uri.parse(store.getImageUri());
                //holder.storeImage.setImageURI(uri);  //Convert back to uri to display properlly.
                showSmallImage(uri, holder.storeImage);
            }else holder.storeImage.setImageResource(R.drawable.ic_store_icon);
            //  holder.rowPosition = position;

        } else if(navMenuId == R.id.cart || navMenuId == OUTGOING_CART_ID){
            StoreActivity.CartListInfo cartList = (StoreActivity.CartListInfo)SPInfo.get(position);
            if(cartList.getCartId().equals("Empty_0")){
               holder.storeName.setText("Empty");
            }else holder.storeName.setText(cartList.getCartId());
            if(cartList.getCartStatus() == OPENED || holder.storeName.getText() == "Empty"){
                holder.productPrice.setText("....");
                holder.productPrice.setTextColor(R.color.darkTextColor);
            } else {
                if(navMenuId == R.id.cart){
                    holder.productPrice.setText("PAID");
                }else holder.productPrice.setText("PENDING");
            }
            holder.storeImage.setImageResource(R.drawable.ic_cart);
            holder.favorites.setVisibility(View.GONE);
        }else{  //Binds product data
            final StoreActivity.ProductInfo product = (StoreActivity.ProductInfo) SPInfo.get(position);
            holder.storeName.setText(product.getProductName());
            if(product.getImageUriOne() != null){
                Uri uri = Uri.parse(product.getImageUriOne());
                showSmallImage(uri, holder.storeImage);
            }else holder.storeImage.setImageResource(product.getProductIcon());

            if(navMenuId == INCOMING_CART_CHECKOUT || navMenuId == OUTGOING_CART_PROCESS){
                if(navMenuId == INCOMING_CART_CHECKOUT){
                    if(check <= SPInfo.size()){ //Initially populating the RV
                        holder.stockSelect.setSelection(Integer.parseInt(product.getProductNo())-1);
                    }
                    int price = Integer.parseInt(product.getPrice());
                    //int stock = Integer.parseInt(holder.stockSelect.getSelectedItem().toString());  //Value is gotten from the spinner instead
                    int stock = holder.stockSelect.getSelectedItemPosition() + 1; //Value is gotten from the spinner instead
                    int dynamicPrice = price * stock;
                    holder.productPrice.setText("N" + dynamicPrice); //Sets price * the number of items selected with the spinner.

                    setPhaseIndicator(holder.deleteCartProduct, product.getProcessingPhase());
                    Button checkout = (Button)CheckoutFragment.mWorkingView.findViewById(R.id.checkout_b);
                    checkout.setText("CHECKOUT N"+ DasRecyclerAdapter.cost);  //This should be done only once in cart fragment
                    //Log.d("Button Set", String.valueOf(cost));

                }else if(navMenuId == OUTGOING_CART_PROCESS){
                    if(check <= SPInfo.size()){ //Initially populating the RV
                        holder.stockSelect.setSelection(Integer.parseInt(product.getProductNo())-1);
                    }
                    /*
                    int price = Integer.parseInt(product.getPrice());
                    int stock = Integer.parseInt(holder.stockSelect.getSelectedItem().toString());  //Value is gotten from the spinner instead
                    int dynamicPrice = price * stock;*/
                    holder.productPrice.setText("N" + product.getPrice()); //Sets price * the number of items selected with the spinner.
                    setPhaseIndicator(holder.deleteCartProduct, product.getProcessingPhase());
                }
            }else{
                holder.productPrice.setText("N" + product.getPrice()); //Sets Price per item straight from Cart array
                if(navMenuId == PRODUCT_NAV_MENU){ //Used to add delete and remove favorites from products
                    holder.favorites.setVisibility(View.GONE);
                    if(position == 0){
                        holder.productPrice.setVisibility(View.GONE);
                    }
                    if(Dashboard.isDeleting == true && position > 0){
                        holder.deleteCartProduct.setVisibility(View.VISIBLE);
                    }
                }

                if(product.getUserName().equals(FirebaseUtil.userName)){
                    holder.favorites.setVisibility(View.GONE);
                }else{
                    if(ProductViewActivity.doesArrayListHaveAttribute(Dashboard.favourites, product.getProductId())){
                        holder.favorites.setImageResource(R.drawable.ic_favorite_checked);
                    }

                    holder.favorites.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //DasViewHolder viewHolder = holder;
                            ImageButton favoriteButton = (ImageButton) v;
                            DatabaseReference favouritesDatabase = FirebaseUtil.mUserDataReference.child("favourites").child(product.getProductId());
                            if(!ProductViewActivity.doesArrayListHaveAttribute(Dashboard.favourites, product.getProductId())){ //If product is not in favourites
                                //favouritesDatabase.setValue(product); //Add to DB;
                                favouritesDatabase.setValue(product.getProductName()); //Add to DB;
                                //Dashboard.favourites.add(product); //Add to Array list
                                Dashboard.favourites.add(ExpandableListDataGen.makeFavoriteProductLink(product.getProductId(), product.getProductName()));
                                favoriteButton.setImageResource(R.drawable.ic_favorite_checked); //Check it
                                ProductViewActivity.updateFavouriteCountDB("add", product.getProductId());
                            }else{  //If product is in favourites
                                favouritesDatabase.removeValue();//Update DB
                                //Dashboard.favourites.remove(product); // Remove from ArrayList
                                Dashboard.favourites.remove(ExpandableListDataGen.makeFavoriteProductLink(product.getProductId(), product.getProductName()));
                                favoriteButton.setImageResource(R.drawable.ic_favorite_border_unchecked); //Uncheck it
                                ProductViewActivity.updateFavouriteCountDB("remove", product.getProductId());
                            }
                        }
                    });
                }
            }
        }
    }

    private void setPhaseIndicator(ImageButton deleteCartProduct, String processingPhase) {
        if(processingPhase == null){
            processingPhase = "A";
        }
        switch (processingPhase){
            case "1":
                deleteCartProduct.setImageResource(R.drawable.ic_processing_coloured);
                break;
            case "2":
                deleteCartProduct.setImageResource(R.drawable.ic_processing);
                break;
            case "3":
                deleteCartProduct.setImageResource(R.drawable.ic_product_delivered_coloured);
                break;
            case "4":
                deleteCartProduct.setImageResource(R.drawable.ic_product_delivered);
                break;
                default:
                    deleteCartProduct.setImageResource(R.drawable.ic_close_black_24dp);
                    break;
        }
    }

    private void showSmallImage(Uri uri, ImageView smallImage) {
        if (uri != null) { //Resizes the image to match the screen width and 2/3 of the image view height, if the url isnt empty
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                int height = Resources.getSystem().getDisplayMetrics().heightPixels;
                Picasso.with(viewHolderContext)
                        .load(uri)//.load(url)
                        .resize(width*2, height*2/3)
                        .centerCrop()
                        .into(smallImage);
            }
    }

    @Override
    public int getItemCount() {
        return SPInfo.size(); // Size of data from source
    }



    public static class DasViewHolder extends RecyclerView.ViewHolder {
        TextView storeName;
        public TextView productPrice;
        ImageView storeImage;
        public static ImageButton favorites;
        public static  ImageButton deleteCartProduct;
        Spinner stockSelect;
        private int cartAccess = OPENED;
        String[] numbers = {"1", "2"};
        private ArrayAdapter mCartFlow;

        public DasViewHolder(@NonNull final View itemView, int myPos) { //Constructor matching super Should describe how to bind data to  single row
            super(itemView);
            storeName = itemView.findViewById(R.id.storename_das_tv);
            productPrice = itemView.findViewById(R.id.product_price_tv);
            storeImage = itemView.findViewById(R.id.storeimage_das_iv);
            favorites = itemView.findViewById(R.id.rv_favorite_ib);
            deleteCartProduct = itemView.findViewById(R.id.remove_from_cart_ib);
            deleteCartProduct.setVisibility(View.INVISIBLE);
            stockSelect = itemView.findViewById(R.id.productnum_sp);

            if(navMenuId == INCOMING_CART_CHECKOUT || navMenuId == OUTGOING_CART_PROCESS){ //If view holder is created for cart.
                StoreActivity.ProductInfo produce = (StoreActivity.ProductInfo) SPInfo.get(myPos);
                String stockNum = "0";
                if(navMenuId == INCOMING_CART_CHECKOUT){
                    StoreActivity.CartListInfo cart = Dashboard.inCartList.get(rowclickPosition);
                    cartAccess = cart.getCartStatus();
                    if(cartAccess == OPENED){
                        stockNum = produce.getStockNo();   //Gets the quantity of products remaining in the vendor's possession.
                    }else stockNum = produce.getProductNo(); //Gets the quantity of products bought by the customer.

                }else if(navMenuId == OUTGOING_CART_PROCESS){
                    StoreActivity.ProductInfo productInfoToProcess = Dashboard.outgoingCart.get(rowclickPosition);
                    cartAccess = CLOSED;
                    stockNum = produce.getProductNo(); //Gets the quantity of products bought by the customer.
                }
                deleteCartProduct.setVisibility(View.VISIBLE);//Add the delete icon only when in incoming cart
                int range = Integer.parseInt(stockNum);
                int g = 0;
                numbers = new String[range];
                while (g < range){
                    String naming = "x";
                    numbers[g] = naming.concat(String.valueOf(g+1));
                    g++;
                }
                if(myPosition == SPInfo.size() - 1){
                    myPosition = 0;
                }else myPosition++;

                //Array Adapter to serve spinner data.
                mCartFlow = new ArrayAdapter(itemView.getContext(), android.R.layout.simple_selectable_list_item, numbers);
                stockSelect.setAdapter(mCartFlow);
                ConstraintLayout dasRowCl = itemView.findViewById(R.id.das_row_cl); // Row CL reference
                stockSelect.setLayoutParams(favorites.getLayoutParams()); // Transfer Favorite Imgae button parameters to that spinner.
                dasRowCl.removeView(favorites); //Replace favorite image button
                stockSelect.setVisibility(View.VISIBLE);

                if(cartAccess == OPENED){

                    stockSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(final AdapterView<?> adapterView, final View view, int i, long l) { //Once the spinner item  has been set
                                check++;
                                if(check > SPInfo.size()){ //This condition was put there to avoid refreshing the Rv Adapter after the initial item setting of eack row spinner.
                                    StoreActivity.ProductInfo product = (StoreActivity.ProductInfo) SPInfo.get(getAdapterPosition());
                                    //product.setProductNo(adapterView.getSelectedItem().toString()); //Set the product no according to the spinner selection
                                    int proNo = adapterView.getSelectedItemPosition() + 1;
                                    product.setProductNo(String.valueOf(proNo)); //Set the product no according to the spinner selection
                                    FirebaseUtil.mUserDataReference.child("incomingcart").child(product.getProductId()).child("productNo").setValue(String.valueOf(proNo));
                                    CheckoutFragment.mCartRecyclerAdapter.notifyDataSetChanged();
                                }
                                cost = DasRecyclerAdapter.getCost();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });
                } else if(cartAccess == CLOSED){
                    deleteCartProduct.setImageResource(R.drawable.ic_processing_coloured);
                    stockSelect.setEnabled(false);
                }
            }

            if(cartAccess != CLOSED){   //If statement is there to avoid deleting a product that has been checked out
                deleteCartProduct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int productPosition = getAdapterPosition();  //Get the position of the item to be deleted
                        StoreActivity.ProductInfo deleting = (StoreActivity.ProductInfo) SPInfo.get(productPosition);
                        SPInfo.remove(deleting); //Delete it from Array List.
                        if(navMenuId == INCOMING_CART_CHECKOUT){
                            FirebaseUtil.mUserDataReference.child("incomingcart").child(deleting.getProductId()).removeValue(); //Remove product from FB DB.
                            check = SPInfo.size()-1; //Makes variable "check" 1 less than the Array size, in other to address the issue of resetting the spinners after an item gets deleted
                            Dashboard.prepareInOutCart(); //Call the cleanup method I created to avoid a null pointer error
                            cost = getCost(); //recalculate the total cost.
                            CheckoutFragment.mCartRecyclerAdapter.notifyDataSetChanged(); // Refresh Rv adapter
                        } else if(navMenuId == PRODUCT_NAV_MENU){
                            FirebaseUtil.mUserDataReference.child("products").child(deleting.getProductId()).removeValue(); //Remove product from myProductList FB DB.
                            //int positionToDelete = Dashboard.myProductsList.indexOf(deleting) ;   //finds the index of the product to be removed from the main Array List.
                            Dashboard.myProductsList.remove(deleting); //Also remove product from the main myProducts List products array
                            FirebaseUtil.mAppDataReference.child("dashboardfeeds").child(deleting.getProductId()).removeValue(); //Remove product from all ProductList FB DB.
                            Dashboard.dashboardFeeds.remove(deleting); //Also remove product from the main all Products List products array
                            //Remember to find a way to delete a deleted product for other users favorites, carts, etc.
                            File tradeProductFiles = new File(Dashboard.mAppDirectory, "Products");  //Get the file directory for Products.
                            File productNameFile = new File(tradeProductFiles, deleting.getProductName() + " images");
                            StorageReference cloudDirectoryToDelete = FirebaseUtil.mStorageRef.child("Products").child(deleting.getProductName() + " images");
                            int i;
                            for(i=0; i<4; i++){
                                File directoryToDelete = new File(productNameFile, deleting.getProductName() + (300+i) + ".jpg");
                                if(directoryToDelete.exists()){directoryToDelete.delete();}   // Remove image from local storage.
                                StorageReference loopDelete = cloudDirectoryToDelete.child(deleting.getProductName() + (300+i));
                                loopDelete.delete();  //Remove images from cloud storage
                            }
                            Dashboard.dasRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            if(Dashboard.mProgressBar.getVisibility() == View.VISIBLE){
                Dashboard.mProgressBar.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rowclickPosition = getAdapterPosition(); //Get the RV row position were the click occurred

                    if(navMenuId == R.id.dashboard || navMenuId == R.id.stores || navMenuId == PRODUCT_NAV_MENU || navMenuId == R.id.favorites || navMenuId ==  0){
                    if (rowclickPosition == 0 && navMenuId == R.id.stores || rowclickPosition == 0 && navMenuId == PRODUCT_NAV_MENU) {
                        //Toast.makeText(itemView.getContext(), "Creating New store!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(itemView.getContext(), StoreActivity.class);
                        itemView.getContext().startActivity(intent);
                    } else {
                        if (DasRecyclerAdapter.navMenuId == R.id.stores) {
                            //Toast.makeText(itemView.getContext(), "Opening store!", Toast.LENGTH_LONG).show();
                            StoreActivity.StoreInfo store = (StoreActivity.StoreInfo) SPInfo.get(rowclickPosition); //Select store from ArrayList
                            Intent intent = new Intent(itemView.getContext(), Dashboard.class);
                            intent.putExtra("Selected store", store);
                            intent.putExtra("instance is for", "Store products");
                            intent.putExtra("Selected navMenu", PRODUCT_NAV_MENU); //A virtual nav Menu was created for Store Products.
                            StoreActivity.rowClickPos = rowclickPosition;
                            itemView.getContext().startActivity(intent);
                        } else {
                            StoreActivity.ProductInfo product = (StoreActivity.ProductInfo) SPInfo.get(rowclickPosition); //Select store from ArrayList
                            Intent intent = new Intent(itemView.getContext(), ProductViewActivity.class);
                            //Dashboard.navItemId = 75;
                            intent.putExtra("Selected product", product);
                            intent.putExtra("Route", "Not cart");
                            ProductViewActivity.rowClickPos = rowclickPosition;
                            //intent.putExtra("Selected navMenu", PRODUCT_NAV_MENU); //A virtual nav Menu was created for Store Products.
                            itemView.getContext().startActivity(intent);
                        }
                    }
                }

                if(navMenuId == R.id.cart || navMenuId == OUTGOING_CART_ID){
                    //Handle event when cart is empty and there is a click
                    Fragment fraging = CheckoutFragment.newInstance("My", "Checkout");
                    FragmentTransaction transaction = Dashboard.mFragmentManager.beginTransaction();
                    transaction.replace(R.id.forfragment_fl, fraging);
                    transaction.addToBackStack("Checkout");
                    transaction.commit();
                    //((CheckoutFragment) fraging).onButtonPressed();
                }

                if(navMenuId == INCOMING_CART_CHECKOUT || navMenuId == OUTGOING_CART_PROCESS){
                    StoreActivity.ProductInfo product = (StoreActivity.ProductInfo) SPInfo.get(rowclickPosition); //Select store from ArrayList
                    String productProcessPhase = product.getProcessingPhase();
                    if(navMenuId == OUTGOING_CART_PROCESS && productProcessPhase.equals("1")){ //Moves to the next phase of product processing.
                        product.setProcessingPhase("2");
                        FirebaseUtil.mUserDataReference.child("outgoingcart").child(product.getProductId()).child("processingPhase").setValue("2");
                        //FirebaseUtil.mydatabase.child("userData").child(product.getBuyerUsername()).child("incomingcart").child(product.getProductId()).child("processingPhase").setValue("2");
                        CartActivity.buildTaskJDForIncoming(product.getProductId(), product, null);
                    }
                    Intent intent = new Intent(itemView.getContext(), ProductViewActivity.class);
                    intent.putExtra("Selected product", product);
                    intent.putExtra("Route", "Cart");
                    ProductViewActivity.rowClickPos = rowclickPosition;
                    itemView.getContext().startActivity(intent);
                }
                }
            });
        }
    }

}
