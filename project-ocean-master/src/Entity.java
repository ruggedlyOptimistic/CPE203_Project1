import java.util.List;
import java.util.Optional;

import processing.core.PImage;

/*
Entity ideally would includes functions for how all the entities in our virtual world might act...
 */


final class Entity
{
   private EntityKind kind;
   private String id;
   private Point position;
   private List<PImage> images;
   private int imageIndex;
   private int resourceLimit;
   private int resourceCount;
   private int actionPeriod;
   private int animationPeriod;

   // Migrated from Functions
   public final String CRAB_KEY = "crab";
   public final String CRAB_ID_SUFFIX = " -- crab";
   public final int CRAB_PERIOD_SCALE = 4;
   public final int CRAB_ANIMATION_MIN = 50;
   public final int CRAB_ANIMATION_MAX = 150;

   public final String QUAKE_KEY = "quake";
   public final int QUAKE_ANIMATION_REPEAT_COUNT = 10;

   public final String FISH_ID_PREFIX = "fish -- ";
   public final int FISH_CORRUPT_MIN = 20000;
   public final int FISH_CORRUPT_MAX = 30000;

   public final int ATLANTIS_ANIMATION_PERIOD = 70;
   public final int ATLANTIS_ANIMATION_REPEAT_COUNT = 7;

   public final String QUAKE_ID = "quake";
   public final int QUAKE_ACTION_PERIOD = 1100;
   public final int QUAKE_ANIMATION_PERIOD = 100;

   public Entity(EntityKind kind, String id, Point position,
      List<PImage> images, int resourceLimit, int resourceCount,
      int actionPeriod, int animationPeriod)
   {
      this.kind = kind;
      this.id = id;
      this.position = position;
      this.images = images;
      this.imageIndex = 0;
      this.resourceLimit = resourceLimit;
      this.resourceCount = resourceCount;
      this.actionPeriod = actionPeriod;
      this.animationPeriod = animationPeriod;
   }

   public EntityKind getKind()
   {
      return kind;
   }

   public String getId()
   {
      return id;
   }
   public Point getPosition()
   {
      return position;
   }

   public void setPosition(Point pos)
   {
      this.position = pos;
   }

   public List<PImage> getImages()
   {
      return images;
   }
   public int getImageIndex()
   {
      return imageIndex;
   }
   public int getResourceLimit() {
      return resourceLimit;
   }
   public int getResourceCount() {
      return resourceCount;
   }
   public int getActionPeriod() {
      return actionPeriod;
   }
   public int getAnimationPeriod()
   {
      switch (this.getKind()) // using this.getKind()
      {
         case OCTO_FULL:
         case OCTO_NOT_FULL:
         case CRAB:
         case QUAKE:
         case ATLANTIS:
            return this.animationPeriod;
         default:
            throw new UnsupportedOperationException(
                    String.format("getAnimationPeriod not supported for %s",
                            this.kind));
      }
   }

   public void nextImage()
   {
      this.imageIndex = (this.imageIndex + 1) % this.images.size();
   }

   public void executeOctoFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Entity> fullTarget = world.findNearest(getPosition(),
              EntityKind.ATLANTIS);

