package com.app.fitness24;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.app.fitness24.adapters.MealAdapter;
import com.app.fitness24.models.Meal;
import com.app.fitness24.models.Profile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MealsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton actionButton;
    MealAdapter adapter;
    List<Meal> list;


    final int REQUEST_CAMERA_PERMISSION_KEY = 1;

    String[] PERMISSIONS =
            {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
    int REQUEST_CAMERA = 1;

    String imageFilePath = "", TAG = "theH";

    Uri filePath;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meals);

        recyclerView = findViewById(R.id.recycler_view);
        actionButton = findViewById(R.id.fab);

        list = new ArrayList<>();


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("meals").child(mAuth.getUid());

        Log.d(TAG, "onCreate: "+myRef.toString());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot.getValue());
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();

                Log.d(TAG, "onDataChange: getChildrenCount "+dataSnapshot.getChildrenCount());
                list.clear();
                for (DataSnapshot data : snapshotIterator){
                    Meal meal = data.getValue(Meal.class);
                    list.add(meal);
                }


                adapter = new MealAdapter(MealsActivity.this, list);
                recyclerView.setLayoutManager(new GridLayoutManager(MealsActivity.this, 2));
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasPermissions(MealsActivity.this, PERMISSIONS)) {
                    takePhotoFromCamera();
                } else {
                    ActivityCompat.requestPermissions(MealsActivity.this, PERMISSIONS, REQUEST_CAMERA_PERMISSION_KEY);
                }
            }
        });
    }

    private void uploadImage() {
//        filePath = Uri.parse(imageFilePath);
        Log.d(TAG, "uploadImage: imageFilePath: " + imageFilePath);
        filePath = Uri.fromFile(new File(imageFilePath));
        Log.d(TAG, "uploadImage: filePath: " + filePath.toString());

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child(UUID.randomUUID().toString());
            Log.d(TAG, "uploadImage: getPath: " + ref.getPath() + " getDownloadUrl: " + ref.getStorage().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    myRef.child(UUID.randomUUID().toString()).setValue(new Meal(getCurrentDate(),imageUrl));
                                    Log.d(TAG, "onSuccess: imageUrl: " + imageUrl);
                                    //createNewPost(imageUrl);
                                }
                            });

                            progressDialog.dismiss();
                            Toast.makeText(MealsActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.d(TAG, "onFailure: " + e.getMessage());
                            Toast.makeText(MealsActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION_KEY) {
            for (int i = 0; i < grantResults.length; i++) {
                Log.d(TAG, "onRequestPermissionsResult: Perm " + i + (grantResults[i] == PackageManager.PERMISSION_GRANTED));
            }

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePhotoFromCamera();
                Log.d(TAG, "onRequestPermissionsResult: Granted");
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Not granted");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: CAMERA_1");
            Log.d(TAG, "onActivityResult: imageFilePath: " + imageFilePath);

            String temp = imageFilePath;
            imageFilePath = compressImage(imageFilePath);
            deleteImage(temp);
            Bitmap reducedSizeBitmap = getBitmap(imageFilePath);
            Log.d(TAG, "onActivityResult: compressImage: imageFilePath: " + imageFilePath);

            if (reducedSizeBitmap != null) {

                uploadImage();

            } else {
                Log.d(TAG, "onActivityResult: Error while reducedSizeBitmap");
            }

        }
    }

    private File createImageFile() throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
        String imageFileName = "IMG_" + dateFormat.format(new Date());

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        storageDir.mkdir();
        imageFilePath = image.getAbsolutePath();
        Log.d(TAG, "createImageFile :imageFilePath: " + imageFilePath);
        return image;
    }

    private void takePhotoFromCamera() {
        imageFilePath = "";
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                Log.d(TAG, "takePhotoFromCamera: :IOException: " + ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(MealsActivity.this, "com.app.fitness24.provider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }
        }
    }

    private Bitmap getBitmap(String path) {

        Log.d(TAG, "getBitmap: " + path);
        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d(TAG, "getBitmap: scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
//                Log.d(TAG, "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            return b;
        } catch (IOException e) {
            Log.d(TAG, "getBitmap: IOException: " + e.getMessage(), e);
            return null;
        }
    }

    public String compressImage(String imageUri) {
        Log.d(TAG, "compressImage: imageUri: " + imageUri);
        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            Log.d(TAG, "compressImage: OutOfMemoryError: " + exception);
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            Log.d(TAG, "compressImage: OutOfMemoryError: " + exception);
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d(TAG, "compressImage: Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d(TAG, "compressImage:Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d(TAG, "compressImage: Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d(TAG, "compressImage: Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            Log.d(TAG, "compressImage: IOException: " + e);
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        Log.d(TAG, "compressImage: compressed" + filename);
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            Log.d(TAG, "compressImage: FileNotFoundException: " + e);
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
        String imageFileName = "IMG_" + dateFormat.format(new Date());
        String uriSting = (file.getAbsolutePath() + "/" + imageFileName + ".jpg");


        return uriSting;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//            todo add new line
//            cursor.close();
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public void deleteImage(String path) {
        File fdelete = new File(path);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.e(TAG, "file Deleted :" + path);
                callBroadCast();
            } else {
                Log.e(TAG, "file not Deleted :" + path);
            }
        }
    }

    public void callBroadCast() {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", " >= 14");
            MediaScannerConnection.scanFile(MealsActivity.this,
                    new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.e(TAG, "Scanned " + path + ":");
                            Log.e(TAG, "-> uri=" + uri);
                        }
                    });
        } else {
            Log.e(TAG, " < 14");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath())));
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {

        Log.d("theH", "hasPermissions: permission size= " + permissions.length);

        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                continue;
            } else {
                Log.d("theH", "hasPermissions: returning false");
                return false;
            }
        }
        Log.d("theH", "hasPermissions: returning true");
        return true;
    }
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDate = sdf.format(new Date());
        return currentDate;
    }
}