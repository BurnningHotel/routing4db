package com.google.code.routing4db.strategy;

import java.lang.reflect.Method;

/**
 * ����Դ·�ɲ���
 * */
public interface RoutingStrategy {
	
	/**
	 * ִ�д˲��ԣ�ѡ���Ӧ������Դ��������key���õ�RoutingHolder�У��������Ĭ������Դ
	 * ������currentDataSourekeyΪnull��
	 * */
	public void route(Object target, Method method, Object[] args);

}
