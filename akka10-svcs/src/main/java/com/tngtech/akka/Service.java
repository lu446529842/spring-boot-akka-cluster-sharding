package com.tngtech.akka;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class Service extends UntypedActor {
  private LoggingAdapter log = Logging.getLogger( getContext().system(), this );

  public static Props props() {
    return Props.create( Service.class );
  }

  public Service() {
    FiniteDuration duration = Duration.create( 2, TimeUnit.SECONDS );
    //This step simulates the Service it self sends Swap messages to simulate the slow response
    getContext().system()
                .scheduler()
                .schedule( duration,
                           duration,
                           getSelf(), //Receiver is the same sender
                           new Swap(), //Service it self sending the become actor message in this case it is Swap Object
                           getContext().dispatcher(), getSelf() );
  }

  Procedure<Object> slow = new Procedure<Object>() {
    @Override
    public void apply( Object message ) throws Exception {
      log.info( "SLOW: Received request of type {}", message );
      if ( message instanceof Task ) {
        Task t=(Task) message;
        log.info( "^^^^^^^^^^^^^^^^ slowing the task and no response will be sent to this request. and circuit breaker will be opened ", message);
        Thread.sleep( 1000 );
      } else if ( message instanceof Swap ) {
        log.info( "&&&&&&&&&&&&&&&&&&&& if it is already swapped message in this example we will convert this back to Task by doing un become this will make the circuit half open.", message);
        getContext().unbecome();
      }
    }
  };

  @Override
  public void onReceive( Object message ) throws Exception {
    log.info( "Received request of type {}", message );
    if ( message instanceof Task ) {
      Task t=(Task) message;
      sendResponse(t);
    } else if ( message instanceof Swap ) {
      getContext().become( slow );
    }
  }

  private void sendResponse(Task task) {
    getSender().tell( new Response(task), getSelf() );
  }

  public static class Task implements Serializable {
    private final int id;

    public Task( int id ) {
      this.id = id;
    }

    @Override
    public String toString() {
      return "Task{" +
             "id=" + id +
             '}';
    }
  }

  public static class Response implements Serializable {
    private Task task;
    public Response(Task task){
      this.task=task;
    }
    public Task getTask(){
      return task;
    }
  }

    public static class Swap {
/*
    private Task task;
    public Swap(Task task){
      this.task=task;
    }
    public Task getTask(){
      return task;
    }
*/
  }

}
