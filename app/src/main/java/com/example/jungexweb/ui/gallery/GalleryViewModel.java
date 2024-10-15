package com.example.jungexweb.ui.gallery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GalleryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("카메라와 권한설정 관련 내용");
    }
    /*외부에서 수정하기 위한 코드....이건 알지? */
    public void setText(String newText){
        mText.setValue(newText);
    }

    public LiveData<String> getText() {
        return mText;
    }
}