package com.maxigundan.maxinenga2015;

import java.io.File;

import android.os.Bundle;
import android.util.Log;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.MovieTextureStatus;
import com.metaio.sdk.jni.EPLAYBACK_STATUS;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.tools.io.AssetsManager;

public class Template extends ARViewActivity
{

    private IGeometry mMoviePlane;
    private MetaioSDKCallbackHandler mSDKCallback;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mMoviePlane = null;

        mSDKCallback = new MetaioSDKCallbackHandler();

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mSDKCallback.delete();
        mSDKCallback = null;
    }

    @Override
    protected int getGUILayout()
    {
        // Attaching layout to the activity
        return R.layout.template;
    }

    @Override
    protected void loadContents()
    {
        try
        {

            // Getting a file path for tracking configuration XML file
            final File trackingConfigFile = AssetsManager.getAssetPathAsFile(getApplicationContext(), "TrackingData_MarkerlessFast.xml");

            // Assigning tracking configuration
            boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
            MetaioDebug.log("Tracking data loaded: " + result);


            // Loading movie geometry
            final File moviePath =
                    AssetsManager.getAssetPathAsFile(getApplicationContext(), "movie.3g2");
            if (moviePath != null)
            {
                mMoviePlane = metaioSDK.createGeometryFromMovie(moviePath, false);
                if (mMoviePlane != null)
                {
                    mMoviePlane.setScale(1.6f);
                    MetaioDebug.log("Loaded geometry " + moviePath);
                }
                else
                {
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: " + moviePath);
                }
            }

        }
        catch (Exception e)
        {
            MetaioDebug.log(Log.ERROR, "Failed to load content: " + e);
        }
    }


    @Override
    protected void onGeometryTouched(IGeometry geometry)
    {
        MetaioDebug.log("Template.onGeometryTouched: " + geometry);

        if (geometry.equals(mMoviePlane))
        {
            final MovieTextureStatus status = mMoviePlane.getMovieTextureStatus();
            if (status.getPlaybackStatus() == EPLAYBACK_STATUS.EPLAYBACK_STATUS_PLAYING)
            {
                mMoviePlane.pauseMovieTexture();
            }
            else
            {
                mMoviePlane.startMovieTexture(true);
            }
        }

    }


    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
    {
        return mSDKCallback;
    }

    final class MetaioSDKCallbackHandler extends IMetaioSDKCallback
    {

        @Override
        public void onSDKReady()
        {
            MetaioDebug.log("The SDK is ready");
        }

        @Override
        public void onAnimationEnd(IGeometry geometry, String animationName)
        {
            MetaioDebug.log("animation ended" + animationName);
        }

        @Override
        public void onMovieEnd(IGeometry geometry, File filePath)
        {
            MetaioDebug.log("movie ended" + filePath.getPath());
        }

        @Override
        public void onNewCameraFrame(ImageStruct cameraFrame)
        {
            MetaioDebug.log("a new camera frame image is delivered" + cameraFrame.getTimestamp());
        }

        @Override
        public void onCameraImageSaved(File filePath)
        {
            MetaioDebug.log("a new camera frame image is saved to" + filePath.getPath());
        }

        @Override
        public void onScreenshotImage(ImageStruct image)
        {
            MetaioDebug.log("screenshot image is received" + image.getTimestamp());
        }

        @Override
        public void onScreenshotSaved(File filePath)
        {
            MetaioDebug.log("screenshot image is saved to" + filePath.getPath());
        }

        @Override
        public void onTrackingEvent(TrackingValuesVector trackingValues)
        {
            for (int i=0; i<trackingValues.size(); i++)
            {
                final TrackingValues v = trackingValues.get(i);
                MetaioDebug.log("Tracking state for COS "+v.getCoordinateSystemID()+" is "+v.getState());
            }			// We only have one COS, so there can only ever be one TrackingValues structure passed.
            // Play movie if the movie button was selected and we're currently tracking.
            if (trackingValues.isEmpty() || !trackingValues.get(0).isTrackingState())
            {
                if (mMoviePlane != null)
                {
                    mMoviePlane.pauseMovieTexture();
                }
            }
            else
            {
                if (mMoviePlane != null)
                {
                    mMoviePlane.startMovieTexture(true);
                }
            }
        }

        @Override
        public void onInstantTrackingEvent(boolean success, File filePath)
        {
            if (success)
            {
                MetaioDebug.log("Instant 3D tracking is successful");
            }
        }
    }
}
