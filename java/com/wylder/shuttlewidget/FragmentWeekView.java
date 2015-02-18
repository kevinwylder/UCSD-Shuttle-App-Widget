package com.wylder.shuttlewidget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by kevin on 2/13/15.
 *
 * A fragment that displays the WeekView
 */
public class FragmentWeekView extends Fragment {

    private WeekView mainView;

    // this will be added to the ConstraintDatabase's ArrayList<OnDatabaseUpdatedListener> and
    // will update the UI with the retrieved ScheduleConstraints
    public ConstraintDatabase.OnDatabaseUpdatedListener updateListener = new ConstraintDatabase.OnDatabaseUpdatedListener() {
        @Override
        public void onUpdate(ScheduleConstraint[] newConstraints) {
            mainView.displayConstraints(newConstraints);
        }
    };

    /**
     * This method will return the WeekView to be in the Fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis){
        mainView = new WeekView(getActivity());
        return mainView;
    }

    /**
     * A workaround for a bug in FragmentManagerImpl, more info: https://code.google.com/p/android/issues/detail?id=19211
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        //first saving my state, so the bundle wont be empty.
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY",  "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

}
