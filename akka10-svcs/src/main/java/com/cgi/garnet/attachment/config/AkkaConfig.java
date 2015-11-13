package com.cgi.garnet.attachment.config;

import akka.actor.*;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Function;
import akka.japi.Option;
import akka.routing.RoundRobinPool;
import com.cgi.garnet.attachment.service.DataStoreException;
import com.cgi.garnet.attachment.service.ServiceUnavailable;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import event.AssignmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.*;

@Configuration
@Lazy
@ComponentScan(basePackages = {"com.cgi.garnet.attachment.config",
        "com.cgi.garnet.attachment.rest", "com.cgi.garnet.attachment.service"})
public class AkkaConfig extends WebMvcConfigurerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(AkkaConfig.class);


    @Autowired
    private SpringExtension springExtension;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Actor system singleton for this application.
     */
    @Bean
    public ActorSystem actorSystem() {
        ActorSystem actorSystem = ActorSystem.create("ClusterSystem", akkaConfiguration());
        springExtension.initialize(applicationContext);
        return actorSystem;
    }

    @Bean
    public ActorRef ePublisher() {
        return actorSystem().actorOf(springExtension.props("publisher").withRouter(new RoundRobinPool(5)), "publisher");
    }


    @Bean
    public ClusterShardingSettings initClusterShardingSettings(){
        Option<String> roleOption = Option.none();
        ClusterShardingSettings settings = ClusterShardingSettings.create(actorSystem());
        return  settings;
    }


    @Bean
    public ClusterSharding clusterSharding(){
        return  ClusterSharding.get(actorSystem());
    }



    @Bean
    public Props workerSupervisorProps(){
        return springExtension.props("workerSupervisor");
    }

    @Bean
    public Props listenerProps(){
        return springExtension.props("listener");
    }

    @Bean
    public Props workerProps(){
        return springExtension.props("worker");
    }


    /**
     * Always start the top supervisor. Let the supervisor create it's own children in this case listerSuperVisor has Listener Actor and listener Actor it self is a supervisor for worker.
     * @return
     */

    @Bean
    public ActorRef initWorkerSupervisor() {
        ActorRef sub = actorSystem().actorOf(workerSupervisorProps(), "workerSupervisor");
        return sub;

    }

    @Bean
    public ActorRef initListener() {
        ActorRef sub = actorSystem().actorOf(listenerProps(), "listener");
        return sub;

    }


    @Bean
    public ActorRef initWorkerShardRegion() {
        return clusterSharding().start("worker", workerProps(), initClusterShardingSettings(), assignmentShardignessageExtractor());
    }

    @Bean
    public ActorRef workerSupervisorShardRegion() {
        return clusterSharding().start("workerSupervisor", workerSupervisorProps(), initClusterShardingSettings(), assignmentShardignessageExtractor());
    }


    @Bean
    @Scope(value = "prototype")
    public ShardRegion.MessageExtractor assignmentShardignessageExtractor() {
        ShardRegion.MessageExtractor  messageExtractor = new ShardRegion.MessageExtractor() {
            @Override
            public Object entityMessage(Object message) {
                return message;
            }

            @Override
            public String entityId(Object message) {
                if (message instanceof AssignmentEvent) {
                    String id=((AssignmentEvent) message).getModuleId().toString();
                    return id;
                }
                return  null;
            }
            @Override
            public String shardId(Object message) {
                int numberOfShards = 100;
                if (message instanceof AssignmentEvent) {
                    String uid = ((AssignmentEvent) message).getModuleId().toString();
                    String shardId=String.valueOf(uid.length() % numberOfShards);;
                    System.out.println("ShardId --->" + shardId);
                    return shardId;
                } else {
                    System.out.println("ShardId is null ??????????????????????");
                    return null;
                }
            }

        };
        return messageExtractor;
    }



    @Bean
    public SupervisorStrategy oneToAll() {

        SupervisorStrategy strategy = new AllForOneStrategy(-1,Duration.Inf(), new Function<Throwable, SupervisorStrategy.Directive>() {
            @Override
            public SupervisorStrategy.Directive apply(Throwable t) {
                if (t instanceof ServiceUnavailable) {
                    System.out.println("oneToAll :restartOrEsclate strategy, restarting");
                    return stop();
                }else if (t instanceof DataStoreException) {
                    System.out.println("oneToAll :DataStoreException strategy invoked, stopping %%%%%%%%%%%%%%%%%%%%%%%");
                    return restart();
                } else {
                    System.out.println("oneToAll :restartOrEsclate strategy, escalate");
                    return escalate();
                }
            }
        });

        return strategy;
    }

    @Bean
    // Restart the child when ServiceUnavailable is thrown.
    // After 3 restarts within 5 seconds it will escalate to the supervisor which may stop the process.
    public SupervisorStrategy restartOrEsclate() {
        SupervisorStrategy strategy = new OneForOneStrategy(-1,Duration.create("5 seconds"), new Function<Throwable, SupervisorStrategy.Directive>() {
            @Override
            public SupervisorStrategy.Directive apply(Throwable t) {
                if (t instanceof NullPointerException) {
                    System.out.println("oneToOne: restartOrEsclate strategy, restarting the actor");
                    return restart();
                }else if (t instanceof ServiceUnavailable) {
                    System.out.println("oneToOne: restartOrEsclate strategy, escalate");
                    return escalate();
                }else if (t instanceof DataStoreException) {
                    System.out.println("oneToOne: DataStoreException invoked, escalating to oneToAll @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//                    return restart();
                    return stop();
                }  else {
                    System.out.println("oneToOne: final else called escalating to oneToAll");
                    return escalate();
                }
            }
        });
        return strategy;
    }

    /**
     * Read configuration from application.conf file
     */
    @Bean
    public Config akkaConfiguration() {
        return ConfigFactory.load();
    }

}
