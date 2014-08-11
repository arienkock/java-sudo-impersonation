package nl.supposed.security;

import static nl.supposed.security.User.ROOT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

public class AppSec {
	private static InheritableThreadLocal<Queue<User>> userStack = new InheritableThreadLocal<Queue<User>>() {
		protected Queue<User> childValue(Queue<User> parentQueue) {
			return Collections.asLifoQueue(new LinkedList<User>(parentQueue));
		}

		protected Queue<User> initialValue() {
			return Collections.asLifoQueue(new LinkedList<User>());
		}
	};

	public static void sudo(final Runnable task) {
		sudo(ROOT, task);
	}

	public static void sudo(User user, final Runnable task) {
		try {
			sudo(user, new Callable<Void>() {
				public Void call() throws Exception {
					task.run();
					return null;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T sudo(Callable<T> task) throws Exception {
		return sudo(ROOT, task);
	}

	public static <T> T sudo(User user, Callable<T> task) throws Exception {
		userStack.get().add(user);
		try {
			return task.call();
		} finally {
			userStack.get().remove();
		}
	}

	public static boolean isRoot() {
		return currentUser() == ROOT;
	}

	public static User currentUser() {
		return userStack.get().peek();
	}

	public static List<User> getIdentityChain() {
		// create a copy because no one but us should have access to the actual
		// stack
		return new ArrayList<User>(userStack.get());
	}
}
