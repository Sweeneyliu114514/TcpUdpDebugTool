package com.sweeneyliu.activitytest.ui.http;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HttpViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HttpViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Http Fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}