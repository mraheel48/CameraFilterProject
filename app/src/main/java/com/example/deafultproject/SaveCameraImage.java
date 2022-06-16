package com.example.deafultproject;

import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.util.Log;

import org.wysaid.myUtils.ImageUtil;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;


public class SaveCameraImage extends CamerafileUtil {


    public static String saveBitmap(Bitmap bmp) {
        String path = getPath();
        long currentTime = System.currentTimeMillis();
        String filename = path + "/" + currentTime + ".jpg";
        return saveBitmap(bmp, filename);
    }

    public static String saveBitmap(Bitmap bmp, String filename) {

        Log.e(LOG_TAG, "saving Bitmap : " + filename);

        try {
            FileOutputStream fileout = new FileOutputStream(filename);
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bufferOutStream);
            bufferOutStream.flush();
            bufferOutStream.close();

            if (bmp != null){
                bmp.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Log.i("Bitmap", filename + "saved!");
        return filename;
    }

    public static class FaceRects {
        public int numOfFaces; // 实际检测出的人脸数
        public FaceDetector.Face[] faces; // faces.length >= numOfFaces
    }

    public static ImageUtil.FaceRects findFaceByBitmap(Bitmap bmp) {
        return findFaceByBitmap(bmp, 1);
    }

    public static ImageUtil.FaceRects findFaceByBitmap(Bitmap bmp, int maxFaces) {

        if (bmp == null) {

            Log.e(LOG_TAG, "Invalid Bitmap for Face Detection!");
            return null;
        }

        Bitmap newBitmap = bmp;

        if (newBitmap.getConfig() != Bitmap.Config.RGB_565) {
            newBitmap = newBitmap.copy(Bitmap.Config.RGB_565, false);
        }

        ImageUtil.FaceRects rects = new ImageUtil.FaceRects();
        rects.faces = new FaceDetector.Face[maxFaces];

        try {
            FaceDetector detector = new FaceDetector(newBitmap.getWidth(), newBitmap.getHeight(), maxFaces);
            rects.numOfFaces = detector.findFaces(newBitmap, rects.faces);
        } catch (Exception e) {
            Log.e(LOG_TAG, "findFaceByBitmap error: " + e.getMessage());
            return null;
        }


        if (newBitmap != bmp) {
            newBitmap.recycle();
        }
        return rects;
    }


}
