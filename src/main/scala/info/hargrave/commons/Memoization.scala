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

        /**
         * Forget the input value's cached return value
         *
         * @param input input value
         * @return former return value, if present
         */
        def forget(input: Input): Option[Value] = inputCache.remove(input)

        /**
         * Forget all input value associations
         */
        def forgetAll(): Unit = inputCache.clear()

        /**
         * Lookup all inputs that correspond the the specified output value
         *
         * @param output output value
         * @return output
         */
        def inputsFor(output: Value): Iterable[Input] = inputCache.filter(_._2 == output).map(_._1)

        /**
         * View a copy of the input/output cache
         *
         * @return input/output cache
         */
        def associations: Map[Input, Value] = inputCache.toMap
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
