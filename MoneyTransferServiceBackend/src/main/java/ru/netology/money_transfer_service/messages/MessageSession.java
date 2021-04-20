package ru.netology.money_transfer_service.messages;

public class MessageSession {
    private final Object readLock;
    private final Object writeLock;
    private boolean isWrote;

    public MessageSession(Object readLock, Object writeLock) {
        this.readLock = readLock;
        this.writeLock = writeLock;
    }

    public boolean isWrote() {
        return isWrote;
    }

    public void setWrote(boolean wrote) {
        isWrote = wrote;
    }

    public Object getReadLock() {
        return readLock;
    }

    public Object getWriteLock() {
        return writeLock;
    }
}