      if (fullTarget.isPresent() &&
              moveToFull(this, world, fullTarget.get(), scheduler))
      {
         //at atlantis trigger animation
         scheduleActions(scheduler, world, imageStore);

         //transform to unfull
         transformFull(world, scheduler, imageStore);
      }
      else
      {  // scheduleEvent(entity, action, long)
         scheduler.scheduleEvent(this, createActivityAction(world, imageStore), getActionPeriod());
      }
   }

   public void executeOctoNotFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Entity> notFullTarget = world.findNearest(getPosition(),
              EntityKind.FISH);

      if (!notFullTarget.isPresent() ||
              !moveToNotFull(this, world, notFullTarget.get(), scheduler) ||
              !transformNotFull(world, scheduler, imageStore))
      {
         scheduler.scheduleEvent(this,
                 createActivityAction(world, imageStore),
                 getActionPeriod());
      }
   }

   public void executeFishActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      Point pos = getPosition();  // store current position before removing

      world.removeEntity(this);
      scheduler.unscheduleAllEvents(this);

      Entity crab = createCrab(getId() + CRAB_ID_SUFFIX,
              pos, getActionPeriod() / CRAB_PERIOD_SCALE,
              CRAB_ANIMATION_MIN +
                      Functions.rand.nextInt(CRAB_ANIMATION_MAX - CRAB_ANIMATION_MIN),
              imageStore.getImageList(CRAB_KEY));

      world.addEntity(crab);
      scheduleActions(scheduler, world, imageStore);
   }

   public void executeCrabActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Entity> crabTarget = world.findNearest(getPosition(), EntityKind.SGRASS);
      long nextPeriod = getActionPeriod();

      if (crabTarget.isPresent())
      {
         Point tgtPos = crabTarget.get().position;

         if (moveToCrab(world, crabTarget.get(), scheduler))
         {
            Entity quake = createQuake(tgtPos,
                    imageStore.getImageList(QUAKE_KEY));

            world.addEntity(quake);
            nextPeriod += getActionPeriod();
            scheduleActions(scheduler, world, imageStore);
         }
      }

      scheduler.scheduleEvent(this,
              createActivityAction(world, imageStore),
              nextPeriod);
   }

   public void executeQuakeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      scheduler.unscheduleAllEvents(this);
      world.removeEntity(this);
   }

   public void executeAtlantisActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
   {
      scheduler.unscheduleAllEvents(this);
      world.removeEntity(this);
   }

   public void executeSgrassActivity(WorldModel world,
                                     ImageStore imageStore, EventScheduler scheduler)
   {
      Optional<Point> openPt = world.findOpenAround(getPosition());

      if (openPt.isPresent())
      {
         Entity fish = createFish(FISH_ID_PREFIX + getId(),
                 openPt.get(), FISH_CORRUPT_MIN +
                         Functions.rand.nextInt(FISH_CORRUPT_MAX - FISH_CORRUPT_MIN),
                 imageStore.getImageList(VirtualWorld.FISH_KEY));
         world.addEntity(fish);
         scheduleActions(scheduler, world, imageStore);
      }

      scheduler.scheduleEvent(this,
              createActivityAction(world, imageStore),
              getActionPeriod());
   }

   public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore)
   {
      switch (getKind())
      {
         case OCTO_FULL:
            scheduler.scheduleEvent(this,
                    createActivityAction(world, imageStore),
                    getActionPeriod());
            scheduler.scheduleEvent(this, createAnimationAction(0),
                    getAnimationPeriod());
            break;

         case OCTO_NOT_FULL:
            scheduler.scheduleEvent(this,
                    createActivityAction(world, imageStore),
                    getActionPeriod());
            scheduler.scheduleEvent(this,
                    createAnimationAction(0), getAnimationPeriod());
            break;

         case OBSTACLE:
            break;
         case FISH:
            scheduler.scheduleEvent(this,
                    createActivityAction(world, imageStore),
                    getActionPeriod());
            break;

         case CRAB:
            scheduler.scheduleEvent(this,
                    createActivityAction(world, imageStore),
                   getActionPeriod());
            scheduler.scheduleEvent(this,
                    createAnimationAction(0), getAnimationPeriod());
            break;

         case QUAKE:
            scheduler.scheduleEvent(this,
                    createActivityAction(world, imageStore),
                    getActionPeriod());
            scheduler.scheduleEvent(this,
                    createAnimationAction(QUAKE_ANIMATION_REPEAT_COUNT),
                    getAnimationPeriod());
            break;

         case SGRASS:
            scheduler.scheduleEvent(this,
                    createActivityAction(world, imageStore),
                    getActionPeriod());
            break;
         case ATLANTIS:
            scheduler.scheduleEvent(this,
                    createAnimationAction(ATLANTIS_ANIMATION_REPEAT_COUNT),
                    getAnimationPeriod());
            break;

         default:
      }
   }

   public boolean transformNotFull(WorldModel world, EventScheduler scheduler, ImageStore imageStore)
   {
      if (getResourceCount() >= getResourceLimit())
      {
         Entity octo = createOctoFull(getId(), getResourceLimit(),
                 getPosition(), getActionPeriod(), getAnimationPeriod(),
                 getImages());

         world.removeEntity(this);
         scheduler.unscheduleAllEvents(this);

         world.addEntity(octo);
         scheduleActions(scheduler, world, imageStore);

         return true;
      }

      return false;
   }

   public void transformFull(WorldModel world, EventScheduler scheduler, ImageStore imageStore)
   {
      Entity octo = createOctoNotFull(getId(), getResourceLimit(),
              getPosition(), getActionPeriod(), getAnimationPeriod(),
              getImages());

      world.removeEntity(this);
      scheduler.unscheduleAllEvents(this);

      world.addEntity(octo);
      scheduleActions(scheduler, world, imageStore);
   }

   public boolean moveToNotFull(Entity octo, WorldModel world,
                                Entity target, EventScheduler scheduler)
   {
      if (Functions.adjacent(octo.position, target.position))
      {
         octo.resourceCount += 1;
         world.removeEntity(target);
         scheduler.unscheduleAllEvents(target);

         return true;
      }
      else
      {
         Point nextPos = nextPositionOcto(octo, world, target.position);

         if (!octo.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            world.moveEntity(octo, nextPos);
         }
         return false;
      }
   }

   public boolean moveToFull(Entity octo, WorldModel world,
                             Entity target, EventScheduler scheduler)
   {
      if (Functions.adjacent(octo.position, target.position))
      {
         return true;
      }
      else
      {
         Point nextPos = nextPositionOcto(octo, world, target.position);

         if (!octo.position.equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            world.moveEntity(octo, nextPos);
         }
         return false;
      }
   }

   public boolean moveToCrab(WorldModel world,
                             Entity target, EventScheduler scheduler)
   {
      if (Functions.adjacent(getPosition(), target.position))
      {
         world.removeEntity(target);
         scheduler.unscheduleAllEvents(target);
         return true;
      }
      else
      {
         Point nextPos = nextPositionCrab(this, world, target.position);

         if (!getPosition().equals(nextPos))
         {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            if (occupant.isPresent())
            {
               scheduler.unscheduleAllEvents(occupant.get());
            }

            world.moveEntity(this, nextPos);
         }
         return false;
      }
   }

   public Point nextPositionOcto(Entity entity, WorldModel world,
                                 Point destPos)
   {
      int horiz = Integer.signum(destPos.getX() - getPosition().getX());
      Point newPos = new Point(getPosition().getX() + horiz,
              getPosition().getY());

      if (horiz == 0 || world.isOccupied(newPos))
      {
         int vert = Integer.signum(destPos.getY() - getPosition().getY());
         newPos = new Point(getPosition().getX(),
                 getPosition().getY() + vert);

         if (vert == 0 || world.isOccupied(newPos))
         {
            newPos = entity.position;
         }
      }

      return newPos;
   }

   public Point nextPositionCrab(Entity entity, WorldModel world,
                                 Point destPos)
   {
      int horiz = Integer.signum(destPos.getX() - getPosition().getX());
      Point newPos = new Point(getPosition().getX() + horiz,
              getPosition().getY());

      Optional<Entity> occupant = world.getOccupant(newPos);

      if (horiz == 0 ||
              (occupant.isPresent() && !(occupant.get().kind == EntityKind.FISH)))
      {
         int vert = Integer.signum(destPos.getY() - getPosition().getY());
         newPos = new Point(getPosition().getX(), getPosition().getY() + vert);
         occupant = world.getOccupant(newPos);

         if (vert == 0 ||
                 (occupant.isPresent() && !(occupant.get().kind == EntityKind.FISH)))
         {
            newPos = entity.position;
         }
      }

      return newPos;
   }
   public Action createAnimationAction(int repeatCount)
   {
      return new Action(ActionKind.ANIMATION, this, null, null, repeatCount);
   }

   public Action createActivityAction(WorldModel world,
                                             ImageStore imageStore)
   {
      return new Action(ActionKind.ACTIVITY, this, world, imageStore, 0);
   }
   public Entity createOctoFull(String id, int resourceLimit,
                                       Point position, int actionPeriod, int animationPeriod,
                                       List<PImage> images)
   {
      return new Entity(EntityKind.OCTO_FULL, id, position, images,
              resourceLimit, resourceLimit, actionPeriod, animationPeriod);
   }

   public static Entity createOctoNotFull(String id, int resourceLimit,
                                          Point position, int actionPeriod, int animationPeriod,
                                          List<PImage> images)
   {
      return new Entity(EntityKind.OCTO_NOT_FULL, id, position, images,
              resourceLimit, 0, actionPeriod, animationPeriod);
   }
   public static Entity createAtlantis(String id, Point position,
                                       List<PImage> images)
   {
      return new Entity(EntityKind.ATLANTIS, id, position, images,
              0, 0, 0, 0);
   }

   public static Entity createObstacle(String id, Point position,
                                       List<PImage> images)
   {
      return new Entity(EntityKind.OBSTACLE, id, position, images,
              0, 0, 0, 0);
   }

   public static Entity createFish(String id, Point position, int actionPeriod,
                                   List<PImage> images)
   {
      return new Entity(EntityKind.FISH, id, position, images, 0, 0,
              actionPeriod, 0);
   }

   public Entity createCrab(String id, Point position,
                                   int actionPeriod, int animationPeriod, List<PImage> images)
   {
      return new Entity(EntityKind.CRAB, id, position, images,
              0, 0, actionPeriod, animationPeriod);
   }

   public Entity createQuake(Point position, List<PImage> images)
   {
      return new Entity(EntityKind.QUAKE, QUAKE_ID, position, images,
              0, 0, QUAKE_ACTION_PERIOD, QUAKE_ANIMATION_PERIOD);
   }

   public static Entity createSgrass(String id, Point position, int actionPeriod,
                                     List<PImage> images)
   {
      return new Entity(EntityKind.SGRASS, id, position, images, 0, 0,
              actionPeriod, 0);
   }

   public static boolean parseOcto(String[] properties, WorldModel world,
                                   ImageStore imageStore)
   {
      if (properties.length == VirtualWorld.OCTO_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[VirtualWorld.OCTO_COL]),
                 Integer.parseInt(properties[VirtualWorld.OCTO_ROW]));
         Entity entity = createOctoNotFull(properties[VirtualWorld.OCTO_ID],
                 Integer.parseInt(properties[VirtualWorld.OCTO_LIMIT]),
                 pt,
                 Integer.parseInt(properties[VirtualWorld.OCTO_ACTION_PERIOD]),
                 Integer.parseInt(properties[VirtualWorld.OCTO_ANIMATION_PERIOD]),
                 imageStore.getImageList(VirtualWorld.OCTO_KEY));
         world.tryAddEntity(entity);
      }

      return properties.length == VirtualWorld.OCTO_NUM_PROPERTIES;
   }

   public static boolean parseObstacle(String[] properties, WorldModel world,
                                       ImageStore imageStore)
   {
      if (properties.length == VirtualWorld.OBSTACLE_NUM_PROPERTIES)
      {
         Point pt = new Point(
                 Integer.parseInt(properties[VirtualWorld.OBSTACLE_COL]),
                 Integer.parseInt(properties[VirtualWorld.OBSTACLE_ROW]));
         Entity entity = createObstacle(properties[VirtualWorld.OBSTACLE_ID],
                 pt, imageStore.getImageList(VirtualWorld.OBSTACLE_KEY));
         world.tryAddEntity(entity);
      }

      return properties.length == VirtualWorld.OBSTACLE_NUM_PROPERTIES;
   }

   public static boolean parseFish(String[] properties, WorldModel world,
                                   ImageStore imageStore)
   {
      if (properties.length == VirtualWorld.FISH_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[VirtualWorld.FISH_COL]),
                 Integer.parseInt(properties[VirtualWorld.FISH_ROW]));
         Entity entity = createFish(properties[VirtualWorld.FISH_ID],
                 pt, Integer.parseInt(properties[VirtualWorld.FISH_ACTION_PERIOD]),
                 imageStore.getImageList(VirtualWorld.FISH_KEY));
         world.tryAddEntity(entity);
      }

      return properties.length == VirtualWorld.FISH_NUM_PROPERTIES;
   }

   public static boolean parseAtlantis(String[] properties, WorldModel world,
                                       ImageStore imageStore)
   {
      if (properties.length == VirtualWorld.ATLANTIS_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[VirtualWorld.ATLANTIS_COL]),
                 Integer.parseInt(properties[VirtualWorld.ATLANTIS_ROW]));
         Entity entity = createAtlantis(properties[VirtualWorld.ATLANTIS_ID],
                 pt, imageStore.getImageList(VirtualWorld.ATLANTIS_KEY));
         world.tryAddEntity(entity);
      }

      return properties.length == VirtualWorld.ATLANTIS_NUM_PROPERTIES;
   }

   public static boolean parseSgrass(String[] properties, WorldModel world,
                                     ImageStore imageStore)
   {
      if (properties.length == VirtualWorld.SGRASS_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[VirtualWorld.SGRASS_COL]),
                 Integer.parseInt(properties[VirtualWorld.SGRASS_ROW]));
         Entity entity = createSgrass(properties[VirtualWorld.SGRASS_ID],
                 pt,
                 Integer.parseInt(properties[VirtualWorld.SGRASS_ACTION_PERIOD]),
                 imageStore.getImageList(VirtualWorld.SGRASS_KEY));
         world.tryAddEntity(entity);
      }

      return properties.length == VirtualWorld.SGRASS_NUM_PROPERTIES;
   }

   public static boolean processLine(String line, WorldModel world,
                              ImageStore imageStore)
   {
      String[] properties = line.split("\\s");
      if (properties.length > 0)
      {
         switch (properties[VirtualWorld.PROPERTY_KEY])
         {
            case VirtualWorld.BGND_KEY:
               return VirtualWorld.parseBackground(properties, world, imageStore);
            case VirtualWorld.OCTO_KEY:
               return parseOcto(properties, world, imageStore);
            case VirtualWorld.OBSTACLE_KEY:
               return parseObstacle(properties, world, imageStore);
            case VirtualWorld.FISH_KEY:
               return parseFish(properties, world, imageStore);
            case VirtualWorld.ATLANTIS_KEY:
               return parseAtlantis(properties, world, imageStore);
            case VirtualWorld.SGRASS_KEY:
               return parseSgrass(properties, world, imageStore);
         }
      }

      return false;
   }

}
