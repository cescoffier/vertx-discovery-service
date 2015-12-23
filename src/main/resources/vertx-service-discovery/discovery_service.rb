require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.service.discovery.DiscoveryService
module VertxServiceDiscovery
  #  @author <a href="http://escoffier.me">Clement Escoffier</a>
  class DiscoveryService
    # @private
    # @param j_del [::VertxServiceDiscovery::DiscoveryService] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxServiceDiscovery::DiscoveryService] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [String] serviceItf 
    # @param [Hash{String => Object}] properties 
    # @param [String] address 
    # @yield 
    # @return [void]
    def publish(serviceItf=nil,properties=nil,address=nil)
      if serviceItf.class == String && properties.class == Hash && address.class == String && block_given?
        return @j_del.java_method(:publish, [Java::java.lang.String.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(serviceItf,::Vertx::Util::Utils.to_json_object(properties),address,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling publish(serviceItf,properties,address)"
    end
    # @param [String] id 
    # @yield 
    # @return [void]
    def unpublish(id=nil)
      if id.class == String && block_given?
        return @j_del.java_method(:unpublish, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(id,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling unpublish(id)"
    end
    # @param [String] serviceItf 
    # @param [Hash{String => Object}] filter 
    # @yield 
    # @return [void]
    def get_service(serviceItf=nil,filter=nil)
      if serviceItf.class == String && filter.class == Hash && block_given?
        return @j_del.java_method(:getService, [Java::java.lang.String.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCore::Handler.java_class]).call(serviceItf,::Vertx::Util::Utils.to_json_object(filter),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling get_service(serviceItf,filter)"
    end
    # @param [String] serviceItf 
    # @param [Hash{String => Object}] filter 
    # @yield 
    # @return [void]
    def get_services(serviceItf=nil,filter=nil)
      if serviceItf.class == String && filter.class == Hash && block_given?
        return @j_del.java_method(:getServices, [Java::java.lang.String.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCore::Handler.java_class]).call(serviceItf,::Vertx::Util::Utils.to_json_object(filter),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt != nil ? JSON.parse(elt.encode) : nil } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling get_services(serviceItf,filter)"
    end
  end
end
