package com.cgi.garnet.com.cgi.counter.cluster.sharding;

import akka.actor.*;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import com.cgi.garnet.attachment.service.DataStoreException;
import com.cgi.garnet.attachment.service.ServiceUnavailable;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;

/**
 * Created by davenkat on 9/28/2015.
 */
public class Subscriber extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public Subscriber() {
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        // subscribe to the topic named "content"
        mediator.tell(new DistributedPubSubMediator.Subscribe("content", "grp1", getSelf()), getSelf());
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());

    }

    //private final ActorRef myEntity = getContext().actorOf(Props.create(MyEntity.class), "myEntity");

    ActorRef myEntity = ClusterSharding.get(getContext().system()).shardRegion("MyEntity");
    private SupervisorStrategy strategy = new OneForOneStrategy(-1, Duration.create("5 seconds"), new Function<Throwable, SupervisorStrategy.Directive>() {
        @Override
        public SupervisorStrategy.Directive apply(Throwable t) {
            if (t instanceof NullPointerException) {
                System.out.println("oneToOne: restartOrEsclate strategy, restarting the actor");
                return restart();
            } else if (t instanceof ServiceUnavailable) {
                System.out.println("oneToOne: restartOrEsclate strategy, escalate");
                return escalate();
            } else if (t instanceof DataStoreException) {
                System.out.println("oneToOne: DataStoreException invoked, escalating to oneToAll @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                return escalate();
            } else {
                System.out.println("oneToOne: final else called escalating to oneToAll");
                return escalate();
            }
        }
    });

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public void onReceive(Object msg) {
        if (msg instanceof MyCounter) {
            log.info("Got: {}", msg);
            myEntity.forward(msg, getContext());
        } else if (msg instanceof DistributedPubSubMediator.Subscribe)
            log.info("subscribe started !!!!!!!!!!!!");
        else if (msg instanceof DistributedPubSubMediator.SubscribeAck)
            log.info("subscribing");
        else
            unhandled(msg);
    }
}