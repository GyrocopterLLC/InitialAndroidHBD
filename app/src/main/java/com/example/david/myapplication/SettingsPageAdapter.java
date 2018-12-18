package com.example.david.myapplication;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.david.myapplication.settingsFragments.SettingsControl;
import com.example.david.myapplication.settingsFragments.SettingsFOC;
import com.example.david.myapplication.settingsFragments.SettingsData;
import com.example.david.myapplication.settingsFragments.SettingsMotor;
import com.example.david.myapplication.settingsFragments.SettingsThrottle;
import com.example.david.myapplication.settingsFragments.SettingsUtil;

public class SettingsPageAdapter extends FragmentPagerAdapter {
    public SettingsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch(position) {
            case 0:
                return "Data";
            case 1:
                return "FOC";
            case 2:
                return "Motor";
            case 3:
                return "Utilities";
            case 4:
                return "Controller";
            case 5:
                return "Throttle";
            default:
                return "null";
        }
    }

    @Override
    public Fragment getItem(int i) {
        Fragment toReturn;
        switch(i) {
            case 0:
                // First page - top level commands
                toReturn = SettingsData.newInstance();
                break;
            case 1:
                // Second page - FOC
                toReturn = SettingsFOC.newInstance();
                break;
            case 2:
                toReturn = SettingsMotor.newInstance();
                break;
            case 3:
                toReturn = SettingsUtil.newInstance();
                break;
            case 4:
                toReturn = SettingsControl.newInstance();
                break;
            case 5:
                toReturn = SettingsThrottle.newInstance();
                break;
            default:
                toReturn = null;
                break;
        }
        return toReturn;
    }

    @Override
    public int getCount() {
        return 6;
    }
}
