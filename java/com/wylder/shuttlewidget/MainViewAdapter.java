package com.wylder.shuttlewidget;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

/**
 * Created by kevin on 2/6/15.
 *
 * An adapter for the ViewPager in StopSchedulerService
 * This adapter will show 3 views, a Lookup view, week View, and list view, each with their own
 * fragment. A copy of the constraint database is necessary to register listeners to update the UI
 */
public class MainViewAdapter extends FragmentPagerAdapter {

    private static final int FIND_BUS_POSITION = 0;
    private static final int WEEKVIEW_POSITION = 1;
    private static final int LISTVIEW_POSITION = 2;
    private static final int NUMBER_OF_PAGES = 3;

    // a reference to StopSchedulerActivity's database
    private ConstraintDatabase database;

    /**
     * Constructor of the adapter, with a pointer to StopSchedulerActivity's ConstraintDatabase
     * @param manager   fragment manager of the FragmentActivity
     * @param database  a reference to the database
     */
    public MainViewAdapter(FragmentManager manager, ConstraintDatabase database) {
        super(manager);
        this.database = database;
    }

    /**
     * this method is where each fragment is instantiated.
     * Many fragments need listeners to be added to the ConstraintDatabase so that it has a list
     * of constraints; they need to be setup here.
     * @param position
     * @return
     */
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case FIND_BUS_POSITION:
                return new FragmentShuttleLookup();
            case WEEKVIEW_POSITION:
                FragmentWeekView weekView = new FragmentWeekView();
                database.addOnDatabaseUpdatedListener(weekView.updateListener);
                return weekView;
            case LISTVIEW_POSITION:
                FragmentListView listView = new FragmentListView();
                database.addOnDatabaseUpdatedListener(listView.updateListener);
                listView.giveDatabaseCopy(database);    // necessary to let it delete elements
                return listView;
            default:
                return null;
        }
    }

    /**
     * This method is run when the activity is resumed. Update the UI
     */
    @Override
    public void restoreState(Parcelable parcelable, ClassLoader loader){
        super.restoreState(parcelable, loader);
        database.runUpdateListeners();
    }

    /**
     * This method is run when the ViewPager is created. Update the UI
     */
    @Override
    public void finishUpdate(ViewGroup container){
        super.finishUpdate(container);
        database.runUpdateListeners();
    }

    @Override
    public int getCount() {
        return NUMBER_OF_PAGES;
    }


}
