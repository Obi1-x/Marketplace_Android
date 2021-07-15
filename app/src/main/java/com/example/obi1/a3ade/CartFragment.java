package com.example.obi1.a3ade;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

import static com.example.obi1.a3ade.Dashboard.navItemId;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CartFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final int OUTGOING_CART_ID = 500;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private View mCartWorkingView;
    private RecyclerView mRealCartRV;
    public static DasRecyclerAdapter mRealCartRecyclerAdapter;

    public CartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CartFragment newInstance(String param1, String param2) {
        CartFragment fragment = new CartFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cart2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCartWorkingView = view;

        final Spinner cartFlowSpinner = (Spinner)view.findViewById(R.id.in_out_spin); //Spinner refernce.

     /*   ArrayList inOrOut = new ArrayList(); // List array to populate spinner with text.
        inOrOut.add("Incoming Products"); //You can later use xml resource to populate the spinner instead.
        inOrOut.add("Outgoing Products");

        //Array Adapter to serve spinner data.
        ArrayAdapter cartArrayAdapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_item, inOrOut);
        cartArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //Dropdown Spinner resource reference
        cartFlowSpinner.setAdapter(cartArrayAdapter);*/

        cartFlowSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //Spinner's On Item selected listener.
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String cartSelection = adapterView.getSelectedItem().toString();
                if(cartSelection.equals("Outgoing products")){
                    navItemId = OUTGOING_CART_ID; //Give navItem an arbitrary value if outgoing items is selected.
                } else navItemId = R.id.cart; //Else leave the id at cart
                shuffleRvData(navItemId);//Update the recycler adapter.
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                adapterView.setSelection(0);
            }
        });

        mRealCartRV = (RecyclerView)view.findViewById(R.id.real_cartInout_rv); //RV reference.
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 2); //Grid Layout manager Object.
        mRealCartRV.setLayoutManager(gridLayoutManager);

    }

    private void shuffleRvData(int navItemId) {
        mRealCartRecyclerAdapter = new DasRecyclerAdapter(mCartWorkingView.getContext(), navItemId);
        mRealCartRV.setAdapter(mRealCartRecyclerAdapter);  //Created a new adapter to handle the incomingCart list data item
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
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
