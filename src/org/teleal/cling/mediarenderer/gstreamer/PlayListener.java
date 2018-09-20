package org.teleal.cling.mediarenderer.gstreamer;

import android.media.MediaPlayer;

public class PlayListener {

	
	private static GstMediaPlayer gstMediaPlayer = null;
	private static MediaPlayer mediaPlayer = null;
	
	public static boolean imageShow = false;
	public static boolean audioShow = false;
//	public static void setPlayTarget(String target)
//	{
//		playTarget = target;
//	}
//	
//	public static String getPlayTarget()
//	{
//		return playTarget;
//	}


	
	public static void setGstMediaPlayer(GstMediaPlayer tempgstMediaPlayer)
	{
		gstMediaPlayer = tempgstMediaPlayer;
	}
	
	
	public static GstMediaPlayer getGstMediaPlayer()
	{
		return gstMediaPlayer;
	}
	
	
	
	public static void setMediaPlayer(MediaPlayer tempMediaPlayer)
	{
		mediaPlayer = tempMediaPlayer;
	}
	
	public static MediaPlayer getMediaPlayer()
	{
		return mediaPlayer;
	}
	
	
	
}
