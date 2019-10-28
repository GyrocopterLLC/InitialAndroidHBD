package com.example.david.myapplication.settingsFragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;

import com.example.david.myapplication.R;

// Inherit to support lazy inflating in a ViewStub
public abstract class BaseViewStubFragment extends Fragment {
    private Bundle mSavedInstanceState;
    private boolean mHasInflated = false;
    private ViewStub mViewStub;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        // Just inflate the stub for now
        View view = inflater.inflate(R.layout.fragment_viewstub, container, false);
        mViewStub = (ViewStub) view.findViewById(R.id.fragmentViewStub);
        mViewStub.setLayoutResource(getViewStubLayoutResource());
        mSavedInstanceState = savedInstance;

        if (getUserVisibleHint() && !mHasInflated) {
            View inflatedView = mViewStub.inflate();
            onCreateViewAfterViewStubInflated(inflatedView, mSavedInstanceState);
            afterViewStubInflated(view);
        }
        return view;
    }

    public abstract void onCreateViewAfterViewStubInflated(View inflatedView, Bundle savedInstanceState);
    /**
     * The layout ID associated with this ViewStub
     * @see ViewStub#setLayoutResource(int)
     * @return
     */
    @LayoutRes
    protected abstract int getViewStubLayoutResource();

    /**
     *
     * @param originalViewContainerWithViewStub
     */
    @CallSuper
    protected void afterViewStubInflated(View originalViewContainerWithViewStub) {
        mHasInflated = true;
        // Kill the progress bar
        if(originalViewContainerWithViewStub != null) {
            ProgressBar pb = originalViewContainerWithViewStub.findViewById(R.id.stubProgressBar);
            pb.setVisibility(View.GONE);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && mViewStub != null &&  mViewStub.getParent() != null && !mHasInflated) {
            View inflatedView = mViewStub.inflate();
            onCreateViewAfterViewStubInflated(inflatedView, mSavedInstanceState);
            afterViewStubInflated(getView());
        }
    }


    // Without this, the pager adapter will remove fragments but this class won't reload them!
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHasInflated = false;
    }
}
