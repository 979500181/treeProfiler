package org.iamzhongyong.profiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ��ӱ��֮�󣬽���ͳ��ʱ��
 * �����Ƿ�������������
 * 
 * ʹ�ò��裺
 * 1������Ҫ��Profiler�ĵط������@ProfilerAnno ���ע�⣬�����ڷ����ϻ��������棻
 * 2��ϵͳĬ���ǹرյģ�Ҳ����Ĭ�ϲ�����������ܣ�
 * 3�������Ҫ��ʼ��������ͨ��curl���ظ�һ�£�����λ���ڣ�ProfilerSwitch��
 * 4��������ش򿪣�Ĭ����500ms��ʱ�򣬲Ż��ӡ��־�������������ʱ�䣬���Զ�̬������ProfilerSwitch��
 * 5����־�ļ����Խ���ָ����
 * 6������ǿ����ܻ����Ų����⣬������֮�󣬿��ؼǵùرգ�
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfilerAnno {
	String desc() default "";
}
