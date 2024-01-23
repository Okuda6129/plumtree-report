package simulator.Event;

import simulator.CommunicationLog;
import simulator.Const;
import simulator.Const.MessageType;
import simulator.Message.AbstractMessage;
import simulator.Node.AbstractNode;
import simulator.Settings;

public class CommunicationEvent extends AbstractEvent {
    private AbstractNode sender;
    private AbstractNode receiver;
    private AbstractMessage msg;

    public CommunicationEvent(AbstractNode sender, AbstractNode receiver, AbstractMessage msg, long timestamp,
            long procDelay, boolean isRegularEvent) {
        super(timestamp, procDelay, isRegularEvent);
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
    }

    @Override
    public void process() {
        MessageType msgType = msg.getMessageType();

        if (!msgType.equals(MessageType.CHECK)) {
            if (sender.isDown()) {
                if (sender.equals(receiver) && msgType.equals(MessageType.EAGER_PUSH)) {
                    System.out.println("Eager push is skipped because node " + sender.getNodeId() + " is down.");
                }
                return;
            }

            if (!sender.equals(receiver)) {
                sender.getLogList().add(new CommunicationLog(timestamp, msg, null, receiver));
            }

            if (Const.RND.nextDouble() < Settings.LOSS_RATE && !sender.equals(receiver))
                return;
            if (receiver.isDown())
                return;

            if (!sender.equals(receiver)) {
                receiver.getLogList().add(new CommunicationLog(timestamp, msg, sender, null));
            }
        }

        receiver.receive(sender, msg, this.timestamp + this.procDelay);
    }

    public AbstractNode getSender() {
        return sender;
    }

    public AbstractNode getReceiver() {
        return receiver;
    }

    public AbstractMessage getMsg() {
        return msg;
    }
}
