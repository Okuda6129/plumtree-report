package simulator.Event;

public abstract class AbstractEvent implements Comparable<AbstractEvent> {
    protected long timestamp;
    protected long procDelay;
    private boolean isRegularEvent;

    public AbstractEvent(long timestamp, long procDelay, boolean isRegularEvent) {
        this.timestamp = timestamp;
        this.procDelay = procDelay;
        this.isRegularEvent = isRegularEvent;
    }

    public abstract void process();

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isRegularEvent() {
        return isRegularEvent;
    }

    @Override
    public int compareTo(AbstractEvent o) {
        if(this.equals(o)) return 0;

        int order = Long.signum(this.timestamp - o.timestamp);
        if(order != 0) return order;

        order = System.identityHashCode(this) - System.identityHashCode(o);
        return order;
    }
}
