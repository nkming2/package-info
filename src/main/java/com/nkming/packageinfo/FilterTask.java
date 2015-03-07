package com.nkming.packageinfo;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public abstract class FilterTask
		extends AsyncTask<FilterTask.Params, Void, List<AppInfo>>
{
	public static class Params
	{
		List<AppInfo> apps;
		AppFilter filter;
	}

	@Override
	protected List<AppInfo> doInBackground(Params... params)
	{
		List<AppInfo> apps = params[0].apps;
		AppFilter filter = params[0].filter;

		List<String> keywords = parseKeywords(filter.keyword);
		if (apps == null)
		{
			return null;
		}
		if (keywords.isEmpty() && filter.appFlags.isEmpty())
		{
			return apps;
		}

		List<AppInfo> results = new ArrayList<>();
		for (AppInfo info : apps)
		{
			if (isCancelled())
			{
				return null;
			}
			if (isMatchKeywords(info, keywords)
					&& isMatchAppFlags(info, filter.appFlags))
			{
				results.add(info);
			}
		}
		return results;
	}

	private List<String> parseKeywords(String query)
	{
		String upper = query.toUpperCase(Locale.ENGLISH);
		StringTokenizer st = new StringTokenizer(upper);
		ArrayList<String> keywords = new ArrayList<>();
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			keywords.add(token.toUpperCase(Locale.ENGLISH));
		}
		return keywords;
	}

	private boolean isMatchKeywords(AppInfo info, List<String> keywords)
	{
		return isMatchString(info.getAppLabel(), keywords)
				|| isMatchString(info.getPackageName(), keywords);
	}

	private boolean isMatchString(String str, List<String> filters)
	{
		String upperStr = str.toUpperCase(Locale.ENGLISH);
		for (String f : filters)
		{
			if (!upperStr.contains(f))
			{
				return false;
			}
		}
		return true;
	}

	private boolean isMatchAppFlags(AppInfo info,
			EnumMap<AppInfo.Flag, Boolean> appFlags)
	{
		if (appFlags.isEmpty())
		{
			return true;
		}
		for (AppInfo.Flag f : AppInfo.Flag.values())
		{
			Boolean value = appFlags.get(f);
			if (value != null && info.isFlag(f) != value)
			{
				return false;
			}
		}
		return true;
	}
}
