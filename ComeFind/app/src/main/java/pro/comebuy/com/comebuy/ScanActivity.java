package pro.comebuy.com.comebuy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

public class ScanActivity extends AppCompatActivity {
    String path;
    private Vision vision;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        path=getIntent().getStringExtra("path");
        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer("AIzaSyB4k5LieQKUQ3zThnKwryvB9zzQ0t3fBe4"));

        vision = visionBuilder.build();
        path=getIntent().getStringExtra("path");

        objectDetection(path);




    }

    private void objectDetection(final String path) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //InputStream inputStream = getResources().openRawResource(R.raw.exa);
                    Bitmap src= BitmapFactory.decodeFile(path);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] photoData = baos.toByteArray();;

                    //byte[] photoData = IOUtils.toByteArray(inputStream);

                    Image inputImage = new Image();
                    inputImage.encodeContent(photoData);

                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("LABEL_DETECTION");

                    AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Collections.singletonList(desiredFeature));

                    BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                    batchRequest.setRequests(Collections.singletonList(request));

                    BatchAnnotateImagesResponse batchResponse =
                            vision.images().annotate(batchRequest).execute();

                   /* final TextAnnotation text = batchResponse.getResponses()
                            .get(0).getFullTextAnnotation();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    text.getText(), Toast.LENGTH_LONG).show();
                        }
                    });*/
                   convertResponseToString(batchResponse);

                } catch(Exception e) {
                    Log.d("ERROR", e.getMessage());
                }
            }
        });
    }
    private void convertResponseToString(BatchAnnotateImagesResponse response) {


        StringBuilder message = new StringBuilder("I found these things:\n\n");
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {

                Log.d("score is"+label.getScore(), label.getDescription());
                String search = label.getDescription();
                String url = "https://www.amazon.in/s?k="+search;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;



            }
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();




        } else {
        }

    }

}
