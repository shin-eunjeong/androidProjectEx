package com.example.jungexweb.ui.viewNever;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
/*해당 화면하고 연결되어야 한다. */
import com.example.jungexweb.databinding.FragmentViewneverBinding;

public class ViewNeverFragment extends Fragment {
    private WebView webView;
    /* 일반 url 넣으니 429 에러 뜸 모바일 주소로 넣으니깐 정상 작동*/
    private String url ="https://m.naver.com";
    private FragmentViewneverBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ViewNeverViewModel viewNeverViewModel =
                new ViewModelProvider(this).get(ViewNeverViewModel.class);

        binding = FragmentViewneverBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // 구성 내용
        webView = binding.viewNever;
        webView.getSettings().setJavaScriptEnabled(true); //자바스크립트를 사용할 수 있게 하는 기능
        webView.loadUrl(url );
        webView.setWebChromeClient(new WebChromeClient());//크롬 사용 가능하게
        webView.setWebViewClient(new WebViewClientClass());

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //현재페이지의  url를 읽어오는 기능 , 새창읽기, 특정페이지 특정기능 넣기등 가능
            view.loadUrl(request.getUrl().toString());
            return true ;
        }
    }

}