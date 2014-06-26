package com.asg.aj.application;

import com.asg.aj.application.model.*;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class FrontServlet extends HttpServlet {

    private Logger logger = Logger.getLogger(getClass());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Start...");
        long start = System.currentTimeMillis();
        SomeObject1 so1 = new SomeObject1();
        try {
            so1.doSomething1();
        } catch (Throwable e) {
            logger.info("Error: " + e);
        }
        try {
            so1.doSomething2();
        } catch (Throwable e) {
            logger.info("Error: " + e);
        }
        SomeObject2 so2 = new SomeObject2();
        try {
            so2.doSomething1();
        } catch (Throwable e) {
            logger.info("Error: " + e);
        }
        try {
            so2.doSomething2();
        } catch (Throwable e) {
            logger.info("Error: " + e);
        }
        logger.info("Stop (executing time: " + (System.currentTimeMillis() - start) + ", ms)");
    }
}
