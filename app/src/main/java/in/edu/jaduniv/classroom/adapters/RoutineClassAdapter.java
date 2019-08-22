package in.edu.jaduniv.classroom.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.object.__Class;

public final class RoutineClassAdapter extends RecyclerView.Adapter<RoutineClassAdapter.ViewHolder> {

    private final ArrayList<__Class> classes;

    public RoutineClassAdapter(ArrayList<__Class> classes) {
        this.classes = classes;
    }

    @NonNull
    @Override
    public RoutineClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoutineClassAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.routine_class_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineClassAdapter.ViewHolder holder, int position) {
        final __Class _class = classes.get(holder.getAdapterPosition());
        ((TextView) holder.getView().findViewById(R.id.class_subject)).setText(String.format("%s | %s", _class.getName(), _class.getDescription()));
        ((TextView) holder.getView().findViewById(R.id.class_prof)).setText(_class.getProf());
        ((TextView) holder.getView().findViewById(R.id.class_location)).setText(_class.getLocation());
        ((TextView) holder.getView().findViewById(R.id.class_start_time)).setText(_class.getStartTime());
        ((TextView) holder.getView().findViewById(R.id.class_end_time)).setText(_class.getEndTime());
        int height = holder.getView().getLayoutParams().height, calcHeight = (int) (holder.getView().getContext().getResources().getDimension(R.dimen.class_item_height) * (_class.getLength() + 1) / 2);
        holder.getView().getLayoutParams().height = calcHeight > height ? calcHeight : height;
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public View getView() {
            return view;
        }
    }
}
