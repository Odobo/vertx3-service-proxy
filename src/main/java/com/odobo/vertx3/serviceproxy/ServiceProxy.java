package com.odobo.vertx3.serviceproxy;

import com.odobo.vertx3.serviceproxy.handler.ExceptionHandler;
import com.odobo.vertx3.serviceproxy.handler.FailHandler;
import com.odobo.vertx3.serviceproxy.handler.ServiceFailHandler;
import com.odobo.vertx3.serviceproxy.handler.ServiceHandler;
import com.odobo.vertx3.serviceproxy.meta.MethodMeta;
import com.odobo.vertx3.serviceproxy.meta.ServiceMetaData;
import com.odobo.vertx3.serviceproxy.serializer.ProxyJsonSerializer;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User: plenderyou
 * Date: 19/10/15
 * Time: 09:11
 *
 * Main helper class to create clients and services of an interface that uses the underlying eventbus
 */
public class ServiceProxy {
    public static final String METHOD = "method";
    public static final String ARGUMENTS = "arguments";
    public static final String RETURN = "return";


    /**
     * Create a client
     * @param vertx the vertx instance to use
     * @param address The eventbus address that the service listens on
     * @param clientInterface  the interface to use the for the proxy
     * @param <T>
     * @return a proxy
     */
    public static <T> T createClient(final Vertx vertx, final String address, final Class<T> clientInterface) {

        return createClient(vertx, address, null, clientInterface);

    }

    public static <T> T createClient(final Vertx vertx, final String address, final DeliveryOptions options, final Class<T> clientInterface) {

        final ServiceMetaData serviceMetaData = new ServiceMetaData(clientInterface);

        return (T) Proxy.newProxyInstance(clientInterface.getClassLoader(), new Class[]{clientInterface}, new ServiceProxyInvocationHandler(vertx, serviceMetaData, address, options));

    }

    /**
     * Register a service implementation
     * @param vertx a vertx instance to use
     * @param address the eventbus address to use
     * @param classInterface the interface of the service
     * @param implementation the delegate that gets called
     * @param exceptionHandler An exception handler for mapping exceptions to messages
     * @param <T> the type
     * @return a consumer object
     */
    public static <T> MessageConsumer<JsonObject> registerService(final Vertx vertx, final String address, final Class<T> classInterface, final T implementation, final ExceptionHandler exceptionHandler) {
        final ServiceMetaData serviceMetaData = new ServiceMetaData(classInterface);

        return new EventBusHandler<>(vertx, address, serviceMetaData, implementation, exceptionHandler).getConsumer();

    }


    /**
     * This class listens on the eventbus and calls the delegate
     * @param <T>
     */
    private static class EventBusHandler<T> implements Handler<Message<JsonObject>> {
        private final Vertx vertx;
        private final String address;
        private final ServiceMetaData metaData;
        private final T delegate;
        private final MessageConsumer<JsonObject> consumer;
        private final ExceptionHandler exceptionHandler;

        public EventBusHandler(final Vertx vertx, final String address, ServiceMetaData metaData, final T delegate, final ExceptionHandler exceptionHandler) {
            this.vertx = vertx;
            this.address = address;
            this.metaData = metaData;
            this.delegate = delegate;
            this.exceptionHandler = exceptionHandler;

            this.consumer = vertx.eventBus().<JsonObject>consumer(address).handler(this);
        }

        public MessageConsumer<JsonObject> getConsumer() {
            return consumer;
        }

        @Override
        public void handle(final Message<JsonObject> event) {


            final JsonObject request = event.body();

            final String methodName = request.getString(METHOD);

            final MethodMeta methodData = metaData.getMeta(methodName);

            final List<Object> arguments = convertArguments(methodData, request);

            // Construct a handler that is passed to the service for callbacks
            arguments.add(createHandler(methodData.getReturnType().getType(), event, methodData));

            try {
                methodData.getMethod().invoke(delegate, arguments.toArray());

            } catch (InvocationTargetException ite) {
                this.exceptionHandler.handle(ite.getCause(), new ServiceFailHandler<>(event));
            }
            catch (Throwable throwable) {
                this.exceptionHandler.handle(throwable, new ServiceFailHandler<>(event));
            }
        }


        private List<Object> convertArguments(final MethodMeta method, final JsonObject request) {

            final List<Object> arguments = new ArrayList<>();
            final JsonArray argumentArray = request.getJsonArray(ARGUMENTS);

            for(int i=0; i<method.getArguments().size(); i++) {
                ServiceMetaData.Argument arg = method.getArguments().get(i);
                final ProxyJsonSerializer serializer = arg.getSerializer();
                final Object a = serializer == null ? argumentArray.getValue(i) : serializer.fromJson(argumentArray.getJsonObject(i), arg.getType());
                arguments.add(a);
            }

            return arguments;
        }
    }

