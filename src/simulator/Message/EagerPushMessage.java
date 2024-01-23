package simulator.Message;

import java.util.ArrayList;
import java.util.List;

//import simulator.CommunicationLog;
import simulator.Const.MessageType;
import simulator.Node.PlumtreeNode;

public class EagerPushMessage extends AbstractMessage {
    private int numHops = 0;
    private List<PlumtreeNode> ReportList = new ArrayList<PlumtreeNode>();// List宣言
    // getterで作成

    public EagerPushMessage() {// getterを作る
        super(MessageType.EAGER_PUSH);
    }

    private EagerPushMessage(int messageId, int numHops) {
        super(messageId, MessageType.EAGER_PUSH);
        this.numHops = numHops;
        // this.ReportList.addAll(ReportList);// ReportListのすべてを今のReportListに入れる
    }

    public EagerPushMessage cloneToForward(PlumtreeNode node) {
        // ReportList.add(nodeId);
        EagerPushMessage msg = new EagerPushMessage(messageId, numHops + 1);
        msg.ReportList.addAll(this.ReportList);
        msg.ReportList.add(node);
        return msg;
    }

    public int getNumHops() {
        return this.numHops;
    }

    public List<PlumtreeNode> getReportList() {// getterの設定
        return this.ReportList;
    }

    public void addAll(List<Integer> rl) {
    }
}
