package de.ancash.thread;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class IThread extends Thread{
	
	public static void main(String[] args) {
		IThread t = new IThread(() -> work());
		t.start();
	}
	
	@SuppressWarnings("nls")
	private static void work() {
		IThread.setContext("string", "12");
		IThread.setContext("arr", new int[] {1, 2, 3});
		IThread.setContext("err", new test());
		IThread.setContext("map", new HashMap<>());
		throw new IllegalStateException();
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
		if(!isIThread())
			return null;
		return (IThread) Thread.currentThread();
	}
	
	private final Runnable r;
	private final boolean printContextOnException;
	private final ConcurrentHashMap<String, Object> context = new ConcurrentHashMap<String, Object>();
	
	public IThread(Runnable r) {
		this(r, true);
	}
	
	public IThread(Runnable r, boolean printContextOnException) {
		this.r = r;
		this.printContextOnException = printContextOnException;
	}
	
	public void clear() {
		context.clear();
	}
	
	public static Object getContext(String s) {
		return asIThread().context.get(s);
	}
	
	public static IThread setContext(String s, Object o) {
		IThread it = asIThread();
		it.context.put(s, o);
		return it;
	}
	
	@SuppressWarnings("nls")
	@Override
	public void run() {
		if(!printContextOnException)
			r.run();
		else {
			try {
				r.run();
			} catch (Throwable th) {
				th.printStackTrace();
				if(context.isEmpty()) {
					System.err.println("no context");
				}
				System.err.println("context:");
				context.entrySet().forEach(entry -> {
					try {
						System.err.println(entry.getKey() + ": " + entry.getValue());
					} catch(Throwable t) {
						System.err.println("could not print value of '" + entry.getKey() + "': " + t.getLocalizedMessage());
					}
				});
			}
		}
	}
}
