package in.edu.jaduniv.classroom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import in.edu.jaduniv.classroom.R;

public class ClassAdapter extends BaseAdapter {

    private Context context;
    private String[] gridNames = {"Events and Notices", "Routine", "Notes", "Syllabus"};

    public ClassAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int i) {
        return gridNames[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View ConvertView = view;
        if (ConvertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ConvertView = inflater.inflate(R.layout.class_adapter_item, viewGroup, false);
        }

        TextView tvGridTitle = (TextView) ConvertView.findViewById(R.id.tv_grid_item_title);
        tvGridTitle.setText(gridNames[i]);

        return ConvertView;
    }
}