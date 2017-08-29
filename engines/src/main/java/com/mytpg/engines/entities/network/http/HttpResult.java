package com.mytpg.engines.entities.network.http;

/**
 * Created by stalker-mac on 08.11.14.
 */
public class HttpResult
{
    public enum ResponseType {NotDefined, HTML, JSON, Others, XML}

    private String m_result = null;
    private ResponseType m_responseType = ResponseType.NotDefined;

    public HttpResult(String ArgResult, ResponseType ArgResponseType)
    {
        this.m_responseType = ArgResponseType;
        this.m_result = ArgResult;
    }

    public ResponseType getResponseType()
    {
        return this.m_responseType;
    }

    public String getResult()
    {
        return this.m_result;
    }

    public void setResponseType(ResponseType ArgResponseType)
    {
        this.m_responseType = ArgResponseType;
    }

    public void setResult(String ArgResult)
    {
        this.m_result = ArgResult;
    }
}
