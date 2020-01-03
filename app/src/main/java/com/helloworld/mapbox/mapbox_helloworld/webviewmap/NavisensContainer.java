package com.helloworld.mapbox.mapbox_helloworld.webviewmap;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.helloworld.mapbox.mapbox_helloworld.R;

public class NavisensContainer extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container);
        Fragment newFragment = new NavisensMaps();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.mapContainer, newFragment, newFragment.getClass().getName()).commit();
    }
}
