package counter.sharding.example.app;

import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Created by davenkat on 11/2/2015.
 */
public class Counter extends UntypedPersistentActor {

    public static enum CounterOp {
        INCREMENT, DECREMENT
    }

    public static class Get implements Serializable {
        final public long counterId;

        public Get(long counterId) {
            this.counterId = counterId;
        }
    }

    public static class EntityEnvelope implements Serializable {
        final public long id;
        final public Object payload;

        public EntityEnvelope(long id, Object payload) {
            this.id = id;
            this.payload = payload;
        }
    }

    public static class CounterChanged implements Serializable {
        final public int delta;

        public CounterChanged(int delta) {
            this.delta = delta;
        }
    }


    int count = 0;

    // getSelf().path().name() is the entity identifier (utf-8 URL-encoded)
    @Override
    public String persistenceId() {
        return "Counter-" + getSelf().path().name();
    }

    public Counter() {
/*
        mediator = DistributedPubSub.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe("content", "grp1", getSelf()), getSelf());
*/

        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
    }


    @Override
    public void preStart() throws Exception {
        super.preStart();
        context().setReceiveTimeout(Duration.create(120, TimeUnit.SECONDS));
    }

    void updateState(CounterChanged event) {
        count += event.delta;
        System.out.println("Updated state count ===================" + count);
    }

    @Override
    public void onReceiveRecover(Object msg) {
        if (msg instanceof CounterChanged) {
            CounterChanged cc = (CounterChanged) msg;
            System.out.println("Recoverd counter changed ===================" + cc.delta);
            updateState((CounterChanged) msg);
        } else
            unhandled(msg);
    }

    @Override
    public void onReceiveCommand(Object msg) {
        if (msg instanceof Get)
            getSender().tell(count, getSelf());

        else if (msg == CounterOp.INCREMENT)
            persist(new CounterChanged(+1), new Procedure<CounterChanged>() {
                public void apply(CounterChanged evt) {
                    updateState(evt);
                    throw new RuntimeException("exception occured");
                }
            });

        else if (msg == CounterOp.DECREMENT)
            persist(new CounterChanged(-1), new Procedure<CounterChanged>() {
                public void apply(CounterChanged evt) {
                    updateState(evt);
                }
            });

        else if (msg.equals(ReceiveTimeout.getInstance())) {
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            System.out.println("@@@@@@@@@@@@@ counter subscribing @@@@@@@@@@@@@");
        } else
            unhandled(msg);
    }
}