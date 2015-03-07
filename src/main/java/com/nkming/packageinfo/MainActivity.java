package com.nkming.packageinfo;

import android.app.ActivityManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.nkming.utils.graphic.BitmapCache;

public class MainActivity extends ActionBarActivity
		implements SearchDialog.SearchDialogListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		int heapMb = ((ActivityManager)getSystemService(ACTIVITY_SERVICE))
				.getMemoryClass();
		BitmapCache.init(heapMb * 1024 * 1024 / 4);

		setContentView(R.layout.activity_main);
		if (savedInstanceState == null)
		{
			mFrag = new MainFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, mFrag)
					.commit();
		}
		getSupportActionBar().setElevation(getResources().getDimension(
				R.dimen.toolbar_z));
	}

	@Override
	public void onSearchRequest(AppFilter req)
	{
		mFrag.onSearchRequest(req);
	}

	private MainFragment mFrag;
}
