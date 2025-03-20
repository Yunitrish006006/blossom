package studio.vy.TeleportSystem.Component;

import java.util.UUID;

public class TeleportRequest {
    private final UUID requester;
    private final UUID target;
    private final long timestamp;

    public TeleportRequest(UUID requester, UUID target) {
        this.requester = requester;
        this.target = target;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getRequester() { return requester; }
    public UUID getTarget() { return target; }
    public long getTimestamp() { return timestamp; }
}
