package com.example.obi1.a3ade;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.obi1.a3ade.StockUpProductActivity.BOX;
import static com.example.obi1.a3ade.StockUpProductActivity.STOCK;
import static com.example.obi1.a3ade.StockUpProductActivity.RECEIVER;
import static com.example.obi1.a3ade.StockUpProductActivity.UNSTOCK;
import static com.example.obi1.a3ade.StockUpProductActivity.VIEW;
import static com.example.obi1.a3ade.StockUpProductActivity.productStockInfo;

public class StockingRecyclerAdapter extends RecyclerView.Adapter<StockingRecyclerAdapter.StockingViewHolder> {
    private final Context stockContext;
    private ArrayList STInfo;
    private LayoutInflater stockRowLayoutInflater;
    static int accessingStockTo, previousAccessingStockToValue;

    public StockingRecyclerAdapter(Context context){
        this.stockContext = context;

        stockRowLayoutInflater = LayoutInflater.from(this.stockContext);
        if(accessingStockTo == STOCK || accessingStockTo == UNSTOCK || accessingStockTo == BOX || accessingStockTo == RECEIVER){
            STInfo = StockUpProductActivity.productStockInfo;
        } else STInfo = StockUpProductActivity.uploadedProductStockInfo;
    }

    @NonNull
    @Override
    public StockingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View stockItemView = stockRowLayoutInflater.inflate(R.layout.stock_row_rv, parent, false); // Inflate the view using the a layout resource and the created inflater.
        return new StockingViewHolder(stockItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockingViewHolder holder, int position) {
        int tally = holder.getAdapterPosition();
        holder.rowTally.setText(String.valueOf(tally + 1) + ".");
        StockUpProductActivity.StockUpInfo stockRow = (StockUpProductActivity.StockUpInfo) STInfo.get(position);
        if(stockRow.getUniqueCode() != null){
            holder.uniqueCodeET.setText(stockRow.getUniqueCode());
            if(accessingStockTo == VIEW){
                switch(stockRow.getStockPhase()){
                    case "Stocked":
                        holder.uniqueCodeET.setTextColor(Color.BLACK);
                        break;
                    case "Unstocked for boxing":
                        holder.uniqueCodeET.setTextColor(Color.YELLOW);
                        break;
                    case "Boxed":
                        holder.uniqueCodeET.setTextColor(Color.BLUE);
                        break;
                    case "Received":
                        holder.uniqueCodeET.setTextColor(Color.GREEN);
                        break;
                }
            }
        }
        if(accessingStockTo == VIEW || accessingStockTo == BOX){
            String authSelection = stockRow.getAuthType();
            switch (authSelection){
                case "IMEI":
                    holder.authTypeSelector.setSelection(0);
                    break;
                case "Product code":
                    holder.authTypeSelector.setSelection(1);
                    break;
                case "Generate":
                    holder.authTypeSelector.setSelection(2);
                    break;
            }
            holder.authTypeSelector.setEnabled(false);
            holder.uniqueCodeET.setEnabled(false);
        }
    }

    @Override
    public int getItemCount(){
        return STInfo.size();
    }

    public class StockingViewHolder extends RecyclerView.ViewHolder{
        TextView rowTally;
        Spinner authTypeSelector;
        EditText uniqueCodeET;
        Button scan;
        ImageButton remove;

        public StockingViewHolder(@NonNull final View itemView) {
            super(itemView);
            rowTally = (TextView)itemView.findViewById(R.id.stockTally_tv);
            authTypeSelector = (Spinner)itemView.findViewById(R.id.pgIMEselect_s);
            uniqueCodeET = (EditText)itemView.findViewById(R.id.pgIME_ed);
            scan = (Button)itemView.findViewById(R.id.scanQR_b);
            remove = (ImageButton)itemView.findViewById(R.id.removerow_ib);

            authTypeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = (String)parent.getSelectedItem();
                    StockUpProductActivity.StockUpInfo stockToSetAuthType = (StockUpProductActivity.StockUpInfo)STInfo.get(getAdapterPosition());
                    stockToSetAuthType.setAuthType(selectedItem);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    StockUpProductActivity.StockUpInfo stockToSetAuthType = (StockUpProductActivity.StockUpInfo)STInfo.get(getAdapterPosition());
                    stockToSetAuthType.setAuthType("IMEI");
                }
            });

            TextWatcher watcherForUniqueCode = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    StockUpProductActivity.StockUpInfo stockData = (StockUpProductActivity.StockUpInfo) productStockInfo.get(getAdapterPosition());
                    stockData.setUniqueCode(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) { }
            };

            if(accessingStockTo == VIEW){ //To view stocked products.
                uniqueCodeET.removeTextChangedListener(watcherForUniqueCode);
                //uniqueCodeET.setEnabled(false);
                scan.setEnabled(false); //scan.setVisibility(View.GONE);  Adjust visibility when you have found a way to encrypt or hide the unique code when viewing stocks.
            }else{    //To set products
                authTypeSelector.setEnabled(true);
                uniqueCodeET.setEnabled(true);
                uniqueCodeET.addTextChangedListener(watcherForUniqueCode);
                if(accessingStockTo == BOX){
                    scan.setText("BOX IT");
                }
                scan.setEnabled(true);//scan.setVisibility(View.VISIBLE);
                scan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String message = "A";
                        if(accessingStockTo == BOX){
                            message = "Opening Video camera";
                        }else message = "Opening QR scanner.";
                        Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //Might disable this action when viewing stocks.
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int stockingRow = getAdapterPosition();
                    StockUpProductActivity.StockUpInfo stockInfoToDelete = (StockUpProductActivity.StockUpInfo) STInfo.get(stockingRow);
                    STInfo.remove(stockingRow);
                    if(accessingStockTo == VIEW){
                        StockUpProductActivity.mStockReference.child(stockInfoToDelete.getUniqueCode()).removeValue();  //Delete stock from database.
                    }else if(accessingStockTo == BOX){
                        stockInfoToDelete.setStockPhase("Stocked");
                        StockUpProductActivity.mStockReference.child(stockInfoToDelete.getUniqueCode()).setValue(stockInfoToDelete);  //Re stock product by resetting phase back to stocked.
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }
}
