package com.example.obi1.a3ade;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.text.DateFormat;
import java.util.Date;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableListTitle;
    private HashMap<String, List> expandableListDetail;
    public static boolean productdescriptionAccess; //True means vendor = username.
    public static String xListAdapterProductName;
    public static String xListAdapterProductPrice;
    public static String xListAdapterProductDescription;

    public ExpandableListAdapter(Context context, List<String> expandableListTitle, HashMap<String, List> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
        }

        @Override
        public Object getChild(int listPosition, int expandedListPosition) {
            return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition);
        }

        @Override
        public long getChildId(int listPosition, int expandedListPosition) {
            return expandedListPosition;
        }

        @Override
        public View getChildView(int listPosition, final int expandedListPosition, boolean isLastChild, View childView, ViewGroup parent) {
            int expandedListPositionOffset = expandedListPosition;
            final Object expandedListText = (Object) getChild(listPosition, expandedListPosition);
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            String group = (String)getGroup(listPosition);
            switch(group){
                case "Product info":
                    childView = layoutInflater.inflate(R.layout.productinfo_expansion, null);
                    TextView ProductName = (TextView)childView.findViewById(R.id.product_name_tv);
                    TextView ProductPrice = (TextView)childView.findViewById(R.id.product_price_tv);
                    final EditText ProductDescription = (EditText)childView.findViewById(R.id.product_descrip_ed);

                    ProductName.setText(xListAdapterProductName); //Set product name.
                    ProductPrice.setText(xListAdapterProductPrice);//Set product Price.
                    ProductDescription.setText(xListAdapterProductDescription);//Set product decription.
                    if(ProductDescription.isEnabled()){
                        ProductDescription.setEnabled(productdescriptionAccess); //Text colour has to be set in the xml file
                    }
                    break;
                case "Ratings":
                    if(productdescriptionAccess && expandedListPosition == 0){
                        expandedListPositionOffset++;
                    }
                    if(expandedListPositionOffset == 0){
                        childView = layoutInflater.inflate(R.layout.ratings_expansion, null);
                       // if(!productdescriptionAccess){  //Attend to this later
                            final RatingBar ratingBar = (RatingBar)childView.findViewById(R.id.customer_ratings_rb);
                            Button rateProduct = (Button)childView.findViewById(R.id.rateproduct_b);
                            rateProduct.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String dateTimeOfRating = DateFormat.getDateTimeInstance().format(new Date()); //date and time.
                                    String ratingToStore = String.valueOf(ratingBar.getRating()); //Rating.
                                    StoreActivity.RatingInfo ratingEntry = new StoreActivity.RatingInfo(FirebaseUtil.userName, dateTimeOfRating, ratingToStore);
                                    ProductViewActivity.uploadProductRating(ratingEntry); //Store values in rating store.
                                    Toast.makeText(context, "Rating Submitted!", Toast.LENGTH_SHORT).show();
                                    ProductViewActivity.expandableListAdapter.notifyDataSetChanged();
                                }
                            });
                     //   }else childView.setVisibility(View.GONE);
                    }else{
                        childView = layoutInflater.inflate(R.layout.customer_ratings, null);
                        TextView raterUsername = (TextView)childView.findViewById(R.id.username_tv);
                        TextView timeDate = (TextView) childView.findViewById(R.id.date_time_tv);
                        RatingBar customersRating = (RatingBar)childView.findViewById(R.id.productrating_rb);
                        //StoreActivity.RatingInfo ratedproducts = (StoreActivity.RatingInfo) Dashboard.ratingPerProduct.get(expandedListPosition-1);
                        StoreActivity.RatingInfo ratedproducts = (StoreActivity.RatingInfo) expandedListText;
                        raterUsername.setText(ratedproducts.getUsername());
                        timeDate.setText(ratedproducts.getDateTime());
                        customersRating.setRating(Float.parseFloat(ratedproducts.getRatingToStore()));
                    }

                    break;
                case "Order info":
                    int index = getChildrenCount(listPosition) - 1;
                    if(expandedListPositionOffset == index){ //Checks if its the last item in the Array.
                        childView = layoutInflater.inflate(R.layout.auth_logisticsinfo, null);
                        TextView title = childView.findViewById(R.id.auth_logtitle_tv);
                        EditText fieldEntry = childView.findViewById(R.id.auth_logvalue_et);
                        Button boxItems = childView.findViewById(R.id.boxItems_b);
                        if(productdescriptionAccess){ //Vendor
                            if(ExpandableListDataGen.productEListData.getProcessingPhase().equals("2")){ //Its time to box the item after unstocking at least an item.
                               if(StockUpProductActivity.stockPhaseGrouping("Unstocked for boxing", ExpandableListDataGen.productEListData.getBuyerUsername(), ExpandableListDataGen.productEListData.getCartId() ) > 0){ //if at least 1 item was unstocked
                                   title.setText("Package processed items.");
                                   fieldEntry.setVisibility(View.GONE);
                                   StockingRecyclerAdapter.accessingStockTo = StockUpProductActivity.BOX;
                                   boxItems.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {   //Similar to what we have in Poduct View activity
                                           Intent stockUpIntent = new Intent(context, StockUpProductActivity.class);
                                           stockUpIntent.putExtra("Products Specs", ProductViewActivity.alternateProductInfo);
                                           context.startActivity(stockUpIntent);
                                       }
                                   }); //Open stockup activity
                               }else{
                                   boxItems.setVisibility(View.GONE);
                                   title.setText("Logistics info:");
                               }
                            }else{
                                title.setText("Logistics info:");
                                boxItems.setVisibility(View.GONE);
                                fieldEntry.setEnabled(false);
                                fieldEntry.setText(ProductViewActivity.alternateProductInfo.getLogisticsInfo());
                            }
                        }else{
                            title.setText("Comment:");//Customer
                            boxItems.setVisibility(View.GONE);
                        }
                    }else{
                        childView = layoutInflater.inflate(R.layout.test_listitems, null);
                        TextView expandedListTextView = childView.findViewById(R.id.expandedListItem_tv);
                        expandedListTextView.setText((String) expandedListText);
                    }
                    break;
                    default:
                        childView = layoutInflater.inflate(R.layout.test_listitems, null);
                        TextView expandedListTextView = childView.findViewById(R.id.expandedListItem_tv);
                        expandedListTextView.setText((String) expandedListText);
                        break;
            }
            return childView;
        }

        @Override
        public int getChildrenCount(int listPosition) {
          return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).size();
        }

        @Override
        public Object getGroup(int listPosition) {
            return this.expandableListTitle.get(listPosition);
        }

        @Override
        public int getGroupCount() {
            return this.expandableListTitle.size();
        }

        @Override
        public long getGroupId(int listPosition) {
            return listPosition;
        }

        @Override
        public View getGroupView(int listPosition, boolean isExpanded, View groupView, ViewGroup parent) {
            String listTitle = (String) getGroup(listPosition);
            if (groupView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                groupView = layoutInflater.inflate(R.layout.productview_item_list, null);
            }
            TextView listTitleTextView = (TextView) groupView.findViewById(R.id.itemslist_title_tv);
            listTitleTextView.setTypeface(null, Typeface.BOLD);
            listTitleTextView.setText(listTitle);
            return groupView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int listPosition, int expandedListPosition) {
            return true;
        }
}
