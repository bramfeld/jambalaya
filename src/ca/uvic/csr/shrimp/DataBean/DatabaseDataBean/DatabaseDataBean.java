/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.DatabaseDataBean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import ca.uvic.csr.shrimp.DataBean.AbstractDataBean;

/**
 * Manages and buffers data extracted from a database backend via JDBC.
 * TODO should eventually contain methods, fields, etc for use with any database, in any domain.
 *
 * @author Rob Lintern
 */
public abstract class DatabaseDataBean extends AbstractDataBean {

    protected String url;
    protected String user;
    protected String password;
    protected Connection con;
    protected String driverClassName;

    public DatabaseDataBean(String url, String user, String password, String driverClassName) {
        super();
        this.url = url;
        this.user = user;
        this.password = password;
        this.driverClassName = driverClassName;
    }

    protected Connection getConnection() {
    	try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
        if (con == null) {
            try {
                con = DriverManager.getConnection(url, user, password);
                System.out.println("Connected to: " + url );
            } catch (SQLException e) {
                e.printStackTrace();
                con = null;
            }
        }
        return con;
    }

}
