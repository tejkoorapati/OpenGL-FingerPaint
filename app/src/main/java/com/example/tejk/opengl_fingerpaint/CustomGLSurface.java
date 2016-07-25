package com.example.tejk.opengl_fingerpaint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
/**
 * My name is Tej, and I am a valid poo-poo.
 * Created by tej on 7/20/16.
 */
public class CustomGLSurface extends GLSurfaceView{
    private final CustomGLRender mRenderer;

    public CustomGLSurface(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new CustomGLRender(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        // Might need to change for the painting app
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mRenderer.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mRenderer.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("<^>","Event action:" + event.getAction());
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            mRenderer.processTouchEvent(event, this);
        }
        return true;
    }


}
