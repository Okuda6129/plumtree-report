package simulator.Event;

import simulator.Const;
import simulator.Settings;
import simulator.Util;
import simulator.Node.AbstractNode;

public class NodeRestoreEvent extends AbstractEvent {
    private AbstractNode node;

    public NodeRestoreEvent(AbstractNode node, long timestamp, long procDelay, boolean isRegularEvent) {
        super(timestamp, procDelay, isRegularEvent);
        this.node = node;
    }

    @Override
    public void process() {
        node.setIsDown(false);
        node.restore(timestamp);

        if(Settings.FILE_OUTPUT) {
            String jsonLine = ","+Const.LINE_SEP+"{\"type\":\"node_status_change\", \"content\":{\"timestamp\":"+timestamp+", \"node_id\":"+node.getNodeId()+", \"new_status\":\"up\"}}";
            Util.output(Settings.OUTPUT_FILE_PATH, jsonLine, true);
        }
    }
}