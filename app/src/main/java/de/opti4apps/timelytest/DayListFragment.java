package de.opti4apps.timelytest;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.Day_;
import de.opti4apps.timelytest.event.DayDatasetChangedEvent;
import de.opti4apps.timelytest.event.DayMultibleSelectionEvent;
import de.opti4apps.timelytest.event.DaySelectedEvent;
import io.objectbox.Box;
import io.objectbox.query.Query;

/**
 * A fragment representing a list of Days.
 * <p/>
 */
public class DayListFragment extends Fragment {

    public static final String TAG = DayListFragment.class.getSimpleName();
    private final List<Day> mDayList = new ArrayList<>();
    private final String[] mMonthArray = new String[12];
    private final String[] mYearArray = new String[35];
    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    @BindView(R.id.fab)
    FloatingActionButton mFloatingActionButton;
    @BindView(R.id.year_spinner)
    Spinner mYearSpinner;
    @BindView(R.id.month_spinner)
    Spinner mMonthSpinner;
    private Box<Day> mDayBox;
    private Query<Day> mDayQuery;
    private ActionMode mActionMode;
    private ActionModeCallback mActionModeCallback = new ActionModeCallback();
    private int mCurrentMonthArrayPosition = -1;
    private int mCurrentYearArrayPosition = -1;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DayListFragment() {
    }

    @SuppressWarnings("unused")
    public static DayListFragment newInstance() {
        return new DayListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDayBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Day.class);
        mDayQuery = mDayBox.query().orderDesc(Day_.day).build();
        EventBus.getDefault().register(this);

        mDayList.addAll(mDayQuery.find());
        setHasOptionsMenu(true);
        setRetainInstance(true);
        initArrays();

    }

    private void initArrays() {
        LocalDate date;

        for (int i = 0; i < mMonthArray.length; i++) {
            date = new LocalDate(0, i + 1, 1);
            mMonthArray[i] = date.monthOfYear().getAsShortText();
        }

        for (int i = 0; i < mYearArray.length; i++) {
            date = new LocalDate(2000 + i, 1, 1);
            mYearArray[i] = date.year().getAsShortText();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_list, container, false);
        ButterKnife.bind(this, view);

        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(new MyDayRecyclerViewAdapter(mDayList));
        mRecyclerView.setHasFixedSize(true);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mMonthArray);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMonthSpinner.setAdapter(monthAdapter);
        if (mCurrentMonthArrayPosition == -1)
            mCurrentMonthArrayPosition = monthAdapter.getPosition(LocalDate.now().monthOfYear().getAsShortText());
        mMonthSpinner.setSelection(mCurrentMonthArrayPosition);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mYearArray);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYearSpinner.setAdapter(yearAdapter);
        if (mCurrentYearArrayPosition == -1)
            mCurrentYearArrayPosition = yearAdapter.getPosition(LocalDate.now().year().getAsShortText());
        mYearSpinner.setSelection(mCurrentYearArrayPosition);

        return view;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @OnClick(R.id.fab)
    public void onFABClicked() {
        EventBus.getDefault().post(new DaySelectedEvent(0));
    }

    @OnItemSelected(R.id.month_spinner)
    public void onMonthSpinnerItemSelected(Spinner spinner, int position) {
        mCurrentMonthArrayPosition = position;
        spinnerItemSelected();
    }

    @OnItemSelected(R.id.year_spinner)
    public void onYearSpinnerItemSelected(Spinner spinner, int position) {
        mCurrentYearArrayPosition = position;
        spinnerItemSelected();
    }

    private void spinnerItemSelected() {
        String dateString = mMonthSpinner.getAdapter().getItem(mCurrentMonthArrayPosition).toString() + mYearSpinner.getAdapter().getItem(mCurrentYearArrayPosition).toString();
        Log.d(TAG, dateString);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMyyyy");
        DateTime dateTime = DateTime.parse(dateString, formatter);
        Date min = dateTime.withDayOfMonth(1).toDate();
        Date max = dateTime.withDayOfMonth(1).plusMonths(1).minusDays(1).toDate();
        mDayQuery = mDayBox.query().between(Day_.day, min, max).orderDesc(Day_.day).build();
        EventBus.getDefault().post(new DayDatasetChangedEvent(TAG));
    }

    @Subscribe
    public void onDayDataSetChanged(DayDatasetChangedEvent event) {
        mDayList.clear();
        mDayList.addAll(mDayQuery.find());
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Subscribe
    public void onDayMultibleSelection(DayMultibleSelectionEvent event) {
        Log.d(TAG, "onDayMultibleSelection: selection size = " + event.selectionSize);
        if (event.selectionSize > 0 && mActionMode == null) {
            ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);

        } else if (event.selectionSize <= 0) {
            mActionMode.finish();
        }

    }

    private void clearSelection() {

        for (Map.Entry<Day, Integer> entry : ((MyDayRecyclerViewAdapter) mRecyclerView.getAdapter()).getSelection().entrySet()) {
            CardView view = (CardView) mRecyclerView.getLayoutManager().findViewByPosition(entry.getValue());
            if (view != null) {
                view.setActivated(false);
                view.setCardBackgroundColor(Color.WHITE);
            }
        }
        ((MyDayRecyclerViewAdapter) mRecyclerView.getAdapter()).getSelection().clear();
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            mode.getMenuInflater().inflate(R.menu.menu_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {

                case R.id.delete:
                    mDayBox.remove(((MyDayRecyclerViewAdapter) mRecyclerView.getAdapter()).getSelection().keySet());
                    clearSelection();
                    EventBus.getDefault().post(new DayDatasetChangedEvent(TAG));
                    Log.d(TAG, "menu_remove");
                    mode.finish();
                    return true;

                default:

                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            clearSelection();
            mActionMode = null;
        }
    }
}
