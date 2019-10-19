import java.util.*;

/*
EventScheduler: ideally our way of controlling what happens in our virtual world
 */

final class EventScheduler
{
   private PriorityQueue<Event> eventQueue;
   private Map<Entity, List<Event>> pendingEvents;
   private double timeScale;

   public EventScheduler(double timeScale)
   {
      this.eventQueue = new PriorityQueue<>(new EventComparator());
      this.pendingEvents = new HashMap<>();
      this.timeScale = timeScale;
   }

   public PriorityQueue<Event> getEventQueue()
   {
      return eventQueue;
   }
   public Map<Entity, List<Event>> getPendingEvents()
   {
      return pendingEvents;
   }
   public double getTimeScale()
   {
      return timeScale;
   }

   public void scheduleEvent(Entity entity, Action action, long afterPeriod)
   {
      long time = System.currentTimeMillis() +
              (long)(afterPeriod * getTimeScale());
      Event event = new Event(action, time, entity);

      this.eventQueue.add(event);

      // update list of pending events for the given entity
      List<Event> pending = getPendingEvents().getOrDefault(entity,
              new LinkedList<>());
      pending.add(event);
      getPendingEvents().put(entity, pending);
   }

   public void unscheduleAllEvents(Entity entity)
   {
      List<Event> pending = getPendingEvents().remove(entity);

      if (pending != null)
      {
         for (Event event : pending)
         {
            getEventQueue().remove(event);
         }
      }
   }

   public void removePendingEvent(Event event)
   {
      List<Event> pending = getPendingEvents().get(event.getEntity());

      if (pending != null)
      {
         pending.remove(event);
      }
   }

   public void updateOnTime(long time)
   {
      while (!getEventQueue().isEmpty() &&
              getEventQueue().peek().getTime() < time)
      {
         Event next = getEventQueue().poll();

         removePendingEvent(next);

         next.getAction().executeAction(this);
      }
   }
}
