package com.mountain.mytracker.activity.test;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;

import com.mountain.mytracker.activity.MainActivity;

public class MainActivityUnitTest extends android.test.ActivityUnitTestCase<MainActivity> {
	
	private Integer mountain_list_button_id;
	private int main_logo_id;
	private MainActivity activity;
	
	public MainActivityUnitTest(){
		super(MainActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Intent intent = new Intent(this.getInstrumentation().getTargetContext(), MainActivity.class);
		startActivity(intent, null, null);
		activity = getActivity();
	}
	
	@SuppressWarnings("static-access")
	public void testLayout(){
		mountain_list_button_id = com.mountain.mytracker.activity.R.id.main_mountain_btn;
		main_logo_id = com.mountain.mytracker.activity.R.id.main_logo;
		Log.v("in test", mountain_list_button_id.toString());
		assertNotNull(activity);
		assertNotNull(activity.findViewById(mountain_list_button_id));
		assertNotNull(activity.findViewById(main_logo_id));
		Button view = (Button) activity.findViewById(mountain_list_button_id);
		assertEquals("Incorrect label", "Munti", view.getText());
	}
	
	public void testIntentTriggerviaOnClick(){
		mountain_list_button_id = com.mountain.mytracker.activity.R.id.main_mountain_btn;
		assertNotNull(activity);
		Button view = (Button) activity.findViewById(mountain_list_button_id);
		assertNotNull("Button not allowed to be null",view);
		view.performClick();
		Intent triggeredIntent = getStartedActivityIntent();
		assertNotNull("Intent was null", triggeredIntent);
	}
}
