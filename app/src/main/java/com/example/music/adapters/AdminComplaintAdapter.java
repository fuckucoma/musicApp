package com.example.music.adapters;

import android.content.Context;
import androidx.annotation.NonNull; // Исправили импорт
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.music.models.Complaint;
import com.example.music.repository.AdminRepository;
import com.example.test.R;

import java.util.List;

public class AdminComplaintAdapter extends RecyclerView.Adapter<AdminComplaintAdapter.ComplaintViewHolder> {

    private final Context context;
    private final List<Complaint> complaintList;

    public AdminComplaintAdapter(Context context, List<Complaint> complaintList) {
        this.context = context;
        this.complaintList = complaintList;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);

        // Здесь важно, чтобы complaint.getUser() != null
        // Иначе будет NPE
        if (complaint.getUser() != null) {
            holder.userName.setText(complaint.getUser().getUsername());
        } else {
            holder.userName.setText("Unknown User");
        }

        holder.message.setText(complaint.getMessage());
        holder.status.setText(complaint.getStatus());

        holder.resolveButton.setOnClickListener(v -> updateComplaintStatus(complaint, "решено"));
        holder.rejectButton.setOnClickListener(v -> updateComplaintStatus(complaint, "отказано"));
    }

    private void updateComplaintStatus(Complaint complaint, String status) {
        AdminRepository.getInstance().updateComplaintStatus(complaint.getId(), status, new AdminRepository.MyCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                complaint.setStatus(status);  // Обновляем статус в локальном списке
                notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable t) {
                // Обработка ошибки
            }
        });
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView userName, message, status;
        Button resolveButton, rejectButton;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.complaint_user_name);
            message = itemView.findViewById(R.id.complaint_message);
            status = itemView.findViewById(R.id.complaint_status);
            resolveButton = itemView.findViewById(R.id.button_resolve);
            rejectButton = itemView.findViewById(R.id.button_reject);
        }
    }
}
