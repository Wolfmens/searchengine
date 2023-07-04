package searchengine.services;

public class InterapterThread implements Runnable{

    private final Thread thread;

    public InterapterThread(Thread thread){
        this.thread = thread;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
           e.printStackTrace();
        }
        thread.interrupt();
    }
}

