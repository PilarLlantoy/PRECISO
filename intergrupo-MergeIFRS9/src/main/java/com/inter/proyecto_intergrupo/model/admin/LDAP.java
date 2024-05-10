package com.inter.proyecto_intergrupo.model.admin;
//import admin.paramLDAP;
//import DAO.DAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.http.HttpSession;


public class LDAP {

    private Hashtable<String,String> env;
    private HashMap<String,String> Ldapurls;


    public LDAP() {
        setLdapUrls();
    }

    private void setLdapUrls() {
        this.Ldapurls = new HashMap();
        Ldapurls.put("ldap://82.255.60.62:389", "@dg.co.igrupobbva");
        Ldapurls.put("ldap://82.255.60.71:389", "@cm.co.igrupobbva");
        Ldapurls.put("ldap://82.255.60.146:389", "@fd.co.igrupobbva");
        Ldapurls.put("ldap://82.255.60.143:389", "@me.co.igrupobbva");
        Ldapurls.put("ldap://82.255.60.166:389", "@sg.co.igrupobbva");
        Ldapurls.put("ldap://82.255.60.145:389", "@va.co.igrupobbva");
    }


    public String inicializarLDAP(String user, String password) {
        this.env = new Hashtable();
        String res="Error,data 52e";

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put("com.sun.jndi.ldap.read.timeout", "40000");
        env.put("com.sun.jndi.ldap.connect.timeout", "40000");
        env.put(Context.SECURITY_CREDENTIALS, password);

        for(Map.Entry<String,String> k : Ldapurls.entrySet()){

            env.put(Context.PROVIDER_URL,k.getKey());
            env.put(Context.SECURITY_PRINCIPAL,user+k.getValue());

            try {
                DirContext context = new InitialLdapContext(env, null);
                // Autenticación exitosa
                res="exitosa";
                System.out.println(res);
                context.close();
                return res;
            } catch (Exception e) {
                // Autenticación fallida
                if (!e.getMessage().contains("data 52e")){
                    res=e.getMessage();
                }
                System.out.println("Autenticación fallida: "+e.getMessage());
            }
        }
        return res;
    }


}


