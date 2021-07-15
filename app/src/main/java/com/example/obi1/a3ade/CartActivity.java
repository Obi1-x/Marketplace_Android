package com.example.obi1.a3ade;

import android.net.Uri;
import android.os.Bundle;

import java.io.Serializable;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class CartActivity extends AppCompatActivity implements CheckoutFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_vew1);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       Fragment selectedFragment = CheckoutFragment.newInstance("My", "Cart");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.forfragment_fl, selectedFragment);
        transaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //Button button = (Button)findViewById(R.id.checkout_b);
        int p = 0;
    }

    public static ArrayList arrangeOutCartList(ArrayList<StoreActivity.ProductInfo> outgoingCartProducts) {
        Dashboard.outCartList.clear();
        int i;
        String cartIdIterator = "A";
        for(i=0; i<outgoingCartProducts.size(); i++){
            StoreActivity.ProductInfo boughtProduct = (StoreActivity.ProductInfo)outgoingCartProducts.get(i);
            cartIdIterator = boughtProduct.getCartId();
            boolean result = ProductViewActivity.doesArrayListHaveAttribute(Dashboard.outCartList, cartIdIterator);
            if(cartIdIterator.equals(boughtProduct.getCartId()) && result == false){  //!=
                Dashboard.outCartList.add(new StoreActivity.CartListInfo(cartIdIterator, ProductViewActivity.CLOSED));
            }
        }
        return Dashboard.outCartList;
    }

    public static void buildTaskJDForOutgoing(StoreActivity.ProductInfo prod, String prodID, String route){
        String taskTimestamp2 = String.valueOf(System.currentTimeMillis());
        FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp2).child("action").setValue("moveToOutgoing");
        FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp2).child("recipient").setValue(prod.getUserName());
        if(route.equals("product")){
            FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp2).child(prodID).setValue(prod); //Dispatches product to task node instead of the vendor's.
        }else if(route.equals("processingPhase")){
            StoreActivity.TaskInfo proPhase = new StoreActivity.TaskInfo(prod.getProcessingPhase());
            FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp2).child(prodID).setValue(proPhase);  //Update vendor outgoing cart product product phase.
        }
    }

    public static void buildTaskJDForIncoming(String prodId, StoreActivity.ProductInfo prod, String logistics){
        String taskTimestamp1 = String.valueOf(System.currentTimeMillis());
        FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp1).child("action").setValue("moveToIncoming");
        FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp1).child("recipient").setValue(prod.getBuyerUsername());
        StoreActivity.TaskInfo proPhase = new StoreActivity.TaskInfo(prod.getProcessingPhase());
        if(logistics != null){ //!logistics.isEmpty()
            proPhase.setLogisticsInfo(logistics);
        }
        FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp1).child(prodId).setValue(proPhase);
    }

    public static void buildPretaskJDToBufferStock(StoreActivity.ProductInfo prod, StockUpProductActivity.StockUpInfo stockToBuffer){
        String taskTimestamp4 = String.valueOf(System.currentTimeMillis());
        FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp4).child("action").setValue("createtask");
        FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp4).child("recipient").setValue(prod.getUserName());

        StoreActivity.TaskInfo proPhase = new StoreActivity.TaskInfo(null);
        proPhase.setUniqueCode(stockToBuffer.getUniqueCode());
        proPhase.setAuthType(stockToBuffer.getAuthType());
        proPhase.setSoldTo(prod.getBuyerUsername());
        FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp4).child(prod.getProductId()).setValue(proPhase);
    }
}
