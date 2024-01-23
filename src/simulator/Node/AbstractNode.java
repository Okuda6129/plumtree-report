package simulator.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import simulator.CommunicationLog;
import simulator.Const;
import simulator.Event.NodeDownEvent;
import simulator.Message.AbstractMessage;

public abstract class AbstractNode {
    protected int nodeId;
    protected List<CommunicationLog> logList = new ArrayList<CommunicationLog>();
    protected boolean isDown = false;
    protected boolean isConnected = false;

    // For WM model
    protected double x;
    protected double y;

    public AbstractNode(int nodeId) {
        this.nodeId = nodeId;
    }

    public abstract void setNeighbors(HashSet<AbstractNode> neighbors);

    public abstract void send(AbstractNode receiver, AbstractMessage msg, long timestamp);

    public abstract void receive(AbstractNode sender, AbstractMessage msg, long timestamp);

    public abstract void broadcast(AbstractMessage msg, long timestamp);

    public abstract void bootup(long timestamp);

    public void down(long timestamp, long period) {
        Const.EVENT_QUEUE.add(new NodeDownEvent(this, period, timestamp, 0, false));
    }

    public abstract void restore(long timestamp);

    public abstract String getNeighborsJson();

    public int getNodeId() {
        return this.nodeId;
    }

    public List<CommunicationLog> getLogList() {
        return this.logList;
    }

    public boolean isDown() {
        return this.isDown;
    }

    public void setIsDown(boolean isDown) {
        this.isDown = isDown;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
