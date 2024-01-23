package simulator.Event;

import java.util.HashSet;
import java.util.Iterator;

import simulator.Message.GraftMessage;
import simulator.Node.PlumtreeNode;

public class GraftEvent extends AbstractEvent {
    private PlumtreeNode ihaveReceiver;
    private PlumtreeNode ihaveSender;
    private HashSet<Integer> msgIds;    // Message IDs those are marked as missing

    public GraftEvent(PlumtreeNode ihaveReceiver, PlumtreeNode ihaveSender, HashSet<Integer> msgIds, long timestamp, long procDelay, boolean isRegularEvent) {
        super(timestamp, procDelay, isRegularEvent);
        this.ihaveReceiver = ihaveReceiver;
        this.ihaveSender = ihaveSender;
        this.msgIds = msgIds;
    }

    @Override
    public void process() {
        if(ihaveReceiver.isDown()) return;

        Iterator<Integer> itr = msgIds.iterator();
        while(itr.hasNext()){
            int msgId = itr.next();
            if(ihaveReceiver.getReceivedEagerMsgs().containsKey(msgId)){
                itr.remove();
            }
        }

        if(msgIds.size() > 0){
            ihaveReceiver.updateLink(ihaveSender, null, null, ihaveSender, timestamp);
            GraftMessage graftMsg = new GraftMessage();
            graftMsg.getMsgIds().addAll(msgIds);

            ihaveReceiver.send(ihaveSender, graftMsg, timestamp + procDelay);
        }
    }
}
