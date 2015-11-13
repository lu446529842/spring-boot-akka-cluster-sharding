package counter.sharding.example.app;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.cluster.Cluster;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ShardRegion;
import akka.japi.pf.ReceiveBuilder;

/**
 * Created by davenkat on 11/6/2015.
 */
public class IllustrateGracefulShutdown extends AbstractActor {

    public IllustrateGracefulShutdown() {
        final ActorSystem system = context().system();
        final Cluster cluster = Cluster.get(system);
        final ActorRef region = ClusterSharding.get(system).shardRegion("Entity");

        receive(ReceiveBuilder.
                match(String.class, s -> s.equals("leave"), s -> {
                    context().watch(region);
                    region.tell(ShardRegion.gracefulShutdownInstance(), self());
                }).
                match(Terminated.class, t -> t.actor().equals(region), t -> {
                    cluster.registerOnMemberRemoved(() -> system.terminate());
                    cluster.leave(cluster.selfAddress());
                }).build());
    }
}