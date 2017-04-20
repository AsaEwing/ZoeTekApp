package com.example.asaewing.me01.BLE_data;

public interface ActionCallback
{
	public void onSuccess(Object data);
	public void onFail(int errorCode, String msg);
}
