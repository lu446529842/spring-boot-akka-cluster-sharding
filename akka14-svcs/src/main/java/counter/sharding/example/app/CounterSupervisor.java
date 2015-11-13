package counter.sharding.example.app;

import akka.actor.*;
import akka.japi.Function;
import com.cgi.garnet.attachment.service.DataStoreException;
import com.cgi.garnet.attachment.service.ServiceUnavailable;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.*;

/**
 * Created by davenkat on 11/5/2015.
 */
public class CounterSupervisor extends UntypedActor {

    private ActorRef counter = null;

    @Override
    public void preStart() throws Exception {
        initActor();
        super.preStart();
    }

    private void initActor(){
        // If we don't get any progress within 15 seconds then the service
        // is unavailable
        counter = getContext().actorOf(Props.create(Counter.class), "theCounter");
//        subscriber = getContext().watch(getContext().actorOf(springExtension.props("subscriber").withRouter(new RoundRobinPool(1)), "subscriber"));
        //  getContext().setReceiveTimeout(Duration.create("15 seconds"));
        System.out.println("CounterSupervisor Starting up");
    }

    public SupervisorStrategy restartOrEsclate() {
        SupervisorStrategy strategy = new OneForOneStrategy(0, Duration.create("5 seconds"), new Function<Throwable, Directive>() {
            @Override
            public Directive apply(Throwable t) {
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
                    System.out.println("Final else restartOrEsclate strategy, escalate");
                    return escalate();
                }
            }
        });
        return strategy;
    }


    @Override
    public SupervisorStrategy supervisorStrategy() {
        System.out.println("Supervisor strategy invoked.");
        return restartOrEsclate();
    }

    static final Object Reconnect = "Reconnect";


    @Override
    public void onReceive(Object msg) {
        counter.forward(msg, getContext());

        if (msg instanceof Counter.EntityEnvelope) {
            counter.forward(msg, getContext());
        } else if (msg instanceof Terminated) {
            System.out.println("CounterSupervisor: Termination kicked off and  Reconnect process started.");
            getContext().system().scheduler().scheduleOnce(Duration.create(10, "seconds"), getSelf(), Reconnect, getContext().dispatcher(), null);
        } else if (msg.equals(Reconnect)) {
            System.out.println("CounterSupervisor:  Reconnect process started.");
            // Re-establish storage after the scheduled delay
            initActor();
        } else {
            unhandled(msg);
        }

    }
}