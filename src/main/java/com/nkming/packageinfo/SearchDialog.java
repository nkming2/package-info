package com.nkming.packageinfo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.EnumMap;
import java.util.Map;

public class SearchDialog extends DialogFragment
{
	public interface SearchDialogListener
	{
		public void onSearchRequest(AppFilter req);
	}

	public static SearchDialog newInstance(AppFilter filter)
	{
		SearchDialog product = new SearchDialog();
		if (filter != null)
		{
			Bundle args = new Bundle();
			args.putString(ARGS_KEYWORD, filter.keyword);
			args.putSerializable(ARGS_APP_FLAGS, filter.appFlags);
			product.setArguments(args);
		}
		return product;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		mLayoutInflater = LayoutInflater.from(getActivity());

		View v = mLayoutInflater.inflate(R.layout.fragment_search, null);
		initView(v);

		MaterialDialog.Builder builder = new MaterialDialog.Builder(
				getActivity());
		builder.title("Search App")
				.positiveText("Search")
				.negativeText("Discard")
				.theme(Theme.LIGHT)
				.customView(v, true)
				.callback(new MaterialDialog.ButtonCallback()
						{
							@Override
							public void onPositive(MaterialDialog materialDialog)
							{
								onSearchClick();
							}
						});
/*
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Search App")
				.setPositiveButton("Search", null)
				.setNegativeButton("Discard", null)
				.setView(v);
*/
		return builder.build();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		if (!(getActivity() instanceof SearchDialogListener))
		{
			Log.wtf(LOG_TAG, "Activity must implement SearchDialogListener");
		}
		mListener = (SearchDialogListener)getActivity();
	}

	public void initView(View v)
	{
		mKeyword = (TextView)v.findViewById(R.id.keyword);
		mAddFlag = (Button)v.findViewById(R.id.add_flag);
		mAddFlag.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onAddFlagClick(v);
			}
		});

		mFlagContainer = (LinearLayout)v.findViewById(R.id.filter_flags);
		//mFlagContainer.setHasFixedSize(true);
		//mFlagContainer.setLayoutManager(new LinearLayoutManager(getActivity()));

		//mFlagAdapter = new MyAdapter();
		//mFlagContainer.setAdapter(mFlagAdapter);

		if (getArguments() != null)
		{
			Bundle args = getArguments();
			mKeyword.setText(args.getString(ARGS_KEYWORD));
			EnumMap<AppInfo.Flag, Boolean> appFlags =
					(EnumMap<AppInfo.Flag, Boolean>)args.getSerializable(
							ARGS_APP_FLAGS);
			for (Map.Entry<AppInfo.Flag, Boolean> f : appFlags.entrySet())
			{
				addFlag(f.getKey(), f.getValue());
			}
		}
	}

