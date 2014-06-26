package com.asg.aj.application.model;

import org.apache.log4j.Logger;

public class SomeObject1 {

    private Logger logger = Logger.getLogger(getClass());

    public void doSomething1() throws SomeException {
        logger.info(getClass() + ": " + "doSomething1()");
    }

    public void doSomething2() throws SomeException {
        logger.info(getClass() + ": " + "doSomething2()");
    }
}
