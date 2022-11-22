package frasc;

import java.util.concurrent.locks.Condition;

public class BinarySemaphore {
    Mutex mutex;
    Condition condition;
    boolean value;

    BinarySemaphore(boolean initialValue) {
        initialValue = false;
        value = initialValue;
    }

    public void hold() {
        mutex.lock();
        while (!this.value) {
            try {
                condition.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        this.value = false;
    }

    public void post() {
        synchronized (this) {
            mutex.lock();
            this.value = true;
        }
        condition.notify();
    }
}
