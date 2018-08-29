package in.edu.jaduniv.classroom.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.object.JoinRequest;

public class JoinRequestAdapter extends RecyclerView.Adapter<JoinRequestAdapter.JoinViewHolder> {

    private ArrayList<JoinRequest> joinRequests;

    public JoinRequestAdapter(ArrayList<JoinRequest> joinRequests) {
        this.joinRequests = joinRequests;
    }

    static class JoinViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPhone;

        JoinViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvPhone = (TextView) itemView.findViewById(R.id.tv_phone_no);
        }
    }

    @Override
    public JoinRequestAdapter.JoinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.join_request, parent, false);
        return new JoinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(JoinRequestAdapter.JoinViewHolder holder, int position) {
        JoinRequest request = joinRequests.get(position);
        holder.tvPhone.setText(request.getPhone());
        holder.tvName.setText(request.getName());
    }

    @Override
    public int getItemCount() {
        return joinRequests.size();
    }
}
