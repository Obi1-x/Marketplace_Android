package com.example.obi1.a3ade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.viewpager.widget.PagerAdapter;

import static androidx.core.app.ActivityCompat.startActivityForResult;


public class VpScreenSlidePagerAdapter extends PagerAdapter {
    Context context;
    Bundle bundle;
    public static int positioning;
    LayoutInflater layoutInflater;
    private static final int PICTURE_RESULT = 42; //Upload image request code


    public VpScreenSlidePagerAdapter(Context context, Bundle bundle) {
        this.context = context;
        this.bundle = bundle;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return StoreActivity.NUM_PAGES;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        View itemView = layoutInflater.inflate(R.layout.viewpager_layout, container, false);

        final ImageView imageView = (ImageView) itemView.findViewById(R.id.slide_layout_iv);
        if(StoreActivity.mImageSliderUri[position].toString().equals("A")){  //(StoreActivity.mImageSliderUri == null){
            imageView.setImageResource(R.drawable.ic_das_add_black_24dp);
        }else{
            imageView.setImageURI(StoreActivity.mImageSliderUri[position]);
        }

        container.addView(itemView);
        //positioning = position - 1;
        Class requestingClass = context.getClass();
        if(requestingClass.equals(StoreActivity.class)){
            //listening to image click
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // Intent of type get contents  used to the an image resource.
                    intent.setType("image/jpeg"); //Data type expected
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true); //The source of the image should be local
                    //Activity activity = new StoreActivity();
                    Activity active = (Activity) context;
                    startActivityForResult(active, Intent.createChooser(intent, "Insert Image"), PICTURE_RESULT, bundle); //Remember to ask for permission at the Manifest.
                }
            });
        }
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
