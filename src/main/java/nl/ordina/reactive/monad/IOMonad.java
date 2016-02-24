package nl.ordina.reactive.monad;

/*
sealed trait IOAction[+A] extends Function1[WorldState, (WorldState, A)]

abstract class IOApplication {
  private class WorldStateImpl(id:BigInt) extends WorldState {
    def nextState = new WorldStateImpl(id + 1)
  }
  final def main(args:Array[String]):Unit = {
    val ioAction = iomain(args)
    ioAction(new WorldStateImpl(0));
  }
  def iomain(args:Array[String]):IOAction[_]
}

object RTConsole {
  val getString = IOAction(Console.readLine)
  def putString(s: String) = IOAction(Console.print(s))
  def putLine(s: String) = IOAction(Console.println(s))
}

object HelloWorld extends IOApplication {
  import IOAction._
  import RTConsole._

  def sayHello(n:String) = n match {
    case "Bob" => putLine("Hello, Bob")
    case "Chuck" => putLine("Hey, Chuck")
    case "Sarah" => putLine("Helloooo, Sarah")
    case _ => fail("match exception")
  }

  def ask(q:String) = putString(q) >> getString

  def processString(s:String) = s match {
    case "quit" => putLine("Catch ya later")
    case _ => (sayHello(s) or
        putLine(s + ", I don't know you.")) >>

        loop
  }

  val loop:IOAction[Unit] = // gives stack overflow
    for {
      name <- ask("What's your name? ");
      _ <- processString(name)
    } yield ()

  def iomain(args:Array[String]) = {
    putLine("This is an example of the IO monad.") >>
    putLine("Enter a name or 'quit'") >>
    loop
  }
  / * of: for{
        _ <- putString("This is an example of the IO monad.");
        _ <- putString("What's your name?");
        name <- getString;
        _ <- putString("Hello " + name)
    } yield ()
  * --/
}

        sealed abstract class IOAction[+A] extends Function1[WorldState, (WorldState, A)] {
        def map[B](f:A => B):IOAction[B] = flatMap {x => IOAction.unit(f(x))}
        def flatMap[B](f:A => IOAction[B]):IOAction[B]= new ChainedAction(this, f)

private class ChainedAction[+A, B](
        action1: IOAction[B],
        f: B => IOAction[A]) extends IOAction[A] {
        def apply(state1:WorldState) = {
        val (state2, intermediateResult) = action1(state1);
        val action2 = f(intermediateResult)
      / * return * / action2(state2)
        }
        }

        def >>[B](next: => IOAction[B]):IOAction[B] =
        for {
        _ <- this;
        second <- next
        } yield second

        def <<[B](next: => IOAction[B]):IOAction[A] =
        for {
        first <- this;
        _ <- next
        } yield first

        def filter(
        p: A => Boolean,
        msg:String):IOAction[A] =
        flatMap{x =>
        if (p(x)) IOAction.unit(x)
        else IOAction.fail(msg)}
        def filter(p: A => Boolean):IOAction[A] =
        filter(p, "Filter mismatch")
        }

        object IOAction {
        def apply[A](expression: => A):IOAction[A] = new SimpleAction(expression)

private class SimpleAction[+A](expression: => A) extends IOAction[A] {
        def apply(state:WorldState) = (state.nextState, expression)
        }

private class UnitAction[+A](value: A) extends IOAction[A] {
        def apply(state:WorldState) = (state, value)
        }

        def unit[A](value:A):IOAction[A] = new UnitAction(value)

private class FailureAction(e:Exception) extends IOAction[Nothing] {
        def apply(state:WorldState) = throw e
        }

private class UserException(msg:String) extends Exception(msg)

        def fail(msg:String) = ioError(new UserException(msg))
        def ioError[A](e:Exception):IOAction[A] = new FailureAction(e)
        }

private class HandlingAction[+A](
        action:IOAction[A],
        handler: Exception => IOAction[A])
        extends IOAction[A] {
        def apply(state:WorldState) = {
        try {
        action(state)
        } catch {
        case e:Exception => handler(e)(state)
        }
        }
        }

        def onError[B >: A](handler: Exception => IOAction[B]): IOAction[B] =
        new HandlingAction(this, handler)

        def or[B >: A](alternative:IOAction[B]):IOAction[B] =
        this onError {ex => alternative}
        }
        }
        */
public class IOMonad {
    // TODO impl IOMonad
}