/*
	private static class MyAdapter
			extends RecyclerView.Adapter<MyAdapter.ViewHolder>
	{
		public static class ViewHolder extends RecyclerView.ViewHolder
		{
			public ViewHolder(View v)
			{
				super(v);
				remove = (ImageView)v.findViewById(R.id.remove);
				label = (TextView)v.findViewById(R.id.label);
				ui_switch = (Switch)v.findViewById(R.id.ui_switch);
			}

			public ImageView remove;
			public TextView label;
			public Switch ui_switch;
		}

		public void addData(AppInfo.Flag flag)
		{
			mFlags.add(new Pair<>(flag, true));
			notifyItemInserted(mFlags.size() - 1);
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			View v = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.app_flag_row, parent, false);
			return new ViewHolder(v);
		}

		@Override
		public void onBindViewHolder(ViewHolder vh, int position)
		{
			Pair<AppInfo.Flag, Boolean> item = mFlags.get(position);
			vh.label.setText(item.first.toString());
		}

		@Override
		public int getItemCount()
		{
			return mFlags.size();
		}

		private List<Pair<AppInfo.Flag, Boolean>> mFlags = new ArrayList<>();
	}
*/

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ SearchDialog.class.getSimpleName();

	private static final AppInfo.Flag[] FLAGS = {AppInfo.Flag.kAllowBackup,
			AppInfo.Flag.kExternalStorage,
			AppInfo.Flag.kGame,
			AppInfo.Flag.kInstalled,
			AppInfo.Flag.kLargeHeap,
			AppInfo.Flag.kPersistent,
			AppInfo.Flag.kSystem,
			AppInfo.Flag.kPrivileged,
			AppInfo.Flag.kDebuggable,
			AppInfo.Flag.kMultiarch};

	private static final int[] IDS = {R.id.menu_allow_backup,
			R.id.menu_external,
			R.id.menu_game,
			R.id.menu_user,
			R.id.menu_large_heap,
			R.id.menu_persistent,
			R.id.menu_system,
			R.id.menu_privileged,
			R.id.menu_debuggable,
			R.id.menu_multiarch};

	private static final String ARGS_KEYWORD = "keyword";
	private static final String ARGS_APP_FLAGS = "appFlags";

	private void onAddFlagClick(View v)
	{
		PopupMenu popup = new PopupMenu(getActivity(), v);
		for (int i = 0; i < FLAGS.length; ++i)
		{
			if (!AppInfo.isFlagValid(FLAGS[i]))
			{
				continue;
			}

			if (!mActiveFlags.containsKey(FLAGS[i]))
			{
				popup.getMenu().add(Menu.NONE, IDS[i], Menu.NONE,
						FLAGS[i].toString());
			}
		}

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem menuItem)
			{
				return onAddAppFlagMenuClick(menuItem);
			}
		});
		popup.show();
	}

	private boolean onAddAppFlagMenuClick(MenuItem menuItem)
	{
		for (int i = 0; i < IDS.length; ++i)
		{
			if (IDS[i] == menuItem.getItemId())
			{
				addFlag(FLAGS[i], false);
				return true;
			}
		}
		return false;
	}

	private void addFlag(AppInfo.Flag flag, boolean state)
	{
		mActiveFlags.put(flag, state);
		//mFlagAdapter.addData(FLAGS[i]);
		addFlagView(flag, state);

		if (mActiveFlags.size() == IDS.length)
		{
			mAddFlag.animate().alpha(0).setDuration(500).setListener(
					new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationEnd(Animator animation)
						{
							mAddFlag.setVisibility(View.GONE);
						}
					});
		}
	}

	private void addFlagView(final AppInfo.Flag flag, boolean state)
	{
		final View root = mLayoutInflater.inflate(R.layout.app_flag_row,
				mFlagContainer, false);

		View remove = root.findViewById(R.id.remove);
		remove.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onRemoveFlagClick(root, flag);
			}
		});

		CheckBox checkbox = (CheckBox)root.findViewById(R.id.ui_switch);
		checkbox.setChecked(state);
		checkbox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked)
			{
				mActiveFlags.put(flag, isChecked);
			}
		});

		TextView label = (TextView)root.findViewById(R.id.label);
		label.setText(flag.toString());

		mFlagContainer.addView(root);
	}

	private void onRemoveFlagClick(View root, AppInfo.Flag flag)
	{
		mFlagContainer.removeView(root);
		mActiveFlags.remove(flag);
		if (mAddFlag.getVisibility() != View.VISIBLE)
		{
			mAddFlag.animate().alpha(1).setDuration(500).setListener(
					new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationStart(Animator animation)
						{
							mAddFlag.setVisibility(View.VISIBLE);
						}
					});
		}
	}

	private void onSearchClick()
	{
		AppFilter filter = new AppFilter();
		filter.keyword = mKeyword.getText().toString();
		filter.appFlags = mActiveFlags;
		mListener.onSearchRequest(filter);
	}

	private TextView mKeyword;

	private EnumMap<AppInfo.Flag, Boolean> mActiveFlags = new EnumMap<>(
			AppInfo.Flag.class);
	private Button mAddFlag;
	private LinearLayout mFlagContainer;
	//private MyAdapter mFlagAdapter;

	private SearchDialogListener mListener;

	private LayoutInflater mLayoutInflater;
}
