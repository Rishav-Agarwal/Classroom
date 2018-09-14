package in.edu.jaduniv.classroom.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.fragment.DayFragment;
import in.edu.jaduniv.classroom.object.__Class;
import in.edu.jaduniv.classroom.utility.TimeUtils;

public final class RoutineDayAdapter extends FragmentStatePagerAdapter {

    private final int DAYS_COUNT;
    private final ArrayList<ArrayList<__Class>> timeTable;

    //Store list of fragments in our viewpager
    private Fragment[] fragments;

    public RoutineDayAdapter(FragmentManager fm, ArrayList<ArrayList<__Class>> timeTable) {
        super(fm);
        DAYS_COUNT = 6;
        this.timeTable = timeTable;
        fragments = new Fragment[DAYS_COUNT];
    }

    @Override
    public Fragment getItem(int position) {
        return DayFragment.getInstance(timeTable.get(position));
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments[position] = fragment;
        View view = fragment.getView();
        if (view != null) {
            RecyclerView recyclerView = view.findViewById(R.id.rv_day);
            RoutineClassAdapter dayAdapter = (RoutineClassAdapter) recyclerView.getAdapter();
            TextView textView = view.findViewById(R.id.rv_day_empty);

            if (dayAdapter.getItemCount() == 0) {
                recyclerView.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
            }
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return DAYS_COUNT;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return TimeUtils.getDay(position);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Log.d("Day", "Dataset changed");
        for (int i = 0; i < getCount(); ++i) {
            if (fragments[i] == null)
                continue;
            View view = fragments[i].getView();
            Log.d("Day view", String.valueOf(fragments[i]) + " :: " + String.valueOf(view) + " :: " + fragments[i].getTag());
            if (view != null) {
                RecyclerView recyclerView = view.findViewById(R.id.rv_day);
                RoutineClassAdapter adapter = (RoutineClassAdapter) recyclerView.getAdapter();
                adapter.notifyDataSetChanged();

                TextView textView = view.findViewById(R.id.rv_day_empty);

                if (adapter.getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);
                }
            }
        }
    }
}