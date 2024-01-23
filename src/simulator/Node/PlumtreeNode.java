package simulator.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.PlainDocument;

import simulator.CommunicationLog;
import simulator.Const;
import simulator.Const.MessageType;
import simulator.Const.Method;
import simulator.Settings;
import simulator.Util;
import simulator.Event.AbstractEvent;
import simulator.Event.CommunicationEvent;
import simulator.Event.GraftEvent;
import simulator.Message.AbstractMessage;
import simulator.Message.EagerPushMessage;
import simulator.Message.GraftMessage;
import simulator.Message.IhaveMessage;
import simulator.Message.ReportMessage;

public class PlumtreeNode extends AbstractNode {
    private Set<PlumtreeNode> eagerPushPeers = new HashSet<PlumtreeNode>();
    private Set<PlumtreeNode> lazyPushPeers = new HashSet<PlumtreeNode>();
    private List<PlumtreeNode> RouteList = new ArrayList<PlumtreeNode>();// new Node have List
    private List<PlumtreeNode> CloneList = new ArrayList<PlumtreeNode>();
    private Map<Integer, EagerPushMessage> receivedEagerMsgs = new HashMap<Integer, EagerPushMessage>(); // Message ID,
                                                                                                         // Message
    private Deque<EagerPushMessage> lazyQueue = new ArrayDeque<EagerPushMessage>();

    public List<PlumtreeNode> getCloneList() {// getterの設定
        return this.CloneList;
    }

    public List<PlumtreeNode> getRouteList() {
        return this.RouteList;
    }

    public PlumtreeNode(int nodeId) {
        super(nodeId);
    }

    public void setNeighbors(HashSet<AbstractNode> neighbors) {
        for (AbstractNode neighbor : neighbors) {
            if (!neighbor.equals(this))
                eagerPushPeers.add((PlumtreeNode) neighbor);
        }
    }

    public void send(AbstractNode receiver, AbstractMessage msg, long timestamp) {
        switch (msg.getMessageType()) {
            case EAGER_PUSH:
            case CHECK:
            case PRUNE:
            case REPORT:
            case GRAFT:
                if (receiver.equals(this)) {
                    Const.EVENT_QUEUE.add(new CommunicationEvent(this, receiver, msg, timestamp, 0, false));
                } else {
                    Const.EVENT_QUEUE
                            .add(new CommunicationEvent(this, receiver, msg, timestamp, Settings.COMM_DELAY, false));
                }
                break;
            case IHAVE:
                if (receiver.equals(this)) {
                    Const.EVENT_QUEUE.add(new CommunicationEvent(this, receiver, msg, timestamp, 0, true));
                } else {
                    Const.EVENT_QUEUE
                            .add(new CommunicationEvent(this, receiver, msg, timestamp, Settings.COMM_DELAY, true));
                }
                break;

            default:
        }
    }

    public void receive(AbstractNode sender, AbstractMessage msg, long timestamp) {
        switch (msg.getMessageType()) {
            case CHECK:
                if (!isConnected) { // not yet received CHECK message.
                    isConnected = true;
                    for (AbstractNode to : eagerPushPeers) {
                        if (!to.equals(sender))
                            send(to, msg, timestamp);
                    }
                }
                break;
            case EAGER_PUSH:
                receiveEagerPush((PlumtreeNode) sender, (EagerPushMessage) msg, timestamp);
                break;
            case PRUNE:
                if (eagerPushPeers.contains(sender)) {
                    updateLink(null, (PlumtreeNode) sender, (PlumtreeNode) sender, null, timestamp);
                }
                break;

            case REPORT:
                receiveReport((PlumtreeNode) sender, (ReportMessage) msg, timestamp);
                break;
            case IHAVE:
                receiveIhave((PlumtreeNode) sender, (IhaveMessage) msg, timestamp);
                break;
            case GRAFT:
                GraftMessage graftMsg = (GraftMessage) msg;
                updateLink((PlumtreeNode) sender, null, null, (PlumtreeNode) sender, timestamp);
                for (int id : graftMsg.getMsgIds()) {
                    send(sender, receivedEagerMsgs.get(id).cloneToForward(this), timestamp + Settings.PROCESS_DELAY);
                }
                break;
            default:
        }
    }

