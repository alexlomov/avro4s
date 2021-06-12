package com.sksamuel.avro4s.schema

import com.sksamuel.avro4s.{AvroNamespace, AvroSchema}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AvroNamespaceTest extends AnyWordSpec with Matchers {

  "@AvroNamespace" should {
    "support namespace annotations on records" in {

      @AvroNamespace("com.yuval") case class AnnotatedNamespace(s: String)

      val schema = AvroSchema[AnnotatedNamespace]
      schema.getNamespace shouldBe "com.yuval"
    }

    "support namespace annotations in nested records" in {

      @AvroNamespace("com.yuval") case class AnnotatedNamespace(s: String, internal: InternalAnnotated)
      @AvroNamespace("com.yuval.internal") case class InternalAnnotated(i: Int)

      val expected = new org.apache.avro.Schema.Parser().parse(getClass.getResourceAsStream("/namespace.avsc"))
      val schema = AvroSchema[AnnotatedNamespace]
      schema.toString(true) shouldBe expected.toString(true)
    }

    "support namespace annotations on field" in {

      case class InternalAnnotated(i: Int)
      @AvroNamespace("com.yuval") case class AnnotatedNamespace(s: String, @AvroNamespace("com.yuval.internal") internal: InternalAnnotated)

      val expected = new org.apache.avro.Schema.Parser().parse(getClass.getResourceAsStream("/namespace.avsc"))
      val schema = AvroSchema[AnnotatedNamespace]
      schema.toString(true) shouldBe expected.toString(true)
    }

    "favour namespace annotations on field over record" in {

      @AvroNamespace("ignore")
      case class InternalAnnotated(i: Int)

      @AvroNamespace("com.yuval") case class AnnotatedNamespace(s: String, @AvroNamespace("com.yuval.internal") internal: InternalAnnotated)

      val expected = new org.apache.avro.Schema.Parser().parse(getClass.getResourceAsStream("/namespace.avsc"))
      val schema = AvroSchema[AnnotatedNamespace]
      schema.toString(true) shouldBe expected.toString(true)
    }

    "support namespace annotations on case classes at field level" in {
      val expected = new org.apache.avro.Schema.Parser().parse(getClass.getResourceAsStream("/namespace_enum_field_level.json"))
      val schema = AvroSchema[Teapot]
      schema.toString(true) shouldBe expected.toString(true)
    }

    "support namespace annotations on case classes at class level" in {
      val expected = new org.apache.avro.Schema.Parser().parse(getClass.getResourceAsStream("/namespace_enum_class_level.json"))
      val schema = AvroSchema[Location]
      schema.toString(true) shouldBe expected.toString(true)
    }

    "support namespace annotations on sealed trait enums at the trait level" in {
      val expected = new org.apache.avro.Schema.Parser().parse(getClass.getResourceAsStream("/namespace_enum_trait_level.json"))
      val schema = AvroSchema[Africa]
      schema.toString(true) shouldBe expected.toString(true)
    }

    "empty namespace" in {

      @AvroNamespace("")
      case class Foo(s: String)

      val expected = new org.apache.avro.Schema.Parser().parse(getClass.getResourceAsStream("/namespace_empty.json"))
      val schema = AvroSchema[Foo]
      schema.toString(true) shouldBe expected.toString(true)
    }
  }
}

@AvroNamespace("wobble")
sealed trait Africa
case object Cameroon extends Africa
case object Chad extends Africa
case object Comoros extends Africa

case class Tea(name: String)

@AvroNamespace("wibble")
case class Teapot(@AvroNamespace("wobble") tea: Tea)

@AvroNamespace("wibble")
case class Location(city: City)

@AvroNamespace("wobble")
case class City(name: String)