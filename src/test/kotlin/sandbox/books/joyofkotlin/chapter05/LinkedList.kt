package sandbox.books.joyofkotlin.chapter05

import sandbox.books.joyofkotlin.chapter07.Result
import java.lang.IllegalArgumentException

/*
Collections can be classified as:
  * linear collection (like list, there is connection between elements)
  * associative collection (no connection between elements, like maps or sets)
  * graph collection (trees, like binary tree)
 */

// Sealed classes are implicitly abstract and their constructor
// is implicitly private
sealed class List<out A> {
    abstract fun isEmpty(): Boolean
    abstract val head: A
    abstract val tail: List<A>
    abstract fun setHead(a: @UnsafeVariance A): List<A>
    abstract fun drop(n: Int): List<A>
    abstract fun dropWhile(p: (A) -> Boolean): List<A>
    abstract fun reverse(): List<A>
    abstract fun lengthMemoized(): Int
    abstract fun headSafe(): Result<A>

    object Nil : List<Nothing>() {
        override fun isEmpty() = true
        override val head: Nothing
            get() = throw IllegalArgumentException("head called on an empty list")
        override val tail
            get() = throw IllegalArgumentException("tail called on an empty list")
        override fun setHead(a: Nothing): List<Nothing> =
            throw IllegalArgumentException("setHead called on an empty list")
        override fun drop(n: Int) =
            drop(this, n)
        override fun toString(): String = "[NIL]"
        override fun dropWhile(p: (Nothing) -> Boolean): List<Nothing> =
            dropWhile(this, p)
        override fun reverse(): List<Nothing> =
            reverse(
                invoke(),
                this
            )
        override fun lengthMemoized(): Int = 0
        override fun headSafe() = Result<Nothing>()
    }

    class Cons<A>(
        override val head: A,
        override val tail: List<A>
    ) : List<A>() {
        private val length: Int = tail.lengthMemoized() + 1

        override fun isEmpty() = false

        override fun toString(): String = "[${toString("", this)}NIL]"

        override fun setHead(a: A): List<A> = tail.cons(a)

        override fun drop(n: Int): List<A> =
            drop(this, n)

        override fun dropWhile(p: (A) -> Boolean): List<A> =
            dropWhile(this, p)

        override fun reverse(): List<A> =
            reverse(
                invoke(),
                this
            )

//        fun <B> myFoldLeft(acc: B, f: (B) -> (A) -> B): B =
//            List.foldLeft(acc, this, f)

        private tailrec fun toString(acc: String, list: List<A>): String =
            when (list) {
                Nil -> acc
                is Cons -> toString("$acc${list.head}, ", list.tail)
            }

        override fun lengthMemoized(): Int = length

        override fun headSafe() = Result(head)
    }

    fun cons(a: @UnsafeVariance A): List<A> =
        Cons(a, this)

