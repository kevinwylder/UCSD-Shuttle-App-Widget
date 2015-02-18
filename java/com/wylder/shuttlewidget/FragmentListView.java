package com.wylder.shuttlewidget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by kevin on 2/13/15.
 *
 * A fragment that displays a ListView containing each ScheduleConstraint in the ConstraintDatabase
 * unlike other Fragments in the FragmentPagerAdapter, this one needs a copy of the ConstraintDatabase
 * so it can delete elements.
 */
public class FragmentListView extends ListFragment {

    private ConstraintDatabase database;
    private ScheduleConstraint[] constraints;
    private boolean flagEmptyList = false;

    /**
     * this will be added to the ConstraintDatabase's OnDatabaseUpdateListener ArrayList, and will be
     * called to show the data in the database
     */
    public ConstraintDatabase.OnDatabaseUpdatedListener updateListener = new ConstraintDatabase.OnDatabaseUpdatedListener() {
        @Override
        public void onUpdate(ScheduleConstraint[] newConstraints) {
            constraints = newConstraints;
            if(newConstraints.length == 0){
                flagEmptyList = true;   // if empty, don't let the user delete things
                // create a new array with one null element, signaling the ListAdapter to show help text
                constraints = new ScheduleConstraint[]{null};
            }else{
                flagEmptyList = false;
            }
            ConstraintListAdapter adapter = new ConstraintListAdapter(getActivity(), constraints);
            setListAdapter(adapter);
        }
    };

    /**
     * This method is called when the user clicks an element in the list
     * @param position the position in the list that the user clicked. It corresponds to a ScheduleConstraint
     *                 in the ScheduleConstraint[], constraints. We use this information and the reference
     *                 to the ConstraintDatabase to delete the selected element
     */
    @Override
    public void onListItemClick(ListView view, View selectedView, int position, long id){
        if(flagEmptyList){
            return;         // don't show a dialog if the list is empty
        }
        final int pos = position;       // make final so the onClickListener can refer to it
        AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
        dialog.setTitle("Delete");
        dialog.setMessage("Delete this Constraint from the schedule?");
        dialog.setNegativeButton("No", null);
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // runs when the user clicks "yes"
                database.removeConstraint(constraints[pos]);
            }
        });
        dialog.create().show();
    }

    /**
     * A method that must be called before items can be successfully be deleted from the database
     * @param database
     */
    public void giveDatabaseCopy(ConstraintDatabase database){
        this.database = database;
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
