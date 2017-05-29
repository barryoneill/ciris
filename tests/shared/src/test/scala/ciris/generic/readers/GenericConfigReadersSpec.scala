package ciris.generic.readers

import ciris.PropertySpec
import ciris.generic._
import shapeless._
import shapeless.test.illTyped

final class FloatValue(val value: Float) extends AnyVal

final class PrivateFloatValue private (val value: Float) extends AnyVal

object PrivateFloatValue {
  def withValue(value: Float): PrivateFloatValue =
    new PrivateFloatValue(value)
}

final case class PrivateDoubleValue private (value: Double) extends AnyVal

object PrivateDoubleValue {
  def withValue(value: Double): PrivateDoubleValue =
    new PrivateDoubleValue(value)
}

final case class Port(value: Int) extends AnyVal

sealed trait DoubleOrBoolean
final case class DoubleValue(value: Double) extends DoubleOrBoolean
final case class BooleanValue(value: Boolean) extends DoubleOrBoolean

final case class IntValue(value: Int)

final class GenericConfigReadersSpec extends PropertySpec {
  "GenericConfigReaders" when {
    "reading value classes" should {
      "successfully read value class values" in {
        forAll { value: Float =>
          readValue[FloatValue](value.toString) shouldBe Right(new FloatValue(value))
        }
      }

      "return a failure for wrong value class values" in {
        forAll { value: String =>
          whenever(fails(value.toFloat)) {
            readValue[FloatValue](value) shouldBe a[Left[_, _]]
          }
        }
      }
    }

    "reading value classes with private constructors" should {
      "not be able to read value classes with private constructors" in {
        illTyped { """readValue[PrivateFloatValue]("1.0")""" }
      }
    }

    "reading product value classes" should {
      "successfully read product value class values" in {
        forAll { value: Int =>
          readValue[Port](value.toString) shouldBe Right(Port(value))
        }
      }

      "return a failure for wrong product value class values" in {
        forAll { value: String =>
          whenever(fails(value.toInt)) {
            readValue[Port](value) shouldBe a[Left[_, _]]
          }
        }
      }
    }

    "reading product value classes with private constructors" should {
      "successfully read product value class values" in {
        forAll { value: Double =>
          readValue[PrivateDoubleValue](value.toString) shouldBe
            Right(PrivateDoubleValue.withValue(value))
        }
      }

      "return a failure for wrong product value class values" in {
        forAll { value: String =>
          whenever(fails(value.toDouble)) {
            readValue[PrivateDoubleValue](value) shouldBe a[Left[_, _]]
          }
        }
      }
    }

    "reading coproducts" should {
      type DoubleOrBooleanCoproduct = DoubleValue :+: BooleanValue :+: CNil

      "successfully read coproduct values" in {
        forAll { double: Double =>
          readValue[DoubleOrBooleanCoproduct](double.toString) shouldBe
            Right(Coproduct[DoubleOrBooleanCoproduct](DoubleValue(double)))
        }

        forAll { boolean: Boolean =>
          readValue[DoubleOrBooleanCoproduct](boolean.toString) shouldBe
            Right(Coproduct[DoubleOrBooleanCoproduct](BooleanValue(boolean)))
        }
      }

      "return a failure for wrong coproduct values" in {
        forAll { string: String =>
          whenever(fails(string.toDouble)) {
            whenever(fails(string.toBoolean)) {
              readValue[DoubleOrBooleanCoproduct](string) shouldBe a[Left[_, _]]
            }
          }
        }
      }

      "successfully read generic coproduct values" in {
        forAll { double: Double =>
          readValue[DoubleOrBoolean](double.toString) shouldBe
            Right(DoubleValue(double))
        }

        forAll { boolean: Boolean =>
          readValue[DoubleOrBoolean](boolean.toString) shouldBe
            Right(BooleanValue(boolean))
        }
      }

      "return a failure for wrong generic coproduct values" in {
        forAll { string: String =>
          whenever(fails(string.toDouble)) {
            whenever(fails(string.toBoolean)) {
              readValue[DoubleOrBoolean](string) shouldBe a[Left[_, _]]
            }
          }
        }
      }
    }

    "reading unary products" should {
      "successfully read unary product values" in {
        forAll { int: Int =>
          readValue[IntValue](int.toString) shouldBe Right(IntValue(int))
        }
      }

      "return a failure for wrong unary product values" in {
        forAll { string: String =>
          whenever(fails(string.toInt)) {
            readValue[IntValue](string) shouldBe a[Left[_, _]]
          }
        }
      }
    }
  }
}
