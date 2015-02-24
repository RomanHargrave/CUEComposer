package info.hargrave.commons.javafx

import java.util.{List => JList}
import javafx.beans.WeakListener
import javafx.collections.ListChangeListener
import scala.ref.WeakReference
import javafx.collections.ListChangeListener.Change

/**
 * A ListBinding implementation that doesn't suffer from index overrun errors.
 * Also, Subscriptions!
 *
 * @author Roman Hargrave <roman@hargrave.info>
 */
class SafeListBinding[T](destination: JList[T]) extends AnyRef with ListChangeListener[T] with WeakListener {

    private val listRef = WeakReference(destination)

    private def applyPermutation(change: Change[_ <: T]): Unit = {

        destination.subList(change.getFrom, change.getTo).clear()
        destination.addAll(change.getFrom, change.getList.subList(change.getFrom, change.getTo))
    }

    private def applyAddition(change: Change[_ <: T]): Unit = {

        destination.addAll(change.getFrom, change.getAddedSubList)
    }

    private def applyDeletion(change: Change[_ <: T]): Unit = {

        val ahead = change.getFrom + change.getRemovedSize

        /*
         * This is a hack/fix for a JavaFX collections corner case bug (only verified to exist the oracle JavaFX implementation).
         * Effectively prevent a situation where JavaFX can try to create a sublist from `tail` `tail+1`
         * This can happen if a list of unknown size >= 1 experiences a removal at `tail` or of the only element
         */
        if (change.getRemovedSize == 1) {
            destination.subList(change.getFrom, change.getFrom).clear()
        } else if (ahead > destination.size || destination.size == 0) {
            throw new IllegalStateException("Invalid List Change (Deletion): Change extends past the destination tail")
        } else {
            destination.subList(change.getFrom, ahead).clear()
        }
    }

    override def onChanged(change: Change[_ <: T]): Unit = if(wasGarbageCollected()) {
        change.getList.removeListener(this)
    } else {
        while (change.next()) {
            if(change.wasPermutated())   applyPermutation(change)
            else {
                if (change.wasAdded())   applyAddition(change)
                if (change.wasRemoved()) applyDeletion(change)
            }
        }
    }

    override def wasGarbageCollected(): Boolean = listRef.get.isEmpty
}
object SafeListBinding {

    import javafx.collections.ObservableList
    import scalafx.event.subscriptions.Subscription

    def apply[T](destination: JList[T], master: ObservableList[T]): Subscription = if(destination eq master) {
        throw new IllegalArgumentException("An observable list may not be bound to itself")
    } else  {
        val listener = new SafeListBinding(destination)
        master.addListener(listener)
        new BindingSubscription(master, listener)
    }

    final class BindingSubscription[T](list: ObservableList[T], listener: ListChangeListener[T]) extends Subscription {

        override def cancel(): Unit = list.removeListener(listener)
    }
}