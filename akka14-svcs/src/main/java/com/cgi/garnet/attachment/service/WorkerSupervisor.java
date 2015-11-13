package com.cgi.garnet.attachment.service;

import akka.actor.*;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.RoundRobinPool;
import com.cgi.garnet.attachment.config.SpringExtension;
import event.AssignmentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class WorkerSupervisor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Autowired
    private Car car;

    @Autowired
    private SpringExtension springExtension;

    @Autowired
    private SupervisorStrategy oneToAll;

    @Autowired
    private Props workerProps;

    private ActorRef workerRef;

    @Override
    public void preStart() throws Exception {
        initActor();
        super.preStart();
    }

    private void initActor() {
        log.info("WorkerSupervisor Starting up");
        workerRef = getContext().actorOf(workerProps, "worker");
    //    getContext().setReceiveTimeout(Duration.create("15 seconds"));
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        System.out.println("WorkerSupervisor: oneToAll supervisorStrategy invoked #################################################");
        return oneToAll;
    }

    static final Object Reconnect = "Reconnect";

    public void onReceive(Object msg) {
System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%v "+msg.toString());
 /*       if (msg instanceof Props) {
            log.info("ListenerSupervisor: Got: {}, Don't know what to do here &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            getSender().tell(getContext().actorOf((Props) msg), getSelf());
        }
 */
        if (msg instanceof AssignmentEvent) {
            log.info("Worker Supervisor: Got and forwarding to the persistent actor worker: ", ((AssignmentEvent) msg).getEventType());
            workerRef.forward(msg, getContext());
        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            log.info("ListenerSupervisor subscribing");
        } else if (msg == ReceiveTimeout.getInstance()) {
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
            // No progress within 15 seconds, ServiceUnavailable
            log.error("ListenerSupervisor: Timeout kicked, Shutting down due to unavailable service");
            getContext().system().terminate();
        } else if (msg instanceof Terminated) {
            System.out.println("ListenerSupervisor: Termination kicked !!!!!!!!!!!!!!!");
            getContext().system().scheduler().scheduleOnce(Duration.create(10, "seconds"), getSelf(), Reconnect, getContext().dispatcher(), null);
        } else if (msg.equals(Reconnect)) {
            System.out.println("ListenerSupervisor: Reconnect process started !!!!!!!!!!!!!!!");
            // Re-establish storage after the scheduled delay
            initActor();
        } else {
            unhandled(msg);
        }
    }
}