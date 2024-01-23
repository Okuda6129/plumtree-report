package simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import simulator.Const.NodeType;
import simulator.Event.AbstractEvent;
import simulator.Message.CheckMessage;
import simulator.Node.AbstractNode;
import simulator.Node.PlumtreeNode;

public class NetworkGenerator {
    private Map<AbstractNode, HashSet<AbstractNode>> nodes = new HashMap<AbstractNode, HashSet<AbstractNode>>(); // Node,
                                                                                                                 // Neighbors
    private NodeType nodeType;

    public NetworkGenerator(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Set<AbstractNode> generateNetwork() {
        // Generate network and check if it is not divided into multiple networks
        for (int i = 0; i < Settings.NUM_GEN_NETWORK_RETRY + 1; i++) {
            nodes.clear();
            for (int j = 0; j < Settings.NUM_NODES; j++) {
                switch (nodeType) {
                    case PLUMTREE:
                        nodes.put(new PlumtreeNode(j), new HashSet<AbstractNode>());
                        break;
                    default:
                }
            }

            switch (Settings.RANDOM_GRAPH_MODEL) {
                case ER:
                    generateERNetwork();
                    break;
                case WS:
                    generateWSNetwork();
                    break;
                case BA:
                    generateBANetwork();
                    break;
                case BITCOIN:
                    generateBitcoinNetwork();
                    break;
                case WM:
                    generateWMNetwork();
                default:
            }

            for (AbstractNode node : nodes.keySet()) {
                node.setNeighbors(nodes.get(node));
            }

            if (checkNetworkConnectivity()) {
                break;
            } else if (i == Settings.NUM_GEN_NETWORK_RETRY) {
                System.out.println("Failed to generate network.");
                return null;
            } else {
                System.out.println("Generated network is divided. Retrying to generate network.");
            }
        }

        return nodes.keySet();
    }

    private void generateERNetwork() {
        ArrayList<AbstractNode> nodeList = new ArrayList<AbstractNode>(nodes.keySet());
        for (int i = 0; i < nodeList.size() - 1; i++) {
            for (int j = i + 1; j < nodeList.size(); j++) {
                if (Const.RND.nextDouble() < Settings.PROBABILITY_OF_LINK) {
                    nodes.get(nodeList.get(i)).add(nodeList.get(j));
                    nodes.get(nodeList.get(j)).add(nodeList.get(i));
                }
            }
        }
    }

    private void generateWSNetwork() {
        ArrayList<AbstractNode> nodeList = new ArrayList<AbstractNode>(nodes.keySet());
        int numNodes = nodeList.size();

        // Construct a regular ring lattice
        for (int i = 0; i < numNodes; i++) {
            for (int j = 1; j <= Settings.NUM_SUCCESSORS; j++) {
                nodes.get(nodeList.get(i)).add(nodeList.get((i + j) % numNodes));
                nodes.get(nodeList.get((i + j) % numNodes)).add(nodeList.get(i));
            }
        }

        // Rewire links with probability PROBABILITY_OF_LINK
        for (int i = 0; i < numNodes; i++) {
            AbstractNode node = nodeList.get(i);
            for (int j = 1; j <= Settings.NUM_SUCCESSORS; j++) {
                if (Const.RND.nextDouble() < Settings.PROBABILITY_OF_REWIRE) {
                    nodes.get(node).remove(nodeList.get((i + j) % numNodes));
                    nodes.get(nodeList.get((i + j) % numNodes)).remove(node);

                    AbstractNode candidate = getNodeAtRandom();
                    while (candidate.equals(node) || nodes.get(node).contains(candidate)) {
                        candidate = getNodeAtRandom();
                    }
                    nodes.get(node).add(candidate);
                    nodes.get(candidate).add(node);
                }
            }
        }
    }

    private void generateBANetwork() {
        ArrayList<AbstractNode> nodeList = new ArrayList<AbstractNode>(nodes.keySet());

        // Construct a complete graph with NUM_INITIAL_NODES nodes
        for (int i = 0; i < Settings.NUM_INITIAL_NODES; i++) {
            for (int j = i + 1; j < Settings.NUM_INITIAL_NODES; j++) {
                nodes.get(nodeList.get(i)).add(nodeList.get(j));
                nodes.get(nodeList.get(j)).add(nodeList.get(i));
            }
        }

        // Number of links in the initial complete graph
        int numLinks = Settings.NUM_INITIAL_NODES * (Settings.NUM_INITIAL_NODES - 1) / 2;

        for (int i = Settings.NUM_INITIAL_NODES; i < Settings.NUM_NODES; i++) {
            int addedLinks = 0;
            while (addedLinks < Settings.NUM_INITIAL_NODES) {
                // numLinks*2 means the total number of degrees of all nodes.
                // Example: Consider there are three nodes A, B, C. Their degrees are 1, 1, and
                // 2, respectively.
                // New node D select A to connect if r=0, B if r=1, C if r=2 or r=3.
                int r = Const.RND.nextInt(numLinks * 2);
                for (int j = 0; j < i; j++) {
                    r -= nodes.get(nodeList.get(j)).size();
                    if (r < 0) {
                        if (!nodes.get(nodeList.get(i)).contains(nodeList.get(j))) {
                            nodes.get(nodeList.get(i)).add(nodeList.get(j));
                            nodes.get(nodeList.get(j)).add(nodeList.get(i));
                            addedLinks++;
                            numLinks++;
                        }
                        break;
                    }
                }
            }
        }
    }

    private void generateBitcoinNetwork() {
        HashMap<Integer, Integer> numInboundLinks = new HashMap<Integer, Integer>(); // Node ID, Number of inbound links
        for (AbstractNode node : nodes.keySet()) {
            numInboundLinks.put(node.getNodeId(), 0);
        }

        for (AbstractNode node : nodes.keySet()) {
            for (int i = 0; i < Settings.NUM_OUTBOUND_LINKS; i++) {
                AbstractNode candidate = getNodeAtRandom();
                if (candidate.equals(node) || nodes.get(node).contains(candidate)
                        || numInboundLinks.get(candidate.getNodeId()) >= Settings.NUM_MAX_INBOUND_LINKS) {
                    continue;
                } else {
                    nodes.get(node).add(candidate);
                    nodes.get(candidate).add(node);
                    numInboundLinks.put(candidate.getNodeId(), numInboundLinks.get(candidate.getNodeId()) + 1);
                }
            }
        }
    }

    private void generateWMNetwork() {
        ArrayList<AbstractNode> nodeList = new ArrayList<AbstractNode>(nodes.keySet());
        int numNodes = nodeList.size();
        // Place nodes at random
        for (int i = 0; i < numNodes; i++) {
            AbstractNode node = nodeList.get(i);
            node.setX(Const.RND.nextDouble(Settings.SIDE_LENGTH));
            node.setY(Const.RND.nextDouble(Settings.SIDE_LENGTH));
        }
        // Connect nodes within COM_RANGE
        double distSquared = Math.pow(Settings.COM_RANGE, 2);
        for (int i = 0; i < numNodes - 1; i++) {
            AbstractNode n1 = nodeList.get(i);
            for (int j = i + 1; j < numNodes; j++) {
                AbstractNode n2 = nodeList.get(j);
                if (Math.pow(n1.getX() - n2.getX(), 2) + Math.pow(n1.getY() - n2.getY(), 2) <= distSquared) {
                    nodes.get(n1).add(n2);
                    nodes.get(n2).add(n1);
                }
            }
        }
    }

    private boolean checkNetworkConnectivity() {
        boolean isConnected = true;

        AbstractNode node = nodes.keySet().iterator().next();
        node.broadcast(new CheckMessage(), 0);

        while (Const.EVENT_QUEUE.size() > 0) {
            AbstractEvent event = Const.EVENT_QUEUE.poll();
            event.process();
        }

        for (AbstractNode n : nodes.keySet()) {
            if (!n.isConnected()) {
                isConnected = false;
            }
        }

        for (AbstractNode n : nodes.keySet()) {
            n.setIsConnected(false);
        }

        return isConnected;
    }

    private AbstractNode getNodeAtRandom() {
        ArrayList<AbstractNode> nodeList = new ArrayList<AbstractNode>(nodes.keySet());
        return nodeList.get(Const.RND.nextInt(nodeList.size()));
    }

    public void outputNetwork() {
        StringBuilder sb = new StringBuilder("");
        sb.append("[" + Const.LINE_SEP);
        boolean isFirst = true;

        for (AbstractNode node : nodes.keySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("," + Const.LINE_SEP);
            }
            sb.append("{\"type\":\"generate_network\", \"content\":");
            sb.append(node.getNeighborsJson());
            sb.append("}");
        }

        Util.output(Settings.OUTPUT_FILE_PATH, new String(sb), false);
    }
}
