package za.co.dubedivine.networks.interceptor

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.support.HttpRequestWrapper
import java.io.IOException


class ClientRequestInterceptor(private val headerName: String,
                               private var headerValue: String) : ClientHttpRequestInterceptor {


    init {
        println("the header name is $headerName")
        println("the header value is $headerValue")
    }


    @Throws(IOException::class)
    override fun intercept(request: HttpRequest?,
                           body: ByteArray?,
                           execution: ClientHttpRequestExecution): ClientHttpResponse {

        val wrapper: HttpRequest = HttpRequestWrapper(request)
        wrapper.headers.set(headerName, headerValue)
        return execution.execute(wrapper, body)
    }
}