package com.example.obi1.a3ade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataGen {
    public static StoreActivity.ProductInfo productEListData;
    private static List product_info;
    //private static List ratings;
    private static StoreActivity.ProfileInfo thisUserProfile;


    public static HashMap<String, List> getVendorGroup(String route, String cartStatus) {

        prepareInfoReviews();
        HashMap<String, List> expandableListDetail = new HashMap<String, List>();
        expandableListDetail.put("Product info", product_info);
        if(route.equals("Cart")){
            List order_info = new ArrayList<String>();
            order_info.add("Buyer username: " + productEListData.getBuyerUsername());
            order_info.add("Buyer's fullname: " + productEListData.getBuyerFullName());
            order_info.add("Buyer address: " + productEListData.getBuyerAddress());
            order_info.add("Payment method: PREPAID.");
            order_info.add(ProductViewActivity.countOfBoxedUnstocked);
            order_info.add("Logistics info: ");
            expandableListDetail.put("Order info", order_info);
        }
        expandableListDetail.put("Ratings", Dashboard.ratingPerProduct);
        return expandableListDetail;
        }

    public static HashMap<String, List> getCustomerGroup(String route, String cartStatus) {
        String address = "Non.", fullName = "Non.";
        if(Dashboard.userProfile.size() > 0){  //Remove this if condition after making the profile Activity pop up immediately after installation.
            thisUserProfile = Dashboard.userProfile.get(0);
            address = thisUserProfile.getAddress();
            fullName = thisUserProfile.getFullName();
        }

        prepareInfoReviews();
        HashMap<String, List> expandableListDetail = new HashMap<String, List>();
        expandableListDetail.put("Product info", product_info);
        if(route.equals("Cart") && cartStatus.equals("CLOSED")){
            List vendor_info = new ArrayList<>();
            vendor_info.add("Vendor username: " + productEListData.getUserName());
            vendor_info.add("Product's store: " + productEListData.getStoreName());
            vendor_info.add("Delivery methods: GIG.");

            List order_info = new ArrayList<>();
            order_info.add("Buyer's username: " + FirebaseUtil.userName);
            order_info.add("Buyer's fullname: " + fullName);
            order_info.add("Buyer's address: " + address);
            order_info.add("Payment method: PREPAID.");
            order_info.add("Logistics info: Tracking ID: 12345432");
            order_info.add("Comment:");
            expandableListDetail.put("Vendor info", vendor_info);
            expandableListDetail.put("Order info", order_info);
        }
        expandableListDetail.put("Ratings", Dashboard.ratingPerProduct);

        return expandableListDetail;
    }

    private static void prepareInfoReviews() {
        product_info = new ArrayList<>();
        product_info.add("Product name");
    }


    public static HashMap<String, String> makeFavoriteProductLink(String productId, String productName){
        HashMap<String, String> favoriteProductLink = new HashMap<String, String>();
        favoriteProductLink.put(productId, productName);
        return favoriteProductLink;
    }
}
