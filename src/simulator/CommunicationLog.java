package simulator;

import simulator.Message.AbstractMessage;
import simulator.Node.AbstractNode;

public class CommunicationLog {
    private long timestamp;
    private AbstractMessage msg;
    private AbstractNode from;
    private AbstractNode to;

    public CommunicationLog (long timestamp, AbstractMessage msg, AbstractNode from, AbstractNode to) {
        if(from != null && to != null) {
            System.out.println("Invalid communication log. (\"from\" and \"to\" should be exclusive)");
        }
        if(from == null && to == null) {
            System.out.println("Invalid communication log. (\"from\" or \"to\" should be specified)");
        }

        this.timestamp = timestamp;
        this.msg = msg;
        this.from = from;
        this.to = to;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public AbstractMessage getMsg() {
        return msg;
    }

    public AbstractNode getFrom() {
        return from;
    }

    public AbstractNode getTo() {
        return to;
    }
}
