package simulator.Event;

public class EmptyEvent extends AbstractEvent {
    public EmptyEvent(long timestamp, long procDelay) {
        super(timestamp, procDelay, false);
    }

    @Override
    public void process() {
        // Do nothing
    }
}