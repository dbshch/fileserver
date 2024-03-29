package uni.akilis.file_server.util;

/**
 * Count the time cost.
 * @author leo
 */
public class TimeConsume {
	private long beginTimeMillis;
	private long endTimeMillis;
	private long timeConsumeMillis;
	public TimeConsume(){
		beginTimeMillis = System.currentTimeMillis();
	}
	public void resetBeginTime(){
		beginTimeMillis = System.currentTimeMillis();
	}
	public long getTimeConsume(){
		endTimeMillis = System.currentTimeMillis();
		timeConsumeMillis = endTimeMillis-beginTimeMillis;
		return timeConsumeMillis;
	}
}