    private void receiveEagerPush(PlumtreeNode sender, EagerPushMessage msg, long timestamp) {
        int msgId = msg.getMessageId();

        if (receivedEagerMsgs.keySet().contains(msgId)) {
            if (eagerPushPeers.contains(sender)) {
                updateLink(null, sender, sender, null, timestamp);

                // System.out.println(CloneList + "" + nodeId);
                send(sender, new AbstractMessage(MessageType.PRUNE), timestamp + Settings.PROCESS_DELAY);
            }
            return;
        }

        receivedEagerMsgs.put(msgId, msg);
        lazyQueue.add(msg);
        RouteList = msg.getReportList();// getReportList
        CloneList.clear();
        CloneList.addAll(RouteList);
        // CloneList = RouteList;// Cloneの作り方が分からないです
        // CloneList.add(nodeId);// nodeIdを追加

        /*
         * RouteList = msg.getReportList();// getReportList
         * CloneList.addAll(RouteList);
         * // CloneList = RouteList;// Cloneの作り方が分からないです
         * CloneList.add(nodeId);// nodeIdを追加
         * 
         * System.out.println(CloneList + "" + nodeId);
         */

        /*
         * public EagerPushMessage cloneToForward(int nodeId) { //
         * ReportList.add(nodeId);
         * EagerPushMessage msg = new EagerPushMessage(messageId, numHops + 1);
         * msg.ReportList.addAll(this.ReportList);
         * msg.ReportList.add(nodeId);
         * return msg;
         * }
         */

        for (PlumtreeNode to : eagerPushPeers) {
            if (to.equals(sender))
                continue;
            send(to, msg.cloneToForward(this), timestamp + Settings.PROCESS_DELAY);
        }

        if (Settings.METHOD.equals(Method.OPTIMIZED)) {
            IhaveMessage ihaveMsg = null;
            PlumtreeNode ihaveSender = null;
            int lazyHop = Integer.MAX_VALUE; // Minimum hop among IHAVE messages including the message ID of the eager
                                             // push.
            for (CommunicationLog log : logList) {
                if (log.getMsg().getMessageType().equals(MessageType.IHAVE) && log.getFrom() != null) {
                    IhaveMessage m = (IhaveMessage) log.getMsg();
                    if (m.getMsgIds().containsKey(msgId)) {
                        if (lazyHop > m.getMsgIds().get(msgId) + 1) {
                            lazyHop = m.getMsgIds().get(msgId) + 1;
                            ihaveMsg = m;
                            ihaveSender = (PlumtreeNode) log.getFrom();
                        }
                    }
                }
            }

            if (ihaveMsg != null && !sender.equals(this)) {
                int eagerHop = msg.getNumHops();
                if (eagerHop - lazyHop > Settings.OPT_THRESHOLD) {
                    updateLink(ihaveSender, sender, sender, ihaveSender, timestamp);
                    send(sender, new AbstractMessage(MessageType.PRUNE), timestamp + Settings.PROCESS_DELAY);

                    // The Message ID set included in the GRAFT message is empty, so the IHAVE
                    // sender will not send EAGER_PUSH and just will update its link.
                    send(ihaveSender, new GraftMessage(), timestamp + Settings.PROCESS_DELAY);
                }
            }
        }
    }

    private void receiveReport(PlumtreeNode sender, ReportMessage msg, long timestamp) {
        // System.out.println(sender.getNodeId());

        if (!CloneList.isEmpty()) {
            AbstractNode latest = CloneList.get(CloneList.size() - 1);
            System.out.print(nodeId + ",");
            // List<PlumtreeNode> lazyPushPeersList = new ArrayList<>(this.lazyPushPeers);//
            // add
            // PlumtreeNode lazyPushPeer = (PlumtreeNode) lazyPushPeersList.get(1);// add
            send(latest, msg, timestamp);
            // send(lazyPushPeer, msg, timestamp);// add
        } else
            System.out.println("receive" + nodeId);
    }

    private void receiveIhave(PlumtreeNode sender, IhaveMessage msg, long timestamp) {
        if (sender.equals(this)) {
            Iterator<EagerPushMessage> itr = lazyQueue.iterator();
            while (itr.hasNext()) {
                EagerPushMessage lazyMsg = itr.next();
                msg.getMsgIds().put(lazyMsg.getMessageId(), lazyMsg.getNumHops());
                itr.remove();
            }

            for (PlumtreeNode lazyPushPeer : lazyPushPeers) {
                send(lazyPushPeer, msg, timestamp + Settings.PROCESS_DELAY);
            }
            Const.EVENT_QUEUE.add(new CommunicationEvent(this, this, new IhaveMessage(),
                    timestamp + Settings.LAZY_PUSH_INTERVAL, 0, true));
        } else {
            HashSet<Integer> unreceivedEagerMsgIds = new HashSet<Integer>();
            for (int id : msg.getMsgIds().keySet()) {
                if (!receivedEagerMsgs.keySet().contains(id)) {
                    unreceivedEagerMsgIds.add(id);
                } else if (Settings.METHOD.equals(Method.OPTIMIZED)) {
                    PlumtreeNode eagerSender = null;
                    for (CommunicationLog log : logList) {
                        // Find the first received EAGER_PUSH message with the message ID.
                        // logList is sorted by the order of execution, so we can find it by searching
                        // from the beginning of logList.
                        if (log.getMsg().getMessageId() == id && log.getFrom() != null) {
                            eagerSender = (PlumtreeNode) log.getFrom();
                            break;
                        }
                    }

                    // If eagerSender is staill in eagerPushPeers, then check the optimization
                    // possibility.
                    if (eagerPushPeers.contains(eagerSender)) {
                        int eagerHop = receivedEagerMsgs.get(id).getNumHops();
                        int lazyHop = msg.getMsgIds().get(id) + 1;
                        if (eagerHop - lazyHop > Settings.OPT_THRESHOLD) {
                            if (eagerSender == null)
                                continue;

                            updateLink(sender, eagerSender, eagerSender, sender, timestamp);
                            send(eagerSender, new AbstractMessage(MessageType.PRUNE),
                                    timestamp + Settings.PROCESS_DELAY);

                            // The Message ID set included in the GRAFT message is empty, so the IHAVE
                            // sender will not send EAGER_PUSH and just will update its link.
                            send(sender, new GraftMessage(), timestamp + Settings.PROCESS_DELAY);
                        }
                    }
                }
            }
            if (unreceivedEagerMsgIds.size() > 0) {
                Const.EVENT_QUEUE.add(new GraftEvent(this, sender, unreceivedEagerMsgIds,
                        timestamp + Settings.MISSING_EAGER_WAITING_TIME, 0, false));
            }
        }
    }

