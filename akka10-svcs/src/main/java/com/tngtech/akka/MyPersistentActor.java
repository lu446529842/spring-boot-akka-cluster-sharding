package com.tngtech.akka;

import akka.actor.ActorPath;
import akka.japi.Function;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActorWithAtLeastOnceDelivery;

import java.io.Serializable;

/**
 * Created by davenkat on 10/30/2015.
 */
class MyPersistentActor extends UntypedPersistentActorWithAtLeastOnceDelivery {
    private final ActorPath destination;

    @Override
    public String persistenceId() {
        return "sub9";
    }

    public MyPersistentActor(ActorPath destination) {
        this.destination = destination;
    }

    public void onReceiveCommand(Object message) {
        if (message instanceof String) {
            String s = (String) message;
            persist(new MsgSent(s), new Procedure<MsgSent>() {
                public void apply(MsgSent evt) {
                    updateState(evt);
                }
            });
        } else if (message instanceof Confirm) {
            Confirm confirm = (Confirm) message;
            persist(new MsgConfirmed(confirm.deliveryId), new Procedure<MsgConfirmed>() {
                public void apply(MsgConfirmed evt) {
                    updateState(evt);
                }
            });
        } else {
            unhandled(message);
        }
    }

    public void onReceiveRecover(Object event) {
        updateState(event);
    }

    void updateState(Object event) {
        if (event instanceof MsgSent) {
            final MsgSent evt = (MsgSent) event;
            deliver(destination, new Function<Long, Object>() {
                public Object apply(Long deliveryId) {
                    return new Msg(deliveryId, evt.s);
                }
            });
        } else if (event instanceof MsgConfirmed) {
            final MsgConfirmed evt = (MsgConfirmed) event;
            confirmDelivery(evt.deliveryId);
        }
    }
}
class Msg implements Serializable {
    public final long deliveryId;
    public final String s;

    public Msg(long deliveryId, String s) {
        this.deliveryId = deliveryId;
        this.s = s;
    }
}

class Confirm implements Serializable {
    public final long deliveryId;

    public Confirm(long deliveryId) {
        this.deliveryId = deliveryId;
    }
}


class MsgSent implements Serializable {
    public final String s;

    public MsgSent(String s) {
        this.s = s;
    }
}
class MsgConfirmed implements Serializable {
    public final long deliveryId;

    public MsgConfirmed(long deliveryId) {
        this.deliveryId = deliveryId;
    }
}
