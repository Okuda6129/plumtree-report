package simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.event.AncestorEvent;
import javax.swing.tree.AbstractLayoutCache.NodeDimensions;

import org.w3c.dom.NodeList;

import simulator.Const.MessageType;
import simulator.Event.AbstractEvent;
import simulator.Event.ChangeLossRateEvent;
import simulator.Event.CommunicationEvent;
import simulator.Event.EmptyEvent;
import simulator.Message.AbstractMessage;
import simulator.Message.EagerPushMessage;
import simulator.Message.ReportMessage;
import simulator.Node.AbstractNode;
import simulator.Node.PlumtreeNode;

public class Main {
    private NetworkGenerator networkGenerator = new NetworkGenerator(Settings.NODE_TYPE);
    private Set<AbstractNode> nodes;

    public void proc() {
        System.out.println("Generating network");
        nodes = networkGenerator.generateNetwork();
        if (nodes == null) {
            return;
        }
        if (Settings.FILE_OUTPUT) {
            networkGenerator.outputNetwork();
        }

        System.out.println("Starting simulation");
        loadScenario();
        preProcess();
        runSimulation();
        System.out.println("Simulation completed" + Const.LINE_SEP);

        // Post-process of simulation
        postProcess();
    }

    /**
     * 
     */
    private void loadScenario() {
        /*
         * Write simulation process here.
         */

        AbstractNode node = getNodeAtRandom();// Randamから固定
        AbstractNode node2 = getNodeAtRandom();
        System.out.println("ReportNode");
        System.out.println("from[" + node2.getNodeId() + "]");
        System.out.println("rootnode[" + node.getNodeId() + "]");

        for (int i = 0; i < 10; i++) {
            node.broadcast(new EagerPushMessage(), 2000 * i);

        }
        // node.broadcast(new EagerPushMessage(), 20000);
        PlumtreeNode pn = (PlumtreeNode) node2;
        for (int j = 1; j <= 3; j++) {
            pn.report(new ReportMessage(null), 4100 * j);
            // node2.report(new ReportMessage(null), 5500 * j);false

        }
        // pn.report(new ReportMessage(null), 30000);

        changeLossRate(0.8, 10100);

        /*
         * PlumtreeNode pn = (PlumtreeNode) node;// 書込中
         * int max = 0;// MessageIDの最大値
         * EagerPushMessage mx = ;// 初期化（messageの最大値が分かるため）
         * for (EagerPushMessage m : pn.getReceivedEagerMsgs().values()) {
         * if (max < m.getMessageId()) {
         * mx = m;// when mx List of max
         * max = m.getMessageId();// exchange max
         * }
         * }
         * 
         * if (!mx.getReportList().isEmpty()) {// リストが空じゃないなら
         * int latest = mx.getReportList().get(mx.getReportList().size() - 1);//
         * latestにリストの最新の一個手前を取り出す
         * pn.report(new ReportMessage(null), 10000);
         * }
         */
        /*
         * for (int i = 0; i < 100; i++) {
         * node = getNodeAtRandom();
         * node.down(1100, 1000);
         * }
         */
        /*
         * node = getNodeAtRandom();
         * node.broadcast(new EagerPushMessage(), 2000);
         * node = getNodeAtRandom();
         * node.broadcast(new EagerPushMessage(), 3000);
         */

        /*
         * node = getNodeAtRandom();
         * node.broadcast(new EagerPushMessage(), 4000);
         * node = getNodeAtRandom();
         * node.broadcast(new EagerPushMessage(), 5000);
         * 
         * changeLossRate(0, 5100);
         * 
         * node = getNodeAtRandom();
         * node.broadcast(new EagerPushMessage(), 6000);
         * node = getNodeAtRandom();
         * node.broadcast(new EagerPushMessage(), 7000);
         */

        // If you want to continue simulation until certain time, use setEmptyEvent().
        // For examle, you can continue simulation until sufficient number of IHAVE
        // messages are exchanged.
        setEmptyEvent(50000);

    }

    private void preProcess() {
        for (AbstractNode node : nodes) {
            node.bootup(Const.RND.nextInt((int) Settings.LAZY_PUSH_INTERVAL));
        }
    }