    companion object {
        operator fun <A> invoke(vararg az: A): List<A> =
            az.foldRight(invoke()) {
                    a: A, list: List<A> ->
                Cons(a, list)
            }

        @Suppress("UNCHECKED_CAST")
        operator fun <A> invoke(): List<A> = Nil as List<A>

        tailrec fun <A> drop(list: List<A>, n: Int): List<A> =
            when (list) {
                Nil -> list
                is Cons -> if (n <= 0) list else drop(
                    list.tail,
                    n - 1
                )
            }

        tailrec fun <A> dropWhile(list: List<A>, p: (A) -> Boolean): List<A> =
            when (list) {
                Nil -> list
                is Cons -> if (p(list.head)) dropWhile(
                    list.tail,
                    p
                ) else list
            }

        fun <A> concat(list1: List<A>, list2: List<A>): List<A> =
            when (list1) {
                Nil -> list2
                is Cons -> concat(
                    list1.tail,
                    list2
                ).cons(list1.head)
            }

        tailrec fun <A> reverse(acc: List<A>, list: List<A>): List<A> =
            when (list) {
                Nil -> acc
                is Cons -> reverse(
                    acc.cons(list.head),
                    list.tail
                )
            }

        fun <A, B> foldRight(
            list: List<A>,
            identityVal: B,
            f: (A) -> (B) -> B
        ): B =
            when (list) {
                Nil -> identityVal
                is Cons -> f(list.head) (foldRight(list.tail, identityVal, f))
            }

        tailrec fun <A, B> coFoldRight(
            acc: B,
            list: List<A>,
            identity: B,
            f: (A) -> (B) -> B
        ): B =
            when (list) {
                List.Nil -> acc
                is List.Cons -> coFoldRight(f(list.head)(acc), list.tail, identity, f)
            }

        // This is stack safe and corecursive
        tailrec fun <A, B> foldLeft(acc: B, list: List<A>, f: (B) -> (A) -> B): B =
            when (list) {
                Nil -> acc
                is Cons -> foldLeft(f(acc) (list.head), list.tail, f)
            }

        fun <A> lastSafe(list: List<A>): Result<A> =
            foldLeft(Result(), list) { _: Result<A> -> { y: A -> Result(y) } }

        // A different way to call headSafe
        fun <A> headSafe(list: List<A>): Result<A> =
            foldRight(list, Result()) { x: A -> { _: Result<A> -> Result(x) } }

        fun <A> flattenResult(list: List<Result<A>>): List<A> =
            list.flatMap { ra -> ra.map { List(it) }.getOrElse(invoke()) }

        fun <A> flatten(list: List<List<A>>): List<A> =
            list.foldRight(invoke()) { x -> x::concat }

        fun <A> sequence(list: List<Result<A>>): Result<List<A>> =
            list.foldRight(Result(invoke())) { x ->
                { y: Result<List<A>> ->
                    Result.map2(x, y) { a -> { b: List<A> -> b.cons(a) } }
                }
            }

        fun <A> sequence2(list: List<Result<A>>): Result<List<A>> =
            list.filter { !it.isEmpty() }
                .foldRight(Result(invoke())) { x ->
                    { y: Result<List<A>> ->
                        Result.map2(x, y) { a -> { b: List<A> ->
                            b.cons(a)
                        } }
                    }
                }

        fun <A, B> traverse(list: List<A>, f: (A) -> Result<B>): Result<List<B>> =
            list.foldRight((Result(invoke()))) { x ->
                { y: Result<List<B>> ->
                    Result.map2(f(x), y) { a -> { b: List<B> -> b.cons(a) } }
                }
            }

        fun <A, B, C> zipWith(list1: List<A>, list2: List<B>, f: (A) -> (B) -> C): List<C> {
            tailrec
            fun zipWith(acc: List<C>, list1: List<A>, list2: List<B>): List<C> =
                when (list1) {
                    List.Nil -> acc
                    is List.Cons -> when (list2) {
                        List.Nil -> acc
                        is List.Cons -> zipWith(acc.cons(f(list1.head) (list2.head)),
                            list1.tail, list2.tail)
                    }
                }
            return zipWith(invoke(), list1, list2).reverse()
        }

        fun <A, B, C> product(list1: List<A>, list2: List<B>, f: (A) -> (B) -> C): List<C> =
            list1.flatMap { a -> list2.map { b -> f(a) (b) } }

        fun <A, B> unzip(list: List<Pair<A, B>>): Pair<List<A>, List<B>> =
            list.coFoldRight(Pair(invoke(), invoke())) { pair ->
                { listPair: Pair<List<A>, List<B>> ->
                    Pair(listPair.first.cons(pair.first), listPair.second.cons(pair.second))
                }
            }
    }

    fun getAt(index: Int): Result<A> {
        tailrec
        fun <A> getAt(list: List<A>, index: Int): Result<A> =
            when (list) {
                Nil -> Result.failure("Dead code. Should never execute.")
                is Cons ->
                    if (index == 0)
                        Result(list.head)
                    else
                        getAt(list.tail, index - 1)
            }
        return if (index < 0 || index >= lengthMemoized())
            Result.failure("Index out of bound")
        else
            getAt(this, index)
    }

