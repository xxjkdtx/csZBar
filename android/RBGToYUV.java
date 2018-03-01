package org.cloudsky.cordovaPlugins;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import net.sourceforge.zbar.Image;

/**
 * Created by Administrator on 2018/2/7 0007.
 */

public class RBGToYUV {

    private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        // 帧图片的像素大小
        final int frameSize = width * height;
        // ---YUV数据---
        int Y, U, V;
        // Y的index从0开始
        int yIndex = 0;
        // UV的index从frameSize开始
        int uvIndex = frameSize;

        // ---颜色数据---
        int a, R, G, B;
        //
        int argbIndex = 0;
        //

        // ---循环所有像素点，RGB转YUV---
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                // a is not used obviously
                a = (argb[argbIndex] & 0xff000000) >> 24;
                R = (argb[argbIndex] & 0xff0000) >> 16;
                G = (argb[argbIndex] & 0xff00) >> 8;
                B = (argb[argbIndex] & 0xff);
                //
                argbIndex++;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                //
                Y = Math.max(0, Math.min(Y, 255));
                U = Math.max(0, Math.min(U, 255));
                V = Math.max(0, Math.min(V, 255));

                // NV21 has a plane of Y and interleaved planes of VU each
                // sampled by a factor of 2
                // meaning for every 4 Y pixels there are 1 V and 1 U. Note the
                // sampling is every other
                // pixel AND every other scanline.
                // ---Y---
                yuv420sp[yIndex++] = (byte) Y;
                // ---UV---
                if ((j % 2 == 0) && (i % 2 == 0)) {
                    //
                    yuv420sp[uvIndex++] = (byte) V;
                    //
                    yuv420sp[uvIndex++] = (byte) U;
                }
            }
        }
    }




    /*
     * 获取位图的YUV数据
     */
    public static Image getYUVByBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        bitmap = zoomImg(bitmap,400);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] argb = new int[width * height];

        bitmap.getPixels(argb, 0, width, 0, 0, width, height);

// 这里我踩平了一个坑，数组长度要按如下格式写，其他同理
//方法亦可。网络上其他前辈写的=new byte[inputWidth
// * inputHeight*3/2]在某些情况下会出现数组越界异常
        byte[] yuv = new byte[width
                * height
                + ((width % 2 == 0 ? width : (width + 1)) * (height % 2 == 0 ? height
                : (height + 1))) / 2];
        encodeYUV420SP(yuv, argb, width, height);

        bitmap.recycle();

        Image barcode = new Image(width, height,"Y800");
        barcode.setData(yuv);
        return barcode;
    }

    public static Bitmap zoomImg(Bitmap bm, int maxLenth){
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scale;
        if(width>height){
            scale = ((float) maxLenth) / width;
        }else{
            scale = ((float) maxLenth) / height;
        }
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

}
