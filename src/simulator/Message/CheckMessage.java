package simulator.Message;

import simulator.Const;
import simulator.Const.MessageType;

public class CheckMessage extends AbstractMessage {
    public CheckMessage() {
        super(Const.CHECK_MESSAGE_ID, MessageType.CHECK);
    }    
}
