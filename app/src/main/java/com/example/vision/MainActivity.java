package com.example.vision;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.widget.Toast;

import com.example.vision.databinding.MainActivityBinding;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    private static final int CAMERA_PERMISSION_CODE = 1;

    MainActivityBinding mainBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        replaceFragment(new LiveViewFragment());
        instance = this;

        checkCameraPermissions();

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
}