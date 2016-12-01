package com.example.wobistdu;

public class WSmsMessage {
	
	private String sender;
	private String messageBody;
	
	public WSmsMessage(String sender, String msg)
	{
		this.sender=sender;
		this.messageBody=msg;
	}
	
	public String getSender()
	{
		return this.sender;
	}
	
	public String getMessageBody()
	{
		return this.messageBody;
	}
	
	public Boolean contains(String toMatch)
	{
		if(messageBody.contains(toMatch))
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}

}
