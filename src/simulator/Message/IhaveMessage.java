package simulator.Message;

import java.util.HashMap;
import simulator.Const.MessageType;

public class IhaveMessage extends AbstractMessage {
    private HashMap<Integer, Integer> msgIds = new HashMap<Integer, Integer>();    // Message ID to advertise and the hop count.

    public IhaveMessage() {
        super(MessageType.IHAVE);
    }
    
    public HashMap<Integer, Integer> getMsgIds(){
        return msgIds;
    }
}
