package nl.supposed.security;

import static nl.supposed.security.AppSec.currentUser;
import static nl.supposed.security.AppSec.getIdentityChain;
import static nl.supposed.security.AppSec.isRoot;
import static nl.supposed.security.AppSec.sudo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import nl.supposed.security.User;

import org.junit.Test;

public class AppSecTest {
	private volatile int userCount = 0;
	private volatile int errorCount = 0;
	private Vector<Thread> allThreads = new Vector<Thread>();
	private int maxDepth = 5;
	private int spread = 3;

	/**
	 * test nesting user identities
	 */
	@Test
	public void nestingTest() {
		final User jack = new User("Jack");
		sudo(jack, new Runnable() {
			public void run() {
				assertTrue("same user", jack == currentUser());
				final User janet = new User("Janet");
				sudo(janet, new Runnable() {
					public void run() {
						assertTrue("same user", janet == currentUser());
						sudo(new Runnable() {
							public void run() {
								assertTrue("is root user", isRoot());
								System.out.println(getIdentityChain());
							}
						});
						System.out.println(getIdentityChain());
					}
				});
				System.out.println(getIdentityChain());
			}
		});
	}

	@Test
	public void testConcurrency() throws InterruptedException {
		doInThread(0, null);
		int maxThreads = (int) Math.pow(spread, maxDepth);
		int currentThreads = 0;
		while ((currentThreads = allThreads.size()) < maxThreads) {
			System.out.println(currentThreads + " of " + maxThreads
					+ " waiting..");
			Thread.sleep(100);
		}
		boolean allFinished = false;
		while (!allFinished) {
			boolean deadness = true;
			for (Thread t : new ArrayList<Thread>(allThreads)) {
				deadness = deadness && !t.isAlive();
			}
			allFinished = deadness;
			System.out.println("They're not ALL dead. waiting.");
			Thread.sleep(100);
		}
		assertEquals("no errors", errorCount, 0);
	}

	private void doInThread(final int depth, final User parent) {
		if (depth < maxDepth) {
			for (int iteration = 0; iteration < spread; iteration++) {
				final int currentIteration = iteration;
				Thread thread = new Thread(new Runnable() {
					public void run() {
						final User sudoUser = new User(String.format(
								"id %d, depth %d, run %d", userCount++, depth,
								currentIteration));
						sudo(sudoUser, new Runnable() {
							public void run() {
								doInThread(depth + 1, sudoUser);
								try {
									long start = System.currentTimeMillis();
									while (System.currentTimeMillis() - start < 1000) {
										assertTrue("same user",
												sudoUser == currentUser());
										List<User> identityChain = getIdentityChain();
										assertTrue(
												"correct depth and chan size",
												identityChain.size() == depth + 1);
										if (identityChain.size() > 1) {
											assertTrue("correct parent",
													parent == identityChain
															.get(1));
										}
									}
								} catch (Throwable t) {
									errorCount++;
								}
								System.out.println(getIdentityChain());
							}
						});
					}
				});
				allThreads.add(thread);
				thread.start();
			}
		}
	}
}
