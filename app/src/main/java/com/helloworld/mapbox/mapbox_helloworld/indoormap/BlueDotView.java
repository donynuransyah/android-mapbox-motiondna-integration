package com.helloworld.mapbox.mapbox_helloworld.indoormap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import com.helloworld.mapbox.mapbox_helloworld.SmoothEstimate;
import com.helloworld.mapbox.mapbox_helloworld.imagelibrary.SubsamplingScaleImageView;

/**
 * Extends great ImageView library by Dave Morrissey. See more:
 * https://github.com/davemorrissey/subsampling-scale-image-view.
 */
public class BlueDotView extends SubsamplingScaleImageView implements ScaleGestureDetector.OnScaleGestureListener {

    private Context mContext;
    private float uncertaintyRadius = 15f;
    private float dotRadius = 1.0f;
    private PointF dotCenter = null;
    private double heading = -1.0;
    private SmoothEstimate smoothEstimate = new SmoothEstimate();
    Paint bluedot = new Paint();
    private double METER;


    public void setUncertaintyRadius(float uncertaintyRadius) {
        this.uncertaintyRadius = uncertaintyRadius;
    }

    public void setDotRadius(float dotRadius) {
        this.dotRadius = dotRadius;
    }

    public void setDotCenter(PointF dotCenter) {
        this.dotCenter = dotCenter;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public BlueDotView(Context context) {
        this(context, null);
        mContext = context;
    }

    public BlueDotView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    private void initialise() {
        Log.d("BluedotView", "Initialize");
        setWillNotDraw(false);
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);

        bluedot.setAntiAlias(true);
        bluedot.setStyle(Paint.Style.FILL);
        bluedot.setColor(Color.BLUE);
    }

    public void setInitializeDot(PointF pointF) {
        this.dotCenter = pointF;
        postInvalidate();
    }

    public void moveDot(PointF pointF) {
        this.dotCenter = pointF;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e(getClass().getName(), "canvas " + canvas.getWidth() + ":" + canvas.getHeight());
        if (!isReady()) {
            return;
        }

        Log.e(getClass().getName(), "dot null" + dotCenter.x + ":" + dotCenter.y);

        METER = 0.0002645833;

        if (dotCenter != null) {
            Log.e(getClass().getName(), "dot not null" + dotCenter.x + ":" + dotCenter.y);
            // Update smooth estimate
            smoothEstimate.update(dotCenter.x, dotCenter.y, (float) ((heading) / 180.0 * Math.PI), // Map degrees to radians
                    uncertaintyRadius,
                    System.currentTimeMillis());

            PointF vPoint = sourceToViewCoord(smoothEstimate.getX(), smoothEstimate.getY());
            vPoint.x = 500F;
            vPoint.y = 500F;
            // Paint uncertainty circle
            float scaledUncertaintyRadius = getScale() * smoothEstimate.getRadius();
            bluedot.setAlpha(30);
            Log.e(getClass().getName(), "dot vpoint " + vPoint.x + ":" + vPoint.y + ":" + scaledUncertaintyRadius);
            canvas.drawCircle(vPoint.x, vPoint.y, scaledUncertaintyRadius, bluedot);
            // Paint center point
            float scaledDotRadius = getScale() * dotRadius;
            bluedot.setAlpha(90);
            canvas.drawCircle(vPoint.x, vPoint.y, scaledDotRadius, bluedot);

            // Paint heading triangle if available
            if (heading != -1.0) {
                bluedot.setAlpha(255);
                Path triangle = headingTriangle(vPoint.x, vPoint.y,
                        // Note: Rotate up-pointing angle to right (for unit circle)
                        smoothEstimate.getHeading() - (float) Math.PI / 2,
                        scaledDotRadius);
                canvas.drawPath(triangle, bluedot);
            }
        }
        postInvalidate();
    }

    /**
     * Trigonometric (unit circle) computation of the heading arrow triangle
     *
     * @param x X coordinate of the estimate (circle) center
     * @param y Y coordinate of the estimate (circle) center
     * @param a Heading angle in radians (zero pointing right)
     * @param r Radius of the estimate circle
     * @return Path representing the heading triangle
     */
    private static Path headingTriangle(float x, float y, float a, float r) {
        float x1 = (float) (x + 0.9 * r * Math.cos(a));
        float y1 = (float) (y + 0.9 * r * Math.sin(a));
        float x2 = (float) (x + 0.2 * r * Math.cos(a + 0.5 * Math.PI));
        float y2 = (float) (y + 0.2 * r * Math.sin(a + 0.5 * Math.PI));
        float x3 = (float) (x + 0.2 * r * Math.cos(a - 0.5 * Math.PI));
        float y3 = (float) (y + 0.2 * r * Math.sin(a - 0.5 * Math.PI));

        Path triangle = new Path();
        triangle.moveTo(x1, y1);
        triangle.lineTo(x2, y2);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x1, y1);

        return triangle;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Toast.makeText(mContext, "Scaling onScale", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Toast.makeText(mContext, "Scaling onScaleBegin", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        Toast.makeText(mContext, "Scaling onScaleEnd", Toast.LENGTH_SHORT).show();
    }
}
