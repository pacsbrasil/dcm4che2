package org.dcm4che.auditlog;

import java.lang.Exception;

public class ArrInputException extends Exception
{
	public ArrInputException() { super(); }
	public ArrInputException(String desc) { super(desc); }
	public ArrInputException(String desc, Throwable cause) { super(desc,cause); }
}
