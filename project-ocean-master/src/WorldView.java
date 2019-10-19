import processing.core.PApplet;
import processing.core.PImage;

import java.util.Optional;

/*
WorldView ideally mostly controls drawing the current part of the whole world
that we can see based on the viewport
*/

final class WorldView
{
   private PApplet screen;
   private WorldModel world;
   private int tileWidth;
   private int tileHeight;
   private Viewport viewport;

   public WorldView(int numRows, int numCols, PApplet screen, WorldModel world,
      int tileWidth, int tileHeight)
   {
      this.screen = screen;
      this.world = world;
      this.tileWidth = tileWidth;
      this.tileHeight = tileHeight;
      this.viewport = new Viewport(numRows, numCols);
   }

   public PApplet getScreen()
   {
      return screen;
   }
   public WorldModel getWorld()
   {
      return world;
   }
   public int getTileWidth()
   {
      return tileWidth;
   }
   public int getTileHeight()
   {
      return tileHeight;
   }
   public Viewport getViewport()
   {
      return viewport;
   }

   public void shiftView(int colDelta, int rowDelta)
   {
      int newCol = Functions.clamp(getViewport().getCol() + colDelta, 0,
              this.world.getNumCols() - getViewport().getNumCols());
      int newRow = Functions.clamp(getViewport().getRow() + rowDelta, 0,
              getWorld().getNumRows() - getViewport().getNumRows());

      getViewport().shift(newCol, newRow);
   }

   public void drawBackground()
   {
      for (int row = 0; row < getViewport().getNumRows(); row++)
      {
         for (int col = 0; col < getViewport().getNumCols(); col++)
         {
            Point worldPoint = getViewport().viewportToWorld(col, row);
            Optional<PImage> image = getWorld().getBackgroundImage(worldPoint);
            if (image.isPresent())
            {
               getScreen().image(image.get(), col * getTileWidth(),
                       row * getTileHeight());
            }
         }
      }
   }

   public void drawEntities()
   {
      for (Entity entity : getWorld().getEntities())
      {
         Point pos = entity.getPosition();

         if (getViewport().contains(pos))
         {
            Point viewPoint = getViewport().worldToViewport(pos.getX(), pos.getY());
            getScreen().image(Functions.getCurrentImage(entity),
                    viewPoint.getX() * getTileWidth(), viewPoint.getY() * getTileHeight());
         }
      }
   }

   public void drawViewport()
   {
      drawBackground();
      drawEntities();
   }
}
