package com.example.david.myapplication;

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 * <p>
 * See the Android Training lesson <a href=
 * "http://developer.android.com/training/basics/fragments/communicating.html"
 * >Communicating with Other Fragments</a> for more information.
 *
 * A Bluetooth user fragment wants to send and receive data on Bluetooth.
 * This interface allows the fragment to send data to its parent activity.
 * Receiving data is done through directly called methods in the Fragment
 */
public interface BluetoothUserFragmentInteractionListener {

    // Called when a fragment wants to write Bluetooth data
    void onWrite(StringBuffer outData);
}