    public void broadcast(AbstractMessage msg, long timestamp) {
        send(this, msg, timestamp);
    }

    // 通報メソッド
    public void report(AbstractMessage msg, long timestamp) {
        send(this, msg, timestamp);
    }

    public void bootup(long timestamp) {
        send(this, new IhaveMessage(), timestamp);
    }

    public void restore(long timestamp) {
        boolean needRestartLazyPush = true;
        for (AbstractEvent event : Const.EVENT_QUEUE) {
            if (event instanceof CommunicationEvent) {
                CommunicationEvent commEvent = (CommunicationEvent) event;
                if (commEvent.getMsg().getMessageType().equals(MessageType.IHAVE) && commEvent.getSender().equals(this)
                        && commEvent.getReceiver().equals(this)) {
                    needRestartLazyPush = false;
                    break;
                }
            }
        }
        if (needRestartLazyPush) {
            bootup(timestamp + Const.RND.nextInt((int) Settings.LAZY_PUSH_INTERVAL));
        }
    }

    public String getNeighborsJson() {
        StringBuilder sb = new StringBuilder("");
        sb.append("{\"node_id\":" + nodeId + ", \"eager_push_peers\":[");
        Iterator<PlumtreeNode> itr = eagerPushPeers.iterator();
        while (itr.hasNext()) {
            PlumtreeNode eager = itr.next();
            if (itr.hasNext())
                sb.append(eager.getNodeId() + ", ");
            else
                sb.append(eager.getNodeId());
        }
        sb.append("], \"lazy_push_peers\":[");
        itr = lazyPushPeers.iterator();
        while (itr.hasNext()) {
            PlumtreeNode lazy = itr.next();
            if (itr.hasNext())
                sb.append(lazy.getNodeId() + ", ");
            else
                sb.append(lazy.getNodeId());
        }
        sb.append("]}");

        return new String(sb);
    }

    public void updateLink(PlumtreeNode addToEager, PlumtreeNode removeFromeEager, PlumtreeNode addToLazy,
            PlumtreeNode removeFromLazy, long timestamp) {
        if (addToEager != null)
            eagerPushPeers.add(addToEager);
        if (removeFromeEager != null)
            eagerPushPeers.remove(removeFromeEager);
        if (addToLazy != null)
            lazyPushPeers.add(addToLazy);
        if (removeFromLazy != null)
            lazyPushPeers.remove(removeFromLazy);

        if (Settings.FILE_OUTPUT) {
            StringBuilder sb = new StringBuilder("," + Const.LINE_SEP);
            sb.append("{\"type\":\"update_link\", \"content\":{\"timestamp\":" + timestamp + ", \"node_id\":" + nodeId);
            sb.append(", \"eager_push_peers\":[");
            Iterator<PlumtreeNode> itr = eagerPushPeers.iterator();
            while (itr.hasNext()) {
                PlumtreeNode eager = itr.next();
                if (itr.hasNext())
                    sb.append(eager.nodeId + ", ");
                else
                    sb.append(eager.nodeId);
            }

            sb.append("], \"lazy_push_peers\":[");
            itr = lazyPushPeers.iterator();
            while (itr.hasNext()) {
                PlumtreeNode lazy = itr.next();
                if (itr.hasNext())
                    sb.append(lazy.nodeId + ", ");
                else
                    sb.append(lazy.nodeId);
            }
            sb.append("]}}");
            Util.output(Settings.OUTPUT_FILE_PATH, new String(sb), true);
        }
    }

    public Map<Integer, EagerPushMessage> getReceivedEagerMsgs() {
        return this.receivedEagerMsgs;
    }
}
