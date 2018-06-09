package com.vum.tst2.Views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vum.tst2.Activities.MainActivity;
import com.vum.tst2.R;


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

        ((MainActivity)getActivity()).listActivity.updateUI();

        return v;
    }
}
