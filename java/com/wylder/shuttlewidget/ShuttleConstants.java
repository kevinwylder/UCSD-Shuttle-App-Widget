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

    public static final String[] routeNames = new String[]{
            "Counter Campus Loop",
            "Clockwise Campus Loop"
    };

    public static final int[] widgetColors = new int[]{
            Color.RED,
            Color.BLUE,

            Color.WHITE     // included as a default color
    };

    public static final int[] textColors = new int[]{
            Color.WHITE,
            Color.WHITE,


            Color.BLACK     // included as a default color
    };

    public static final String[][] stopNames = {
            {
                    "Torrey Pines",
                    "ERC",
                    "Pangea Parking",
                    "Peterson Hall",
                    "Muir Apartments",
                    "Pacific Hall",
                    "Revelle Parking",
                    "Che Café",
                    "Gilman & Osler",
                    "Mandelville",
                    "Gilman & Myers",
                    "Pepper Canyon",
                    "Canyonview Pool",
                    "Warren Aptmnts",
                    "Hopkins Parking",
                    "North Point"
            },
            {
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
            }
    };

    public static final int[] onlineRouteIds = {
            1113, 1114
    };

    public static final int[][] onlineStopIds = {
            {
                    93814,  493852, 141062,
                    141080, 240096, 382806,
                    93943,  493872, 9158,
                    757589, 34893,  382807,
                    32417,  141014, 141030,
                    141042
            },
            {
                    39814,  382908, 757600,
                    382909, 377671, 382910,
                    34674,  239930, 382911,
                    141138, 239948, 93904,
                    141111, 382912, 382913,
                    141056
            }
    };

}
