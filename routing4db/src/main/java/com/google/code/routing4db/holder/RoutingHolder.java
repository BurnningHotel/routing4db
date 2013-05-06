package com.google.code.routing4db.holder;


/**
 * ��ŵ�ǰ����Դ·�ɶ�Ӧ��key
 * */
public class RoutingHolder {
	
	/**
	 * ��������Դ·�ɶ�Ӧ��key
	 * */
	private static final ThreadLocal<String> routingKeyHolder = new ThreadLocal<String>();
	
	public static String getCurrentDataSourceKey(){
		return routingKeyHolder.get();
	}
	
	/**
	 * ��������Դ��·��key
	 * */
	public static void setCurrentDataSourceKey(String dataSourceKey){
		routingKeyHolder.set(dataSourceKey);
	}
	
	public static void clean(){
		routingKeyHolder.set(null);
	}

}
