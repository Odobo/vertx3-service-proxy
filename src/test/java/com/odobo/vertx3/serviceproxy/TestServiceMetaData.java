package com.odobo.vertx3.serviceproxy;

import com.odobo.vertx3.serviceproxy.meta.MethodMeta;
import com.odobo.vertx3.serviceproxy.meta.ServiceMetaData;
import com.odobo.vertx3.serviceproxy.serializer.DefaultJsonSerializer;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * User: plenderyou
 * Date: 08/12/2015
 * Time: 1:59 PM
 */
public class TestServiceMetaData {

    @Test
    public void testIdentifier() throws Exception {

        Method[] methods = SampleInterface.class.getDeclaredMethods();
        String id = MethodMeta.identifierFromMethod(methods[0]);
        Assert.assertEquals("method1", id);
        id = MethodMeta.identifierFromMethod(methods[1]);
        Assert.assertEquals("specialMethod1", id);
    }


    @Test
    public void testMetaData() throws Exception {

        ServiceMetaData smd = new ServiceMetaData(SampleInterface.class);

        Assert.assertEquals(2, smd.getMethods().size());
        Method[] methods = SampleInterface.class.getDeclaredMethods();

        MethodMeta meta = smd.getMeta(methods[0]);

        Assert.assertNotNull(meta);

        Assert.assertEquals("method1", meta.identifier());
        Assert.assertEquals(3, meta.getArguments().size());

        ServiceMetaData.Argument argument = meta.getArguments().get(0);
        Assert.assertEquals(String.class, argument.getType());
        Assert.assertNull(argument.getSerializer());


        argument = meta.getArguments().get(1);
        Assert.assertEquals(int.class, argument.getType());
        Assert.assertNull(argument.getSerializer());


        argument = meta.getArguments().get(2);
        Assert.assertEquals(SamplePojo.class, argument.getType());
        Assert.assertEquals(DefaultJsonSerializer.class, argument.getSerializer().getClass());

        meta = smd.getMeta(methods[1]);
        Assert.assertEquals("specialMethod1", meta.identifier());

        argument = meta.getArguments().get(1);
        Assert.assertEquals(SamplePojo.class, argument.getType());
        Assert.assertEquals(TestJsonSerializer.class, argument.getSerializer().getClass());

    }
}