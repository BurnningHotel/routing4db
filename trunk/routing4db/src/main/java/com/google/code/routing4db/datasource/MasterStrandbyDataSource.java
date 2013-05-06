package com.google.code.routing4db.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

public class MasterStrandbyDataSource extends AbstractDataSource implements InitializingBean{

	/**
	 * ����ͱ��⣬
	 * */
	private Object masterDataSource;
	
	private Object standbyDataSource;
	
	
	private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
	
	/**
	 * ������ı�׼����Դ
	 * */
	private DataSource resolvedMasterDataSource;
	
	private DataSource resolvedStandbyDataSource;

	/**
	 * ��ǰ��������
	 * */
	protected DataSource currentDataSource;
	
	
	/**
	 * ���ʱ����, ��λms Ĭ��10��
	 * */
	private long checkTimeInterval = 10000;
	/**
	 * ���������ļ�
	 * */
	private Properties configProperties;
	
	@Override
	public Connection getConnection() throws SQLException {
		try{
			return this.getCurrentDataSource().getConnection();
		}catch(SQLException sqle){
			 logger.error("Get Connection Exception " + currentDataSource , sqle);
			 this.switchToAvailableDataSource(); //�Զ��л�
			 throw sqle;
		}
	}

	@Override
	public Connection getConnection(String username, String password)throws SQLException {
		try{
		  return this.getCurrentDataSource().getConnection(username, password);
		}catch(SQLException sqle){
			 logger.error("Get Connection With Args Exception " + currentDataSource , sqle);
			 this.switchToAvailableDataSource(); //�Զ��л�
			 throw sqle;
		}
	}

	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.masterDataSource == null) {
			throw new IllegalArgumentException("Property 'masterDataSource' is required");
		}
		if(this.standbyDataSource == null){
			throw new IllegalArgumentException("Property 'standbyDataSource' is required");
		}
		if(configProperties != null){
			String checkTimeIntervalStr = configProperties.getProperty("checkTimeInterval");
			if(checkTimeIntervalStr != null){
				checkTimeInterval = Long.parseLong(checkTimeIntervalStr);
			}
		}
		//�����������Դ
		resolvedMasterDataSource = this.resolveSpecifiedDataSource(masterDataSource);
		resolvedStandbyDataSource = this.resolveSpecifiedDataSource(standbyDataSource);
		currentDataSource  = this.resolvedMasterDataSource;
		Thread thread = new CheckMasterAvailableDaemonThread();
		thread.start();
	}


	
	/**
	 * ���δ���ӱ��⣬���������ã��������⣬��������ã��������ӱ���. 
	 * ����Ѿ����ӵ����⣬���������ã��л�������
	 * */
	protected void switchToAvailableDataSource(){
		if(currentDataSource == resolvedStandbyDataSource){
			if(this.isDataSourceAvailable(resolvedMasterDataSource)){
				currentDataSource = resolvedMasterDataSource;
			}
		}else{
			currentDataSource = resolvedMasterDataSource;
			if(!this.isDataSourceAvailable(resolvedMasterDataSource)){
				currentDataSource =  resolvedStandbyDataSource;
			}
		}
	}
	
	
	
	/**
	 * ��������Ƿ����, ����Ե������쳣����׼���쳣
	 * */
	protected boolean isDataSourceAvailable(DataSource dataSource){
		Connection  conn = null;
		String select = "select 1";
		try{
			conn = dataSource.getConnection();
			 Statement stmt = conn.createStatement();
			 if(stmt.execute(select)){
				 return true;
			 }
			 stmt.close();
		}catch(SQLException e){
			logger.error("CheckDataSourceAvailable Exception", e);
			return false;
		}finally{
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Close Connection Exception", e);
				}
			}
		}
		return false;
	}

	/**
	 * Resolve the specified data source object into a DataSource instance.
	 * <p>The default implementation handles DataSource instances and data source
	 * names (to be resolved via a {@link #setDataSourceLookup DataSourceLookup}).
	 * @param dataSource the data source value object as specified in the
	 * {@link #setTargetDataSources targetDataSources} map
	 * @return the resolved DataSource (never <code>null</code>)
	 * @throws IllegalArgumentException in case of an unsupported value type
	 */
	protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
		if (dataSource instanceof DataSource) {
			return (DataSource) dataSource;
		}
		else if (dataSource instanceof String) {
			return this.dataSourceLookup.getDataSource((String) dataSource);
		}
		else {
			throw new IllegalArgumentException(
					"Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
		}
	}
	
	/**
	 * Set the DataSourceLookup implementation to use for resolving data source
	 * name Strings in the {@link #setTargetDataSources targetDataSources} map.
	 * <p>Default is a {@link JndiDataSourceLookup}, allowing the JNDI names
	 * of application server DataSources to be specified directly.
	 */
	public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = (dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup());
	}
	
	protected DataSource getCurrentDataSource(){
		return currentDataSource;
	}
	
	public void setMasterDataSource(Object masterDataSource) {
		this.masterDataSource = masterDataSource;
	}

	public void setStandbyDataSource(Object standbyDataSource) {
		this.standbyDataSource = standbyDataSource;
	}
	

	public void setConfigProperties(Properties configProperties) {
		this.configProperties = configProperties;
	}
	
	/**
	 * ����̣߳��л�����������������ã����л�������
	 * */
	private class CheckMasterAvailableDaemonThread extends Thread{
		public CheckMasterAvailableDaemonThread(){
			this.setDaemon(true);
			this.setName("MasterStandbyCheckMasterAvailableDaemonThread");
		}
		 @Override
		 public void run() {
			 while(true){
				 switchToAvailableDataSource();
				 try {
					Thread.sleep(checkTimeInterval);
				} catch (InterruptedException e) {
					logger.warn("Check Master InterruptedException", e);
				}
			 }
		 }
	}
	
	
}
