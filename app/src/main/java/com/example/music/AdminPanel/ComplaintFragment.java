package com.example.music.AdminPanel;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.music.adapters.AdminComplaintAdapter;
import com.example.music.models.Complaint;
import com.example.music.repository.AdminRepository;
import com.example.test.R;

import java.util.List;

public class ComplaintFragment extends Fragment {

    private RecyclerView complaintsRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complaints, container, false);
        complaintsRecyclerView = view.findViewById(R.id.complaints_recycler_view);
        complaintsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchComplaints();
        return view;
    }

    private void fetchComplaints() {
        AdminRepository.getInstance().fetchComplaints(new AdminRepository.MyCallback<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> data) {
                AdminComplaintAdapter adapter = new AdminComplaintAdapter(getContext(), data);
                complaintsRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(Throwable t) {
                Log.e("ComplaintFragment", "Ошибка загрузки жалоб: " + t.getMessage());
            }
        });
    }
}
