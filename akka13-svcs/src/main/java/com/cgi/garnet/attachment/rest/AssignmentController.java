package com.cgi.garnet.attachment.rest;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.RoundRobinPool;
import com.cgi.garnet.attachment.config.SpringExtension;
import event.AssignmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/garnet/v1/tenants")
public class AssignmentController {

    private static final Logger LOG = LoggerFactory.getLogger(AssignmentController.class);

    @Autowired
    private EventDispatcher eventDispatcher;

    @RequestMapping(value = "/apps/event", method = RequestMethod.POST)
    @ResponseBody
    public  void postEvent(@NotNull @Valid @RequestBody AssignmentEvent assignmentEvent,HttpServletRequest request){
        eventDispatcher.dispathEvent(assignmentEvent);
    }

}
