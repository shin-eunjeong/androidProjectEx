package com.example.jungexweb.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jungexweb.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    /**
    * 241018 스래드 관련 예제
     * 프로세스 안에서 여러작업을 동시처리하는데 메모리공간을 공유하여 효율적 업무를 함
    * */
    Button btn_start, btn_end;
    Thread thread;
    boolean isThread = false;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        btn_start=(Button)binding.homeThreadStart;
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MainActivity btn_start :" ,"스레드 시작 입력");
                isThread = true;
                thread = new Thread(() -> {
                    while(isThread){
                        try {
                            Thread.sleep(5000);// 5초 동안 잠시 쉬어라
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        //스래드와 핸들러는 같이 쓰인다.
                        handler.sendEmptyMessage(0); //5초쉬고 메세지를 보내라.
                    }
                });
                thread.start();

            }
        });
        btn_end=(Button)binding.homeThreadStop;
        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MainActivity btn_end :" ,"스레드 종료 입력");
                isThread = false;

            }
        });

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            Toast.makeText(getContext(),"Toast업무 처리입니다.", Toast.LENGTH_SHORT).show();
        }
    };

}