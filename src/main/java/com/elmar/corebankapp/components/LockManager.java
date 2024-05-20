package com.elmar.corebankapp.components;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockManager {

    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public void lock(long accountId) {
        ReentrantLock lock = lockMap.computeIfAbsent(accountId, id -> new ReentrantLock());
        lock.lock();
    }

    public void unlock(long accountId) {
        ReentrantLock lock = lockMap.get(accountId);
        if (lock != null) {
            lock.unlock();
        }
    }

    public void removeLock(long accountId) {
        ReentrantLock lock = lockMap.get(accountId);
        if (lock != null && !lock.isLocked()) {
            lockMap.remove(accountId);
        }
    }
}
