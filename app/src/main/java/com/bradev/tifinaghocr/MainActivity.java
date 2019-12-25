package com.bradev.tifinaghocr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private ImageButton editBtn;
    private TextView resultText;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.result);
        editBtn = findViewById(R.id.edit_btn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = CropImage.getPickImageResultUri(this, data);
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri());
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 64, 64, true);
                imageView.setImageBitmap(resized);
                imageView.setVisibility(View.VISIBLE);
                editBtn.setVisibility(View.VISIBLE);
                String s = bitmapToMatrix(resized);
                requestResult(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getImage(View view) {
        CropImage.startPickImageActivity(this);
    }

    private String bitmapToMatrix(Bitmap bitmap){
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[][] matrix = new int[width][height];

        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = bitmap.getPixel(i, j);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                int gray = (r + g + b) / 3;
                matrix[i][j] = gray;
            }
        }

        return Arrays.deepToString(matrix);
    }

    private void requestResult(final String data){
        String url = "http://192.168.10.6:8000/api/";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                resultText.setText(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Connection error !!", Toast.LENGTH_SHORT).show();
                Log.e("Error: ", error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void edit(View view) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(this);
    }
}
