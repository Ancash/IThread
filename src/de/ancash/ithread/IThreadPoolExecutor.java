package de.ancash.ithread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class IThreadPoolExecutor extends ThreadPoolExecutor {

	public static IThreadPoolExecutor newFixedThreadPool(int nThreads) {
		return new IThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	public static IThreadPoolExecutor newCachedThreadPool() {
		return new IThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}

	public IThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, IThreadFactory.INSTANCE);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return submit(() -> {
			task.run();
			return null;
		});
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return submit(() -> {
			task.run();
			return result;
		});
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		
		if (t == null && r instanceof Future<?>) {
			Future<?> future = (Future<?>) r;
			try {
				future.get();
			} catch (CancellationException ce) {
				t = ce;
			} catch (ExecutionException ee) {
				t = ee.getCause();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt(); // ignore/reset
			}
		}
		
		if (t != null)
			IThread.onThrowable(Thread.currentThread(), t);
		IThread.clearContext();
	}
}
