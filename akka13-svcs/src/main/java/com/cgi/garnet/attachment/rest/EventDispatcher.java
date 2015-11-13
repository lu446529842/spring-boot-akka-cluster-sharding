package com.cgi.garnet.attachment.rest;

import akka.actor.ActorRef;
import event.AssignmentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by davenkat on 10/10/2015.
 */
@Component
public class EventDispatcher {


    @Autowired
    ActorRef ePublisher;


    public void dispathEvent(AssignmentEvent assignmentEvent) {
        ePublisher.tell(assignmentEvent, null);
    }
}