    private static <X> ServiceHandler<X> createHandler(Class<X> clazz, final Message<JsonObject> event, final MethodMeta methodMeta) {
        return new ProxyServiceHandler<X>(event, methodMeta.getReturnType().getSerializer() );
    }


    /**
     * A handler that puts responses on the event bus
     * @param <T>
     */
    private static class ProxyServiceHandler<T> implements ServiceHandler<T> {

        private final Message<JsonObject> event;
        private final ProxyJsonSerializer serialiser;

        public ProxyServiceHandler(final Message<JsonObject> event, final ProxyJsonSerializer serializer) {
            this.event = event;
            this.serialiser = serializer;
        }

        @Override
        public void ok(final T value) {
            if( value != null ) {
                final JsonObject returnValue = new JsonObject();
                Object o = serialiser == null ? value : serialiser.toJson(value);
                returnValue.put(RETURN, o);
                event.reply(returnValue);
            } else {
                event.reply(null);
            }
        }

        @Override
        public void fail(final int errCode, final JsonObject context) {
            event.fail(errCode, context.encode());
        }



    }


    /**
     * The client side of the proxy using Java's InvocationHandler to send messages on the event bus to the
     * required format
     */
    private static class ServiceProxyInvocationHandler implements InvocationHandler {
        private static final Logger logger = LoggerFactory.getLogger(ServiceProxyInvocationHandler.class);

        private final Vertx vertx;
        private final String address;
        private final ServiceMetaData metaData;
        private final DeliveryOptions options;

        private ServiceProxyInvocationHandler(final Vertx vertx, final ServiceMetaData metaData, String address, final DeliveryOptions options) {
            this.vertx = vertx;
            this.metaData = metaData;
            this.address = address;

            this.options = options == null ? new DeliveryOptions() : options;

        }


        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

            final MethodMeta methodData = this.metaData.getMeta(method);

            final JsonObject message = convertToRequest(methodData, args);
            if(logger.isDebugEnabled()) {
                logger.debug("invoke:  " + message);
            }

            final ServiceMetaData.Argument returnType = methodData.getReturnType();
            ServiceHandler sh = (ServiceHandler)args[args.length-1];

            vertx.eventBus().send(address, message, this.options,  new AsyncResultHandler<Message<JsonObject>>() {
                @Override
                public void handle(final AsyncResult<Message<JsonObject>> event) {

                    if( event.succeeded() ) {
                        if( returnType.getType().isAssignableFrom(Void.class) || event.result().body() == null) {
                            sh.ok(null);
                        } else {
                            final ProxyJsonSerializer serializer = returnType.getSerializer();
                            try {
                                sh.ok(serializer == null ? event.result().body().getValue(RETURN) : serializer.fromJson(event.result().body().getJsonObject(RETURN), returnType.getType()));
                            }catch (Throwable t) {
                                // Bad conversion
                                sh.fail(FailHandler.BAD_DESERIALIZATION, new JsonObject().put("error", t.getMessage()));
                            }
                        }
                    } else {
                        ReplyException re = (ReplyException) event.cause();
                        switch (re.failureType()) {
                            case NO_HANDLERS:
                            case TIMEOUT:
                                sh.fail(re.failureCode(), new JsonObject().put("error", re.getMessage()));
                                break;
                            case RECIPIENT_FAILURE:
                            default:
                                sh.fail(re.failureCode(), new JsonObject(re.getMessage()));
                                break;
                        }
                    }
                }
            });
            return null;
        }

        /**
         * Returns a request Object for the service using the arguments
         * @param methodMeta meta data about the method
         * @param args the argument list
         * @return the constructed message to go on the eventbus
         */
        private JsonObject convertToRequest(final MethodMeta methodMeta, final Object[] args) {

            Objects.nonNull(methodMeta);
            final JsonObject request = new JsonObject()
                .put(METHOD, methodMeta.identifier());

            final JsonArray arguments = new JsonArray();

            for (int i = 0; i < methodMeta.getArguments().size(); i++) {

                ServiceMetaData.Argument arg = methodMeta.getArguments().get(i);
                final ProxyJsonSerializer serializer = arg.getSerializer();
                if (args[i] == null) {
                    arguments.addNull();
                } else {
                    arguments.add(serializer == null ? args[i] : serializer.toJson(args[i]));
                }
            }

            return request.put(ARGUMENTS, arguments);
        }

    }


}
