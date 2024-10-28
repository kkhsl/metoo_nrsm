package com.metoo.nrsm.core.config.utils.gather.strategy;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-23 16:04
 */
public class DataCollector implements Runnable{

    private Context context;

    private DataCollectionStrategy strategy;

    public DataCollector(Context context, DataCollectionStrategy strategy) {
        this.context = context;
        this.strategy = strategy;
    }
    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            strategy.collectData(context);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(context.getLatch() != null){
                context.getLatch().countDown();
            }
        }
    }
}
