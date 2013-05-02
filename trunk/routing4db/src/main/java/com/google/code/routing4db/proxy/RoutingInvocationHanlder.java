package com.google.code.routing4db.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.code.routing4db.holder.RoutingHolder;
import com.google.code.routing4db.strategy.RoutingStrategy;


/**
 * ��̬�������ؽӿڵ�����
 * */
public class RoutingInvocationHanlder implements InvocationHandler {

	/**
	 * �������
	 * */
	private Object proxyTarget;
	
	/**
	 * ����ӿ��ϵķ���
	 * */
	private Map<Method,Object> proxyInterfaceMethods;
	
	/**
	 * ·�ɲ���
	 * */
	private RoutingStrategy routingStrategy;
	
	
	/**
	 * @param proxyTarget �������
	 * @param interfaceClass �ӿ�class
	 * @param routingStrategy ·�ɲ���
	 * */
	public RoutingInvocationHanlder(Object proxyTarget,Class<?> interfaceClass, RoutingStrategy routingStrategy) {
		super();
		if(proxyTarget == null){
			throw new IllegalArgumentException("proxyTarget must not be null");
		}
		if(interfaceClass == null){
			throw new IllegalArgumentException("interfaceClass must be interface class");
		}
		if(routingStrategy == null){
			throw new IllegalArgumentException("routingStrategy must not be null");
		}
		
		this.proxyTarget = proxyTarget;
		this.routingStrategy = routingStrategy;
		
		/**
		 * ����ӿ��ϵķ��������Խӿ��ϵķ���ִ��·���߼�
		 * */
		if(!interfaceClass.isInterface()){ 
			throw new IllegalArgumentException("interfaceClass must be interface class");
		}
		proxyInterfaceMethods = new HashMap<Method,Object>();
		for(Method method : interfaceClass.getMethods()){
			proxyInterfaceMethods.put(method, null);
		}
		for(Class<?> parentInterface : interfaceClass.getInterfaces()){
			for(Method method : parentInterface.getMethods()){
				proxyInterfaceMethods.put(method, null);
			}
		}
	}

    /**
     * �Խӿ��ϵķ���ִ��·���߼���Ȼ��ί�и�ʵ�ʶ�����������ֱ��ί�и��������
     * */
	public Object invoke(Object proxy, Method method, Object[] args)throws Throwable {
		if(!proxyInterfaceMethods.containsKey(method)){
			return method.invoke(proxyTarget, args);
		}
		try{
			routingStrategy.route(proxyTarget, method, args);
			return method.invoke(proxyTarget, args);
		}finally{
			RoutingHolder.clean();
		}
	}

}
