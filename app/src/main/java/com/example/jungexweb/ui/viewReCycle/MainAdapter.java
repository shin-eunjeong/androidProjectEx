package com.example.jungexweb.ui.viewReCycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jungexweb.R;

import java.util.ArrayList;

//alt+enter 눌러서 implement methods 자동생성 후 CustomViewHolder에 alt+enter 해서 create class생성한다 에러 계속되면 improt잘못된거라 make~~선택하면 됨
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.CustomViewHolder> {
    private ArrayList<MainData> arrayList;
    //arrayList construtor 자동생성
    public MainAdapter(ArrayList<MainData> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //리스트 뷰를 생성,viewholder를 생성하고 row layout뿌려주고 holder에 연결
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item,parent,false);
        CustomViewHolder holder = new CustomViewHolder(view);
        //
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        //실제 추가될때 생명주기
        //viewholder에 데이터 삽입
        holder.iv_profile.setImageResource(arrayList.get(position).getIv_profile());
        holder.tv_title.setText(arrayList.get(position).getTv_title());
        holder.tv_content.setText(arrayList.get(position).getTv_content());

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String curName = holder.tv_title.getText().toString(); //현재이름으로 가져옴
                Toast.makeText(view.getContext(),curName, Toast.LENGTH_SHORT).show();
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                remove(holder.getAdapterPosition());
                //리스트 삭제하는 것으로 구현
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        //몇개의 데이터를 리스트로 뿌려져야 하는지 반드시 정의해야함
        return (null != arrayList ? arrayList.size() : 0);
    }

    public void remove(int position){
        try {
             arrayList.remove(position);
             //notify는 새로고침
             notifyItemRemoved(position);
        } catch (IndexOutOfBoundsException ex){
            ex.printStackTrace();
        }
    }
    //viewHolder는 하나의 view를 보존하는 역활을 한다.
    public class CustomViewHolder extends RecyclerView.ViewHolder {
        //RecyclerView.ViewHolder 에러나면 create constructor matching super선택하면 자동생성
        protected ImageView iv_profile;
        protected TextView tv_title;
        protected TextView tv_content;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.iv_profile = (ImageView) itemView.findViewById(R.id.iv_profile);
            this.tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            this.tv_content = (TextView) itemView.findViewById(R.id.tv_content);
        }
    }
}
