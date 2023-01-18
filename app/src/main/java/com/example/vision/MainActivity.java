package com.example.vision;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.widget.VideoView;

import com.example.vision.databinding.MainActivityBinding;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    private static final int CAMERA_PERMISSION_CODE = 1;
    private static final int BLUETOOTH_PERMISSION_CODE = 2;
    private static final String TAG = "MainActivity";

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private VideoView videoView;

    MainActivityBinding mainBinding;

    //BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
    //BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        mainBinding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        replaceFragment(new LiveViewFragment());
        instance = this;

        // Check for permissions
        checkCameraPermissions();

        // Enable Bluetooth connection intent
        /*if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivity(enableBtIntent);
        }*/

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        //previewView = findViewById(R.id.previewView);
        //setCameraProviderListener();

        videoView = (VideoView)findViewById(R.id.videoView);

        // Show video
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.footage));
        videoView.requestFocus();
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.start();
            }
        });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    public static MainActivity getInstance() {
        return instance;
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

        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Camera permission denied, pleases allow permission to utilize camera view.", Toast.LENGTH_SHORT).show();
                }
            case BLUETOOTH_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Bluetooth permission denied, please allow Bluetooth permissions to allow for maximum functionality.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    /*private void setCameraProviderListener() {
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
    }*/

    /*private void bindPreview(ProcessCameraProvider cameraProvider) {

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(224, 224))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
    }*/

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device
                // Get the BluetoothDevice object and its info from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                @SuppressLint("MissingPermission") String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC Address
            }
        }
    };

    @SuppressLint("MissingPermission")
    public void BTConnect(BluetoothDevice device) {
        final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        BluetoothSocket socket = null;
        String MAC = device.getAddress();

        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.connect();
        } catch (IOException e1) {
            try {
                socket.close();
                Log.d("BT_TESTING", "Cannot Connect");
                e1.printStackTrace();
            } catch (IOException e2) {
                Log.d("BT_TESTING", "Socket not closed");
                e2.printStackTrace();
            }
        }
    }
}