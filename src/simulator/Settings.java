package simulator;

import simulator.Const.Method;
import simulator.Const.NodeType;
import simulator.Const.RandomGraphModel;

public class Settings {
    // Parameters
    public static final boolean FILE_OUTPUT = false; // Enable file output. (DEFAULT: false)
    public static final String OUTPUT_FILE_PATH = "output.json"; // Output file path. (DEFAULT: "output.json")

    public static final int NUM_NODES = 100; // Number of nodes. (DEFAULT: 10000)
    public static final long PROCESS_DELAY = 1; // Internal processing delay of each node in milliseconds. (DEFAULT: 1)
    public static final long COMM_DELAY = 10; // Communication delay between nodes in milliseconds. (DEFAULT: 10)
    public static double LOSS_RATE = 0.00; // Loss rate of messages. (DEFAULT: 0.0)

    // public static final int REPORT_NODES = 3;// NodeID of Report Message
    // Sender(Default: 3)
    public static final int NUM_GEN_NETWORK_RETRY = 2; // If the generated network is divided, retry to generate network
                                                       // NUM_GEN_NETWORK_RETRY times. (DEFAULT: 2)
    public static final RandomGraphModel RANDOM_GRAPH_MODEL = RandomGraphModel.WM; // Random graph model. Abailable
                                                                                   // models are ER, WS, BA, and
                                                                                   // BITCOIN. (DEFAULT: BA)
    // ER: Erdos-Renyi model
    // WS: Watts-Strogatz model
    // BA: Barabasi-Albert model
    // BITCOIN: Simple Bitcoin model
    // WM: Wireless mesh network model
    public static final NodeType NODE_TYPE = NodeType.PLUMTREE; // Node type. Currently, available type is PLUMTREE
                                                                // only. (DEFAULT: PLUMTREE)

    public static final long LAZY_PUSH_INTERVAL = 200; // Interval of lazy push in milliseconds. (Default: 1000)
    public static final long MISSING_EAGER_WAITING_TIME = 1000; // Waiting time to sending GRAFT from receiving IHAVE in
                                                                // milliseconds. (Default: 1000)

    public static final Method METHOD = Method.NORMAL; // Available methods are NORMAL and OPTIMIZED. (DEFAULT: NORMAL)
    public static final int OPT_THRESHOLD = 4; // Threshold of the difference of hops in OPTIMIZED method. (DEFAULT: 4)

    // Parameters for network generation based on ER model
    public static final double PROBABILITY_OF_LINK = 0.2; // Probability to link a node pair. (DEFAULT: 0.001)

    // Parameters for network generation based on WS model
    public static final int NUM_SUCCESSORS = 4; // Each node connects to NUM_SUCCESSORS neighbors on each side in a ring
                                                // topology. (DEFAULT: 4)
    public static final double PROBABILITY_OF_REWIRE = 0.01; // Probability to rewire a link. (DEFAULT: 0.01)

    // Parameters for network generation based on BA model
    public static final int NUM_INITIAL_NODES = 4; // Number of initial nodes. (DEFAULT: 4)

    // Parameters for network generation based on Bitcon model
    public static final int NUM_OUTBOUND_LINKS = 8; // Number of outbound links. (DEFAULT: 8)
    public static final int NUM_MAX_INBOUND_LINKS = 125; // Maximum number of inbound links. (DEFAULT: 125)

    // Parameters for network generation based on WM model
    public static final int SIDE_LENGTH = 1000; // Sides length in meters for a square field to place nodes. (DEFAULT:
                                                // 100)
    public static final int COM_RANGE = 200; // The maximum distance allowing to communicate. (DEFAULT: 10)
}
