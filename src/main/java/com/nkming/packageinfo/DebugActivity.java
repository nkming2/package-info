package com.nkming.packageinfo;

import java.util.HashMap;
import java.util.Map;

public class DebugActivity extends com.nkming.utils.DebugActivity
{
	@Override
	protected Map<String, Class<?>> getActivityClasses()
	{
		Map<String, Class<?>> product = new HashMap<>(1);
		product.put(getString(R.string.app_name), MainActivity.class);
		return product;
	}

	@Override
	protected boolean isDefaultShowDebugLog()
	{
		return true;
	}

	@Override
	protected boolean isDefaultShowVerboseLog()
	{
		return false;
	}
}
