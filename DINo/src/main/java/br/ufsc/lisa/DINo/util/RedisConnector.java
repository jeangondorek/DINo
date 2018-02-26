package br.ufsc.lisa.DINo.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

public class RedisConnector implements Connector {

	// private MongoClient client;
	// private Jedis jedis;
	public JedisPool pool;
	private String db;
	private Integer port;
	public String password;

	public boolean connect(String uri, String port, String password) {

		try {
			pool = new JedisPool(new JedisPoolConfig(), uri, Integer.valueOf(port), 2000);

			Jedis jedis = pool.getResource();
			if (password != null & !password.isEmpty()) {
				this.password = password;
				jedis.auth(password);
			}
			jedis.connect();
			jedis.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		db = uri;
		this.port = Integer.valueOf(port);
		this.password = password;
		// jedis = pool.getResource();
		return true;

	}

	@Override
	public boolean put(String key, String value) {
		Jedis jedis = pool.getResource();
		Transaction t = jedis.multi();

		if (jedis != null) {
			jedis.auth(password);
			String rkey = key;
			String rvalue = value.toString();
			jedis.set(rkey, rvalue);
			jedis.close();
			return true;
		}
		jedis.close();
		return false;
	}

	public boolean importData(RelationalDB source, String query) {
		Statement pstmt;
		try {
			pstmt = ((PostgresDB) source).getConnection().createStatement();
			ResultSet result = pstmt.executeQuery(query);
			Jedis jedis = pool.getResource();
			if (password!= null && !password.isEmpty())
				jedis.auth(this.password);
			int i = 0, l = 0;
			Transaction t = jedis.multi();
			while (result.next()) {
				String key = result.getString("?column?");
				String value = result.getString("value");
				t.set(key, value);
				if (++i % 256 == 0) {
					System.out.println("Lote: " + ++l);
					t.exec();
					t = jedis.multi();
				}
			}
			System.out.println("Lote: " + ++l);
			t.exec();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public void close() {
		this.pool.close();
		;
	}

	@Override
	public String toString() {
		return "Redis DB";
	}

	@Override
	public boolean connect(String uri, String port, String user, String password, String DB) {
		return this.connect(uri, port, password);
	}

}
