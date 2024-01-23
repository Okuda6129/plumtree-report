package simulator.Event;

import simulator.Const;
import simulator.Settings;
import simulator.Util;
import simulator.Node.AbstractNode;

public class NodeDownEvent extends AbstractEvent {
    private AbstractNode node;
    private long period;

    public NodeDownEvent(AbstractNode node, long period, long timestamp, long procDelay, boolean isRegularEvent) {
        super(timestamp, procDelay, isRegularEvent);
        this.node = node;
        this.period = period;
    }

    @Override
    public void process() {
        node.setIsDown(true);
        Const.EVENT_QUEUE.add(new NodeRestoreEvent(node, timestamp + period, 0, false));

        if(Settings.FILE_OUTPUT) {
            String jsonLine = ","+Const.LINE_SEP+"{\"type\":\"node_status_change\", \"content\":{\"timestamp\":"+timestamp+", \"node_id\":"+node.getNodeId()+", \"new_status\":\"down\"}}";
            Util.output(Settings.OUTPUT_FILE_PATH, jsonLine, true);
        }
    }
}