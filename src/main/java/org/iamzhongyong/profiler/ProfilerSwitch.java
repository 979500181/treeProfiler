package org.iamzhongyong.profiler;
/**
 * profiler�Ŀ������ǣ�Ŀǰ������һ����̬��������Ҫ�ⲿ�������޸�
 */
public class ProfilerSwitch {

	private static ProfilerSwitch instance = new ProfilerSwitch();
	
	public static ProfilerSwitch getInstance(){
		return instance;
	}
	

	/**
	 * �Ƿ�򿪴�ӡ��־�Ŀ���
	 */
	private boolean openProfilerTree = false;
	
	/**
	 * ��ʱʱ��
	 */
	private long invokeTimeout = 500;
	
	/**
	 * �Ƿ��ӡ����
	 * @return
	 */
	private boolean openProfilerNanoTime = false;
	
	public boolean isOpenProfilerTree() {
		return openProfilerTree;
	}

	public void setOpenProfilerTree(boolean openProfilerTree) {
		this.openProfilerTree = openProfilerTree;
	}

	public long getInvokeTimeout() {
		return invokeTimeout;
	}

	public void setInvokeTimeout(long invokeTimeout) {
		this.invokeTimeout = invokeTimeout;
	}

	public boolean isOpenProfilerNanoTime() {
		return openProfilerNanoTime;
	}

	public void setOpenProfilerNanoTime(boolean openProfilerNanoTime) {
		this.openProfilerNanoTime = openProfilerNanoTime;
	}
	
	
}
