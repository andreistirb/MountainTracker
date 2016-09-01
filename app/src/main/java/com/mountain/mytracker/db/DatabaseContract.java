package com.mountain.mytracker.db;

import android.provider.BaseColumns;

public class DatabaseContract {

	private DatabaseContract() {
	}

	public static abstract class DatabaseEntry implements BaseColumns {
		public static final String TABLE_TRACK = "track"; // nefolosita deocamdata

		public static final String TABLE_TRACK_POINTS = "Trackpoints";
		public static final String TABLE_MOUNTAIN_TRACK = "Tracks";
		public static final String TABLE_MOUNTAIN = "Mountains";
		public static final String TABLE_MY_TRACKS_POINTS = "Mytrackpoints";
		public static final String TABLE_MY_TRACKS = "Mytracks";

		public static final String COL_ID = "_id";

		public static final String COL_MOUNTAIN_NAME = "mountain_name";
		public static final String COL_MOUNTAIN_ID = "mountain_id";
		public static final String COL_MOUNTAIN_DESCRIPTION = "mountain_description";

		public static final String COL_TRACK_ID = "track_id";
		public static final String COL_TRACK_NAME = "track_name";
		public static final String COL_DIFF = "track_difficulty";
		public static final String COL_MRK = "track_mark";
		public static final String COL_LENGTH = "track_length";
		public static final String COL_AVLB = "track_availability";
		public static final String COL_DESCRIPTION = "track_description";

		public static final String COL_LAT = "trackpoint_lat";
		public static final String COL_LON = "trackpoint_lon";
		public static final String COL_ALT = "trackpoint_alt";
		public static final String COL_ORD = "trackpoint_order";
		
		public static final String COL_TIME = "my_track_time";  //timpul parcurs
		public static final String COL_MED_SPEED = "my_track_med_speed"; //viteza medie
		public static final String COL_MAX_SPEED = "my_track_max_speed"; //viteza maxima
		public static final String COL_DISTANCE = "my_track_distance"; //distanta parcursa		
		public static final String COL_TRACK_NO = "my_track_number"; // id-ul trackului
		public static final String COL_TRACK_MAX_ALT = "max_altitude";//altitudinea maxima
		public static final String COL_TRACK_MIN_ALT = "min_altitude";//altitudinea minima

	}
}
