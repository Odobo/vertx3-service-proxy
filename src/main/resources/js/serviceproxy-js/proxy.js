var io = Packages.io;


var logger = io.vertx.core.logging.LoggerFactory.getLogger("com.odobo.vertx3.serviceproxy.proxy");

var ServiceProxy = function () {
};


ServiceProxy.createClient = function (vertx, address, interfaceClass) {
    return new ClientProxy(vertx, address, interfaceClass);
};

ServiceProxy.registerService = function (vertx, address, interfaceClass, impl) {
    return new EventBusService(vertx, address, interfaceClass, impl);
};

function ClientProxy(vertx, address, interfaceClass) {
    var metaData = new com.odobo.vertx3.serviceproxy.meta.ServiceMetaData(interfaceClass);
    var eb = vertx.eventBus();

    metaData.getMethods().forEach(function (m) {
        var methodMeta = metaData.getMeta(m);
        this[m] = function () {
            validateArguments(methodMeta, arguments);
            // Convert the arguments to an array
            var requestArgs = Array.prototype.slice.call(arguments);
            var request = {
                method: m,
                arguments: requestArgs.slice(0, methodMeta.getArguments().size())
            };

            var responseHandler = requestArgs[methodMeta.getArguments().size()];
            eb.send(address, request, function (res, err) {

                if (err == null) {
                    responseHandler(res.body()['return'], null);
                } else {
                    var errorContext = {
                        failureCode: err.failureCode()
                    }
                    // Recipient failure
                    if (err.failureType().ordinal() == 2) {
                        errorContext.context = JSON.parse(err.getMessage());
                    } else {
                        errorContext.context = {error: err.getMessage()};
                    }

                    responseHandler(null, errorContext);
                }
            });

        }
    }.bind(this));
}

function EventBusService(vertx, address, interfaceClass, impl) {
    var metaData = new com.odobo.vertx3.serviceproxy.meta.ServiceMetaData(interfaceClass);

    validateNotNull(vertx, "vertx");
    validateNotNull(address, "address");
    validateNotNull(interfaceClass, "interfaceClass");
    validateNotNull(impl, "implementation");
    validateImplementation(impl, metaData);

    var eb = vertx.eventBus();
    var consumer = eb.consumer(address).handler(function (msg) {
        var methodName = msg.body().method;

        var requestArgs = msg.body().arguments;

        requestArgs.push({
            ok: function (obj) {
                msg.reply({return: obj});
            },
            fail: function (errCode, ctx) {
                msg.fail(errCode, JSON.stringify(ctx));
            }
        });
        var funcToCall = impl[methodName];

        funcToCall.apply(this, requestArgs);

    });

    this.completionHandler = function (handler) {
        consumer.completionHandler(handler);
    }

    this.unregister = function (handler) {
        consumer.unregister(handler);
    }
}

function validateImplementation(impl, meta) {
    // the implementation class has to have a function for every method in the meta
    meta.getMethods().forEach(function (m) {

        if (typeof impl[m] !== 'undefined' && typeof impl[m] === 'function') {
            // It's ok
        } else {
            throw "Implementation does not have a " + m + " function";
        }
    })
}

function validateNotNull(thing, desc) {
    if (thing == null) {
        throw desc + " cannot be null";
    }
}

function validateArguments(meta, args) {
    if (args.length != (meta.getArguments().size() + 1)) {
        throw "Invalid argument list"
    }
}


// We export the Constructor function
module.exports = ServiceProxy;