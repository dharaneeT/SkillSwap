//package Backend;
//
//class Action implements Runnable {
//
//	@Override
//	public void run() {
//		System.out.println("Action start" + Thread.currentThread().getId());
//		System.out.println("Action Going on...." + Thread.currentThread().getId());
//		System.out.println("Action End" + Thread.currentThread().getId());
//	}
//}
//
//public class Athread {
//
//	public static void main(String[] args) {
//		int count = 4;
//		for (int i = 0; i < count; i++) {
//			Action a = new Action();
//			Thread t = new Thread(a);
//			t.start();
//		}
//	}
//}
