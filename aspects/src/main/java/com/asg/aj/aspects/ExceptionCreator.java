package com.asg.aj.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;


@Aspect
public class ExceptionCreator {

    private static final String EXCEPTION_SUFFIX = "Exception";
    private static final String EXCEPTION_MESSAGE_SUFFIX = "Exception_Message";
    // Not the best practice, I think this properties file must be the same for all applications and have the hard path
    private static final String PROP_PATH = "../webapps/aspectj-web-example/WEB-INF/classes/exception_creator.properties";

    public static class ExceptionPropertiesHandler {

        public String className;
        public String message;

        @Override
        public String toString() {
            return String.format("{className: %s, message: %s}", className, message);
        }
    }

    private Logger logger = Logger.getLogger(getClass());

    /**
     * Advice for throwing exception
     *
     * Exception are throwing for all defined in property file (path: {@link #PROP_PATH}) signatures
     * Properties file must have two variables for each signature:
     * signature_{@link #EXCEPTION_SUFFIX}=package.SomeException
     * signature_{@link #EXCEPTION_MESSAGE_SUFFIX}="Some error text"
     * You can use signatures "package.className" for weave all class methods
     * or "package.className.methodName" for weave concrete method
     *
     * @param pjp Proceeding join point
     * @return Join point return object
     * @throws Throwable
     */
    @Around("execution(* com.asg.aj.application.model.*.*(..))")
    public Object translateToDataAccessException(ProceedingJoinPoint pjp) throws Throwable {
        logger.info("Start around advice for join point: " + pjp);
        String className =  pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();
        ExceptionPropertiesHandler eph = getExceptionProperties(className, methodName);
        if (eph != null) {
            try {
                logger.info("Load properties: " + eph);
                Class<?> ex = Class.forName(eph.className);
                try {
                    try {
                        Throwable error = (Throwable) ex.getConstructor(String.class).newInstance(eph.message);
                        if (isExceptionAllowed(pjp, error)) {
                            logger.info("Stop around advice by throw exception: " + error);
                            throw error;
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        logger.error("NoSuchMethodException", e);
                    }
                } catch (InstantiationException e) {
                    logger.error("InstantiationException", e);
                }
            } catch (ClassNotFoundException e) {
                logger.error("ClassNotFoundException", e);
            }
        }
        Object result = pjp.proceed();
        logger.info("Stop around advice without throw exception");
        return result;
    }

    /**
     * Gets exception properties by signature from properties file
     *
     * Definitions rules of property file see {@link ExceptionCreator#translateToDataAccessException(org.aspectj.lang.ProceedingJoinPoint)}
     *
     * @param className Class name
     * @param methodName Method name
     * @return Exception properties handler
     */
    private ExceptionPropertiesHandler getExceptionProperties(String className, String methodName) {
        ExceptionPropertiesHandler eph = getExceptionPropertiesBySignature(className + "." + methodName);
        if (eph == null)
            eph = getExceptionPropertiesBySignature(className);
        return eph;
    }

    /**
     * Gets exception properties by signature from properties file (path: {@link #PROP_PATH})
     *
     * Definitions rules of property file see {@link ExceptionCreator#translateToDataAccessException(org.aspectj.lang.ProceedingJoinPoint)}
     *
     * @param signature signature
     * @return Exception properties handler
     */
    private ExceptionPropertiesHandler getExceptionPropertiesBySignature(String signature) {
        ExceptionPropertiesHandler eph = new ExceptionPropertiesHandler();
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(PROP_PATH);
            prop.load(input);
            eph.className = prop.getProperty(signature + "_" + EXCEPTION_SUFFIX);
            if (eph.className == null)
                return null;
            eph.message = prop.getProperty(signature + "_" + EXCEPTION_MESSAGE_SUFFIX);
        }
        catch (IOException e) {
            logger.error("IOError", e);
        }
        finally {
            try {
                if(input != null)
                    input.close();
            } catch (IOException ignore) {}
        }
        return eph;
    }

    /**
     * Check exception allowed
     *
     * @param jp Proceeding join point
     * @param e Error
     * @return true - if exception is allowed for join point
     */
    private boolean isExceptionAllowed(ProceedingJoinPoint jp, Throwable e) {
        CodeSignature cs = (CodeSignature) jp.getSignature();
        Class[] exceptionTypes = cs.getExceptionTypes();
        for (Class<?> exceptionType : exceptionTypes)
            if (exceptionType.isInstance(e))
                return true;
        return false;
    }

}
