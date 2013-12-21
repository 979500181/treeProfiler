package org.iamzhongyong.profiler;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * �������Բ�ͳ���߳�ִ��ʱ��Ĺ��ߡ�
 *
 * @author Michael Zhou   webx��Դ��ܵ�����
 * @version $Id: Profiler.java 1291 2005-03-04 03:23:30Z baobao $
 * 
 */
public final class Profiler {
	/**����ʵ��Ĵ洢������*/
    private static final ThreadLocal<Entry> entryStack = new ThreadLocal<Entry>();
    
    /**��ʼ��ʱ*/
    public static void start() {
        start((String) null);
    }

    /**��ʼ��ʱ������һ��Entry��ʵ�����*/
    public static void start(String message) {
        entryStack.set(new Entry(message, null, null));
    }

    /**threadLocal���������������ڴ�����̳߳ص����ã�����Ҫ��һ������*/
    public static void reset() {
        entryStack.set(null);
    }

    /**����Entry��������״�ṹ����������ǽ����Root�Ľڵ㣬�Ǿ���Ҫenter����*/
    public static void enter(String message) {
        Entry currentEntry = getCurrentEntry();
        if (currentEntry != null) {
            currentEntry.enterSubEntry(message);
        }
    }

    /**�������н���֮�󣬰ѵ�ǰ��Entry��endTime�����óɵ�ǰʱ��*/
    public static void release() {
        Entry currentEntry = getCurrentEntry();
        if (currentEntry != null) {
            currentEntry.release();
        }
    }

    /**��ȡstart��end��ʱ���*/
    public static long getDuration() {
        Entry entry = (Entry) entryStack.get();
        if (entry != null) {
            return entry.getDuration();
        } else {
            return -1;
        }
    }

    /**��Entry����Ϣdump���������Դ�ӡ����־��ȥ*/
    public static String dump() {
        return dump("", "");
    }

    public static String dump(String prefix1, String prefix2) {
        Entry entry = (Entry) entryStack.get();
        if (entry != null) {
            return entry.toString(prefix1, prefix2);
        } else {
            return "";
        }
    }

    /**��ȡEntry��Ϣ*/
    public static Entry getEntry() {
        return (Entry) entryStack.get();
    }

    /**entry�к���subentry���������������ѭ����������״�Ľṹ*/
    private static Entry getCurrentEntry() {
        Entry subEntry = (Entry) entryStack.get();
        Entry entry = null;
        if (subEntry != null) {
            do {
                entry    = subEntry;
                subEntry = entry.getUnreleasedEntry();
            } while (subEntry != null);
        }
        return entry;
    }

    /**
     * ����һ����ʱ��Ԫ��
     */
    public static final class Entry {
    	//subEntries����ʾ��״���ӽڵ�
        private final List<Entry>   subEntries  = new ArrayList<Entry>(4);
        private final Object message;
        private final Entry  parentEntry;
        private final Entry  firstEntry;
        private final long   baseTime;
        private final long   startTime;
        private long         endTime;

        private Entry(Object message/*������Ϣ*/, Entry parentEntry/*���ڵ���Ϣ*/, Entry firstEntry/*��һ���ڵ�*/) {
            this.message     = message;
            this.startTime   = ProfilerSwitch.getInstance().isOpenProfilerNanoTime()==true?
            															 System.nanoTime():
            													System.currentTimeMillis();
            this.parentEntry = parentEntry;
            this.firstEntry  = (Entry) defaultIfNull(firstEntry, this);
            this.baseTime    = (firstEntry == null) ? 0 : firstEntry.startTime;
        }

        /**
         * ȡ��entry����Ϣ��
         */
        public String getMessage() {
            return defaultIfEmpty((String)message, null);
        }
        
        public static String defaultIfEmpty(String str, String defaultStr) {
            return ((str == null) || (str.length() == 0)) ? defaultStr  : str;
        }
        
        public static Object defaultIfNull(Object object, Object defaultValue) {
            return (object != null) ? object : defaultValue;
        }
        /**��ȡ��ǰ�ڵ�Ŀ�ʼʱ��*/
        public long getStartTime() {
            return (baseTime > 0) ? (startTime - baseTime): 0;
        }

        /**��ȡ��ǰ�ڵ�Ľ���ʱ��*/
        public long getEndTime() {
            if (endTime < baseTime) {
                return -1;
            } else {
                return endTime - baseTime;
            }
        }

        /**��ȡ����ʱ��*/
        public long getDuration() {
            if (endTime < startTime) {
                return -1;
            } else {
                return endTime - startTime;
            }
        }

        /**ȡ��entry�������õ�ʱ�䣬����ʱ���ȥ������entry���õ�ʱ�䡣*/
        public long getDurationOfSelf() {
            long duration = getDuration();
            if (duration < 0) {
                return -1;
            } else if (subEntries.isEmpty()) {
                return duration;
            } else {
                for (int i = 0; i < subEntries.size(); i++) {
                    Entry subEntry = (Entry) subEntries.get(i);
                    duration -= subEntry.getDuration();
                }
                if (duration < 0) {
                    return -1;
                } else {
                    return duration;
                }
            }
        }

        /**ȡ�õ�ǰentry�ڸ�entry����ռ��ʱ��ٷֱȡ�*/
        public double getPecentage() {
            double parentDuration = 0;
            double duration = getDuration();
            if ((parentEntry != null) && parentEntry.isReleased()) {
                parentDuration = parentEntry.getDuration();
            }
            if ((duration > 0) && (parentDuration > 0)) {
                return duration / parentDuration;
            } else {
                return 0;
            }
        }

