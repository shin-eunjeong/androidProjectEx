package com.example.jungexweb.ui.viewReCycle;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jungexweb.R;
import com.example.jungexweb.databinding.FragmentViewrecycleBinding;

import java.util.ArrayList;

public class ViewReCycleFragment extends Fragment {
    private ArrayList<MainData> arrayList;
    private MainAdapter mainAdapter;
    private FragmentViewrecycleBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewrecycleBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 구성 내용
        RecyclerView recyclerView = (RecyclerView) binding.rv;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        arrayList  = MainData.createList(4);
        mainAdapter = new MainAdapter(arrayList);
        recyclerView.setAdapter(mainAdapter);

        Button btn_add = (Button) binding.btnAdd;
        btn_add.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                MainData mainData = new MainData(R.mipmap.ic_launcher,"제목1","리사이클");
                arrayList.add(mainData);
                mainAdapter.notifyDataSetChanged(); //새로고침까지
            }
        });

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}