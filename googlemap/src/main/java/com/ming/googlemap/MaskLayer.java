package com.ming.googlemap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * 类描述:离线地图下载时遮罩层
 * 创建人: sunming
 * 创建时间：2019/9/2 10:19
 * version：1.0
 * Email:sunming@alinktech.com
 */
public class MaskLayer extends View {
    private Paint paint;
    private Rect rect;

    public MaskLayer(Context context) {
        this(context, null);
    }

    public MaskLayer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskLayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MaskLayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        paint = new Paint();
        paint.setColor(OsmApplication.getInstance().getResources().getColor(R.color.half_transparent));
        paint.setFlags(Canvas.ALL_SAVE_FLAG);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 控件在屏幕上的位置的矩阵Rect(left,top,right,bottom)
//        int measuredWidth = getMeasuredWidth();
//        int measuredHeight = getMeasuredHeight();
//        int fourHalfWidth = measuredWidth / 4;
//        int halfHeight = measuredHeight / 2;
//        rect = new Rect(fourHalfWidth, halfHeight - fourHalfWidth, fourHalfWidth * 3, halfHeight + fourHalfWidth);
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        rect = new Rect(100, 230, dm.widthPixels - 100, dm.heightPixels - 330);

        int canvasWidth = getWidth();
        int canvasHeight = getHeight();
        //绘制背景
        canvas.saveLayer(0, 0, canvasWidth, canvasHeight, paint);
        canvas.drawRect(0, 0, canvasWidth, canvasHeight, paint);
        //挖洞
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(rect, paint);
        paint.setXfermode(null);
    }

    public List<Point> getPoints() {
        List<Point> pointList = new ArrayList<>();
        if (rect != null) {
            int left = rect.left;
            int top = rect.top;
            int right = rect.right;
            int bottom = rect.bottom;
            // 左下角(left,bottom)
//            pointList.add(new Point(left, bottom));
//            // 左上角(left,top)
            pointList.add(new Point(left, top));
//            //右上角(right,top)
//            pointList.add(new Point(right, top));
            //右下角(right,bottom)
            pointList.add(new Point(right, bottom));
        }
        return pointList;
    }
}