    @SuppressWarnings("unused")
    private void runSimulation() {
        StringBuilder sb = new StringBuilder("");
        int count = 0;

        while (Const.EVENT_QUEUE.size() > 0) {
            AbstractEvent event = Const.EVENT_QUEUE.poll();

            if (Settings.FILE_OUTPUT) {
                if (event instanceof CommunicationEvent) {
                    CommunicationEvent ce = (CommunicationEvent) event;
                    MessageType msgType = ce.getMsg().getMessageType();
                    if (!ce.getSender().equals(ce.getReceiver())) {
                        count++;
                        sb.append("," + Const.LINE_SEP);
                        sb.append("{\"type\":\"communication\", \"content\":{");
                        sb.append("\"timestamp\":" + event.getTimestamp() + ", ");
                        sb.append("\"message_id\":" + ce.getMsg().getMessageId() + ", ");
                        sb.append("\"message_type\":\"" + msgType + "\", ");
                        sb.append("\"from\":\"" + ce.getSender().getNodeId() + "\", ");
                        sb.append("\"to\":\"" + ce.getReceiver().getNodeId() + "\", ");
                        int hop = 0;
                        if (msgType.equals(MessageType.EAGER_PUSH))
                            hop = ((EagerPushMessage) ce.getMsg()).getNumHops();
                        sb.append("\"hop\":" + hop + "}}");
                        if (count % Const.NUM_BULK_WRITE == 0) {
                            Util.output(Settings.OUTPUT_FILE_PATH, new String(sb), true);
                            sb.setLength(0);
                        }
                    }
                }
            }

            event.process();
            if (checkIrregularEventComplete()) {
                break;
            }
        }

        if (Settings.FILE_OUTPUT) {
            if (sb.length() > 0) {
                Util.output(Settings.OUTPUT_FILE_PATH, new String(sb), true);
            }
            Util.output(Settings.OUTPUT_FILE_PATH, Const.LINE_SEP + "]", true);
        }
    }

