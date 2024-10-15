package com.example.jungexweb.ui.viewReCycle;

import com.example.jungexweb.R;

import java.util.ArrayList;

public class MainData {
    private int iv_profile;
    private String tv_title;
    private String tv_content;

    //alt + ins 나오면 자동 생성 나온다. 생성자. getter setter
    public MainData(int iv_profile, String tv_title, String tv_content) {
        this.iv_profile = iv_profile;
        this.tv_title = tv_title;
        this.tv_content = tv_content;
    }

    public int getIv_profile() {
        return iv_profile;
    }

    public void setIv_profile(int iv_profile) {
        this.iv_profile = iv_profile;
    }

    public String getTv_title() {
        return tv_title;
    }

    public void setTv_title(String tv_name) {
        this.tv_title = tv_title;
    }

    public String getTv_content() {
        return tv_content;
    }

    public void setTv_content(String tv_content) {
        this.tv_content = tv_content;
    }

    public static ArrayList<MainData> createList(int listCount){
        ArrayList<MainData> list = new ArrayList<MainData>();
        for(int i=1;i<=listCount ; i++){
            list.add(new MainData(R.mipmap.ic_launcher,"제목" +i ,"리사이클"+i));
        }
        return list;
    }
}
