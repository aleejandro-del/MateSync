package com.example.matesync.Callbacks;

public interface AuthCallback {
    void onSuccess();
    void onFailure(Exception exception);
}
