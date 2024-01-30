// Copyright (c) FUJITSU COMPONENT LIMITED. All rights reserved.
// Licensed under the MIT License.

package com.example.samplecode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.samplecode.DetailFragment.SampleDetailFragment;


public class MainActivity extends AppCompatActivity
{


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar)findViewById(R.id.detail_toolbar);
        //setSupportActionBar(toolbar);


        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(SampleDetailFragment.ARG_ITEM_ID,
                                getIntent().getStringExtra(SampleDetailFragment.ARG_ITEM_ID));
            SampleDetailFragment fragment = new SampleDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.method_detail_container, fragment)
                    .commit();
        }
    }


}
