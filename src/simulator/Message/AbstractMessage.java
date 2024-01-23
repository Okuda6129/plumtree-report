package simulator.Message;

import simulator.Const.MessageType;

public class AbstractMessage {
    private static int messageIdCounter = 0;

    protected int messageId;
    protected MessageType messageType;

    public AbstractMessage(MessageType messageType){
        this.messageId = messageIdCounter;
        messageIdCounter++;
		this.messageType = messageType;
    }

    protected AbstractMessage(int messageId, MessageType messageType){
        this.messageId = messageId;
        this.messageType = messageType;
    }

    public int getMessageId(){
        return messageId;
    }

	public MessageType getMessageType(){
		return messageType;
	}
}
