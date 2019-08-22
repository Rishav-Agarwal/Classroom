package in.edu.jaduniv.classroom.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.activity.EventAndNotice;

public class FileSelectedFragment extends Fragment {

    TextView tvFileName;
    ImageView ivRemoveFile;
    private String fileName;

    public FileSelectedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fileName = getArguments().getString("fileName", null);
        return inflater.inflate(R.layout.fragment_file_selected, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tvFileName = getView().findViewById(R.id.tv_file_name);
        ivRemoveFile = getView().findViewById(R.id.iv_remove_file);

        if (fileName == null || fileName.equals("null"))
            fileName = "Untitled";

        Log.d("File name", fileName);

        tvFileName.setText(fileName);

        ivRemoveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAndNotice.attachedUri = EventAndNotice.attachedFileName = EventAndNotice.attachedMimeType = null;
                EventAndNotice.attachedFile = null;
                EventAndNotice.getInstance().setSendFragment(null);
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.remove(FileSelectedFragment.this);
                fragmentTransaction.commit();
            }
        });
    }
}