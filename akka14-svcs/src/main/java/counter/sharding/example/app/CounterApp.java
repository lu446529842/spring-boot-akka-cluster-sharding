package counter.sharding.example.app;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Option;

/**
 * Created by davenkat on 11/2/2015.
 */
public class CounterApp {

    public static void main(String[] args){

        final ActorSystem system = ActorSystem.create("ClusterSystem");

        ShardRegion.MessageExtractor messageExtractor;

        {
            messageExtractor = new ShardRegion.MessageExtractor() {

                @Override
                public String entityId(Object message) {
                    if (message instanceof Counter.EntityEnvelope)
                        return String.valueOf(((Counter.EntityEnvelope) message).id);
                    else if (message instanceof Counter.Get)
                        return String.valueOf(((Counter.Get) message).counterId);
                    else
                        return null;
                }

                @Override
                public Object entityMessage(Object message) {
                    if (message instanceof Counter.EntityEnvelope)
                        return ((Counter.EntityEnvelope) message).payload;
                    else
                        return message;
                }

                @Override
                public String shardId(Object message) {
                    int numberOfShards = 100;
                    if (message instanceof Counter.EntityEnvelope) {
                        long id = ((Counter.EntityEnvelope) message).id;
                        return String.valueOf(id % numberOfShards);
                    } else if (message instanceof Counter.Get) {
                        long id = ((Counter.Get) message).counterId;
                        return String.valueOf(id % numberOfShards);
                    } else {
                        return null;
                    }
                }

            };
        }

        Option<String> roleOption = Option.none();
        ClusterShardingSettings settings = ClusterShardingSettings.create(system);
        ClusterSharding.get(system).start("Counter",Props.create(Counter.class), settings, messageExtractor);

        ActorRef counterRegion = ClusterSharding.get(system).start("SupervisedCounter",Props.create(CounterSupervisor.class), settings, messageExtractor);

        counterRegion.tell(new Counter.Get(123), null);

        counterRegion.tell(new Counter.EntityEnvelope(123,Counter.CounterOp.INCREMENT), null);
        counterRegion.tell(new Counter.Get(123), null);

    }


}
