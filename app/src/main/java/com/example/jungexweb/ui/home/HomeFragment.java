package com.example.jungexweb.ui.home;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jungexweb.R;
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
    /** 2410120 다이알로그 관련
     * 이미 interface로 구현되어 있음 가져다가 쓰는 것임. 똑같음 */
    Button btn_home_dialog;
    TextView tv_home_showLog;
    /**  background music 관련 */
    Button home_musicStart, home_musicStop;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        /** thread 관련 */
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

        /** 다이얼로그 관련 */

        btn_home_dialog =(Button) binding.btnHomeDialog;
        tv_home_showLog = (TextView) binding.tvHomeShowLog;

        btn_home_dialog.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(requireActivity());
                //구문에서는 일반 화면이라 해당 java 클래스를 this로 가져왔으나 여기는 fragment여서 변형
                //alert내부를 만든다.
                aBuilder.setIcon(R.mipmap.ic_launcher);   //아이콘 넣기...변경 가능함.
                aBuilder.setTitle("제목");
                aBuilder.setMessage("텍스트로 입력하세요.");
                //추가로 입력한것을 반영까지.
                final EditText editText = new EditText(requireActivity());
                aBuilder.setView(editText);  //alert의 입력값을 home에 표기

                //alert안 버튼 구문 확인은 긍정버튼, 취소는 부정버튼으로 메소드 이름에까지 표기
                aBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String diaResult = editText.getText().toString();
                        tv_home_showLog.setText(diaResult);
                        dialogInterface.dismiss();;
                    }
                });
                aBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                aBuilder.show();  //alert 다이얼로그 보여주기

            }
        });
        /*  background music 관련 */
        home_musicStart =(Button) binding.homeMusicStart;
        home_musicStop =(Button) binding.homeMusicStop;

        home_musicStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().startService(new Intent(getContext(),MusicService.class));
                //android. content. ComponentName startService 함수였으나 fragment여서 변환함.
            }
        });
        home_musicStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().stopService(new Intent(getContext(), MusicService.class));
            }
        });
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