    private void postProcess() {
        HashMap<Integer, Integer> sumHops = new HashMap<Integer, Integer>(); // message ID, sum of hops
        HashMap<Integer, Integer> maxHops = new HashMap<Integer, Integer>(); // message ID, maximum number of hops
        HashMap<Integer, Integer> numReceiveNodes = new HashMap<Integer, Integer>(); // message ID, number of nodes that
                                                                                     // received the message
        HashMap<Integer, Integer> numEagerSends = new HashMap<Integer, Integer>(); // message ID, number of EAGER_PUSH
                                                                                   // messages sent
        HashMap<MessageType, Integer> sumSends = new HashMap<MessageType, Integer>(); // message type, number of
                                                                                      // messages sent
        HashMap<MessageType, Integer> sumReceives = new HashMap<MessageType, Integer>(); // message type, number of
                                                                                         // messages received

        for (AbstractNode node : nodes) {
            for (int msgId : ((PlumtreeNode) node).getReceivedEagerMsgs().keySet()) {
                int hop = ((PlumtreeNode) node).getReceivedEagerMsgs().get(msgId).getNumHops();
                sumHops.put(msgId, sumHops.getOrDefault(msgId, 0) + hop);
                maxHops.put(msgId, Math.max(maxHops.getOrDefault(msgId, 0), hop));
                numReceiveNodes.put(msgId, numReceiveNodes.getOrDefault(msgId, 0) + 1);
            }
            for (CommunicationLog log : node.getLogList()) {
                AbstractMessage msg = log.getMsg();
                MessageType msgType = msg.getMessageType();
                if (msgType.equals(MessageType.EAGER_PUSH) && log.getTo() != null) {
                    int msgId = msg.getMessageId();
                    numEagerSends.put(msgId, numEagerSends.getOrDefault(msgId, 0) + 1);
                }
                if (log.getTo() != null) {
                    sumSends.put(msgType, sumSends.getOrDefault(msgType, 0) + 1);
                }
                if (log.getFrom() != null) {
                    sumReceives.put(msgType, sumReceives.getOrDefault(msgType, 0) + 1);
                }
            }
        }

        System.out.println("----- Number of messages sent in total -----");
        System.out.println(MessageType.EAGER_PUSH + ": " + sumSends.getOrDefault(MessageType.EAGER_PUSH, 0));
        System.out.println(MessageType.PRUNE + ": " + sumSends.getOrDefault(MessageType.PRUNE, 0));
        System.out.println(MessageType.IHAVE + ": " + sumSends.getOrDefault(MessageType.IHAVE, 0));
        System.out.println(MessageType.GRAFT + ": " + sumSends.getOrDefault(MessageType.GRAFT, 0));
        System.out.println("----- Number of messages received in total -----");
        System.out.println(MessageType.EAGER_PUSH + ": " + sumReceives.getOrDefault(MessageType.EAGER_PUSH, 0));
        System.out.println(MessageType.PRUNE + ": " + sumReceives.getOrDefault(MessageType.PRUNE, 0));
        System.out.println(MessageType.IHAVE + ": " + sumReceives.getOrDefault(MessageType.IHAVE, 0));
        System.out.println(MessageType.GRAFT + ": " + sumReceives.getOrDefault(MessageType.GRAFT, 0));
        System.out.println("----- Reliability -----");
        for (Integer messageId : numReceiveNodes.keySet()) {
            // The number of nodes that received the message do not include the source node,
            // so sub 1 from the denominator.
            double reliability = (double) numReceiveNodes.get(messageId) / (Settings.NUM_NODES);
            System.out.println("Message ID " + messageId + ": " + String.format("%.2f", reliability));
        }
        System.out.println("----- Relative Message Redundancy -----");
        for (Integer messageId : numEagerSends.keySet()) {
            double rmr;
            if (numReceiveNodes.containsKey(messageId)) {
                rmr = (double) numEagerSends.get(messageId) / (numReceiveNodes.get(messageId) - 1) - 1;
            } else {
                rmr = 0;
            }
            System.out.println("Message ID " + messageId + ": " + String.format("%.2f", rmr));
        }
        System.out.println("----- Last Delivery Hop (Maximum number of hops) -----");
        for (Integer messageId : maxHops.keySet()) {
            System.out.println("Message ID " + messageId + ": " + maxHops.get(messageId));
        }
        System.out.println("----- Average number of hops -----");
        for (Integer messageId : sumHops.keySet()) {
            double avgHops = (double) sumHops.get(messageId) / (Settings.NUM_NODES - 1);
            System.out.println("Message ID " + messageId + ": " + String.format("%.2f", avgHops));
        }
        System.out.println("NodeList");
        for (AbstractNode n : nodes) {
            PlumtreeNode pn = (PlumtreeNode) n;
            // System.out.println("NodeID" + n.getNodeId());
            for (PlumtreeNode p : pn.getCloneList()) {
                p.getNodeId();
                // System.out.print(p.getNodeId() + ",");
            }
            // System.out.println();
            // System.out.println("NodeID" + n.getNodeId() + getNodeId());

        }
        System.out.println("Sumhops");
        System.out.println(sumHops);

        // System.out.println("ReportNode");
        System.out.println("NodePosition");
        for (AbstractNode p : nodes) {
            // PlumtreeNode pn = (PlumtreeNode) p;
            // System.out.println("NodeId" + p.getNodeId() + ":(" + p.getX() + "," +
            // p.getY() + ")");
        }

        /*
         * System.out.println("NodeList");// 記載中
         * for (AbstractNode n : nodes) {
         * PlumtreeNode pn = (PlumtreeNode) n;
         * System.out.println("Node ID" + n.getNodeId());
         * for (EagerPushMessage m : pn.getReceivedEagerMsgs().values()) {
         * System.out.print(m.getMessageId() + ":");
         * 
         * for (int i : m.getRouteList()) {
         * System.out.print(i + ",");// ノードのIDが出てくる
         * }
         * System.out.println();
         * 
         * }
         * }
         */

        System.out.println();

    }

    // Check if all irregular events are completed.
    // If there is no irregular event, return true.
    private boolean checkIrregularEventComplete() {
        for (AbstractEvent event : Const.EVENT_QUEUE) {
            if (!event.isRegularEvent()) {
                return false;
            }
        }
        return true;
    }

    private AbstractNode getNodeAtRandom() {
        ArrayList<AbstractNode> nodeList = new ArrayList<AbstractNode>(nodes);
        return nodeList.get(Const.RND.nextInt(nodeList.size()));
    }

    private void setEmptyEvent(long timestamp) {
        Const.EVENT_QUEUE.add(new EmptyEvent(timestamp, 0));
    }

    private void changeLossRate(double lossRate, long timestamp) {
        Const.EVENT_QUEUE.add(new ChangeLossRateEvent(lossRate, timestamp, 0, false));
    }

    public static void main(String[] args) {
        new Main().proc();
    }
}
