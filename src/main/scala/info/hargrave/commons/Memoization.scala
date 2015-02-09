package info.hargrave.commons

import scala.collection.mutable.{Map => MutableMap}

/**
 * Provides a memoization system
 */
trait Memoization {

    /**
     * Provides the 'business' logic of the memoization pattern implementation
     *
     * @param body      function body to be captured
     * @tparam Input    function input type
     * @tparam Value    function return type
     */
    private sealed class Capture[Input, Value](body: Input => Value) {

        private val inputCache = MutableMap[Input, Value]()

        def apply(input: Input): Value = inputCache.getOrElseUpdate(input, body(input))
    }

    /**
     * Default, single-arity memo function
     *
     * @param function  function
     * @tparam I        function input type
     * @tparam V        function return type
     * @return          memoized function
     */
    final protected def memoize[I,V](function: I => V): I => V = new Capture[I, V](function).apply
}
