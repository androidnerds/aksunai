package org.androidnerds.app.aksunai;

import org.androidnerds.app.aksunai.Aksunai;

import android.test.ActivityInstrumentationTestCase2;

public class AksunaiTest extends ActivityInstrumentationTestCase2<Aksunai> {

	public AksunaiTest() {
		super("org.androidnerds.app.aksunai", Aksunai.class);
	}
	
	public void testOpenMenu() {
		Aksunai a = getActivity();
		a.openOptionsMenu();
		a.closeOptionsMenu();
	}
}
