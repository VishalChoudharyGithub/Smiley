package com.hack.faceml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button cameraButton;
    private final static int  REQUEST_CAMERA = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takepicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takepicture.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(takepicture,REQUEST_CAMERA);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_CANCELED){return;}

            Bundle extras = data.getExtras();
            Bitmap bitmap =(Bitmap) extras.get("data");
            detectFace(bitmap);
    }

    private void detectFace(Bitmap bitmap) {
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .enableTracking()
                        .build();
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        String resultText = "";

                                        for(Face face:faces){
                                            float smile = Math.round(face.getSmilingProbability()*100);
                                            String condition = "";
                                            if(smile>70)condition = "Always keep smiling 😊";
                                            else if(smile<70&&smile>50) condition = "Smile more please 😊😊";
                                            else if(smile<50) condition = "See I have a joke for you\n\n"+ returnRandom();
                                            resultText = resultText
                                                    .concat("\nSmile: "+smile+"%\n\n")
                                            .concat(condition);

                                        }
                                        if(faces.size()==0){
                                            Toast.makeText(MainActivity.this,"No faces",Toast.LENGTH_SHORT).show();
                                        }else{
                                            Bundle bundle = new Bundle();
                                            bundle.putString(com.hack.faceml.FaceDetection.result_text,resultText);
                                            DialogFragment resultDialog = new ResultDialog();
                                            resultDialog.setArguments(bundle);
                                            resultDialog.setCancelable(false);
                                            resultDialog.show(getSupportFragmentManager(), com.hack.faceml.FaceDetection.result_dialog);
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
    }

    private  String returnRandom(){
        String[] jokes = {"Bean soup and the dumb waiter\nA man in a hotel: Waiter, there is a dead fly in my bean soup.\nWaiter: Oh, the hot soup must have killed it sir.",
                "The funny soup\nCustomer in a hotel: Waiter, this soup tastes funny!\nWaiter: Oh!, the chef must have been laughing when he prepared it sir.",
                "Lady next door\nMan1: Your kid just looks like you.\nMan2: Shhh, not so loud. That's the next door lady's kid.",
                "Man and his son joke\nMan 1: My son does not listen to anything that I say.\nMan 2: Is he so adamnant?\nMan 1: No, he is deaf."};
        return  jokes[((int) (Math.random()*jokes.length))];
    }
}
