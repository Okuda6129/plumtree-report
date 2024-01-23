package simulator.Event;

import simulator.Settings;

public class ChangeLossRateEvent extends AbstractEvent {
    private double lossRate;

    public ChangeLossRateEvent(double lossRate, long timestamp, long procDelay, boolean isRegularEvent) {
        super(timestamp, procDelay, isRegularEvent);
        this.lossRate = lossRate;
    }

    @Override
    public void process() {
        Settings.LOSS_RATE = lossRate;
    }
}