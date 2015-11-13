package com.cgi.garnet.attachment.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.routing.RoundRobinPool;
import com.cgi.garnet.attachment.config.SpringExtension;
import event.AssignmentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class Publisher extends UntypedActor {

    @Autowired
    private ActorSystem actorSystem;

    @Autowired
    private SpringExtension springExtension;

    private ActorRef mediator;

    public Publisher() {
        mediator = DistributedPubSub.get(getContext().system()).mediator();
    }

    @Override
    public void preStart() throws Exception {
//        actorSystem.actorOf(springExtension.props("publisher").withRouter(new RoundRobinPool(5)), "publisher");
        super.preStart();
    }

    public void onReceive(Object msg) {
        if (msg instanceof AssignmentEvent) {
            boolean sendToGroup = true;
            mediator.tell(new DistributedPubSubMediator.Publish("content", msg, sendToGroup), getSelf());
        } else {
            unhandled(msg);
        }
    }
}
