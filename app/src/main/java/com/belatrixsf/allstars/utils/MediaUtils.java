package com.belatrixsf.allstars.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by PedroCarrillo on 5/24/16.
 */
public class MediaUtils {

    public static final String JPEG_FILE_SUFFIX = ".jpg";

    private static MediaUtils ourInstance = new MediaUtils();

    public static MediaUtils get() {
        return ourInstance;
    }

    private MediaUtils() {
    }

    private Bitmap getResizedBitmap(String imagePath){

        int targetW = 200;
        int targetH = 200;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        bmOptions.inScaled = false;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        int degree = getRotationFromImageFile(imagePath);

        if (degree>0){
            Matrix matrix = new Matrix();
            if(bitmap.getWidth()>bitmap.getHeight()){
                matrix.setRotate(degree);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        }

        return bitmap;
    }

    private int getRotationFromImageFile(String photoPath){
        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int degree = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    degree = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    degree = 0;
                    break;
                default:
                    degree = 90;
            }

            return degree;
        } catch (IOException e){
            e.printStackTrace();
            return 0;
        }
    }

    public File getReducedBitmapFile(Uri uri) {
        File file = new File(uri.getPath());
        return getReducedBitmapFile(file.getAbsolutePath(), file.getName());
    }

    public File getReducedBitmapFile(String imagePath, String fileName){
        Bitmap reducedBitmap = getResizedBitmap(imagePath);

        int degree = getRotationFromImageFile(imagePath);

        Matrix matrix = new Matrix();
        matrix.setRotate(degree);
        reducedBitmap = Bitmap.createBitmap(reducedBitmap, 0, 0, reducedBitmap.getWidth(), reducedBitmap.getHeight(), matrix, true);

        return createFileFromBitmap(reducedBitmap, fileName);
    }

    protected File createFileFromBitmap(Bitmap bitmap, String imageName){
        File albumF = getAlbumDir();
        try {
            File imageFile = File.createTempFile(imageName, JPEG_FILE_SUFFIX, albumF);
            OutputStream fOut = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            return imageFile;
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public File createLocalImage(String fileName) throws IOException {
        File storageDir = getAlbumDir();
        File image = File.createTempFile(
                fileName,  /* prefix */
                JPEG_FILE_SUFFIX,         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    public File getAlbumDir() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
    }

    /**
     * Specific methods for Profile Picture Media Handling
     */

    public File getReducedProfilePictureBitmapFile(String imagePath) {
        return getReducedBitmapFile(imagePath,  getLocalProfileFileName());
    }

    public File createLocalProfilePicture() throws IOException {
        return createLocalImage(getLocalProfileFileName());
    }

    public String getLocalProfileFileName() {
        return "allstars_profile_picture";
    }

    /**
     *  Methods to obtain files paths
     */

    public String getFilePathFromMediaUri(Context context, Uri uri) {
        if ( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(
                uri, projection, null, null, null);
        String path = "";
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            path = cursor.getString(columnIndex);
            cursor.close();
        }
        return path;
    }

}