        /** ȡ�õ�ǰentry�ڵ�һ��entry����ռ��ʱ��ٷֱȡ�*/
        public double getPecentageOfAll() {
            double firstDuration = 0;
            double duration = getDuration();
            if ((firstEntry != null) && firstEntry.isReleased()) {
                firstDuration = firstEntry.getDuration();
            }
            if ((duration > 0) && (firstDuration > 0)) {
                return duration / firstDuration;
            } else {
                return 0;
            }
        }

        /**ȡ��������entries��*/
        public List<Entry> getSubEntries() {
            return Collections.unmodifiableList(subEntries);
        }

        /**������ǰentry������¼����ʱ�䡣    */
        private void release() {
            endTime = ProfilerSwitch.getInstance().isOpenProfilerNanoTime()==true?
            													System.nanoTime():
            											System.currentTimeMillis();
        }

        /**�жϵ�ǰentry�Ƿ������*/
        public boolean isReleased() {
            return endTime > 0;
        }

        /**����һ���µ���entry��*/
        private void enterSubEntry(Object message) {
            Entry subEntry = new Entry(message, this, firstEntry);
            subEntries.add(subEntry);
        }

        /** ȡ��δ��������entry,�����е����һ��Ԫ��*/
        private Entry getUnreleasedEntry() {
            Entry subEntry = null;
            if (!subEntries.isEmpty()) {
                subEntry = (Entry) subEntries.get(subEntries.size() - 1);
                if (subEntry.isReleased()) {
                    subEntry = null;
                }
            }
            return subEntry;
        }

        public String toString() {
            return toString("", "");
        }

        private String toString(String prefix1, String prefix2) {
            StringBuffer buffer = new StringBuffer();
            toString(buffer, prefix1, prefix2);
            return buffer.toString();
        }

        private void toString(StringBuffer buffer, String prefix1, String prefix2) {
            buffer.append(prefix1);

            String   message        = getMessage();
            long     startTime      = getStartTime();
            long     duration       = getDuration();
            long     durationOfSelf = getDurationOfSelf();
            double   percent        = getPecentage();
            double   percentOfAll   = getPecentageOfAll();

            Object[] params = new Object[] {
                                  message, // {0} - entry��Ϣ 
            new Long(startTime), // {1} - ��ʼʱ��
            new Long(duration), // {2} - ������ʱ��
            new Long(durationOfSelf), // {3} - �������ĵ�ʱ��
            new Double(percent), // {4} - �ڸ�entry����ռ��ʱ�����
            new Double(percentOfAll) // {5} - ����ʱ�������ɵ�ʱ�����
                              };

            StringBuffer pattern = new StringBuffer("{1,number} ");

            if (isReleased()) {
                pattern.append("[{2,number}");
                if(ProfilerSwitch.getInstance().isOpenProfilerNanoTime()){
                	pattern.append("ns");
                }else{
                	pattern.append("ms");
                }

                if ((durationOfSelf > 0) && (durationOfSelf != duration)) {
                    pattern.append(" ({3,number})");
                    if(ProfilerSwitch.getInstance().isOpenProfilerNanoTime()){
                    	pattern.append("ns");
                    }else{
                    	pattern.append("ms");
                    }
                }

                
                if (percent > 0) {
                    pattern.append(", {4,number,##%}");
                }

                if (percentOfAll > 0) {
                    pattern.append(", {5,number,##%}");
                }

                pattern.append("]");
            } else {
                pattern.append("[UNRELEASED]");
            }

            if (message != null) {
                pattern.append(" - {0}");
            }

            buffer.append(MessageFormat.format(pattern.toString(), params));

            for (int i = 0; i < subEntries.size(); i++) {
                Entry subEntry = (Entry) subEntries.get(i);

                buffer.append('\n');

                if (i == (subEntries.size() - 1)) {
                    subEntry.toString(buffer, prefix2 + "`---", prefix2 + "    "); // ���һ��
                } else if (i == 0) {
                    subEntry.toString(buffer, prefix2 + "+---", prefix2 + "|   "); // ��һ��
                } else {
                    subEntry.toString(buffer, prefix2 + "+---", prefix2 + "|   "); // �м���
                }
            }
        }
    }
    
    public static String[] split(String str, String separatorChars) {
        return split(str, separatorChars, -1);
    }
    private static String[] split(String str, String separatorChars, int max) {
        if (str == null) {
            return null;
        }

        int length = str.length();

        if (length == 0) {
            return new String[0];
        }

        List<String> list = new LinkedList<String>();
        int sizePlus1 = 1;
        int i = 0;
        int start = 0;
        boolean match = false;

        if (separatorChars == null) {
            // null��ʾʹ�ÿհ���Ϊ�ָ���
            while (i < length) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = length;
                        }

                        list.add(str.substring(start, i));
                        match = false;
                    }

                    start = ++i;
                    continue;
                }

                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // �Ż��ָ�������Ϊ1������
            char sep = separatorChars.charAt(0);

            while (i < length) {
                if (str.charAt(i) == sep) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = length;
                        }

                        list.add(str.substring(start, i));
                        match = false;
                    }

                    start = ++i;
                    continue;
                }

                match = true;
                i++;
            }
        } else {
            // һ������
            while (i < length) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = length;
                        }

                        list.add(str.substring(start, i));
                        match = false;
                    }

                    start = ++i;
                    continue;
                }

                match = true;
                i++;
            }
        }

        if (match) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

}
