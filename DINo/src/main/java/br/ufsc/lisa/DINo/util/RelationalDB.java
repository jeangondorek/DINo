package br.ufsc.lisa.DINo.util;

import java.sql.SQLException;
import java.util.List;

import javax.swing.DefaultListModel;

public interface RelationalDB {
	
	public boolean connect(String uri, String porta, String user, String password, String dbName);
	
	public List<String> listDatabases() throws ClassNotFoundException, SQLException;
	public List<String> listTables() throws ClassNotFoundException, SQLException;
	public List<String> listColumns(String tableName) throws ClassNotFoundException, SQLException; 
	
	public void exportRelationalDataToNoSQL(String cmdSql, Connector host, String table) throws SQLException;
	
	
}



