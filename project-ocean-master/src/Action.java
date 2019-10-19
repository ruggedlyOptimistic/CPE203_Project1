/*
Action: ideally what our various entities might do in our virutal world
 */

final class Action
{
    private ActionKind kind;
    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

   public Action(ActionKind kind, Entity entity, WorldModel world,
      ImageStore imageStore, int repeatCount)
   {
      this.kind = kind;
      this.entity = entity;
      this.world = world;
      this.imageStore = imageStore;
      this.repeatCount = repeatCount;
   }
    public ActionKind getKind()
    {
        return kind;
    }
    public Entity getEntity() {
        return entity;
    }

    public WorldModel getWorld() {
        return world;
    }

    public ImageStore getImageStore() {
        return imageStore;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void executeActivityAction(EventScheduler scheduler)
    {
        switch (getEntity().getKind())
        {
            case OCTO_FULL:
                getEntity().executeOctoFullActivity(getWorld(),
                        getImageStore(), scheduler);
                break;

            case OCTO_NOT_FULL:
                getEntity().executeOctoNotFullActivity(getWorld(),
                        getImageStore(), scheduler);
                break;

            case FISH:
                getEntity().executeFishActivity(getWorld(), getImageStore(),
                        scheduler);
                break;

            case CRAB:
                getEntity().executeCrabActivity(getWorld(),
                        getImageStore(), scheduler);
                break;

            case QUAKE:
                getEntity().executeQuakeActivity(getWorld(), getImageStore(),
                        scheduler);
                break;

            case SGRASS:
                getEntity().executeSgrassActivity(getWorld(), getImageStore(),
                        scheduler);
                break;

            case ATLANTIS:
                getEntity().executeAtlantisActivity(getWorld(), getImageStore(),
                        scheduler);
                break;

            default:
                throw new UnsupportedOperationException(
                        String.format("executeActivityAction not supported for %s",
                                getEntity().getKind()));
        }
    }

    public void executeAction(EventScheduler scheduler)
    {
        switch (getKind())
        {
            case ACTIVITY:
                executeActivityAction(scheduler);
                break;

            case ANIMATION:
                executeAnimationAction(scheduler);
                break;
        }
    }

    public void executeAnimationAction(EventScheduler scheduler) // getter used instead of this.~~~ syntax
    {
        getEntity().nextImage();

        if (getRepeatCount() != 1)
        {
            scheduler.scheduleEvent(getEntity(),
                    getEntity().createAnimationAction(Math.max(getRepeatCount() - 1, 0)),
                    getEntity().getAnimationPeriod());
        }
    }
}
