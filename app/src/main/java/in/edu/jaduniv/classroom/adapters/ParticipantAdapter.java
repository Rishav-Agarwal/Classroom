package in.edu.jaduniv.classroom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.object.Participant;

public class ParticipantAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Participant> participants;

    public ParticipantAdapter(Context context, ArrayList<Participant> participants1) {
        this.context = context;
        participants = participants1;
    }

    @Override
    public int getCount() {
        return participants.size();
    }

    @Override
    public Participant getItem(int i) {
        return participants.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.participant, viewGroup, false);
        }

        Participant participant = getItem(i);

        TextView tvName = (TextView) view.findViewById(R.id.tv_participant_name);
        TextView tvPhone = (TextView) view.findViewById(R.id.tv_participant_phone);
        TextView tvAdmin = (TextView) view.findViewById(R.id.tv_admin);

        tvName.setText(participant.getName());
        tvPhone.setText(participant.getPhone());
        if (participant.isAdmin())
            tvAdmin.setVisibility(View.VISIBLE);
        else
            tvAdmin.setVisibility(View.GONE);

        return view;
    }
}
