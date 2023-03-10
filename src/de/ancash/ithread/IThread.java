package de.ancash.ithread;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class IThread extends Thread{
	
	public static void main(String[] args) throws InterruptedException {
		IThreadPoolExecutor pool = IThreadPoolExecutor.newCachedThreadPool();
		for(int i = 0; i<10; i++)
			pool.submit(() -> work());
		pool.shutdown();
	}
	
	@SuppressWarnings("nls")
	private static void work() {
		IThread.setContext("string", Math.random());
		IThread.setContext("arr", new int[] {1, 2, 3});
		IThread.setContext("err", new test());
		IThread.setContext("map", new HashMap<>());
		throw new NullPointerException();
	}
	
	static class test{
		@Override
		public String toString() {
			throw new UnsupportedOperationException("not todays"); //$NON-NLS-1$
		}
	}
	
	public static boolean isIThread() {
		return Thread.currentThread() instanceof IThread;
	}
	
	public static IThread asIThread() {
		return (IThread) Thread.currentThread();
	}
	
	protected final Runnable r;
	protected final boolean printContextOnException;
	protected final ConcurrentHashMap<String, Object> context = new ConcurrentHashMap<String, Object>();
	
	public IThread(Runnable r) {
		this(r, true);
	}
	
	public IThread(Runnable r, boolean printContextOnException) {
		this.r = r;
		this.printContextOnException = printContextOnException;
	}
	
	public static boolean clearContext() {
		if(!isIThread())
			return false;
		asIThread().context.clear();
		return true;
	}
	
	public static Object getContext(String s) {
		return asIThread().context.get(s);
	}
	
	public static boolean setContext(String s, Object o) {
		if(!isIThread())
			return false;
		asIThread().context.put(s, o);
		return true;
	}
	
	@SuppressWarnings("nls")
	public static void onThrowable(Thread thread, Throwable t) {
		if(!(thread instanceof IThread))
			return;
		IThread it = (IThread) thread;
		t.printStackTrace();
		if(it.context.isEmpty()) {
			System.err.println("no context");
		}
		System.err.println("context:");
		it.context.entrySet().forEach(entry -> {
			try {
				System.err.println(entry.getKey() + ": " + entry.getValue());
			} catch(Throwable th) {
				System.err.println("could not print value of '" + entry.getKey() + "': " + th.getLocalizedMessage());
			}
		});
	}
	
	@Override
	public void run() {
		if(!printContextOnException)
			r.run();
		else {
			try {
				r.run();
			} catch (Throwable th) {
				onThrowable(Thread.currentThread(), th);
			}
		}
	}
}
