package com.wylder.shuttlewidget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

/**
 * Created by kevin on 2/6/15.
 *
 * An adapter for the ViewPager in StopSchedulerService
 * This adapter will specifically display the WeekView and a ListView (using ConstraintListAdapter).
 * It holds it's own database, so the only method necessary is updateConstraintsFromDatabase() which
 * updates the data shown in the WeekView and ListView
 * Extending PagerAdapter is for simplicity, there is no need to extend FragmentPagerAdapter because
 * each page is only a view.
 */
public class MainViewAdapter extends FragmentPagerAdapter {

    private static final int FIND_BUS_POSITION = 0;
    private static final int WEEKVIEW_POSITION = 1;
    private static final int LISTVIEW_POSITION = 2;
    private static final int NUMBER_OF_PAGES = 3;

    private ConstraintDatabase database;

    public MainViewAdapter(FragmentManager manager, ConstraintDatabase database) {
        super(manager);
        this.database = database;
    }

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
