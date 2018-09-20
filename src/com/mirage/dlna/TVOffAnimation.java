package com.mirage.dlna;

 import android.graphics.Matrix;  
 import android.view.animation.AccelerateDecelerateInterpolator;  
 import android.view.animation.Animation;  
 import android.view.animation.Transformation;  
   
 public class TVOffAnimation extends Animation {  
     /** 中心点X坐标 **/  
     private int centerX = 0;  
     /** 中心点Y坐标 **/  
     private int centerY = 0;  
     
     private boolean fillafter = true;
     
     TVOffAnimation(boolean fillafter)
     {
    	 this.fillafter = fillafter;
     }
     @Override  
     public void initialize(int width, int height, int parentWidth,  
             int parentHeight) {  
         // void setDuration (long durationMillis)  
         // Since: API Level 1 How long this animation should last.  
         // The duration cannot be negative.  
         setDuration(500);  
   
         // void setFillAfter(boolean fillAfter)  
         // If fillAfter is true, the transformation that this animation  
         // performed will persist when it is finished.  
         setFillAfter(fillafter);  
   
         // 获得图片中心点X坐标  
         centerX = width / 2;  
         // 获得图片中心点Y坐标  
         centerY = height / 2;  
   
         // void setInterpolator (Interpolator i)  
         // Since: API Level 1 Sets the acceleration curve for this animation.  
         // Defaults to a linear interpolation.  
         // setInterpolator(new AccelerateDecelerateInterpolator())  
         // 选择一个速度的效果  
         // AccelerateDecelerateInterpolator  
         // An interpolator where the rate of change starts and ends slowly  
         // but accelerates through the middle.  
         setInterpolator(new AccelerateDecelerateInterpolator());  
     }  
   
     /** 
      * preScale(float sx, float sy, float px, float py)  
      * px 和  py 是固定点， 
      * 例如 px,py=0,0 的话， 
      * 图像会以左上角为基点，向右向下放大缩小。 
      *  
      * 如果是图中心的话，图像便会以图中心为基点， 
      * 向上下左右等比例的放大缩小。 
      *  
      * 一般情况下，如果图像的内部座标不重要的话， 
      * 只用preScale(sy, sy)就可以了。 
      * 要用到px,py的情况，通常是前后还有牵涉Animation的运作。 
      *  
      * 简单讲，放大比例不会改变，都是按sx和sy决定。 
      * 只是px,py那点，在放大前和放大后都会不变。 
      *  
      * 例如: 一个(width)20 (height)10的长方形， 
      * 左上角座标是(0,0)。那右下角是(20,10)。 
      * 如果sx,sy=2,2  即放大两倍。 
      * 当px ,py=0,0放大后，左上角仍然是(0,0)，但右下角会变成(40,20)。 
      *  
      * 但同样是sx,sy=2,2，但px,py=10,5的话， 
      * 放大后，左上角会是(-10,-5)而右下角会是(30,15)。 
      * 唯一座标不变的就只有10,5 那点。但长方形仍然会放大两倍。 
      *  
      * 看上去没有什么不同，但如果用上Animation的话， 
      * 因为Animation对座标是有要求，所以效果也会有不同。 
      *  
      *  
      * interpolatedTime 表示的是当前动画的间隔时间 范围是0-1 
      *  
      * 那么横向来讲前80%的时间我们要横向拉伸到150%， 
      * 变化的速率为 0.5 / 0.8 = 0.625  
      * 所以横向缩放值为 1 + 0.625f * interpolatedTime 
      *  
      * 纵向在前80%的时间是直接减小，最后只留一条高度为0.01f的线。  
      * 变化的速率为 1 / 0.8 = 1.25  
      * 所以纵向缩放值为 1 - 1.25f * interpolatedTime + 0.01f 
      * 当然也可以写成 1 - interpolatedTime / 0.8f + 0.01f 
      *  
      * 后20%的时间里我们要横向从150%压缩至0%，  
      * 变化的速率为 1.5 / 0.2 = 7.5  
      * 所以横向缩放值为 7.5f * (1 - interpolatedTime) 
      *  
      * 纵向保持不变就好了，当横向为0的时候就全部消失了。 
      */  
     @Override  
     protected void applyTransformation(float interpolatedTime, Transformation t) {  
         final Matrix matrix = t.getMatrix();  
         if (interpolatedTime < 0.8) {  
             matrix.preScale(1 + 0.625f * interpolatedTime,  
                     1 - 1.25f * interpolatedTime + 0.01f, centerX, centerY);  
         } else {  
             matrix.preScale(7.5f * (1 - interpolatedTime), 0.01f,   
                     centerX, centerY);  
         }  
     }  
 }  