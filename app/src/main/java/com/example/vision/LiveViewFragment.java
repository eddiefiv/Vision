package com.example.vision;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vision.databinding.LiveViewBinding;
import com.google.common.util.concurrent.ListenableFuture;

public class LiveViewFragment extends Fragment implements View.OnClickListener{

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;

    private LiveViewBinding liveViewBinding;

    public LiveViewFragment() {
        // Required constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.live_view, container, false);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {
    }
}