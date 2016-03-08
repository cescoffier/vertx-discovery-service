require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.msa.abcd.service.HelloService
module VertxMsaHello
  #  @author <a href="http://escoffier.me">Clement Escoffier</a>
  class HelloService
    # @private
    # @param j_del [::VertxMsaHello::HelloService] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxMsaHello::HelloService] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [String] name 
    # @yield 
    # @return [void]
    def say(name=nil)
      if name.class == String && block_given?
        return @j_del.java_method(:say, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(name,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling say(name)"
    end
  end
end
