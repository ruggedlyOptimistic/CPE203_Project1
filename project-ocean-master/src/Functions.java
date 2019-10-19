import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import processing.core.PImage;
import processing.core.PApplet;

/*
Functions - everything our virtual world is doing right now - is this a good design?
 */

final class Functions
{
   public static final Random rand = new Random();
   public static final int COLOR_MASK = 0xffffff;

   public static boolean adjacent(Point p1, Point p2)
   {
      return (p1.getX() == p2.getX() && Math.abs(p1.getY() - p2.getY()) == 1) ||
         (p1.getY() == p2.getY() && Math.abs(p1.getX() - p2.getX()) == 1);
   }
   /*
     Called with color for which alpha should be set and alpha value.
     setAlpha(img, color(255, 255, 255), 0));
   */

   public static void setAlpha(PImage img, int maskColor, int alpha)
   {
      int alphaValue = alpha << 24;
      int nonAlpha = maskColor & COLOR_MASK;
      img.format = PApplet.ARGB;
      img.loadPixels();
      for (int i = 0; i < img.pixels.length; i++)
      {
         if ((img.pixels[i] & COLOR_MASK) == nonAlpha)
         {
            img.pixels[i] = alphaValue | nonAlpha;
         }
      }
      img.updatePixels();
   }

   public static int distanceSquared(Point p1, Point p2)
   {
      int deltaX = p1.getX() - p2.getX();
      int deltaY = p1.getY() - p2.getY();

      return deltaX * deltaX + deltaY * deltaY;
   }

   public static int clamp(int value, int low, int high) // maybe leave in functions
   {
      return Math.min(high, Math.max(value, low));
   }

   public static PImage getCurrentImage(Object entity)
   {
      if (entity instanceof Background)
      {
         return ((Background)entity).getImages()
                 .get(((Background)entity).getImageIndex());
      }
      else if (entity instanceof Entity)
      {
         return ((Entity)entity).getImages().get(((Entity)entity).getImageIndex());
      }
      else
      {
         throw new UnsupportedOperationException(
                 String.format("getCurrentImage not supported for %s",
                         entity));
      }
   }
}
