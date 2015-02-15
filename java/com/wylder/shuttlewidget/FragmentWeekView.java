package com.wylder.shuttlewidget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by kevin on 2/13/15.
 */
public class FragmentWeekView extends Fragment {

    private WeekView mainView;

    public ConstraintDatabase.OnDatabaseUpdatedListener updateListener = new ConstraintDatabase.OnDatabaseUpdatedListener() {
        @Override
        public void onUpdate(ScheduleConstraint[] newConstraints) {
            mainView.displayConstraints(newConstraints);
        }
    };

    @Override
    public void onCreate(Bundle sis){
        super.onCreate(sis);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis){
        mainView = new WeekView(getActivity());
        return mainView;
    }

    public void updateConstraints(ScheduleConstraint[] constraints){
        try {
            mainView.displayConstraints(constraints);
        }catch(NullPointerException exception){
            // mainView not created yet
        }
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
