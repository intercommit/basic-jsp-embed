package com.descartes.basicjsp.embed;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;

import org.apache.catalina.users.MemoryUserDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>MemoryUserDatabaseFactory</code> is used by <code>org.apache.catalina.realm.UserDatabaseRealm</code>
 * to get information about user/password/roles. The default factory looks inside the Tomcat-work directory.
 * This implementation will look for a "users.xml" file anywhere in the (AppBoot) class-path.
 * <br>
 * Add to the Tomcat launcher {@code beforeStart()} with 		
 * <pre> 
tomcat.enableNaming(); // Enable JNDI, required for use of UserDatabaseRealm for security.
ContextResource usersDb = new ContextResource();
usersDb.setName("MyTomcatUsers");
usersDb.setAuth("Container");
usersDb.setScope("Shareable");
usersDb.setType("org.apache.catalina.UserDatabase");
usersDb.setProperty("factory", "com.descartes.basicjsp.embed.MemoryUserDatabaseFactory");
usersDb.setProperty("pathName", "users.xml");
tomcat.getServer().getGlobalNamingResources().addResource(usersDb);

org.apache.catalina.realm.UserDatabaseRealm realmDb = new org.apache.catalina.realm.UserDatabaseRealm();
realmDb.setResourceName("MyTomcatUsers");
webCtx.setRealm(realmDb);

// or use a "context.xml" and load it with:
// webCtx.setConfigFile(urlFile);
 * </pre>
 * @author FWiers
 */
public class MemoryUserDatabaseFactory extends org.apache.catalina.users.MemoryUserDatabaseFactory {

	public static final Logger log = LoggerFactory.getLogger(MemoryUserDatabaseFactory.class);

	public MemoryUserDatabaseFactory() {
		super();
		// log.info("### CONSTRUCTED");
	}
	
	/**
	 * Code copied from {@link org.apache.catalina.users.MemoryUserDatabaseFactory#getObjectInstance(Object, Name, Context, Hashtable)} 
	 * and adjusted to load the "users.xml" anywhere from classpath. 
	 */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?,?> environment) throws Exception  {

    	//org.apache.catalina.startup.ContextConfig cc; 
		// log.info("### BUILDING");
    	
    	log.debug("Creating memory user database.");
    	
        // We only know how to deal with <code>javax.naming.Reference</code>s
        // that specify a class name of "org.apache.catalina.UserDatabase"
        if ((obj == null) || !(obj instanceof Reference)) {
            return (null);
        }
        Reference ref = (Reference) obj;
        if (!"org.apache.catalina.UserDatabase".equals(ref.getClassName())) {
        	log.error("Wrong type for realm: " + ref.getClassName());
            return (null);
        }

        // Create and configure a MemoryUserDatabase instance based on the
        // RefAddr values associated with this Reference
        MemoryUserDatabase database = new MemoryUserDatabase(name.toString());
        RefAddr ra = null;

        ra = ref.get("pathName");
        String usersXml = "users.xml";
        if (ra != null) {
        	usersXml = ra.getContent().toString();
        }
        File usersFile = LaunchWebApp.getFile(usersXml);
        if (usersFile == null || !usersFile.isFile()) {
        	usersFile = new File(usersXml);
        	if (!usersFile.isFile()) {
        		usersFile = null;
        	}
        }
        if (usersFile == null) {
        	throw new IOException("Cannot find the user configuration file " + usersXml);
        }
        database.setPathname(usersFile.getAbsolutePath());
        log.debug("Using authorization configuration file [{}]", usersFile.getAbsolutePath());
        database.setReadonly(true);

        // Return the configured database instance
        database.open();
        return (database);
    }
}
