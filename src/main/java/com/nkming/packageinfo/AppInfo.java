package com.nkming.packageinfo;

import android.os.Build;

import java.util.Comparator;
import java.util.EnumMap;

public class AppInfo
{
	public static enum Flag
	{
		kAllowBackup,
		kExternalStorage,
		kGame,
		kInstalled,
		kLargeHeap,
		kPersistent,
		kSystem,
		kPrivileged,
		kDebuggable,
		kMultiarch;

		@Override
		public String toString()
		{
			switch (Flag.values()[ordinal()])
			{
			case kAllowBackup:
				return "Allow backup";

			case kExternalStorage:
				return "External storage (SD card)";

			case kGame:
				return "Game";

			case kInstalled:
				return "Installed for this user";

			case kLargeHeap:
				return "Large heap";

			case kPersistent:
				return "Persistent";

			case kSystem:
				return "System";

			case kPrivileged:
				return "Privileged";

			case kDebuggable:
				return "Debuggable";

			case kMultiarch:
				return "Require Multiarch";

			default:
				throw new RuntimeException();
			}
		}
	}

	public static class Builder
	{
		public Builder()
		{
			mProduct = new AppInfo();
		}

		public Builder setAppLabel(String appLabel)
		{
			mProduct.mAppLabel = appLabel;
			return this;
		}

		public Builder setIconId(int iconId)
		{
			mProduct.mIconId = iconId;
			return this;
		}

		public Builder setEnabled(boolean flag)
		{
			mProduct.mIsEnabled = flag;
			return this;
		}

		public Builder setFlag(Flag key, boolean flag)
		{
			mProduct.mFlags.put(key, flag);
			return this;
		}

		public Builder setPackageName(String val)
		{
			mProduct.mPackageName = val;
			return this;
		}

		public Builder setVersionName(String val)
		{
			mProduct.mVersionName = val;
			return this;
		}

		public Builder setLastUpdate(long val)
		{
			mProduct.mLastUpdate = val;
			return this;
		}

		public AppInfo getProduct()
		{
			return mProduct;
		}

		private AppInfo mProduct;
	}

	public String getAppLabel()
	{
		return mAppLabel;
	}

	public int getIconId()
	{
		return mIconId;
	}

	public boolean isEnabled()
	{
		return mIsEnabled;
	}

	public static boolean isFlagValid(Flag key)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			if (key == AppInfo.Flag.kInstalled)
			{
				return false;
			}
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
		{
			if (key == AppInfo.Flag.kPrivileged)
			{
				return false;
			}
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
		{
			if (key == AppInfo.Flag.kGame || key == AppInfo.Flag.kMultiarch)
			{
				return false;
			}
		}
		return true;
	}

	public boolean isFlag(Flag key)
	{
		return mFlags.get(key);
	}

	public EnumMap<Flag, Boolean> getFlags()
	{
		return mFlags;
	}

	public String getPackageName()
	{
		return mPackageName;
	}

	public String getVersionName()
	{
		return mVersionName;
	}

	public long getLastUpdate()
	{
		return mLastUpdate;
	}

	public static Comparator<AppInfo> getAppLabelComparator()
	{
		return new Comparator<AppInfo>()
		{
			@Override
			public int compare(AppInfo lhs, AppInfo rhs)
			{
				return lhs.getAppLabel().compareToIgnoreCase(
						rhs.getAppLabel());
			}
		};
	}

	public static Comparator<AppInfo> getAppLabelDescComparator()
	{
		return new Comparator<AppInfo>()
		{
			@Override
			public int compare(AppInfo lhs, AppInfo rhs)
			{
				return rhs.getAppLabel().compareToIgnoreCase(
						lhs.getAppLabel());
			}
		};
	}

	public static Comparator<AppInfo> getPackageComparator()
	{
		return new Comparator<AppInfo>()
		{
			@Override
			public int compare(AppInfo lhs, AppInfo rhs)
			{
				return lhs.getPackageName().compareToIgnoreCase(
						rhs.getPackageName());
			}
		};
	}

	public static Comparator<AppInfo> getPackageDescComparator()
	{
		return new Comparator<AppInfo>()
		{
			@Override
			public int compare(AppInfo lhs, AppInfo rhs)
			{
				return rhs.getPackageName().compareToIgnoreCase(
						lhs.getPackageName());
			}
		};
	}

	private AppInfo()
	{}

	private String mAppLabel;
	private int mIconId = 0;
	private boolean mIsEnabled = true;
	private EnumMap<Flag, Boolean> mFlags = new EnumMap<>(Flag.class);
	private String mPackageName;
	private String mVersionName;
	private long mLastUpdate;
}
