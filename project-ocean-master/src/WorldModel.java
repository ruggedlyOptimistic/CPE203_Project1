import processing.core.PImage;

import java.util.*;

/*
WorldModel ideally keeps track of the actual size of our grid world and what is in that world
in terms of entities and background elements
 */

final class WorldModel
{
   private int numRows;
   private int numCols;
   private Background background[][];
   private Entity occupancy[][];
   private Set<Entity> entities;

   // Migrated from Functions

    public final int FISH_REACH = 1;

   public WorldModel(int numRows, int numCols, Background defaultBackground)
   {
      this.numRows = numRows;
      this.numCols = numCols;
      this.background = new Background[numRows][numCols];
      this.occupancy = new Entity[numRows][numCols];
      this.entities = new HashSet<>();

      for (int row = 0; row < numRows; row++)
      {
         Arrays.fill(this.background[row], defaultBackground);
      }
   }

    public int getNumRows()
    {
        return numRows;
    }
    public int getNumCols()
    {
        return numCols;
    }
    public Background[][] getBackground() {
        return background;
    }
    public Set<Entity> getEntities() {
        return entities;
    }
    public Entity[][] getOccupancy() {
        return occupancy;
    }

    public Optional<Point> findOpenAround(Point pos)
    {
        for (int dy = -FISH_REACH; dy <= FISH_REACH; dy++)
        {
            for (int dx = -FISH_REACH; dx <= FISH_REACH; dx++)
            {
                Point newPt = new Point(pos.getX() + dx, pos.getY() + dy);
                if (withinBounds(newPt) &&
                        !isOccupied(newPt))
                {
                    return Optional.of(newPt);
                }
            }
        }

        return Optional.empty();
    }

    public void tryAddEntity(Entity entity)
    {
        if (isOccupied(entity.getPosition()))
        {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

        addEntity(entity);
    }

    public boolean withinBounds(Point pos)
    {
        return pos.getY() >= 0 && pos.getY() < getNumRows() &&
                pos.getX() >= 0 && pos.getX() < getNumCols();
    }

    public boolean isOccupied(Point pos)
    {
        return withinBounds(pos) &&
                getOccupancyCell(pos) != null;
    }

    public Optional<Entity> findNearest(Point pos, EntityKind kind)
    {
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : getEntities())
        {
            if (entity.getKind() == kind)
            {
                ofType.add(entity);
            }
        }

        return nearestEntity(ofType, pos);
    }
    public Optional<Entity> nearestEntity(List<Entity> entities,
                                                 Point pos)
    {
        if (entities.isEmpty())
        {
            return Optional.empty();
        }
        else
        {
            Entity nearest = entities.get(0);
            int nearestDistance = Functions.distanceSquared(nearest.getPosition(), pos);

            for (Entity other : entities)
            {
                int otherDistance = Functions.distanceSquared(other.getPosition(), pos);

                if (otherDistance < nearestDistance)
                {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }

    /*
       Assumes that there is no entity currently occupying the
       intended destination cell.
    */
    public void addEntity(Entity entity)
    {
        if (withinBounds(entity.getPosition()))
        {
            setOccupancyCell(entity.getPosition(), entity);
            getEntities().add(entity);
        }
    }

    public void moveEntity(Entity entity, Point pos)
    {
        Point oldPos = entity.getPosition();
        if (withinBounds(pos) && !pos.equals(oldPos))
        {
            setOccupancyCell(oldPos, null);
            removeEntityAt(pos);
            setOccupancyCell(pos, entity);
            entity.setPosition(pos);    // using a setter
        }
    }

    public void removeEntity(Entity entity)
    {
        removeEntityAt(entity.getPosition());
    }

    public void removeEntityAt(Point pos)
    {
        if (withinBounds(pos)
                && getOccupancyCell(pos) != null)
        {
            Entity entity = getOccupancyCell(pos);

         /* this moves the entity just outside of the grid for
            debugging purposes */
            entity.setPosition(new Point(-1, -1));
            getEntities().remove(entity);
            setOccupancyCell(pos, null);
        }
    }

    public Optional<PImage> getBackgroundImage(Point pos)
    {
        if (withinBounds(pos))
        {
            return Optional.of(Functions.getCurrentImage(getBackgroundCell(pos)));
        }
        else
        {
            return Optional.empty();
        }
    }

    public void setBackground(Point pos, Background background)
    {
        if (withinBounds(pos))
        {
            setBackgroundCell(pos, background);
        }
    }

    public Optional<Entity> getOccupant(Point pos)
    {
        if (isOccupied(pos))
        {
            return Optional.of(getOccupancyCell(pos));
        }
        else
        {
            return Optional.empty();
        }
    }

    public Entity getOccupancyCell(Point pos)
    {
        return getOccupancy()[pos.getY()][pos.getX()];
    }

    public void setOccupancyCell(Point pos, Entity entity)
    {
        getOccupancy()[pos.getY()][pos.getX()] = entity;
    }

    public Background getBackgroundCell(Point pos)
    {
        return getBackground()[pos.getY()][pos.getX()];
    }

    public void setBackgroundCell(Point pos, Background background)
    {
        getBackground()[pos.getY()][pos.getX()] = background;
    }


}
