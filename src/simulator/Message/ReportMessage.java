package simulator.Message;

import java.util.ArrayList;
import java.util.List;
import simulator.Const.MessageType;
import simulator.Node.PlumtreeNode;

public class ReportMessage extends AbstractMessage {
    List<PlumtreeNode> RL = new ArrayList<PlumtreeNode>();

    public ReportMessage(ArrayList<PlumtreeNode> rl) {
        super(MessageType.REPORT);

        if (rl != null) {
            RL.addAll(rl);
        }
    }
}
