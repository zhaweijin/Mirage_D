/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mirage.util;

import java.util.HashMap;
import java.util.Map;


import android.util.Log;


/**
 * Utilities for dealing with MIME types.
 * Used to implement java.net.URLConnection and android.webkit.MimeTypeMap.
 */
public final class MimetypeMap {
	private final static String TAG = "MimetypeMap";
	
    private static final Map<String, String> mimeTypeToExtensionMap = new HashMap<String, String>();
    private static final Map<String, String> extensionToMimeTypeMap = new HashMap<String, String>();
    
    private MimetypeMap() {
//    	Utils.print(TAG , "MimetypeMap...................111111");
    }
    
    static {
//    	Utils.print(TAG , "MimetypeMap...................222222");
        // The following table is based on /etc/mime.types data minus
        // chemical/* MIME types and MIME types that don't map to any
        // file extensions. We also exclude top-level domain names to
        // deal with cases like:
        //
        // mail.google.com/a/google.com
        //
        // and "active" MIME types (due to potential security issues).

        add("application/oda", "oda");
        add("application/ogg", "ogg");
        add("application/pdf", "pdf");
        add("application/x-dvi", "dvi");
        
        add("audio/3gpp", "3gpp");
        add("audio/amr", "amr");
        add("audio/basic", "snd");
        add("audio/midi", "mid");
//        add("audio/midi", "midi");
        add("audio/midi", "kar");
        add("audio/midi", "xmf");
        add("audio/mobile-xmf", "mxmf");
//        add("audio/mpeg", "mpga");
//        add("audio/mpeg", "mpega");
//        add("audio/mpeg", "mp2");
        add("audio/mpeg", "mp3");
        add("audio/mpeg", "m4a");
        add("audio/mpegurl", "m3u");
        add("audio/prs.sid", "sid");
        add("audio/x-aiff", "aif");
        add("audio/x-aiff", "aiff");
        add("audio/x-aiff", "aifc");
        add("audio/x-gsm", "gsm");
        add("audio/x-mpegurl", "m3u");
        add("audio/x-ms-wma", "wma");
        add("audio/x-ms-wax", "wax");
        add("audio/x-scpls", "pls");
        add("audio/x-sd2", "sd2");
        add("audio/x-wav", "wav");
        
        //add by shujun. maybe some repeat. but no time to check it
        add("audio/aiff" , "aif");
        add("audio/x-aiff" , "aif");
        add("audio/aiff" , "aifc");
        add("audio/x-aiff" , "aifc");
        add("audio/aiff" , "aiff");
        add("audio/x-aiff" , "aiff");
        add("audio/basic" , "au");
        add("audio/x-au" , "au");
        add("audio/make" , "funk");
        add("audio/x-gsm" , "gsd");
        add("audio/x-gsm" , "gsm");
        add("audio/it" , "it");
        add("audio/x-jam" , "jam");
        add("audio/midi" , "kar");
        add("audio/nspaudio" , "la");
        add("audio/x-nspaudio" , "la");
        add("audio/x-liveaudio" , "lam");
        add("audio/nspaudio" , "lma");
        add("audio/x-nspaudio" , "lma");
        add("audio/mpeg" , "m2a");
        add("audio/x-mpequrl" , "m3u");
        add("audio/midi" , "mid");
        add("audio/x-mid" , "mid");
        add("audio/x-midi" , "mid");
        add("audio/midi" , "midi");
        add("audio/x-mid" , "midi");
        add("audio/x-midi" , "midi");
        add("audio/x-vnd.audioexplosion.mjuicemediafile" , "mjf");
        add("audio/mod" , "mod");
        add("audio/x-mod" , "mod");

        add("audio/s3m" , "s3m");
        add("audio/x-psid" , "sid");
        add("audio/basic" , "snd");
        add("audio/x-adpcm" , "snd");
        add("audio/tsp-audio" , "tsi");
        add("audio/tsplayer" , "tsp");
        add("audio/voc" , "voc");
        add("audio/x-voc" , "voc");
        add("audio/voxware" , "vox");
        
        add("audio/wav" , "wav");
        add("audio/x-wav" , "wav");
        add("audio/xm" , "xm");
        //add("audio/ogg" , "oga");
        add("audio/ogg" , "ogg");
        //add("audio/ogg" , "spx");
        add("audio/flac" , "flac");
        
        add("image/bmp", "bmp");
        add("image/gif", "gif");
        add("image/ico", "cur");
        add("image/ico", "ico");
        add("image/ief", "ief");
        add("image/jpeg", "jpg");
        add("image/pcx", "pcx");
        add("image/png", "png");
        add("image/svg+xml", "svg");
        add("image/svg+xml", "svgz");
        add("image/tiff", "tiff");
        add("image/tiff", "tif");
        add("image/vnd.djvu", "djvu");
        add("image/vnd.djvu", "djv");
        add("image/vnd.wap.wbmp", "wbmp");
        add("image/x-cmu-raster", "ras");
        add("image/x-icon", "ico");
        add("image/x-jg", "art");
        add("image/x-jng", "jng");
        add("image/x-ms-bmp", "bmp");
        add("image/x-rgb", "rgb");
        add("image/x-xbitmap", "xbm");
        add("image/x-xpixmap", "xpm");
        add("image/x-xwindowdump", "xwd");

        add("image/x-jg" , "art");
//      add("image/bmp" , "bm");
	  add("image/bmp" , "bmp");
	  add("image/x-windows-bmp" , "bmp");
	  add("image/vnd.dwg" , "dwg");
	  add("image/x-dwg" , "dwg");
	  add("image/vnd.dwg" , "dxf");
	  add("image/x-dwg" , "dxf");
	  add("image/fif" , "fif");
	  add("image/florian" , "flo");
	  add("image/vnd.fpx" , "fpx");
	  add("image/vnd.net-fpx" , "fpx");
	  add("image/g3fax" , "g3");
	  add("image/gif" , "gif");
	  add("image/x-icon" , "ico");
	  add("image/ief" , "ief");
	  add("image/ief" , "iefs");
	  add("image/jpeg" , "jpg");
	  add("image/pjpeg" , "jpg");
	  add("image/vnd.wap.wbmp" , "wbmp");
	  add("image/x-xbitmap" , "xbm");
	  add("image/x-xbm" , "xbm");
	  add("image/xbm" , "xbm");
	  add("image/vnd.xiff" , "xif");
	  add("image/x-xpixmap" , "xpm");
	  add("image/xpm" , "xpm");
	  add("image/png" , "x-png");
	  add("image/x-xwd" , "xwd");
	  add("image/x-xwindowdump" , "xwd");

        add("video/3gpp", "3gpp");
        add("video/3gpp", "3gp");
        add("video/3gpp", "3g2");
        add("video/dl", "dl");
        add("video/dv", "dif");
        add("video/dv", "dv");
        add("video/fli", "fli");
        add("video/flv", "flv");
        add("video/m4v", "m4v");
        add("video/mpeg", "mpeg");
        add("video/mpeg", "mpg");
        add("video/mpeg", "mpe");
        add("video/mp4", "mp4");
        add("video/mpeg", "VOB");
        //add("video/quicktime", "qt");
        add("video/quicktime", "mov");
        add("video/vnd.mpegurl", "mxu");
        add("video/x-la-asf", "lsf");
        add("video/x-la-asf", "lsx");
        add("video/x-mng", "mng");
        add("video/x-ms-asf", "asf");
        add("video/x-ms-asf", "asx");
        add("video/x-ms-wm", "wm");
        add("video/x-ms-wmv", "wmv");
        add("video/x-ms-wmx", "wmx");
        add("video/x-ms-wvx", "wvx");
        add("video/x-msvideo", "avi");
        add("video/x-sgi-movie", "movie");
        
        add("video/animaflex" , "afl");
        add("video/x-ms-asf" , "asf");
        add("video/x-ms-asf" , "asx");
        add("video/x-ms-asf-plugin" , "asx");
        add("video/avi" , "avi");
        add("video/msvideo" , "avi");
        add("video/x-msvideo" , "avi");
        add("video/avs-video" , "avs");
        add("video/x-dv" , "dif");
        add("video/dl" , "dl");
        add("video/x-dl" , "dl");
        add("video/x-dv" , "dv");
        add("video/x-fli" , "fli");
        add("video/x-atomic3d-feature" , "fmf");
        add("video/gl" , "gl");
        add("video/x-gl" , "gl");
        add("video/x-isvideo" , "isu");
        add("video/mpeg" , "m1v");
        add("video/mpeg" , "m2v");
        add("video/x-motion-jpeg" , "mjpg");
        //add("video/quicktime" , "moov");
        //add("video/quicktime" , "mov");
        add("video/x-sgi-movie" , "movie");
        add("video/ogg" , "ogv");
        add("video/wtv" , "wtv");
        add("video/x-matroska","mkv");
        add("video/vnd.dlna.mpeg-tts","tts");
        
        add("video/x-flv", "flv");        
       
    }

