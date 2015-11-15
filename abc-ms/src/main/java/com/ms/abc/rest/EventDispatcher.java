package com.ms.abc.rest;

import akka.actor.ActorRef;
import com.ms.event.EDFEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by davenkat on 10/10/2015.
 */
@Component
public class EventDispatcher {

    @Autowired
    ActorRef publishToSelfActor;

    @Autowired
    ActorRef publishToDefActor;


    public void dispatchToDef(EDFEvent assignmentEvent) {
        publishToDefActor.tell(assignmentEvent, null);
    }

    public void dispatchToSelf(EDFEvent assignmentEvent) {
        publishToSelfActor.tell(assignmentEvent, null);
    }
}
