/*******************************************************************************
 * Copyright 2010 Maxime Lévesque
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package org.squeryl.dsl

import org.squeryl.dsl.ast._
import org.squeryl.internals.OutMapper
import org.squeryl.internals.StatementWriter
import org.squeryl.internals.FieldReferenceLinker
import org.squeryl.Session
import org.squeryl.Schema
import org.squeryl.internals.AttributeValidOnNumericalColumn
import org.squeryl.Query
import java.util.Date
import org.squeryl.internals.Utils

sealed trait TNumeric
sealed trait TOptionBigDecimal extends TNumeric
sealed trait TBigDecimal extends TOptionBigDecimal with TNonOption

sealed trait TOptionDouble extends TOptionBigDecimal
sealed trait TDouble extends TOptionDouble with TBigDecimal with TNonOption

sealed trait TOptionLong extends TOptionDouble
sealed trait TLong extends TOptionLong with TDouble with TNonOption

sealed trait TOptionFloat extends TOptionDouble
sealed trait TFloat extends TOptionFloat with TDouble with TNonOption

sealed trait TOptionInt extends TOptionLong with TOptionFloat
sealed trait TInt extends TOptionInt with TLong with TNonOption with TFloat

sealed trait TOptionByte extends TOptionInt
sealed trait TByte extends TOptionByte with TInt with TNonOption

sealed trait TOption 
 extends TOptionByte with TOptionInt with TOptionFloat with TOptionLong with TOptionDouble with TOptionBigDecimal
 with TOptionDate with TOptionString with TOptionTimestamp

sealed trait TNumericLowerTypeBound 
  extends TByte with TInt with TFloat with TLong with TDouble with TBigDecimal
 
sealed trait TNonOption

sealed trait TOptionLowerBound
 extends TOptionByte with TOptionInt with TOptionFloat with TOptionLong with TOptionDouble with TOptionBigDecimal
 with TOptionDate with TOptionString with TOptionTimestamp
 
sealed trait TEnumValue[A]
sealed trait TOptionEnumValue[A] extends TEnumValue[A]

sealed trait TString extends TOptionString with TNonOption
sealed trait TDate extends TOptionDate with TNonOption
sealed trait TTimestamp extends TOptionTimestamp with TNonOption
sealed trait TByteArray extends TOptionByteArray  with TNonOption
sealed trait TOptionString 
sealed trait TOptionDate
sealed trait TOptionTimestamp
sealed trait TOptionByteArray  
sealed trait TBoolean extends TOptionBoolean  with TNonOption
sealed trait TOptionBoolean
sealed trait TUUID extends TOptionUUID  with TNonOption
sealed trait TOptionUUID

@scala.annotation.implicitNotFound("The left side of the comparison (===, <>, between,...) is not compatible with the right side")
sealed class CanCompare[-A1,-A2]


trait TypedExpression[A1,T1] extends ExpressionNode {
  outer =>
    
  def plus[T3 >: T1 <: TNumeric, T2 <: T3, A2, A3]
         (e: TypedExpression[A2,T2])
         (implicit f: TypedExpressionFactory[A3,T3]) : TypedExpression[A3,T3] = f.convert(e)

  def times[T3 >: T1 <: TNumeric, T2 <: T3, A2, A3]
         (e: TypedExpression[A2,T2])
         (implicit f: TypedExpressionFactory[A3,T3]) : TypedExpression[A3,T3] = f.convert(e)

  def minus[T3 >: T1 <: TNumeric, T2 <: T3, A2, A3]
         (e: TypedExpression[A2,T2])
         (implicit f: TypedExpressionFactory[A3,T3]) : TypedExpression[A3,T3] = f.convert(e)

  def div[T3 >: T1 <: TNumeric, T2 <: T3, A2, A3, A4, T4]
         (e: TypedExpression[A2,T2])
         (implicit f:  TypedExpressionFactory[A3,T3], 
                   tf: Floatifier[T3,A4,T4]): TypedExpression[A4,T4] = tf.floatify(e)

  def +[T3 >: T1 <: TNumeric, T2 <: T3, A2, A3]
         (e: TypedExpression[A2,T2])
         (implicit f: TypedExpressionFactory[A3,T3]) : TypedExpression[A3,T3] = f.convert(e)

  def *[T3 >: T1 <: TNumeric, T2 <: T3, A2, A3]
         (e: TypedExpression[A2,T2])
         (implicit f: TypedExpressionFactory[A3,T3]) : TypedExpression[A3,T3] = f.convert(e)

  def -[T3 >: T1 <: TNumeric, T2 <: T3, A2, A3]
         (e: TypedExpression[A2,T2])
         (implicit f: TypedExpressionFactory[A3,T3]) : TypedExpression[A3,T3] = f.convert(e)

  def /[T3 >: T1 <: TNumeric, T2 <: T3, A2, A3, A4, T4]
         (e: TypedExpression[A2,T2])
         (implicit f:  TypedExpressionFactory[A3,T3], 
                   tf: Floatifier[T3,A4,T4]): TypedExpression[A4,T4] = tf.floatify(e)
                   
  def value: A1
  
  def ===[A2,T2](b: TypedExpression[A2,T2])(implicit ev: CanCompare[T1, T2]) = new EqualityExpression(this, b)
  def <>[A2,T2](b: TypedExpression[A2,T2])(implicit ev: CanCompare[T1, T2]) = new EqualityExpression(this, b)
  
  def gt[A2,T2](b: TypedExpression[A2,T2])(implicit ev: CanCompare[T1, T2]) = new EqualityExpression(this, b)
  def lt[A2,T2](b: TypedExpression[A2,T2])(implicit ev: CanCompare[T1, T2]) = new EqualityExpression(this, b)
  def gte[A2,T2](b: TypedExpression[A2,T2])(implicit ev: CanCompare[T1, T2]) = new EqualityExpression(this, b)
  def lte[A2,T2](b: TypedExpression[A2,T2])(implicit ev: CanCompare[T1, T2]) = new EqualityExpression(this, b)
  
  def >[A2,T2](b: TypedExpression[A2,T2])(implicit ev: CanCompare[T1, T2]) = new EqualityExpression(this, b)
  def <[A2,T2](b: TypedExpression[A2,T2])(implicit ev: CanCompare[T1, T2]) = new EqualityExpression(this, b)
  def >=[A2,T2](b: TypedExpression[A2,T2])(implicit ev: CanCompare[T1, T2]) = new EqualityExpression(this, b)
  def <=[A2,T2](b: TypedExpression[A2,T2])(implicit ev: CanCompare[T1, T2]) = new EqualityExpression(this, b)
  
  //TODO: add T1 <:< TOption to isNull and isNotNull 
  def isNull= new PostfixOperatorNode("is null", this) with LogicalBoolean
  def isNotNull= new PostfixOperatorNode("is not null", this) with LogicalBoolean
  
  def between[A2,T2,A3,T3](b1: TypedExpression[A2,T2], 
                           b2: TypedExpression[A3,T3])
                          (implicit ev1: CanCompare[T1, T2], 
                                    ev2: CanCompare[T2, T3]) = new BetweenExpression(this, b1, b2)
  
  def like[A2,T2 <: TOptionString](s: TypedExpression[A2,T2])(implicit ev: CanCompare[T1,T2]) = new BinaryOperatorNodeLogicalBoolean(this, s, "like")
  
  def ||[A2,T2](e: TypedExpression[A2,T2]) = new ConcatOp[A1,A2,T1,T2](this, e)
  
//  def ||[A2,T2](e: TypedExpression[A2,T2])(implicit ev: TOption <:< (T1 with T2)) = new ConcatOp[A1,A2,T1,T2](this, e)  
//  def ||[A2,T2](e: TypedExpression[A2,T2])(implicit ev: T1 <:< TNonOption, ev2: T2 <:< TNonOption) = new ConcatOp[A1,A2,T1,T2](this, e)
  
  def regex(pattern: String) = new FunctionNode[Boolean](pattern, None: Option[OutMapper[Boolean]], Seq(this)) with LogicalBoolean {

    override def doWrite(sw: StatementWriter) =
      Session.currentSession.databaseAdapter.writeRegexExpression(outer, pattern, sw)
  }
  
  def is(columnAttributes: AttributeValidOnNumericalColumn*)(implicit restrictUsageWithinSchema: Schema) =
    new ColumnAttributeAssignment(_fieldMetaData, columnAttributes)
  
  
  def in[A2,T2](t: Traversable[A2])(implicit cc: CanCompare[T1,T2]): LogicalBoolean = sys.error("!") 
  
  def in[A2,T2](t: Query[A2])(implicit cc: CanCompare[T1,T2]): LogicalBoolean = sys.error("!")
  
  def notIn[A2,T2](t: Traversable[A2])(implicit cc: CanCompare[T1,T2]): LogicalBoolean = sys.error("!") 
  
  def notIn[A2,T2](t: Query[A2])(implicit cc: CanCompare[T1,T2]): LogicalBoolean = sys.error("!")
  
  def ~ = this

  def sample:A1 = mapper.sample

  def mapper: OutMapper[A1]

  def :=[B <% TypedExpression[A1,_]] (b: B) =
    new UpdateAssignment(_fieldMetaData, b : TypedExpression[A1,_])

  def :=(q: Query[Measures[A1]]) =
    new UpdateAssignment(_fieldMetaData, q.ast)

  def defaultsTo[B <% TypedExpression[A1,_]](value: B) /*(implicit restrictUsageWithinSchema: Schema) */ =
    new DefaultValueAssignment(_fieldMetaData, value : TypedExpression[A1,_])

  /**
   * TODO: make safer with compiler plugin
   * Not type safe ! a TypedExpressionNode[T] might not be a SelectElementReference[_] that refers to a FieldSelectElement...   
   */
  private [squeryl] def _fieldMetaData = {
    val ser =
      try {
        this.asInstanceOf[SelectElementReference[_,_]]
      }
      catch { // TODO: validate this at compile time with a scalac plugin
        case e:ClassCastException => {
            throw new RuntimeException("left side of assignment '" + Utils.failSafeString(this.toString)+ "' is invalid, make sure statement uses *only* closure argument.", e)
        }
      }

    val fmd =
      try {
        ser.selectElement.asInstanceOf[FieldSelectElement].fieldMetaData
      }
      catch { // TODO: validate this at compile time with a scalac plugin
        case e:ClassCastException => {
          throw new RuntimeException("left side of assignment '" + Utils.failSafeString(this.toString)+ "' is invalid, make sure statement uses *only* closure argument.", e)
        }
      }
    fmd
  }
}


