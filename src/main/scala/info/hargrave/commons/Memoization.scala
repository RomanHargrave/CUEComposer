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
    sealed class Capture[Input, Value](body: Input => Value) {

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
         * Forget all inputs associated with an output
         *
         * @param output output
         */
        def forgetInputsFor(output: Value): Unit = inputsFor(output).foreach(forget)

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
     * Capture logic for a function that does not take parameters. This has a narrow use case,
     * as it can be used when a function used as an accessor needs to return only a single value
     * that can be determined at the time of the first invocation.
     *
     * This effectively is a single-object cache, a-la singleton factories, but not.
     *
     * @param body      function body
     * @tparam Value    function return value
     */
    sealed class LazyCache[Value](body: => Value) {

        private var cachedReturn: Option[Value] = None

        def apply: Value = cachedReturn match {
            case Some(value)    => value
            case None           =>
                cachedReturn = Some(body)
                cachedReturn.get
        }
    }

    /**
     * Default, single-arity memo function
     *
     * @param function  function
     * @tparam I        function input type
     * @tparam V        function return type
     * @return          memoizing function
     */
    final protected def memoize[I,V](function: I => V): I => V = new Capture[I, V](function).apply

    /**
     * Used for nullary single-return-value corner cases where a single value may be calculated and used from thereon
     * as the return value of the function
     *
     * @param function  function
     * @tparam V        function return type
     * @return          caching function
     */
    final protected def cache[V](function: => V): V = new LazyCache[V](function).apply
}
