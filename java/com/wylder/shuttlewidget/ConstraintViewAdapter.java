package com.wylder.shuttlewidget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
public class ConstraintViewAdapter extends PagerAdapter {

    private static final int NUMBER_OF_PAGES = 2;

    private View[] views = new View[NUMBER_OF_PAGES];
    private ConstraintDatabase database;
    private ScheduleConstraint[] constraints;
    private Context context;


    // listener for the ListView that allows for deletion from the database
    public AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
            // runs when the user clicks the ListView item
            final AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
            dialog.setTitle("Delete");
            dialog.setMessage("Delete this Constraint from the schedule?");
            dialog.setNegativeButton("No", null);
            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // runs when the user clicks "yes"
                    database.removeConstraint(constraints[position]);
                    updateConstraintsFromDatabase();
                }
            });
            dialog.create().show();
        }
    };


    public ConstraintViewAdapter(Context context) {
        super();
        this.context = context;
        database = new ConstraintDatabase(context);
        // set up views
        views[0] = new WeekView(context);
        views[1] = new ListView(context);
        ((ListView) views[1]).setOnItemClickListener(onListClick);
        updateConstraintsFromDatabase();
    }

    /**
     * an Overridden method to put View in the viewGroup based off it's position
     * @param viewGroup the location to expand the page into
     * @param position the page number
     * @return an Object that represents this page, in our case it's the view itself
     */
    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position){
        viewGroup.addView(views[position]);
        return views[position];
    }

    /**
     * A method that clears a ViewGroup of all it's views after removing the page from the list
     * @param viewGroup the viewGroup to clear
     * @param position the page number being destroyed
     * @param destroy the object identifier that goes with this page
     */
    @Override
    public void destroyItem(ViewGroup viewGroup, int position, Object destroy){
        viewGroup.removeAllViews();
    }

    /**
     * tests whether or not the object representation matches the view
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return NUMBER_OF_PAGES;
    }

    /**
     * a public method intended to be called when the ViewPager is out of date with data in the Database
     * ie after addition or deletion in database
     */
    public void updateConstraintsFromDatabase(){
        constraints = database.getAllConstraints();
        ((WeekView) views[0]).displayConstraints(constraints);
        ConstraintListAdapter adapter = new ConstraintListAdapter(context, constraints);
        ((ListView) views[1]).setAdapter(adapter);
    }
}
