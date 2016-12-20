package com.pivotal.pcf.mysqlweb.controller;

import com.pivotal.pcf.mysqlweb.beans.UserPref;
import com.pivotal.pcf.mysqlweb.utils.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.Map;

@Controller
public class AutoLoginController
{
    protected static Logger logger = Logger.getLogger("controller");

    @Autowired
    UserPref userPref;

    @RequestMapping(value = "/autologin", method = RequestMethod.GET)
    public String autoLogin
            (Model model,
             HttpSession session,
             HttpServletRequest request) throws Exception
    {
        logger.info("Received request to auto login");

        ConnectionManager cm = ConnectionManager.getInstance();
        Connection conn;
        String username = null;
        String passwd = null;
        String url = null;

        try
        {
            username = fixRequestParam(request.getParameter("username"));
            passwd = fixRequestParam(request.getParameter("passwd"));
            url = fixRequestParam(request.getParameter("url"));

            logger.info("username = " + username);
            logger.info("passwd = " + passwd);
            logger.info("url = " + url);

            if (username.trim().equals(""))
            {
                conn = AdminUtil.getNewConnection(url);
            }
            else
            {
                conn = AdminUtil.getNewConnection(url, username, passwd);
            }

            conn.setAutoCommit(true);

            MysqlConnection newConn =
                    new MysqlConnection
                            (conn,
                                    url,
                                    new java.util.Date().toString(),
                                    username.toUpperCase());

            cm.addConnection(newConn, session.getId());

            String schema = url.substring(url.lastIndexOf("/") + 1);

            session.setAttribute("user_key", session.getId());
            session.setAttribute("user", username.toUpperCase());
            session.setAttribute("schema", schema);
            session.setAttribute("url", url);
            session.setAttribute("prefs", userPref);
            session.setAttribute("history", new LinkedList());
            session.setAttribute("connectedAt", new java.util.Date().toString());
            session.setAttribute("themeMain", Themes.defaultTheme);
            session.setAttribute("themeMin", Themes.defaultThemeMin);

            Map<String, String> schemaMap = AdminUtil.getSchemaMap();

            schemaMap = QueryUtil.populateSchemaMap
                    (conn, schemaMap, schema);

            session.setAttribute("schemaMap", schemaMap);

            logger.info("schemaMap=" + schemaMap);
            logger.info(userPref.toString());

        }
        catch (Exception ex)
        {
            model.addAttribute("loginerror", ex.getMessage());
            model.addAttribute("loginObj");
            return "login";
        }

        return "main";
    }

    private String fixRequestParam (String s)
    {
        if (s == null)
        {
            return "";
        }
        else
        {
            return s;
        }
    }
}