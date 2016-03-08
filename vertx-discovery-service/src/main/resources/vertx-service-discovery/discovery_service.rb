require 'vertx/vertx'
require 'vertx-service-discovery/service'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.discovery.DiscoveryService
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
    # @param [::Vertx::Vertx] vertx 
    # @param [Hash] options 
    # @return [::VertxServiceDiscovery::DiscoveryService]
    def self.create(vertx=nil,options=nil)
      if vertx.class.method_defined?(:j_del) && options.class == Hash && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtDiscovery::DiscoveryService.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxExtDiscovery::DiscoveryOptions.java_class]).call(vertx.j_del,Java::IoVertxExtDiscovery::DiscoveryOptions.new(::Vertx::Util::Utils.to_json_object(options))),::VertxServiceDiscovery::DiscoveryService)
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx,options)"
    end
    # @param [::Vertx::Vertx] vertx 
    # @param [Hash] record 
    # @return [::VertxServiceDiscovery::Service]
    def self.get_service(vertx=nil,record=nil)
      if vertx.class.method_defined?(:j_del) && record.class == Hash && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtDiscovery::DiscoveryService.java_method(:getService, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxExtDiscovery::Record.java_class]).call(vertx.j_del,Java::IoVertxExtDiscovery::Record.new(::Vertx::Util::Utils.to_json_object(record))),::VertxServiceDiscovery::Service)
      end
      raise ArgumentError, "Invalid arguments when calling get_service(vertx,record)"
    end
    # @return [void]
    def close
      if !block_given?
        return @j_del.java_method(:close, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling close()"
    end
    # @param [Hash] record 
    # @yield 
    # @return [void]
    def publish(record=nil)
      if record.class == Hash && block_given?
        return @j_del.java_method(:publish, [Java::IoVertxExtDiscovery::Record.java_class,Java::IoVertxCore::Handler.java_class]).call(Java::IoVertxExtDiscovery::Record.new(::Vertx::Util::Utils.to_json_object(record)),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling publish(record)"
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
    # @param [Hash{String => Object}] filter 
    # @yield 
    # @return [void]
    def get_record(filter=nil)
      if filter.class == Hash && block_given?
        return @j_del.java_method(:getRecord, [Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCore::Handler.java_class]).call(::Vertx::Util::Utils.to_json_object(filter),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling get_record(filter)"
    end
    # @param [Hash{String => Object}] filter 
    # @yield 
    # @return [void]
    def get_records(filter=nil)
      if filter.class == Hash && block_given?
        return @j_del.java_method(:getRecords, [Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCore::Handler.java_class]).call(::Vertx::Util::Utils.to_json_object(filter),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result.to_a.map { |elt| elt != nil ? JSON.parse(elt.toJson.encode) : nil } : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling get_records(filter)"
    end
    # @param [Hash] record 
    # @yield 
    # @return [void]
    def update(record=nil)
      if record.class == Hash && block_given?
        return @j_del.java_method(:update, [Java::IoVertxExtDiscovery::Record.java_class,Java::IoVertxCore::Handler.java_class]).call(Java::IoVertxExtDiscovery::Record.new(::Vertx::Util::Utils.to_json_object(record)),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling update(record)"
    end
  end
end
