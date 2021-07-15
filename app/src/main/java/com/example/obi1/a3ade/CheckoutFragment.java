package com.example.obi1.a3ade;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.telephony.mbms.MbmsHelper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;

import static com.example.obi1.a3ade.Dashboard.inCartList;
import static com.example.obi1.a3ade.Dashboard.incomingCart;
import static com.example.obi1.a3ade.Dashboard.loadingCart;
import static com.example.obi1.a3ade.Dashboard.navItemId;
import static com.example.obi1.a3ade.Dashboard.outCartList;
import static com.example.obi1.a3ade.Dashboard.outgoingCart;
import static com.example.obi1.a3ade.ProductViewActivity.CLOSED;
import static com.example.obi1.a3ade.ProductViewActivity.OPENED;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CheckoutFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CheckoutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckoutFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    public static View mWorkingView;
    private RecyclerView mCartRV;
    public static DasRecyclerAdapter mCartRecyclerAdapter;
    private int cartStatus;
    private StoreActivity.CartListInfo mCartSeletion;
    private Toolbar mHeadingToolBar;
    private String toolbarTitle;

    public CheckoutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CheckoutFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckoutFragment newInstance(String param1, String param2) {
        CheckoutFragment fragment = new CheckoutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        if(navItemId == R.id.cart){
            mCartSeletion = inCartList.get(DasRecyclerAdapter.rowclickPosition);
            cartStatus = mCartSeletion.getCartStatus();
            loadingCart.clear(); //Gets it ready for below.
            loadingCart = Dashboard.porpulateListWithProducts(mCartSeletion.getCartId(), incomingCart, R.id.dashboard);//Populate storeProducts array ahead of time with products from a particular store
        }else if(navItemId == DasRecyclerAdapter.OUTGOING_CART_ID){
            mCartSeletion = outCartList.get(DasRecyclerAdapter.rowclickPosition);
            cartStatus = CLOSED;
            loadingCart.clear(); //Gets it ready for below.
            loadingCart = Dashboard.porpulateListWithProducts(mCartSeletion.getCartId(), outgoingCart, R.id.dashboard);//Populate storeProducts array ahead of time with products from a particular store
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHeadingToolBar.setTitle(toolbarTitle);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWorkingView = view;

        mCartRV = (RecyclerView)view.findViewById(R.id.cart_inout_rv); //RV reference.
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 2); //Grid Layout manager Object.
        mCartRV.setLayoutManager(gridLayoutManager);
        Button checkout = (Button)view.findViewById(R.id.checkout_b);
        mHeadingToolBar = getActivity().findViewById(R.id.toolbar);
        toolbarTitle = (String) mHeadingToolBar.getTitle();
        if(navItemId == R.id.cart){
            navItemId = DasRecyclerAdapter.INCOMING_CART_CHECKOUT;
            mHeadingToolBar.setTitle("Incoming cart products"); //Changes the heading in the toolbar to that suitable for incoming cart.
            if(cartStatus == OPENED){
                final VariableChangeListener listenForSnapshot = new VariableChangeListener();
                listenForSnapshot.set(0);
                int y = 0;
                while (y < loadingCart.size()){
                    final StoreActivity.ProductInfo producing = loadingCart.get(y);
                    String neededId = ProductViewActivity.extractDefaultID(producing);
                    FirebaseUtil.mAppDataReference.child("dashboardfeeds").child(neededId).child("productNo").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String numb = (String) dataSnapshot.getValue(); //Gets the current number of products as its in dashboard feeds.
                            producing.setStockNo(numb);
                            Log.d("ProductNO", "Quantity: " + producing.getProductNo() + " of " + producing.getStockNo());
                            int count = listenForSnapshot.get();
                            listenForSnapshot.set(count + 1); //Increments the value of the value listener, to keep track of the single value event when it returns;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                    y++;
                }

                listenForSnapshot.setOnIntegerChangeListener(new VariableChangeListener.OnIntegerChangeListener() {
                    @Override
                    public void onIntegerChanged(int newValue) {
                        if(newValue == loadingCart.size()){
                            shuffleRvData(navItemId);//Update the recycler adapter.
                            listenForSnapshot.set(0);
                        }
                    }
                });
            }else shuffleRvData(navItemId);//Update the recycler adapter.
        }else {
            navItemId = DasRecyclerAdapter.OUTGOING_CART_PROCESS;
            mHeadingToolBar.setTitle("Outgoing cart products");  //Changes the heading in the toolbar to that suitable for outgoing cart.
            shuffleRvData(navItemId);//Update the recycler adapter.
        }


        if(cartStatus == OPENED){
            checkout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCartSeletion.setCartStatus(CLOSED); //Close the cart we are working on.  Updates local database.
                    dispatchCartProductInfo();
                    DatabaseReference cartDatabase = FirebaseUtil.mUserDataReference.child("carts").child(mCartSeletion.getCartId()).child("cartStatus");// Update online database.
                    cartDatabase.setValue(CLOSED);
                    Dashboard.mFragmentManager.popBackStack(); //Goes back to incomingCartList fragment
                }
            });
        } else if(cartStatus == CLOSED){
            checkout.setVisibility(View.GONE);
        }
    }

    private void dispatchCartProductInfo() {
        int i;
        for(i=0; i<loadingCart.size(); i++){
            StoreActivity.ProfileInfo profile = Dashboard.userProfile.get(0);
            StoreActivity.ProductInfo product = loadingCart.get(i);
            product.setBuyerFullName(profile.getFullName()); //Set the required profile details.
            product.setBuyerUsername(FirebaseUtil.userName);
            product.setBuyerAddress(profile.getAddress());
            product.setProcessingPhase("1");
            //Remember to set a purchase price at DB. This is the price at whis the product was bought, which reflects any discount or so.
            //FirebaseUtil.mydatabase.child("userData").child(product.getUserName()).child("outgoingcart").child(product.getProductId()).setValue(product); //Transfers product to vendor's outgoing cart
            CartActivity.buildTaskJDForOutgoing(product, product.getProductId(), "product");
            FirebaseUtil.mUserDataReference.child("incomingcart").child(product.getProductId()).child("processingPhase").setValue(product.getProcessingPhase()); //Sets the incoming phase value of the customer's incoming cart.
        }
    }

    public void shuffleRvData(int navItemId) {
        mCartRecyclerAdapter = new DasRecyclerAdapter(mWorkingView.getContext(), navItemId);
        mCartRV.setAdapter(mCartRecyclerAdapter);  //Created a new adapter to handle the incomingCart list data item
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) { // If the passed context is an instance of frag intraction Listener
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
