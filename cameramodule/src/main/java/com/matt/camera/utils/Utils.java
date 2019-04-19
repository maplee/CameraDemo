package com.matt.camera.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author:Created by jiaguofeng on 2019/3/18.
 * Email:
 */
public class Utils {

    private static final String TAG = "Utils";

    public static String getWorkingDirectory() {
        final File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(pictures, "face");
        if (!file.exists()) {
            file.mkdir();
        } else if (file.isFile()) {
            Log.w(TAG, file.getPath() + " is occupied");
            return pictures.getPath();
        }

        return file.getPath();
    }


    public static Bitmap compressBitmap(Context context, Uri uri, int targetWidth, int targetHeight) {
        InputStream input = null;
        Bitmap bitmap = null;

        try {
            input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, (Rect) null, options);
            input.close();
            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;
            if (originalWidth != -1 && originalHeight != -1) {
                boolean be1 = true;
                int widthBe = 1;
                if (originalWidth > targetWidth) {
                    widthBe = originalWidth / targetWidth;
                }

                int heightBe = 1;
                if (originalHeight > targetHeight) {
                    heightBe = originalHeight / targetHeight;
                }

                int be2 = widthBe > heightBe ? heightBe : widthBe;
                if (be2 <= 0) {
                    be2 = 1;
                }

                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inDither = false;
                options.inMutable = true;
                options.inPremultiplied = false;
                options.inJustDecodeBounds = false;
                options.inSampleSize = be2;
                input = context.getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(input, (Rect) null, options);
                input.close();
                input = null;
            } else {
                Object be = null;
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }

            return bitmap;
        }
    }

    /**
     * @param bmp     获取的bitmap数据
     * @param picName 自定义的图片名
     */
    public static void saveBmp2Gallery(final Context context, final Bitmap bmp, final String picName) {
        String fileName = null;
        //系统相册目录
        String galleryPath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;


        // 声明文件对象
        File file = null;
        // 声明输出流
        FileOutputStream outStream = null;

        try {
            // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
            file = new File(galleryPath, picName + ".jpg");

            // 获得文件相对路径
            fileName = file.toString();
            // 获得输出流，如果文件中有内容，追加内容
            outStream = new FileOutputStream(fileName);
            if (null != outStream) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            }

        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //通知相册更新
        MediaStore.Images.Media.insertImage(context.getContentResolver(),
                bmp, fileName, null);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);


    }


    public static String getBitmapDir(){
        return Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;
    }
}
