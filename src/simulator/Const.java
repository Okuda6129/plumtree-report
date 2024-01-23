package simulator;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import simulator.Event.AbstractEvent;

public class Const {
    public static final Queue<AbstractEvent> EVENT_QUEUE = new PriorityQueue<AbstractEvent>();
    public static final Random RND = new Random(1);
    public static final int CHECK_MESSAGE_ID = -1;
    public static final String LINE_SEP = System.getProperty("line.separator");
    public static final int NUM_BULK_WRITE = 10000;

    public static enum RandomGraphModel {
        ER, // Erdos-Renyi model
        WS, // Watts-Strogatz model
        BA, // Barabasi-Albert model
        BITCOIN, // Simple Bitcoin model
        WM
    }

    public static enum NodeType {
        PLUMTREE
    }

    public static enum Method {
        NORMAL,
        OPTIMIZED
    }

    public static enum MessageType {
        EAGER_PUSH,
        PRUNE,
        IHAVE,
        GRAFT,
        REPORT, // add
        CHECK // CHECK message is used to check if the network is not divided
    }
}
