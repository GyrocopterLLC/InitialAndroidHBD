package com.example.david.myapplication.settingsFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.david.myapplication.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsControl#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsControl extends BaseViewStubFragment {

    private SettingsFragmentInteractionListener mListener;

    public SettingsControl() {
        // Required empty public constructor
    }

    /**
     * Default newInstance - just returns the empty constructor version of the class.
     * @return A new instance of fragment SettingsData
     */
    public static SettingsControl newInstance() {
        SettingsControl fragment = new SettingsControl();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public void onCreateViewAfterViewStubInflated(View inflatedView, Bundle savedInstanceState) {
        // Nothing.
    }

    @Override
    protected int getViewStubLayoutResource() {
        return R.layout.fragment_settings_control;
    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
            View v = super.onCreateView(inflater,container,savedInstanceState);
//        View v = inflater.inflate(R.layout.fragment_settings_control, container, false);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SettingsFragmentInteractionListener) {
            mListener = (SettingsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SettingsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
