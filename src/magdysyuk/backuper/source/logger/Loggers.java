package magdysyuk.backuper.source.logger;

import java.text.MessageFormat;

import org.apache.log4j.Logger;


public class Loggers {
	
	private static Logger LOG;
	
	/**
	 * Merge selected string pattern and parameters for him.
	 * @param messageID Name of enum field's for required message
	 * @param messageArguments Parameters for string pattern
	 * @return Message string for selected messageID with inlined arguments
	 * @see MessageFormat
	 */
	private static String getTextLogMessage(TextMessage messageID, Object[] messageArguments) {
		try {
			MessageFormat messageFormatter = new MessageFormat(messageID.getDescription());
			return messageFormatter.format(messageArguments);
		} catch (IllegalArgumentException illegalTextLogMessageArgEx) {
			/*
			 * Because this method called usually 2 times per 1 event
			 * (first - get string with caller class and method,
			 * second - get formatted text of log message),
			 * if arguments are illegal, this exception will be thrown total 2 times
			 * (1 time for each of calls)
			 */
			
			// Names of current class and method. Try to get these names, if unsuccessful then use hardcoded value. 
			String callerIdentifier;
			try {
				String callerIdentifierClass = new Object(){}.getClass().getEnclosingClass().getName();
				String callerIdentifierMethod = new Object(){}.getClass().getEnclosingMethod().getName();
				callerIdentifier = "Class: " + callerIdentifierClass + ", method: " + callerIdentifierMethod;
			} catch (Exception ex) {
				callerIdentifier = "Class: \"Loggers\", method: \"getTextLogMessage\"";
				LOG = Logger.getLogger (callerIdentifier);
				LOG.fatal("Impossible get name of caller object", ex);
			}
			
			LOG = Logger.getLogger (callerIdentifier);
			LOG.fatal("Internal error: invalid message identifier", illegalTextLogMessageArgEx);
			LOG.fatal("messageID: " + messageID);
			int countArguments = messageArguments.length;
			for (int i = 0; i < countArguments; i++) {
				LOG.fatal("Current argument's number: " + i + 
						" (" + (i+1) + " from total " + countArguments + " argument(s))" +
								", value: \"" + messageArguments[i] + "\"");
			}
			
			return "No defined log message";
		}
	}
	
	/**
	 * Get string with name of callers class and method. 
	 * Required for log messages.
	 * @param callerObject Caller class
	 * @return Formatted string with inlined names of class and method of caller object
	 * @see getClass()
	 */
	private static String getCallerString (Object callerObject) {
		
		String callerString = "";
		if (callerObject != null) {
			String callerClass = callerObject.getClass().getName();
			String callerMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
			callerString = getTextLogMessage(TextMessage.LOGGER_CALLER_CLASS_NAME_AND_METHOD_NAME, 
													new Object[]{callerClass, callerMethod});
		} else {
			callerString = "[Receiving caller object through stacktrace is not successful]";
		}
		return callerString;
	}
	
	/**
	 * Write error (or something else) into log.
	 * @param callerObject Object from which was called this method. 
	 * Required for definition names of caller class and method.
	 * @param messageID Identifier of text message for current log event.
	 * @param messageArguments Parameters for inline into text message. 
	 * @param appException Exception, which led to error we want to log.
	 * @see MessageFormat
	 */
	
	public static void fatal(Object callerObject, TextMessage messageID, Throwable appException) {
		LOG = Logger.getLogger(getCallerString(callerObject));
		LOG.fatal(getTextLogMessage(messageID, null), appException);
	}
	public static void fatal (Object callerObject, TextMessage messageID, 
			Object[] messageArguments, Throwable appException) {
		LOG = Logger.getLogger(getCallerString(callerObject));
		LOG.fatal(getTextLogMessage(messageID, messageArguments), appException);
	}
	public static void fatal (Object callerObject, TextMessage messageID, 
			Object[] messageArguments) {
		LOG = Logger.getLogger(getCallerString(callerObject));
		LOG.fatal(getTextLogMessage(messageID, messageArguments));
	}
	public static void fatal (Object callerObject, TextMessage messageID) {
		LOG = Logger.getLogger(getCallerString(callerObject));
		LOG.fatal(messageID.getDescription());
	}
	
	public static void debug (Object callerObject, TextMessage messageID, 
								Object[] messageArguments) {
		LOG = Logger.getLogger(getCallerString(callerObject));
		LOG.debug(getTextLogMessage(messageID, messageArguments));
	}
	public static void debug (Object callerObject, TextMessage messageID) {
		LOG = Logger.getLogger(getCallerString(callerObject));
		LOG.debug(messageID.getDescription());
	}
	
	public static void trace (Object callerObject, TextMessage messageID, 
			Object[] messageArguments) {
		LOG = Logger.getLogger(getCallerString(callerObject));
		LOG.trace(getTextLogMessage(messageID, messageArguments));
	}	
	
}



