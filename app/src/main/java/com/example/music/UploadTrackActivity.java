package com.example.music;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadTrackActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_AUDIO_REQUEST = 2;
    private static final int STORAGE_PERMISSION_CODE = 1;
    private Uri imageUri;
    private Uri audioUri;
    private EditText titleEditText;
    private EditText artistEditText;
    private ImageView imageView;
    private TextView audioFileName;

    private static final String SERVER_URL = "http://192.168.100.30:3000/add-track"; // Замените на ваш URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_track);

        titleEditText = findViewById(R.id.titleEditText);
        artistEditText = findViewById(R.id.artistEditText);
        imageView = findViewById(R.id.imageView);
        audioFileName = findViewById(R.id.audioFileName);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button selectAudioButton = findViewById(R.id.selectAudioButton);
        Button uploadButton = findViewById(R.id.uploadButton);

        checkPermission();

        selectImageButton.setOnClickListener(v -> openImageChooser());
        selectAudioButton.setOnClickListener(v -> openAudioChooser());
        uploadButton.setOnClickListener(v -> uploadTrack());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openAudioChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            audioUri = data.getData();
            audioFileName.setText("Файл выбран: " + audioUri.getLastPathSegment());
        }
    }

    private void uploadTrack() {
        String title = titleEditText.getText().toString().trim();
        String artist = artistEditText.getText().toString().trim();

        if (title.isEmpty() || artist.isEmpty() || imageUri == null || audioUri == null) {
            Toast.makeText(this, "Пожалуйста, заполните все поля и выберите изображение и аудиофайл", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        try {
            ParcelFileDescriptor imagePFD = getContentResolver().openFileDescriptor(imageUri, "r");
            ParcelFileDescriptor audioPFD = getContentResolver().openFileDescriptor(audioUri, "r");

            if (imagePFD == null || audioPFD == null) {
                Toast.makeText(this, "Ошибка при доступе к файлу", Toast.LENGTH_SHORT).show();
                return;
            }

            // Получение динамических имен файлов
            String imageFileName = getFileNameFromUri(imageUri);
            String audioFileName = getFileNameFromUri(audioUri);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("title", title)
                    .addFormDataPart("artist", artist)
                    .addFormDataPart("image", imageFileName,
                            new RequestBody() {
                                @Override
                                public MediaType contentType() {
                                    return MediaType.parse("image/jpeg");
                                }

                                @Override
                                public void writeTo(okio.BufferedSink sink) throws IOException {
                                    try (InputStream is = new FileInputStream(imagePFD.getFileDescriptor())) {
                                        byte[] buffer = new byte[4096];
                                        int read;
                                        while ((read = is.read(buffer)) != -1) {
                                            sink.write(buffer, 0, read);
                                        }
                                    }
                                }
                            })
                    .addFormDataPart("track", audioFileName,
                            new RequestBody() {
                                @Override
                                public MediaType contentType() {
                                    return MediaType.parse("audio/mpeg");
                                }

                                @Override
                                public void writeTo(okio.BufferedSink sink) throws IOException {
                                    try (InputStream is = new FileInputStream(audioPFD.getFileDescriptor())) {
                                        byte[] buffer = new byte[4096];
                                        int read;
                                        while ((read = is.read(buffer)) != -1) {
                                            sink.write(buffer, 0, read);
                                        }
                                    }
                                }
                            })
                    .build();

            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(UploadTrackActivity.this, "Ошибка при загрузке", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(UploadTrackActivity.this, "Трек успешно загружен", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(UploadTrackActivity.this, "Ошибка при загрузке", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при чтении файла", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }

    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}
