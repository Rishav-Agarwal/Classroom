package in.edu.jaduniv.classroom.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.object.JoinRequest;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;

public class JoinRequestAdapter extends RecyclerView.Adapter<JoinRequestAdapter.JoinViewHolder> {

    private ArrayList<JoinRequest> joinRequests;
    private ArrayList<String> joinRequestKeys;
    private String classCode;
    private Context context;

    public JoinRequestAdapter(Context context, ArrayList<JoinRequest> joinRequests, ArrayList<String> joinRequestKeys, String classCode) {
        this.joinRequests = joinRequests;
        this.joinRequestKeys = joinRequestKeys;
        this.classCode = classCode;
        this.context = context;
    }

    @NonNull
    @Override
    public JoinRequestAdapter.JoinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.join_request, parent, false);
        return new JoinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final JoinRequestAdapter.JoinViewHolder holder, int position) {
        final JoinRequest request = joinRequests.get(position);
        holder.tvPhone.setText(request.getPhone());
        holder.tvName.setText(request.getName());
        final DatabaseReference classJoinRef = FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("join_req").child(joinRequestKeys.get(holder.getAdapterPosition()));
        holder.ivAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classJoinRef.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        DatabaseReference reqUserRef = FirebaseUtils.getDatabaseReference().child("users").child(request.getPhone());
                        reqUserRef.child("classes").push().setValue(classCode).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(context, "Participant added!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        holder.ivReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classJoinRef.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Toast.makeText(context, "Join request declined!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return joinRequests.size();
    }

    static class JoinViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPhone;
        ImageView ivAccept, ivReject;

        JoinViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone_no);
            ivAccept = itemView.findViewById(R.id.iv_join_request_accept);
            ivReject = itemView.findViewById(R.id.iv_join_request_reject);
        }
    }
}