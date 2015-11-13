package com.cgi.garnet.attachment.service;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Option;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;
import event.AssignmentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.Date;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class Worker extends UntypedPersistentActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Autowired
    private Car car;

    @Override
    public void preStart() throws Exception {
        System.out.println("Worker Startup ###########################");
        super.preStart();
    }

    @Override
    public String persistenceId() {
        return "worker-" + getContext().parent().path().name();
    }

    @Override
    public void onReceiveRecover(Object msg) {
        if (msg instanceof AssignmentEvent) {
            System.out.println(" Recovered Event -->" + ((AssignmentEvent) msg).getEventType());
        } else {
            unhandled(msg);
        }
    }

    @Override
    public boolean recoveryRunning() {
        log.info("Worker: Recovery running @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        return super.recoveryRunning();
    }

    @Override
    public boolean recoveryFinished() {
        System.out.println("Worker: Recovery finished @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        return super.recoveryFinished();
    }

    @Override
    public void onRecoveryFailure(Throwable cause, scala.Option<Object> event) {
        System.out.println("Worker: Recovery failed @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        super.onRecoveryFailure(cause, event);
    }

    @Override
    public void onReceiveCommand(Object msg) {
        if (msg instanceof AssignmentEvent) {
            log.info("Worker Got: {}", ((AssignmentEvent) msg).getEventType());
            AssignmentEvent evt = ((AssignmentEvent) msg);
            evt.setCreated(new Date());
            persistAsync(evt, new Procedure<AssignmentEvent>() {
                public void apply(AssignmentEvent evt) throws Exception {
                    System.out.println("Worker: onReceiveCommand ####### changed state, successfully persisted event and publishing the event-->" + evt.getEventType());
//                    getContext().system().eventStream().publish(evt);
                    car.startCar(((AssignmentEvent) msg).getEventType());
                    saveSnapshot(evt);
                }
            });

        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            log.info("worker subscribing");
/*
        } else if (msg.equals("leave")) {
            System.out.println("Listener:  Reconnect process started.");
            context().watch(region);
            region.tell(ShardRegion.gracefulShutdownInstance(), self());
        } else if (msg instanceof Terminated) {
            System.out.println("Listener:  Termination process kicked, will reconnect after 10 sec");
            cluster.registerOnMemberRemoved(() -> system.terminate());
            cluster.leave(cluster.selfAddress());
//            getContext().system().scheduler().scheduleOnce(Duration.create(10, "seconds"), getSelf(), Reconnect, getContext().dispatcher(), null);
*/
        } else if (msg.equals(ReceiveTimeout.getInstance())) {
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else
            unhandled(msg);
    }
}
