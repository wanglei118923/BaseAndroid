package com.example.ltbase.base_http;

import android.text.TextUtils;

import com.example.ltbase.base_bean.Response;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import rxhttp.wrapper.annotation.Parser;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.TypeParser;
import rxhttp.wrapper.utils.Converter;

/**
 * 作者：王健 on 2021/8/4
 * 邮箱：845040970@qq.com
 * 描述：
 */
@Parser(name = "Response")
public class ResponseParser<T> extends TypeParser<Response<T>> {

    /**
     * 此构造方法适用于任意Class对象，但更多用于带泛型的Class对象，如：List<Student>
     * <p>
     * 用法:
     * Java: .asParser(new ResponseParser<List<Student>>(){})
     * Kotlin: .asParser(object : ResponseParser<List<Student>>() {})
     * <p>
     * 注：此构造方法一定要用protected关键字修饰，否则调用此构造方法将拿不到泛型类型
     */
    protected ResponseParser() {
        super();
    }

    /**
     * 此构造方法仅适用于不带泛型的Class对象，如: Student.class
     * <p>
     * 用法
     * Java: .asParser(new ResponseParser<>(Student.class))   或者  .asResponse(Student.class)
     * Kotlin: .asParser(ResponseParser(Student::class.java)) 或者  .asResponse(Student::class.java)
     */
    public ResponseParser(Type type) {
        super(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response<T> onParse(@NotNull okhttp3.Response response) throws IOException {
        Response<T> data = Converter.convertTo(response, Response.class, types);
        T t = data.getDatas(); //获取data字段
            if (t == null && types[0] == String.class) {
                /*
                 * 考虑到有些时候服务端会返回：{"errorCode":0,"errorMsg":"关注成功"}  类似没有data的数据
                 * 此时code正确，但是data字段为空，直接返回data的话，会报空指针错误，
                 * 所以，判断泛型为String类型时，重新赋值，并确保赋值不为null
                 */
                t = (T) data.getMessage();
            }
            if(TextUtils.equals(data.getCode(),"00000")){
                //TODO 请求成功code符合业务逻辑
                return data;
            }else if (TextUtils.equals("401",data.getCode())) {
                //TODO Token失效业务逻辑，onError中可以获取
                throw new RefreshTokenException(String.valueOf(data.getCode()), data.getMessage(), response);
            }else{
                //TODO 直接将服务端的错误信息抛出，onError中可以获取
                throw new ParseException(String.valueOf(data.getCode()), data.getMessage(), response);
            }
    }
}