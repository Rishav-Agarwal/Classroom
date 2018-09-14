package in.edu.jaduniv.classroom.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.adapters.RoutineClassAdapter;
import in.edu.jaduniv.classroom.object.__Class;

/**
 * A {@link Fragment} subclass which displays classes in a day.
 */
public class DayFragment extends Fragment {

    private final static String ARG_TIMETABLE = "TIME_TABLE";
    private ArrayList<__Class> classes;

    public DayFragment() {
        // Required empty public constructor
    }

    public static DayFragment getInstance(ArrayList<__Class> classes) {
        DayFragment dayFragment = new DayFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIMETABLE, classes);
        dayFragment.setArguments(args);
        return dayFragment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        classes = (ArrayList<__Class>) ((getArguments() != null) ? getArguments().getSerializable(ARG_TIMETABLE) : null);
        return inflater.inflate(R.layout.fragment_day, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (classes == null)
            return;
        RecyclerView rvDayFrag = view.findViewById(R.id.rv_day);
        rvDayFrag.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        rvDayFrag.setAdapter(new RoutineClassAdapter(classes));
    }
}