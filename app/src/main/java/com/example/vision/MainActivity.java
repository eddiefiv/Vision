package com.example.vision;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Toast;

import com.example.vision.databinding.MainActivityBinding;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    private static final int CAMERA_PERMISSION_CODE = 1;
    private static final String TAG = "MainActivity";

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;

    MainActivityBinding mainBinding;

    // PyTorch model;
    Module module;
    Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        mainBinding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        replaceFragment(new LiveViewFragment());
        instance = this;

        checkCameraPermissions();

        previewView = findViewById(R.id.previewView);
        setCameraProviderListener();

        // Load torch model
        LoadTorchModule("yolopv2.pt");

        mainBinding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()) {

                case R.id.live_view:
                    replaceFragment(new LiveViewFragment());
                    break;
                case R.id.save:
                    replaceFragment(new SaveFragment());
                    break;
                case R.id.settings:
                    replaceFragment(new SettingsFragment());
                    break;
            }

            return true;
        });
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void LoadTorchModule(String fileName) {
        File modelFile = new File(this.getFilesDir(), fileName);

        try {
            if (!modelFile.exists()) {
                InputStream is = getAssets().open(fileName);
                FileOutputStream os = new FileOutputStream(modelFile);

                byte[] buffer = new byte[2048];
                int bytesRead = -1;

                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();
            }

            module = LiteModuleLoader.load(modelFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void checkCameraPermissions() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Camera permission denied, pleases allow permission to utilize camera view. ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setCameraProviderListener() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future
                // This should never be reached
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(224, 224))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotation = image.getImageInfo().getRotationDegrees();
                // analyze image
                image.close();
            }
        });

        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
    }

    public void analyzeImage(ImageProxy image, int rotation) {
        @SuppressLint("UnsafeOptInUsageError") Tensor inputTensor = TensorImageUtils.imageYUV420CenterCropToFloat32Tensor(image.getImage(), rotation, 224, 224,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

        Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

        float[] scores = outputTensor.getDataAsFloatArray();
        float maxScore = -Float.MAX_VALUE;
        int maxScoreIdx = -1;

        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                maxScoreIdx = i;
            }
        }
    }
}