    private static void add(String mimeType, String extension) {
        //
        // if we have an existing x --> y mapping, we do not want to
        // override it with another mapping x --> ?
        // this is mostly because of the way the mime-type map below
        // is constructed (if a mime type maps to several extensions
        // the first extension is considered the most popular and is
        // added first; we do not want to overwrite it later).
        //
        if (!mimeTypeToExtensionMap.containsKey(mimeType)) {
            mimeTypeToExtensionMap.put(mimeType, extension);
            extensionToMimeTypeMap.put(extension, mimeType);
        }
    }


    /**
     * Returns true if the given MIME type has an entry in the map.
     * @param mimeType A MIME type (i.e. text/plain)
     * @return True iff there is a mimeType entry in the map.
     */
    public static boolean hasMimeType(String mimeType) {
        if (mimeType == null || mimeType.length() ==0) {
            return false;
        }
        return mimeTypeToExtensionMap.containsKey(mimeType);
    }

    /**
     * Returns the MIME type for the given extension.
     * @param extension A file extension without the leading '.'
     * @return The MIME type for the given extension or null iff there is none.
     */
    public static String GetMimeType(String extension) {
        if (extension == null || extension.length() ==0) {
            return null;
        }
        return extensionToMimeTypeMap.get(extension);
    }


    /**
     * Returns the registered extension for the given MIME type. Note that some
     * MIME types map to multiple extensions. This call will return the most
     * common extension for the given MIME type.
     * @param mimeType A MIME type (i.e. text/plain)
     * @return The extension for the given MIME type or null iff there is none.
     */
    public static String GetExtension(String mimeType) {
        if (mimeType == null || mimeType.length() ==0) {
            return null;
        }
        return mimeTypeToExtensionMap.get(mimeType);
    }
}
