require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.discovery.ServiceClient
module VertxServiceDiscovery
  #  @author <a href="http://escoffier.me">Clement Escoffier</a>
  class ServiceClient
    # @private
    # @param j_del [::VertxServiceDiscovery::ServiceClient] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxServiceDiscovery::ServiceClient] the underlying java delegate
    def j_del
      @j_del
    end
    # @yield 
    # @return [void]
    def get
      if block_given?
        return @j_del.java_method(:get, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(::Vertx::Util::Utils.from_object(event)) }))
      end
      raise ArgumentError, "Invalid arguments when calling get()"
    end
  end
end
