package de.ancash.thread;

import java.util.concurrent.ThreadFactory;

public class IThreadFactory implements ThreadFactory{

	@Override
	public Thread newThread(Runnable r) {
		return new IThread(r);
	}
	
	public Thread newThread(Runnable r, boolean b) {
		return new IThread(r, b);
	}
}
