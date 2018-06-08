package com.vum.tst2;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public class BlankFragment extends Fragment {

    private View mView;

    public BlankFragment() {
    }


    public static BlankFragment newInstance() {
        return new BlankFragment();
    }

    public View getViewCustom()
    {
        return mView;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_blank, container, false);
        this.mView=v;

        ((MainActivity)getActivity()).updateUI();

        return v;
    }
}
