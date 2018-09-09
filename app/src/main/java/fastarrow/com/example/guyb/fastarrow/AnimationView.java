package fastarrow.com.example.guyb.fastarrow;

/**
 * Created by guyb on 03/09/18.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class AnimationView extends View {

    Paint paint;

    Bitmap bm;
    int bm_offsetX, bm_offsetY;

    Path animPath;
    PathMeasure pathMeasure;
    float pathLength;

    float velocity;            //distance each velocity
    float distance;        //distance moved
    private float time;
    float curX, curY;

    float curAngle;        //current angle
    float targetAngle;    //target angle
    float stepAngle;    //angle each velocity

    float[] pos;
    float[] tan;

    Matrix matrix;

    Path touchPath;
    boolean stop_running = false;

    public AnimationView(Context context) {
        super(context);
        initMyView();
    }

    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMyView();
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMyView();
    }

    public static Bitmap scaleBitmap(Bitmap bitmapToScale, float newWidth, float newHeight) {
        if (bitmapToScale == null)
            return null;
//get the original width and height
        int width = bitmapToScale.getWidth();
        int height = bitmapToScale.getHeight();
// create a matrix for the manipulation
        Matrix matrix = new Matrix();

// resize the bit map
        matrix.postScale(newWidth / width, newHeight / height);

// recreate the new Bitmap and set it back
        return Bitmap.createBitmap(bitmapToScale, 0, 0, bitmapToScale.getWidth(), bitmapToScale.getHeight(), matrix, true);
    }

    public void initMyView() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);

        Bitmap orig_bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_name);
        bm = scaleBitmap(orig_bm, 500, 500);
                bm_offsetX = bm.getWidth() / 2;
        bm_offsetY = bm.getHeight() / 2;

        animPath = new Path();

        pos = new float[2];
        tan = new float[2];

        matrix = new Matrix();

        touchPath = new Path();
        stop_running = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (animPath.isEmpty()) {
            return;
        }

        canvas.drawPath(animPath, paint);

        matrix.reset();

        if ((targetAngle - curAngle) > stepAngle) {
            curAngle += stepAngle;
            matrix.postRotate(curAngle, bm_offsetX, bm_offsetY);
            matrix.postTranslate(curX, curY);
            canvas.drawBitmap(bm, matrix, null);

            invalidate();
        } else if ((curAngle - targetAngle) > stepAngle) {
            curAngle -= stepAngle;
            matrix.postRotate(curAngle, bm_offsetX, bm_offsetY);
            matrix.postTranslate(curX, curY);
            canvas.drawBitmap(bm, matrix, null);

            invalidate();
        } else {
            curAngle = targetAngle;
            if (distance < pathLength) {
                pathMeasure.getPosTan(distance, pos, tan);

                targetAngle = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
                matrix.postRotate(curAngle, bm_offsetX, bm_offsetY);

                curX = pos[0] - bm_offsetX;
                curY = pos[1] - bm_offsetY;
                matrix.postTranslate(curX, curY);

                canvas.drawBitmap(bm, matrix, null);

                distance += velocity;

                invalidate();
            } else {
                matrix.postRotate(curAngle, bm_offsetX, bm_offsetY);
                matrix.postTranslate(curX, curY);
                canvas.drawBitmap(bm, matrix, null);
                init_path();
                invalidate();
            }
        }

    }
    private float calculate_velocity(float length)
    {
        if (length < 2000)
        {
            return 200;
        }
        else if (length < 5000) {
            return 500;
        }
        else if (length < 10000){
            return 700;
        }
        else {
            return 1000;
        }
    }
    private void init_path() {
        Path tempAnimPath;
        PathMeasure tempPathMeasure;


        tempAnimPath = new Path(touchPath);

        tempPathMeasure = new PathMeasure(tempAnimPath, false);
        // If the path isn't too short, create it. Otherwise, keep the old path.
        if (tempPathMeasure.getLength() > 100)
        {
            animPath = new Path(touchPath);
            pathMeasure = new PathMeasure(tempAnimPath, false);
            pathLength = pathMeasure.getLength();
        }

        velocity = calculate_velocity(pathLength);
        distance = 0;
        curY = 0;

        stepAngle = 100;
        curAngle = 0;
        targetAngle = 0;
        Log.d("AnimationView", "distance:" + pathLength + " velocity:" + velocity);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchPath.reset();
                touchPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                touchPath.lineTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                touchPath.lineTo(event.getX(), event.getY());
                init_path();

                invalidate();

                break;

        }

        return true;
    }

}