class ConstantTypedExpression[A1,T1](a: A1) extends ConstantExpressionNode[A1](a, None) with TypedExpression[A1,T1]

class TypedExpressionConversion[A1,T1](val e: TypedExpression[_,_], bf: TypedExpressionFactory[A1,T1]) extends TypedExpression[A1,T1] {
  def value = bf.sample
  
  def mapper: OutMapper[A1] = sys.error("!")
  
  override def inhibited = e.inhibited

  override def doWrite(sw: StatementWriter)= e.doWrite((sw))

  override def children = e.children  
}

trait Floatifier[T1,A2,T2] {
  def floatify(v: TypedExpression[_,_]): TypedExpressionConversion[A2,T2]
}

trait IdentityFloatifier[A1,T1] extends Floatifier[T1,A1,T1]

trait FloatTypedExpressionFactory[A1,T1] extends TypedExpressionFactory[A1,T1] with IdentityFloatifier[A1,T1] {
  def floatify(v: TypedExpression[_,_]): TypedExpressionConversion[A1,T1] = convert(v)
}

trait TypedExpressionFactory[A,T] {

  
  def create(a: A) : TypedExpression[A,T] =
    FieldReferenceLinker.takeLastAccessedFieldReference match {
      case None =>
        new ConstantTypedExpression[A,T](a)
      case Some(n:SelectElement) =>
        new SelectElementReference[A,T](n)
    }
  
  def convert(v: TypedExpression[_,_]): TypedExpressionConversion[A,T]
  def sample: A
  def sampleB = create(sample)
}

trait IntegralTypedExpressionFactory[A1,T1,A2,T2] 
  extends TypedExpressionFactory[A1,T1] with Floatifier[T1,A2,T2] {
  
  def floatify(v: TypedExpression[_,_]): TypedExpressionConversion[A2,T2] = floatifyer.convert(v)
  def floatifyer: TypedExpressionFactory[A2,T2]
}


trait DeOptionizer[A1,T1,A2 <: Option[A1],T2] {
  self: TypedExpressionFactory[A2,T2] =>
    
  def deOptionizer: TypedExpressionFactory[A1,T1]
}

class ConcatOp[A1,A2,T1,T2](a1: TypedExpression[A1,T1], a2: TypedExpression[A2,T2]) extends BinaryOperatorNode(a1,a2, "||") {
  override def doWrite(sw: StatementWriter) =
      sw.databaseAdapter.writeConcatOperator(a1, a2, sw)   
}
