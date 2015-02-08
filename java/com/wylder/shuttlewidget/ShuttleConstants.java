package com.wylder.shuttlewidget;

import android.graphics.Color;

/**
 * Created by kevin on 2/5/15.
 *
 * A class containing static information needed throughout the whole application
 */
public class ShuttleConstants {

    // shuttles are Monday - Saturday, 7am - 11pm
    public static final int DAYS_OF_THE_WEEK = 6;
    public static final int HOUR_START = 7;
    public static final int HOUR_END = 23;

    public static final String[] routes = new String[]{
            "Counter Campus Loop",
            "Clockwise Campus Loop"
    };

    public static final int[] widgetColors = new int[]{
            Color.RED,
            Color.BLUE
    };

    public static final String[] stops = new String[]{
            "Torrey Pines",
            "North Point",
            "Hopkins Parking",
            "Warren Aptmnts",
            "Canyonview Pool",
            "Pepper Canyon",
            "Gilman & Myers",
            "Mandelville",
            "Gilman & Osler",
            "Che Café",
            "Revelle Parking",
            "Pacific Hall",
            "Muir Apartments",
            "Peterson Hall",
            "Pangea Parking",
            "ERC"
    };

}
