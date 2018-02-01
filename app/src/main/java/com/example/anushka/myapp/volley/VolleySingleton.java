package com.example.anushka.myapp.volley;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


public class VolleySingleton
{

    private static VolleySingleton singleton;
    private RequestQueue mQueue;
    private ImageLoader mImageLoader;

    private VolleySingleton()
    {
        mQueue= Volley.newRequestQueue(MyApplication.getAppContext());
        mImageLoader=new ImageLoader(mQueue
                ,
        new ImageLoader.ImageCache()
        {
            LruCache<String , Bitmap> cache=new LruCache<>((int) Runtime.getRuntime().maxMemory()/1024/8);
            @Override
            public Bitmap getBitmap(String url)
            {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap)
            {
                cache.put(url, bitmap);
            }
        });
    }

    public static VolleySingleton getInstance()
    {
        if(singleton==null)
        {
            singleton=new VolleySingleton();
        }

        return singleton;
    }

    public RequestQueue getQueue()
    {
        return mQueue;
    }


    public ImageLoader getmImageLoader()
    {
        return mImageLoader;
    }

}
