package counter.sharding.example.app;

import akka.actor.*;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import com.cgi.garnet.attachment.service.DataStoreException;
import com.cgi.garnet.attachment.service.ServiceUnavailable;
import event.AssignmentEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class ListenerSupervisor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart() throws Exception {
        initActor();
        super.preStart();
    }

    private void initActor(){
        // If we don't get any progress within 15 seconds then the service
        // is unavailable
        //listenerWatch = getContext().watch(getContext().actorOf(listenerProps.withRouter(new RoundRobinPool(1)), "listener"));
        //  getContext().setReceiveTimeout(Duration.create("15 seconds"));
        log.info("ServicesSuperVisor Starting up");
    }
    public SupervisorStrategy restartOrEsclate() {
        SupervisorStrategy strategy = new OneForOneStrategy(0, Duration.create("5 seconds"), new Function<Throwable, SupervisorStrategy.Directive>() {
            @Override
            public SupervisorStrategy.Directive apply(Throwable t) {
                if (t instanceof NullPointerException) {
                    System.out.println("restartOrEsclate strategy, restarting the actor");
                    return restart();
                } else if (t instanceof ServiceUnavailable) {
                    System.out.println("restartOrEsclate strategy, escalate");
                    return escalate();
                } else if (t instanceof DataStoreException) {
                    System.out.println("restartOrEsclate strategy invoked, escalating ##############");
//                    return restart();
                    return escalate();
                } else {
                    System.out.println("ListenerSupervisor: Final else restartOrEsclate strategy, escalate");
                    return restart();
                }
            }
        });
        return strategy;
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return restartOrEsclate();
    }


    public ListenerSupervisor() {
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
    }
    static final Object Reconnect = "Reconnect";

    public void onReceive(Object msg) {
        if (msg instanceof AssignmentEvent) {
            log.info("ListenerSupervisor: Got: {}, Don't know what to do here ", ((AssignmentEvent) msg).getEventType());

        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            log.info("ListenerSupervisor subscribing");
        } else if (msg == ReceiveTimeout.getInstance()) {
            // No progress within 15 seconds, ServiceUnavailable
            log.error("ListenerSupervisor: Timeout kicked, Shutting down due to unavailable service");
            getContext().system().terminate();
        } else if (msg instanceof Terminated) {
            System.out.println("ListenerSupervisor: Termination kicked !!!!!!!!!!!!!!!");
            getContext().system().scheduler().scheduleOnce(Duration.create(10, "seconds"), getSelf(), Reconnect,getContext().dispatcher(), null);
        } else if (msg.equals(Reconnect)) {
            System.out.println("ListenerSupervisor: Reconnect process started !!!!!!!!!!!!!!!");
            // Re-establish storage after the scheduled delay
            initActor();
        } else {
            unhandled(msg);
        }
    }
}