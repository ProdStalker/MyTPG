package com.mytpg.engines.entities.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class VolleySingleton {
    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtxt;

    public static VolleySingleton getInstance() {
        return mInstance;
    }

    private VolleySingleton(Context argContext) {
        mCtxt = argContext;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache(){
                    private final LruCache<String, Bitmap> cache = new LruCache<>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return null;
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {

                    }
                });
    }

    public static synchronized VolleySingleton getInstance(Context argContext)
    {
        if (mInstance == null)
        {
            mInstance = new VolleySingleton(argContext);
        }

        return mInstance;
    }

    public RequestQueue getRequestQueue()
    {
        if (mRequestQueue == null)
        {
            mRequestQueue = Volley.newRequestQueue(mCtxt.getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> argReq){
        addToRequestQueue(argReq, false);
    }

    public <T> void addToRequestQueue(Request<T> argReq, boolean argWithCache)
    {
        argReq.setShouldCache(argWithCache);
        getRequestQueue().add(argReq);
    }

    public ImageLoader getImageLoader()
    {
        return mImageLoader;
    }
}
