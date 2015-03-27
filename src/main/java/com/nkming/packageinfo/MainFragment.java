package com.nkming.packageinfo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.nkming.utils.sys.DeviceInfo;
import com.shamanland.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class MainFragment extends Fragment
		implements LoaderManager.LoaderCallbacks<List<AppInfo>>,
				SearchDialog.SearchDialogListener
{
	public static MainFragment newInstance()
	{
		return new MainFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		if (mRootView == null)
		{
			mRootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			mProgress = (ProgressBar)mRootView.findViewById(R.id.progress);
			mGrid = (RecyclerView)mRootView.findViewById(R.id.app_grid);
			mSearch = (FloatingActionButton)mRootView.findViewById(R.id.fab);
		}
		else
		{
			ViewGroup parent = (ViewGroup)mRootView.getParent();
			if (parent != null)
			{
				parent.removeView(mRootView);
			}
		}
		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		if (!mIsInitialized)
		{
			init();
			mIsInitialized = true;
		}
		else
		{
			reinit();
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (mFilterTask != null)
		{
			mFilterTask.cancel(true);
		}
	}

	@Override
	public Loader<List<AppInfo>> onCreateLoader(int id, Bundle args)
	{
		return new AppInfoLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<AppInfo>> loader, List<AppInfo> data)
	{
		Log.v(LOG_TAG, "onLoadFinished(...)");
		mApps = data;
		mGridAdapter.setData(data);
		showGrid();
		mProgress.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<AppInfo>> loader)
	{
		Log.v(LOG_TAG, "onLoaderReset(...)");
		mApps.clear();
		mGridAdapter.clearData();
		if (isResumed())
		{
			hideGrid();
			mProgress.setVisibility(View.VISIBLE);
		}
	}

	public void onSearchRequest(AppFilter req)
	{
		mActiveFilter = req;
		filterApps(req);
	}

	private static class ViewHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener
	{
		public interface OnClickListener
		{
			public void onItemClick(ViewHolder vh, int position);
		}

		public ViewHolder(AppInfoCard v, OnClickListener listener)
		{
			super(v);
			card = v;
			this.listener = listener;
			v.setOnClickListener(this);
		}

		@Override
		public void onClick(View v)
		{
			if (listener != null)
			{
				listener.onItemClick(this, getPosition());
			}
		}

		public AppInfoCard card;

		public OnClickListener listener;
	}

	private class MyAdapter extends RecyclerView.Adapter<ViewHolder>
	{
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			AppInfoCard v = (AppInfoCard)LayoutInflater.from(parent.getContext())
					.inflate(R.layout.app_card, parent, false);
			return new ViewHolder(v, new ViewHolder.OnClickListener()
			{
				@Override
				public void onItemClick(ViewHolder vh, int position)
				{
					onAppClick(vh, position);
				}
			});
		}

		@Override
		public void onBindViewHolder(ViewHolder vh, int position)
		{
			moveIterator(position);
			AppInfo app = mDisplayAppsIt.next();
			vh.card.inflate(app, mIsExpandIt.next());
		}

		@Override
		public int getItemCount()
		{
			return mDisplayApps.size();
		}

		public void setData(List<AppInfo> data)
		{
			mDisplayApps.addAll(data);
			Collections.sort(mDisplayApps, mSorter);
			mDisplayAppsIt = mDisplayApps.listIterator();

			for (int i = 0; i < mDisplayApps.size(); ++i)
			{
				mIsExpand.add(false);
			}
			mIsExpandIt = mIsExpand.listIterator();

			notifyDataSetChanged();
		}

		public void setFilterResult(List<AppInfo> filterResult)
		{
			Collections.sort(filterResult, mSorter);
			mDisplayAppsIt = mDisplayApps.listIterator();
			mIsExpandIt = mIsExpand.listIterator();
			for (int i = 0; i < filterResult.size() && mDisplayAppsIt.hasNext();)
			{
				AppInfo app = mDisplayAppsIt.next();
				mIsExpandIt.next();
				if (app != filterResult.get(i))
				{
					if (mSorter.compare(app, filterResult.get(i)) > 0)
					{
						mDisplayAppsIt.previous();
						mIsExpandIt.previous();
						mDisplayAppsIt.add(filterResult.get(i));
						mIsExpandIt.add(false);
						++i;
						notifyItemInserted(mDisplayAppsIt.previousIndex());
					}
					else
					{
						mDisplayAppsIt.remove();
						mIsExpandIt.remove();
						notifyItemRemoved(mDisplayAppsIt.nextIndex());
					}
				}
				else
				{
					++i;
				}
			}

			// Insert remaining
			if (filterResult.size() > mDisplayApps.size())
			{
				int from = mDisplayApps.size();
				int to = filterResult.size();
				for (int i = from; i < to; ++i)
				{
					mIsExpand.add(false);
				}
				mDisplayApps.addAll(filterResult.subList(from, to));
				notifyItemRangeInserted(from, to - from);
			}
			// Remove remaining
			else if (mDisplayApps.size() > filterResult.size())
			{
				int from = filterResult.size();
				int to = mDisplayApps.size();
				for (int i = from; i < to; ++i)
				{
					mIsExpandIt.next();
					mIsExpandIt.remove();
					mDisplayAppsIt.next();
					mDisplayAppsIt.remove();
				}
				notifyItemRangeRemoved(from, to - from);
			}

			// Reset iterators
			mDisplayAppsIt = mDisplayApps.listIterator();
			mIsExpandIt = mIsExpand.listIterator();
		}

		public void clearData()
		{
			int size = mDisplayApps.size();
			mDisplayApps.clear();
			mDisplayAppsIt = mDisplayApps.listIterator();
			mIsExpand.clear();
			mIsExpandIt = mIsExpand.listIterator();
			notifyItemRangeRemoved(0, size);
		}

		private void onAppClick(ViewHolder vh, int position)
		{
			// Wait until the current animation ends
			if (vh.card.isAnimating())
			{
				return;
			}

			moveIterator(position);
			AppInfo app = mDisplayAppsIt.next();
			mIsExpandIt.next();
			if (vh.card.isExpand())
			{
				vh.card.collapse(true);
				mIsExpandIt.set(false);
			}
			else
			{
				vh.card.expand(true);
				mIsExpandIt.set(true);
			}
		}

		private void moveIterator(int position)
		{
			int diff = position - mDisplayAppsIt.nextIndex();
			for (int i = 0; i < Math.abs(diff); ++i)
			{
				if (diff > 0)
				{
					mDisplayAppsIt.next();
					mIsExpandIt.next();
				}
				else
				{
					mDisplayAppsIt.previous();
					mIsExpandIt.previous();
				}
			}
		}

		private List<AppInfo> mDisplayApps = new LinkedList<>();
		private ListIterator<AppInfo> mDisplayAppsIt;
		private List<Boolean> mIsExpand = new LinkedList<>();
		private ListIterator<Boolean> mIsExpandIt;
		private Comparator<AppInfo> mSorter = AppInfo.getAppLabelComparator();
	}

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ MainFragment.class.getSimpleName();

	private void init()
	{
		//mGrid.setHasFixedSize(true);
		mGrid.setVisibility(View.INVISIBLE);
		mProgress.setVisibility(View.VISIBLE);

		mGridAdapter = new MyAdapter();
		mGrid.setAdapter(mGridAdapter);
		mGrid.offsetChildrenHorizontal(getResources().getDimensionPixelOffset(
				R.dimen.dp8));
		mGrid.offsetChildrenVertical(getResources().getDimensionPixelOffset(
				R.dimen.dp8));

		getLoaderManager().initLoader(0, null, this);

		mSearch.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						onSearchClick();
					}
				});

		reinit();
	}

	private void reinit()
	{
		mGridManager = new GridLayoutManager(getActivity(),
				DeviceInfo.GetScreenDp(getActivity()).w() / 280);
		mGrid.setLayoutManager(mGridManager);
	}

	private void onSearchClick()
	{
		SearchDialog d = SearchDialog.newInstance(mActiveFilter);
		d.show(getFragmentManager(), "search_dialog");
	}

	private void showGrid()
	{
		mGrid.setVisibility(View.VISIBLE);
		mGrid.setAlpha(0.0f);
		mGrid.animate().alpha(1.0f).setDuration(500);
	}

	private void hideGrid()
	{
		mGrid.setAlpha(1.0f);
		mGrid.animate().alpha(0.0f).setDuration(500)
				.setListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationEnd(Animator animation)
					{
						mGrid.setVisibility(View.INVISIBLE);
					}
				});
	}

	private void filterApps(AppFilter filter)
	{
		if (mFilterTask != null)
		{
			mFilterTask.cancel(true);
		}
		mFilterTask = new FilterTask()
		{
			@Override
			protected void onPostExecute(List<AppInfo> appInfos)
			{
				mGridAdapter.setFilterResult(appInfos);
			}
		};

		FilterTask.Params params = new FilterTask.Params();
		params.apps = mApps;
		params.filter = filter;
		mFilterTask.execute(params);
	}

	private ProgressBar mProgress;

	private List<AppInfo> mApps = new ArrayList<>();

	private RecyclerView mGrid;
	private RecyclerView.LayoutManager mGridManager;
	private MyAdapter mGridAdapter;

	private FloatingActionButton mSearch;

	private boolean mIsInitialized = false;
	private View mRootView;

	private AppFilter mActiveFilter;
	private FilterTask mFilterTask;
}
