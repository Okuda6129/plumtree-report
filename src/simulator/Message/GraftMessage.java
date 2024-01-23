package simulator.Message;

import java.util.HashSet;
import simulator.Const.MessageType;

public class GraftMessage extends AbstractMessage {
    private HashSet<Integer> msgIds = new HashSet<Integer>();    // IDs of missing messages

    public GraftMessage() {
        super(MessageType.GRAFT);
    }
    
    public HashSet<Integer> getMsgIds(){
        return msgIds;
    }
}
