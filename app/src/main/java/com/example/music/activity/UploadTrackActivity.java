package com.example.music.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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

import com.example.test.BuildConfig;
import com.example.test.R;

import java.io.ByteArrayOutputStream;
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

    private static final String SERVER_URL = BuildConfig.BASE_URL +"/tracks/add-track";

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
                Bitmap bitmap = correctImageOrientation(imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка при выборе изображения", Toast.LENGTH_SHORT).show();
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
            ParcelFileDescriptor audioPFD = getContentResolver().openFileDescriptor(audioUri, "r");

            if (audioPFD == null) {
                Toast.makeText(this, "Ошибка при доступе к аудиофайлу", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = correctImageOrientation(imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            String imageFileName = getFileNameFromUri(imageUri);
            String audioFileName = getFileNameFromUri(audioUri);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("title", title)
                    .addFormDataPart("artist", artist)
                    .addFormDataPart("image", imageFileName,
                            RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
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
                        setResult(Activity.RESULT_OK);
                        finish();
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

    private Bitmap correctImageOrientation(Uri imageUri) throws IOException {
        // Получаем метаданные EXIF для изображения
        ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(imageUri));

        // Получаем ориентацию изображения
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        // Создаем Bitmap из URI
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

        // Исправляем ориентацию
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            default:
                // Для других ориентаций ничего не делаем
                return bitmap;
        }

        // Применяем матрицу для корректировки ориентации
        Bitmap correctedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Освобождаем исходный Bitmap
        if (bitmap != correctedBitmap) {
            bitmap.recycle();
        }

        return correctedBitmap;
    }
}
