package com.nkming.packageinfo;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;

import com.nkming.utils.content.AsyncTaskLoaderEx;
import com.nkming.utils.graphic.BitmapCache;
import com.nkming.utils.graphic.BitmapLoader;
import com.nkming.utils.graphic.DrawableUtils;
import com.nkming.utils.io.UriUtils;
import com.nkming.utils.type.Size;
import com.nkming.utils.unit.DimensionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AppInfoLoader extends AsyncTaskLoaderEx<List<AppInfo>>
{
	public static class PackageReceiver extends BroadcastReceiver
	{
		public static void register(PackageReceiver receiver)
		{
			Context context = receiver.mParent.getContext();

			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addDataScheme("package");
			context.registerReceiver(receiver, filter);

			IntentFilter sdFilter = new IntentFilter();
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			context.registerReceiver(receiver, sdFilter);
		}

		public PackageReceiver(AppInfoLoader parent)
		{
			mParent = parent;
		}

		@Override
		public void onReceive(Context context, Intent intent)
		{
			mParent.onContentChanged();
		}

		final private AppInfoLoader mParent;
	}

	public AppInfoLoader(Context context)
	{
		super(context);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public List<AppInfo> loadInBackground()
	{
		Log.v(LOG_TAG, ".loadInBackground()");

		PackageManager pm = getContext().getPackageManager();
		List<PackageInfo> list = pm.getInstalledPackages(0);

		List<AppInfo> apps = new ArrayList<>(list.size());
		for (PackageInfo pi : list)
		{
			ApplicationInfo ai = pi.applicationInfo;
			if (ai == null)
			{
				Log.d(LOG_TAG + ".loadInBackground", String.format(
						"applicationInfo == null in %s", pi.packageName));
				continue;
			}

			AppInfo.Builder builder = new AppInfo.Builder();
			builder.setAppLabel(ai.loadLabel(pm).toString())
					.setIconId(ai.icon)
					.setEnabled(ai.enabled)
					.setPackageName(pi.packageName)
					.setVersionName(pi.versionName)
					.setLastUpdate(pi.lastUpdateTime);
			setApplicationFlags(ai, builder);
			AppInfo app = builder.getProduct();
			apps.add(app);

			cacheIcon(app);
		}
		return apps;
	}

	@Override
	protected void onStartLoading()
	{
		super.onStartLoading();
		if (mReceiver == null)
		{
			mReceiver = new PackageReceiver(this);
			PackageReceiver.register(mReceiver);
		}
	}

	@Override
	protected void onReset()
	{
		super.onReset();
		if (mReceiver != null)
		{
			getContext().unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ AppInfoLoader.class.getSimpleName();

	private void setApplicationFlags(ApplicationInfo ai, AppInfo.Builder builder)
	{
		builder.setFlag(AppInfo.Flag.kAllowBackup,
					((ai.flags & ApplicationInfo.FLAG_ALLOW_BACKUP) != 0));
		builder.setFlag(AppInfo.Flag.kExternalStorage,
				((ai.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0));
		if (AppInfo.isFlagValid(AppInfo.Flag.kGame))
		{
			builder.setFlag(AppInfo.Flag.kGame,
					((ai.flags & ApplicationInfo.FLAG_IS_GAME) != 0));
		}
		if (AppInfo.isFlagValid(AppInfo.Flag.kInstalled))
		{
			builder.setFlag(AppInfo.Flag.kInstalled,
					((ai.flags & ApplicationInfo.FLAG_INSTALLED) != 0));
		}
		builder.setFlag(AppInfo.Flag.kLargeHeap,
				((ai.flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0));
		builder.setFlag(AppInfo.Flag.kPersistent,
				((ai.flags & ApplicationInfo.FLAG_PERSISTENT) != 0));
		builder.setFlag(AppInfo.Flag.kSystem,
				((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0));
		if (AppInfo.isFlagValid(AppInfo.Flag.kPrivileged))
		{
			builder.setFlag(AppInfo.Flag.kPrivileged,
					retrieveIsPrivileged(ai.flags));
		}
		builder.setFlag(AppInfo.Flag.kDebuggable,
				((ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0));
		if (AppInfo.isFlagValid(AppInfo.Flag.kMultiarch))
		{
			builder.setFlag(AppInfo.Flag.kMultiarch,
					((ai.flags & ApplicationInfo.FLAG_MULTIARCH) != 0));
		}
	}

	private boolean retrieveIsPrivileged(int flags)
	{
		try
		{
			Field fieldFLAG_PRIVILEGED = ApplicationInfo.class.getDeclaredField(
					"FLAG_PRIVILEGED");
			fieldFLAG_PRIVILEGED.setAccessible(true);
			int FLAG_PRIVILEGED = fieldFLAG_PRIVILEGED.getInt(null);
			return ((flags & FLAG_PRIVILEGED) != 0);
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG + ".retrieveIsPrivileged", "Error while reflection", e);
			return ((flags & ApplicationInfo.FLAG_SYSTEM) != 0);
		}
	}

	private void cacheIcon(AppInfo app)
	{
		if (true)
		{
			return;
		}

		if (app.getIconId() == 0)
		{
			// No icon
			return;
		}

		BitmapLoader loader = new BitmapLoader(getContext());
		loader.setTargetSize(new Size(96, 96));
		Uri uri = UriUtils.getResourceUri(app.getPackageName(), app.getIconId());
		Bitmap bmp = loader.loadUri(uri);
		if (bmp == null)
		{
			Log.w(LOG_TAG + ".cacheIcon", "Failed while loadUri");
		}
		else
		{
			BitmapCache.putBitmap(uri.toString(), bmp);
		}
	}

	private PackageReceiver mReceiver;
}
