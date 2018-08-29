package in.edu.jaduniv.classroom.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.activity.EventAndNotice;

public class FileSelectedFragment extends Fragment {

    private String uri;
    private String fileName;

    TextView tvFileName;
    ImageView ivRemoveFile;

    public FileSelectedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        uri = getArguments().getString("uri");
        fileName = getArguments().getString("fileName", null);
        return inflater.inflate(R.layout.fragment_file_selected, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tvFileName = (TextView) getView().findViewById(R.id.tv_file_name);
        ivRemoveFile = (ImageView) getView().findViewById(R.id.iv_remove_file);

        if (fileName == null)
            fileName = new File(Uri.parse(uri).getPath()).getName();

        tvFileName.setText(fileName);

        ivRemoveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAndNotice.attachedUri = null;
                EventAndNotice.getInstance().setSendFragment(null);
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.remove(FileSelectedFragment.this);
                fragmentTransaction.commit();
            }
        });
    }
}