    @Suppress("UNCHECKED_CAST")
    fun getAtNoNilCheck(index: Int): Result<A> {
        tailrec
        fun <A> getAt(list: Cons<A>, index: Int): Result<A> =
            if (index == 0)
                Result(list.head)
            else
                getAt(list.tail as Cons, index - 1)
        return if (index < 0 || index >= lengthMemoized())
            Result.failure("Index out of bound")
        else
            getAt(this as Cons, index)
    }

    fun getAtViaFoldLeft(index: Int): Result<A> =
        Pair(Result.failure<A>("Index out of bound"), index).let {
            if (index < 0 || index >= lengthMemoized())
                it
            else
                foldLeft(it) { ta ->
                    { a ->
                        if (ta.second < 0)
                            ta
                        else
                            Pair(Result(a), ta.second - 1)
                    }
                }
        }.first

    fun <B> flatMap(f: (A) -> List<B>): List<B> = flatten(map(f))

    fun <B> foldRight(identityVal: B, f: (A) -> (B) -> B): B =
        List.foldRight(this, identityVal, f)

    fun <B> foldLeft(acc: B, f: (B) -> (A) -> B): B =
        List.foldLeft(acc, this, f)

    fun <B> coFoldRight(identity: B, f: (A) -> (B) -> B): B =
        List.coFoldRight(identity, this.reverse(), identity, f)

    @Suppress("UNCHECKED_CAST")
    fun <B> map(f: (A) -> B): List<B> =
        coFoldRight(Nil as List<B>) { h -> { t: List<B> -> Cons(f(h), t) } }

    fun concat(list: List<@UnsafeVariance A>): List<A> = List.concat(this, list)

    @Suppress("UNCHECKED_CAST")
    fun filter(p: (A) -> Boolean): List<A> =
        coFoldRight(Nil as List<A>) { h -> { t: List<A> ->
            if (p(h)) Cons(h, t) else t }
        }

    @Suppress("UNCHECKED_CAST")
    fun splitAt(index: Int): Pair<List<A>, List<A>> {
        tailrec fun splitAt(
            acc: List<A>,
            list: List<A>,
            i: Int
        ): Pair<List<A>, List<A>> =
            when (list) {
                Nil -> Pair(list.reverse(), acc)
                is Cons -> if (i == 0)
                    Pair(list.reverse(), acc)
                else
                    splitAt(acc.cons(list.head), list.tail, i - 1)
            }

        return when {
            index < 0 -> splitAt(0)
            index > lengthMemoized() -> splitAt(lengthMemoized())
            else -> splitAt(Nil as List<A>, this.reverse(), this.lengthMemoized() - index)
        }
    }

    fun startsWith(sub: List<@UnsafeVariance A>): Boolean {
        tailrec fun startsWith(list: List<A>, sub: List<A>): Boolean =
            when (sub) {
                Nil -> true
                is Cons -> if (list.head == sub.head)
                    startsWith(list.tail, sub.tail)
                else
                    false
            }

        return startsWith(this, sub)
    }

    fun hasSubList(sub: List<@UnsafeVariance A>): Boolean {
        tailrec
        fun <A> hasSubList(list: List<A>, sub: List<A>): Boolean =
            when (list) {
                Nil -> sub.isEmpty()
                is Cons ->
                    if (list.startsWith(sub))
                        true
                    else
                        hasSubList(list.tail, sub)
            }
        return hasSubList(this, sub)
    }

    fun <B> groupBy(f: (A) -> B): Map<B, List<A>> =
        reverse().foldLeft(mapOf<B, List<A>>()) { mt: Map<B, List<A>> ->
            { t: A ->
                f(t).let {
                    mt + (it to (mt.getOrDefault(it, Nil)).cons(t))
                }
            }
        }
}
