package com.cgi.garnet.attachment.service;

import akka.actor.*;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.CircuitBreaker;
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
public class Listener extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    @Autowired
    private SupervisorStrategy restartOrEsclate;

    private ActorRef mediator;

    @Autowired
    ActorRef workerSupervisorShardRegion;

    @Override
    public void preStart() throws Exception {
        initSubscriber();
        super.preStart();
        context().setReceiveTimeout(Duration.create(155, TimeUnit.SECONDS));
    }
    private void initSubscriber(){
        log.info("Starting up");
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return restartOrEsclate;
    }


    private CircuitBreaker breaker;


    public Listener() {
        //Testing copy
        mediator = DistributedPubSub.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe("content", "grp1", getSelf()), getSelf());
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());

        //Commented to just to test the super visor strategy in cluster sharding.
/*
        breaker = new CircuitBreaker(getContext().dispatcher(), getContext().system().scheduler(),2, Duration.create(3, TimeUnit.SECONDS), Duration.create(5, TimeUnit.SECONDS));

        breaker.onOpen(new Runnable() {
            public void run() {
                onOpen();
            }
        });

        breaker.onClose(new Runnable() {
            @Override
            public void run() {
                onClose();
            }
        });

        breaker.onHalfOpen(new Runnable() {
            @Override
            public void run() {
                onHalfOpen();
            }
        });
*/

    }
    public void onOpen() {
        System.out.println("Circuit Breaker is open ################################");
    }

    public void onClose() {
        System.out.println("Circuit Breaker is closed %%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    }

    public void onHalfOpen() {
        System.out.println("Circuit Breaker is half open $$$$$$$$$$$$$$$$$$$$$$$");
    }

    static final Object Reconnect = "Reconnect";

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("Listener:  Post Stop called @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        System.out.println("Listener:  Post Restart called @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        super.postRestart(reason);
    }

    @Override
    public void onReceive(Object msg) {
        log.info("Subscriber Got: {}", msg.toString());
        if (msg instanceof AssignmentEvent) {
            log.info("Subscriber Got: {}", ((AssignmentEvent) msg).getEventType());
            workerSupervisorShardRegion.tell(msg, getSelf());
        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            log.info("Listener: subscribing");
        } else if (msg.equals(ReceiveTimeout.getInstance())){
            //added on 11/6 to test the passivation scenario. On time out,
/*
         If a message is already enqueued to the entity when it stops itself the enqueued message in the mailbox will be dropped.
         To support graceful passivation without loosing such messages the entity actor can send ShardRegion.
         Passivate to its parent Shard. The specified wrapped message in Passivate will be sent back to the entity, which is then supposed to stop itself.
         Incoming messages will be buffered by the Shard between reception of Passivate and termination of the entity.
         Such buffered messages are thereafter delivered to a new incarnation of the entity.
*/
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        }else if (msg instanceof Terminated) {
            System.out.println("Listener:  Termination process kicked, will reconnect after 10 sec");
            getContext().system().scheduler().scheduleOnce(Duration.create(10, "seconds"), getSelf(), Reconnect,getContext().dispatcher(), null);
        }else if (msg.equals(Reconnect)) {
            System.out.println("Listener:  Reconnect process started.");
            // Re-establish storage after the scheduled delay
            initSubscriber();
        }else {
            unhandled(msg);
        }
    }
}