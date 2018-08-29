package in.edu.jaduniv.classroom.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.activity.EventAndNotice;
import in.edu.jaduniv.classroom.activity.Syllabus;
import in.edu.jaduniv.classroom.adapters.ClassAdapter;

public class ClassFragment extends Fragment {

    private String classCode;

    public ClassFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        classCode = getArguments().getString("class");
        return inflater.inflate(R.layout.fragment_class, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final GridView gridView = getView().findViewById(R.id.class_grid_view);
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.class_item_anim);
        GridLayoutAnimationController animationController = new GridLayoutAnimationController(anim);
        gridView.setLayoutAnimation(animationController);
        gridView.setAdapter(new ClassAdapter(getContext()));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Posiion", position + "");
                Intent intent;
                switch (position) {
                    case 0:
                        intent = new Intent(getActivity(), EventAndNotice.class);
                        intent.putExtra("class", classCode);
                        startActivity(intent);
                        break;
                    case 1:
                        //TODO: Implement Routine activity
                        Toast.makeText(getContext(), "This feature is not yet available!", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        //TODO: Implement Notes activity
                        Toast.makeText(getContext(), "This feature is not yet available!", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        intent = new Intent(getActivity(), Syllabus.class);
                        intent.putExtra("class", classCode);
                        startActivity(intent);
                        break;
                }

            }
        });
    }
}