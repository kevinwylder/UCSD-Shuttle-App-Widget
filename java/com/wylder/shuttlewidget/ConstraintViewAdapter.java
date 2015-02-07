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
 */
public class ConstraintViewAdapter extends PagerAdapter {

    private static final int NUMBER_OF_PAGES = 2;

    private View[] views = new View[NUMBER_OF_PAGES];
    private ConstraintDatabase database;
    private ScheduleConstraint[] constraints;
    private Context context;


    public AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
            dialog.setTitle("Delete");
            dialog.setMessage("Delete this Constraint from the schedule?");
            dialog.setNegativeButton("No", null);
            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
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
        views[0] = new WeekView(context);
        views[1] = new ListView(context);
        ((ListView) views[1]).setOnItemClickListener(onListClick);
        updateConstraintsFromDatabase();
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position){
        viewGroup.addView(views[position]);
        return views[position];
    }

    @Override
    public void destroyItem(ViewGroup viewGroup, int position, Object destroy){
        viewGroup.removeAllViews();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return NUMBER_OF_PAGES;
    }

    public void updateConstraintsFromDatabase(){
        constraints = database.getAllConstraints();
        ((WeekView) views[0]).displayConstraints(constraints);
        ConstraintListAdapter adapter = new ConstraintListAdapter(context, constraints);
        ((ListView) views[1]).setAdapter(adapter);
    }
}
