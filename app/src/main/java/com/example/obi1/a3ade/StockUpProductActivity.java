package com.example.obi1.a3ade;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class StockUpProductActivity extends AppCompatActivity {
    public static final int STOCK = 5;
    public static final int VIEW = 10;
    public static final int UNSTOCK = 15;
    public static final int BOX = 20;
    public static final int RECEIVER = 25;
    public static ArrayList<StockUpInfo> productStockInfo;          //Holds the new stock info for each row
    public static ArrayList<StockUpInfo> uploadedProductStockInfo;  //Holds the stock info from DB for each row
    private RecyclerView mStockRv;
    int number = 0;
    public StockingRecyclerAdapter mStockingRecyclerAdapter;
    private StoreActivity.ProductInfo mProductSpecs;
    public static DatabaseReference mStockReference;
    public DataSnapshot pretaskSelfie;
    //private DatabaseReference mBuyerStocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_up_product);

        TextView productName = (TextView)findViewById(R.id.stockproductname_tv);
        TextView productDescrip = (TextView)findViewById(R.id.stockproductdesc_tv);
        TextView productCatergory = (TextView)findViewById(R.id.stockproductCategory_tv);
        TextView specsWaring = (TextView)findViewById(R.id.specswarning_tv);
        Button addNewRow = (Button)findViewById(R.id.addStock_b);
        final Button showStock = (Button)findViewById(R.id.showstock_b);
        final Button stockProductDetails = (Button)findViewById(R.id.stockup_b);

        mStockRv = (RecyclerView)findViewById(R.id.stocking_rv);
        LinearLayoutManager stockUpLayoutManager  = new LinearLayoutManager(this);
        mStockRv.setLayoutManager(stockUpLayoutManager);
        updateStockAdapter();

        //mProductSpecs = (StoreActivity.ProductInfo) getIntent().getSerializableExtra("Products Specs");
        mProductSpecs = ProductViewActivity.alternateProductInfo;
        mStockReference = FirebaseUtil.mUserDataReference.child("products").child(mProductSpecs.getProductId()).child("stocks");
        final String copyOfmThisProductId = String.valueOf(ProductViewActivity.mThisProductId);
        /*
        if(ProductViewActivity.Route.equals("Cart")){
            mBuyerStocks = FirebaseUtil.mydatabase.child("userData").child(mProductSpecs.getBuyerUsername()).child("incomingcart").child(yes);
        }*/

        productName.setText("NAME: " + mProductSpecs.getProductName());
        productDescrip.setText("DESCRIPTION: " + mProductSpecs.getProductDescription());

        if(StockingRecyclerAdapter.accessingStockTo == UNSTOCK){
            specsWaring.setText("NOTE: Unstock products according to specifications above.");
            stockProductDetails.setText("UNSTOCK");
        }else if(StockingRecyclerAdapter.accessingStockTo == BOX){
            specsWaring.setText("NOTE: Package products according to specifications above.");
            stockProductDetails.setText("SUBMIT");
            int i;
            productStockInfo.clear();
            for(i=0; i<uploadedProductStockInfo.size(); i++){
                StockUpInfo stockRowForBoxing = (StockUpInfo)uploadedProductStockInfo.get(i);
                if(stockRowForBoxing.getStockPhase().equals("Unstocked for boxing")){
                    productStockInfo.add(stockRowForBoxing);
                }
            }
        }else if(StockingRecyclerAdapter.accessingStockTo == RECEIVER){
            specsWaring.setText("NOTE: Authenticate products according to specifications above.");
            stockProductDetails.setText("SUBMIT");
            showStock.setVisibility(View.GONE);

            DatabaseReference uniquecodeInPretask = FirebaseUtil.mUserDataReference.child("pretask"); //Used for buyer to receive product
            uniquecodeInPretask.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    pretaskSelfie = dataSnapshot;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        if(StockingRecyclerAdapter.accessingStockTo != BOX){
            addNewRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    productStockInfo.add(new StockUpInfo("2"));
                    mStockingRecyclerAdapter.notifyDataSetChanged();
                }
            });
        }


        if(StockingRecyclerAdapter.accessingStockTo != RECEIVER){
            showStock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(StockingRecyclerAdapter.accessingStockTo == STOCK || StockingRecyclerAdapter.accessingStockTo == UNSTOCK || StockingRecyclerAdapter.accessingStockTo == BOX){ //If stockup was opened to edit product stocks
                        showStock.setText("HIDE STOCKED PRODUCTS");
                        StockingRecyclerAdapter.previousAccessingStockToValue = StockingRecyclerAdapter.accessingStockTo; //Saves the state of accessing Stock
                        StockingRecyclerAdapter.accessingStockTo = VIEW; //Set Stockup to view.
                        stockProductDetails.setEnabled(false);
                    }else if(StockingRecyclerAdapter.accessingStockTo == VIEW){ //If stockup was opened to edit product stocks
                        showStock.setText("SHOW STOCKED PRODUCTS");
                        //StockingRecyclerAdapter.accessingStockTo = STOCK; //Set Stockup to edit.
                        StockingRecyclerAdapter.accessingStockTo = StockingRecyclerAdapter.previousAccessingStockToValue; //Set Stockup to the previous accessing stock value.
                        stockProductDetails.setEnabled(true);
                    }
                    updateStockAdapter();
                }
            });
        }


        stockProductDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if they is an empty Edit Text in any row. Point user's attention if so.
                //Verify the unique details that are in each row. Proceed if its good.
                //Store data of each row in the array list.
                String toastMessage = "Failed!!!";
                int i;
                //final boolean[] upload = {false};

                Object[] JDset = new Object[0];
                HashMap n = null;
                if(StockingRecyclerAdapter.accessingStockTo == RECEIVER){
                    n = (HashMap) pretaskSelfie.getValue(); //Gets all the values in the child node of the Datasnapshot received byt the listener.
                    //JDset = new Object[0];
                    if (n != null) {
                        JDset = n.keySet().toArray();
                    }
                }

                for(i=0; i<productStockInfo.size(); i++){   //Upload data in the arrayList to DB.
                    StockUpInfo stockToUpload = productStockInfo.get(i);
                    if(StockingRecyclerAdapter.accessingStockTo == RECEIVER && n != null){ //When a buyer authenticates a product after receiving it.
                        String JD = JDset[i].toString();
                        //String refUniqueCode = pretaskSelfie.child(JD).child(copyOfmThisProductId).child("uniqueCode").getValue().toString();
                        String refUniqueCode = pretaskSelfie.child(JD).child(mProductSpecs.getProductId()).child("uniqueCode").getValue().toString();
                        String uniqueIdToVerify = stockToUpload.getUniqueCode();
                        if(uniqueIdToVerify.equals(refUniqueCode) && uniqueIdToVerify != null && refUniqueCode != null){
                            HashMap map = (HashMap) n.get(JD);
                            map.remove("action");
                            map.put("action", "moveToProducts");  //Change action to initiate the server backend work.
                            String taskTimestamp3 = String.valueOf(System.currentTimeMillis());
                            FirebaseUtil.mUserDataReference.child("task").child("JD"+taskTimestamp3).setValue(map); //Upload copy of preset node.
                            FirebaseUtil.mUserDataReference.child("pretask").removeValue(); //Clear the pretask node.
                            mProductSpecs.setProcessingPhase("4");
                            Dashboard.outgoingCart.remove(mProductSpecs);
                            Dashboard.outgoingCart.add(mProductSpecs);    //REVISIT!!

                            //Return the database codes below to ProductViewActivity, after figuring out how to handle backstack. From here,
                            CartActivity.buildTaskJDForOutgoing(mProductSpecs, copyOfmThisProductId, "processingPhase");
                            FirebaseUtil.mUserDataReference.child("incomingcart").child(copyOfmThisProductId).child("processingPhase").setValue(mProductSpecs.getProcessingPhase());
                            //to here.
                            toastMessage = "Successful!!!";
                        }
                    }else if(StockingRecyclerAdapter.accessingStockTo != RECEIVER){
                        if(StockingRecyclerAdapter.accessingStockTo == STOCK){  //Adding the products to the stock tray
                            stockToUpload.setStockPhase("Stocked");
                            stockToUpload.setStockPhaseFor("none"); //Notes who this product was un stocked for.
                            stockToUpload.setCartidOfOrder("none");
                        }else if(StockingRecyclerAdapter.accessingStockTo == UNSTOCK){  //Removing the products from the stock tray
                            stockToUpload.setStockPhase("Unstocked for boxing");
                            stockToUpload.setStockPhaseFor(mProductSpecs.getBuyerUsername()); //Notes who this product was un stocked for.
                            stockToUpload.setCartidOfOrder(mProductSpecs.getCartId());
                        }else if(StockingRecyclerAdapter.accessingStockTo == BOX){
                            stockToUpload.setStockPhase("Boxed");
                            stockToUpload.setStockPhaseFor(mProductSpecs.getBuyerUsername()); //Notes who this product was boxed for.
                            stockToUpload.setCartidOfOrder(mProductSpecs.getCartId());
                            CartActivity.buildPretaskJDToBufferStock(mProductSpecs, stockToUpload); //Sends the stock to the buyer's task node for faster verification once its received.
                            //mBuyerStocks.child("stocks").child(stockToUpload.getUniqueCode()).setValue(stockToUpload); //Move the products that have been boxed to the buyer's incoming cart.
                            //uploadedProductStockInfo.remove(stockToUpload); //Ideally, this should be done after the boxing authentication process has been approved.
                        }
                        mStockReference.child(stockToUpload.getUniqueCode()).setValue(stockToUpload);
                        toastMessage = "Successful!!!";
                    }
                }
                Toast.makeText(StockUpProductActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                if(toastMessage.equals("Successful!!!")){
                    productStockInfo.clear();
                    finish();
                }
                /* Until i figured out the mystries of activity back stack
                Intent intent = new Intent(StockUpProductActivity.this, ProductViewActivity.class);
                intent.putExtra("Selected product", mProductSpecs);
                intent.putExtra("Route", "From stock");
                startActivity(intent); //Replace this with popingback stack
                */
            }
        });
    }

    public static void initializeArrayLists() {
        if(productStockInfo == null){
            productStockInfo = new ArrayList<StockUpInfo>();
        }

        if(productStockInfo.size() == 0){
            productStockInfo.add(new StockUpInfo("1"));
        }

        if(uploadedProductStockInfo == null){
            uploadedProductStockInfo = new ArrayList<StockUpInfo>();
        }
    }

    public static int stockPhaseGrouping(String keyword, String hint, String cartID){
        int i, resultCount = 0;
        for(i=0; i<uploadedProductStockInfo.size() && uploadedProductStockInfo.size()>0; i++){
            StockUpInfo stockToGroup = (StockUpInfo)uploadedProductStockInfo.get(i);
            if(hint.equals("non")){  //Not considering buyer username.
                if(stockToGroup.getStockPhase().equals(keyword)){
                    resultCount++;
                }
            }else if(!hint.equals("non") && hint != null && cartID != null){  //Considering buyer username.
                if(stockToGroup.getStockPhase().equals(keyword) && stockToGroup.getStockPhaseFor().equals(hint) && stockToGroup.getCartidOfOrder().equals(cartID)){  ///This performs a specified search base on the hint argument.
                    resultCount++;
                }
            }
        }
        return resultCount;
    }

    private void updateStockAdapter() {
        mStockingRecyclerAdapter = new StockingRecyclerAdapter(this);
        mStockRv.setAdapter(mStockingRecyclerAdapter);
        mStockingRecyclerAdapter.notifyDataSetChanged();
    }

    public static class StockUpInfo implements Serializable{
        private String tally;
        private String authType;
        private String uniqueCode;
        private String stockPhase;
        private String stockPhaseFor;
        private String soldStockTo;
        private String cartidOfOrder;

        public StockUpInfo(){}

        public StockUpInfo(String tally){
            this.tally = tally;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getUniqueCode() {
            return uniqueCode;
        }

        public void setUniqueCode(String uniqueCode) {
            this.uniqueCode = uniqueCode;
        }

        public String getStockPhase() {
            return stockPhase;
        }

        public void setStockPhase(String stockPhase) {
            this.stockPhase = stockPhase;
        }

        public String getSoldStockTo() {
            return soldStockTo;
        }

        public void setSoldStockTo(String soldStockTo) {
            this.soldStockTo = soldStockTo;
        }

        public String getStockPhaseFor() {
            return stockPhaseFor;
        }

        public void setStockPhaseFor(String stockPhaseFor) {
            this.stockPhaseFor = stockPhaseFor;
        }

        public String getCartidOfOrder() {
            return cartidOfOrder;
        }

        public void setCartidOfOrder(String cartidOfOrder) {
            this.cartidOfOrder = cartidOfOrder;
        }
    }
}
