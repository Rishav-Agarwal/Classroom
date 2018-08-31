package in.edu.jaduniv.classroom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.object.Syllabus;

public class SyllabusAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Syllabus> syllabusList;
    private ArrayList<String> keys;
    private PopupMenuOverflowClickListener listener;

    public SyllabusAdapter(Context context, ArrayList<Syllabus> syllabusList, ArrayList<String> keys, PopupMenuOverflowClickListener listener) {
        this.context = context;
        this.syllabusList = syllabusList;
        this.keys = keys;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return syllabusList.size();
    }

    @Override
    public Object getItem(int position) {
        return syllabusList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(Syllabus syllabus, String key) {
        syllabusList.add(syllabus);
        keys.add(key);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                convertView = inflater.inflate(R.layout.syllabus, parent, false);
            }
        }

        TextView tvSubject;
        ImageView ivOverflow;
        if (convertView != null) {
            tvSubject = convertView.findViewById(R.id.tv_subject);
            ivOverflow = convertView.findViewById(R.id.iv_syllbus_item_overflow);

            tvSubject.setText(syllabusList.get(position).getSubject());
            final View finalConvertView = convertView;
            ivOverflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPopupMenuOverflowClicked(finalConvertView, position);
                }
            });
        }

        return convertView;
    }

    public interface PopupMenuOverflowClickListener {
        void onPopupMenuOverflowClicked(View parentView, int position);
    }
}