Overview
========


The Java Event Bus is an "event bus" library implementation that helps to decouple highly dependent components by using a subscribe and publish type architecture. An event bus helps to simplify event communication between multiple components and promotes a more stable and simplistic decoupled interface between each.

This library, as is and modified, can be used for free in personal and commercial applications.

Original project came from "Adam Taft":https://code.google.com/p/simpleeventbus/


Maven Integration
----------------

``` java
<dependency>
	<groupId>org.intelligents-ia</groupId>
	<artifactId>event-bus</artifactId>
	<version>1.3.1</version>
<dependency>
```


Event Bus Pattern
=================

There are many patterns devoted to reducing component coupling. An event bus is one such pattern where objects can "subscribe" to receive certain specific "events" from the bus. As an event is "published" to the event bus, it will be propagated to any subscriber which is interested in the event type. This allows each component to couple solely to the event bus itself and not directly with each other.

An event bus can be thought of as a replacement for the observer pattern, where in the observer pattern, each component is observing an observable directly. In the event bus pattern, each component simply subscribes to the event bus and waits for its event notification methods to be invoked when interesting events have occurred. In this way, an event bus can be thought of like the observer pattern with an extra layer of decoupling.


Example Use
============

Simple example
---------------

A subscriber to the event bus has to simply annotate one or more methods with the @EventHandler annotation. Additionally, the subscriber must subscribe to the bus by calling its subscribe() method. A method annotated with @EventHandler must have a single parameter as an argument to the method which is the event type of interest.

``` java
public class MyEventHandler {
  @EventHandler
  public void handleStringEvent(String event) {
    System.out.println("received: " + event);
  }

  public static void main(String[] args) {
    MyEventHandler meh = new MyEventHandler();
    DefaultEventBus.subscribe(meh);

    // later, events occur, usually from mouse clicks, etc.
    DefaultEventBus.publish("Some String Event");
  }
}
```


Vetoing
-------

A nice feature of this event bus is the ability for a handler to veto certain events. In this way, a vetoed event will not be delivered to any of the regular listeners for that event type. An event handler (subscriber) that wishes to veto an event must simply set the @EventHandler(canVeto=true) parameter and then throw a VetoException, like this:

``` java
public class MyEventHandler {
  @EventHandler
  public void handleStringEvent(String event) {
    throw new AssertionError("Should not happen, event was vetoed.");
  }

  @EventHandler(canVeto=true)
  public void vetoStringEvent(String event) {
    throw new VetoException();
  }

  public static void main(String[] args) {
    MyEventHandler meh = new MyEventHandler();
    DefaultEventBus.subscribe(meh);

    // this time, the event should not be displayed
    // because the event has been vetoed and therefore
    // not delivered to the regular handlers
    DefaultEventBus.publish("Some String Event");
  }
}
```


Weak References
---------------

The DefaultEventBus implements of the EventBus interface uses a WeakReference to store the internal subscriber object. Thus, if the subscriber looses all its strong references, the event bus will automatically remove the garbage collected reference to the subscriber. This is a nice feature to help prevent memory leaks, however it is preferred to call the EventBus unsubscribe() method instead.

A consequence of using WeakReference is that subscribers created without a strong reference may never really receive events. For example, subscribers created as anonymous references, etc.
A future version of this bus may allow for different types of references to be used in the internal bus code.


Other Similar Projects
======================


An existing Java project already exists, called Event Bus by Michael Bushe, that implements the event bus pattern. This project attempts to accomplish a smaller subset of the existing functionality of Mr. Bushe's project. One should consider his project when evaluating this one, as his may better suit your needs. This project is likely more simple to use, but it does not offer the same number of features.

Another best project is "guava":https://code.google.com/p/guava-libraries/wiki/EventBusExplained :
This EventBus allows publish-subscribe-style communication between components without requiring the components to explicitly register with one another (and thus be aware of each other).




Change log
==========

1.3.1
-----

* publication on maven central

1.3.0
-----

adding "BasicEventBus#shutdown(long timeout, TimeUnit unit)"
