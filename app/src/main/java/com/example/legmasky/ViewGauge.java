package com.example.legmasky;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class ViewGauge extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF = new RectF();
    private float progress = 0.6f; // da 0.0 a 1.0
    private int progressColor = Color.parseColor("#5B9BF0");

    public ViewGauge(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(12f);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        backgroundPaint.setColor(Color.parseColor("#33FFFFFF"));

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(12f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(progressColor);
    }

    public void setProgress(float progress, int color) {
        this.progress = progress;
        this.progressColor = color;
        this.progressPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float padding = 10f;
        rectF.set(padding, padding, getWidth() - padding, getHeight() - padding);

        // Disegna l'arco di sfondo (dal basso sinistra in senso orario)
        canvas.drawArc(rectF, 135, 270, false, backgroundPaint);
        // Disegna l'arco del valore
        canvas.drawArc(rectF, 135, 270 * progress, false, progressPaint);
    }
}