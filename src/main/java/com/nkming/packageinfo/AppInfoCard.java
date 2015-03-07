package com.nkming.packageinfo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nkming.utils.graphic.BitmapCache;
import com.nkming.utils.graphic.BitmapLoader;
import com.nkming.utils.io.UriUtils;
import com.nkming.utils.type.Size;

import java.util.Map;

public class AppInfoCard extends CardView
{
	public AppInfoCard(Context context)
	{
		super(context);
	}

	public AppInfoCard(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public AppInfoCard(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mIcon = (ImageView)findViewById(R.id.app_icon);
		mAppName = (TextView)findViewById(R.id.app_name);
		mAppPackage = (TextView)findViewById(R.id.app_package);
		mVersion = (TextView)findViewById(R.id.app_version);
		mLastUpdate = (TextView)findViewById(R.id.app_last_update);
		mAppDetailContainer = (LinearLayout)findViewById(
				R.id.app_detail_container);
		mAppFlagsContainer = (LinearLayout)findViewById(
				R.id.app_flags_container);

		mSettings = mAppDetailContainer.findViewById(R.id.app_footer_settings);
		mPlayStore = mAppDetailContainer.findViewById(R.id.app_footer_play_store);
		mLaunch = mAppDetailContainer.findViewById(R.id.app_footer_launch);
	}

	public void inflate(AppInfo app, boolean isExpand)
	{
		mApp = app;

		loadIcon();
		if (app.isEnabled())
		{
			setCardBackgroundColor(getResources().getColor(
					R.color.card_bg));
		}
		else
		{
			setCardBackgroundColor(getResources().getColor(
					R.color.card_bg_disable));
		}
		mAppName.setText(app.getAppLabel());
		mAppPackage.setText(app.getPackageName());
		mVersion.setText(app.getVersionName());
		int dateFlag = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE;
		mLastUpdate.setText(DateUtils.formatDateTime(getContext(),
				app.getLastUpdate(), dateFlag));

		inflateAppFlags();
		inflateFooter();

		if (isExpand)
		{
			expand(false);
		}
		else
		{
			collapse(false);
		}
	}

	public void expand(boolean is_animate)
	{
		mAppDetailContainer.measure(
				MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		final int height = mAppDetailContainer.getMeasuredHeight();

		if (is_animate)
		{
			animateDetailHeight(0, height);
		}
		else
		{
			mAppDetailContainer.getLayoutParams().height = height;
			mAppDetailContainer.requestLayout();
		}
		mIsExpand = true;
	}

	public void collapse(boolean is_animate)
	{
		if (is_animate)
		{
			animateDetailHeight(mAppDetailContainer.getMeasuredHeight(), 0);
		}
		else
		{
			mAppDetailContainer.getLayoutParams().height = 0;
			mAppDetailContainer.requestLayout();
		}
		mIsExpand = false;
	}

	public boolean isExpand()
	{
		return mIsExpand;
	}

	public boolean isAnimating()
	{
		return mIsAnimating;
	}

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ AppInfoCard.class.getSimpleName();

	private void initAppFlags(AppInfo app)
	{
		LayoutInflater inflater = LayoutInflater.from(getContext());
		mAppFlags = new CheckBox[app.getFlags().size()];
		int i = 0;
		for (Map.Entry<AppInfo.Flag, Boolean> e : app.getFlags().entrySet())
		{
			mAppFlags[i] = (CheckBox)inflater.inflate(R.layout.app_card_checkbox,
					mAppFlagsContainer, false);
			mAppFlags[i].setText(e.getKey().toString());
			mAppFlagsContainer.addView(mAppFlags[i]);
			++i;
		}
	}

	private void inflateAppFlags()
	{
		if (mAppFlagsContainer.getChildCount() == 0)
		{
			initAppFlags(mApp);
		}

		int i = 0;
		for (Map.Entry<AppInfo.Flag, Boolean> e : mApp.getFlags().entrySet())
		{
			mAppFlags[i++].setChecked(e.getValue());
		}
	}

	private void inflateFooter()
	{
		View.OnLongClickListener showTootip = new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				Toast.makeText(getContext(), v.getContentDescription(),
						Toast.LENGTH_SHORT).show();
				return true;
			}
		};

		mSettings.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onSettingsClick();
			}
		});
		mSettings.setOnLongClickListener(showTootip);

		mPlayStore.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onPlayStoreClick();
			}
		});
		mPlayStore.setOnLongClickListener(showTootip);

		mLaunch.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onLaunchClick();
			}
		});
		mLaunch.setOnLongClickListener(showTootip);

		PackageManager pm = getContext().getPackageManager();
		if (pm.getLaunchIntentForPackage(mApp.getPackageName()) == null)
		{
			mLaunch.setEnabled(false);
			mLaunch.setAlpha(0.481f);
		}
		else
		{
			mLaunch.setEnabled(true);
			mLaunch.setAlpha(1.0f);
		}
	}

	private void onSettingsClick()
	{
		Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		i.setData(Uri.parse("package:" + mApp.getPackageName()));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		getContext().startActivity(i);
	}

	private void onPlayStoreClick()
	{
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("market://details?id=" + mApp.getPackageName()));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		getContext().startActivity(i);
	}

	private void onLaunchClick()
	{
		PackageManager pm = getContext().getPackageManager();
		Intent i = pm.getLaunchIntentForPackage(mApp.getPackageName());
		if (i == null)
		{
			Toast.makeText(getContext(), "Cannot launch this app",
					Toast.LENGTH_LONG).show();
			return;
		}

		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		getContext().startActivity(i);
	}

	private void loadIcon()
	{
		int iconId = (mApp.getIconId() == 0)
				? android.R.drawable.sym_def_app_icon : mApp.getIconId();
		Uri uri = UriUtils.getResourceUri(mApp.getPackageName(), iconId);
		// Check if it's in cache
		Bitmap bmp = BitmapCache.getBitmap(uri.toString());
		if (bmp == null)
		{
			BitmapLoader loader = new BitmapLoader(getContext());
			loader.setTargetSize(new Size(96, 96));
			bmp = loader.loadUri(uri);
			if (bmp == null)
			{
				Log.w(LOG_TAG + ".loadIcon", "Failed while loadUri");
				mIcon.setImageDrawable(null);
				return;
			}
			else
			{
				BitmapCache.putBitmap(uri.toString(), bmp);
			}
		}
		mIcon.setImageBitmap(bmp);
	}

	private void animateDetailHeight(int from, int to)
	{
		ValueAnimator anim = ValueAnimator.ofInt(from, to);
		anim.setDuration(200);
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				Integer val = (Integer)animation.getAnimatedValue();
				mAppDetailContainer.getLayoutParams().height = val;
				mAppDetailContainer.requestLayout();
			}
		});
		anim.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				mIsAnimating = false;
			}
		});
		mIsAnimating = true;
		anim.start();
	}

	private AppInfo mApp;

	private ImageView mIcon;
	private TextView mAppName;
	private TextView mAppPackage;
	private TextView mVersion;
	private TextView mLastUpdate;
	private LinearLayout mAppDetailContainer;
	private LinearLayout mAppFlagsContainer;

	private CheckBox mAppFlags[];

	private View mSettings;
	private View mPlayStore;
	private View mLaunch;

	private boolean mIsExpand = false;
	private boolean mIsAnimating = false;
}
