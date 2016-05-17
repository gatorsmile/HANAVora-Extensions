package org.apache.spark.sql.types

import java.sql.Date

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.unsafe.types.UTF8String


class NodeType extends UserDefinedType[Node] {

  override val sqlType = StructType(Seq(
    StructField("path", ArrayType(StringType, containsNull = false), nullable = false),
    StructField("dataType", StringType, nullable = false),
    StructField("preRank", IntegerType, nullable = true),
    StructField("postRank", IntegerType, nullable = true),
    StructField("isLeaf", BooleanType, nullable = true),
    StructField("ordPath", ArrayType(LongType, containsNull=false), nullable = true)
  ))

  override def serialize(obj: Any): Any = obj match {
    case node: Node =>
      InternalRow(new GenericArrayData(node.path.map {
        case null => null
        case p => UTF8String.fromString(p.toString)
      }),
        UTF8String.fromString(node.pathDataTypeJson),
        node.preRank,
        node.postRank,
        node.isLeaf,
        if (node.ordPath == null){
          node.ordPath
        } else {
          new GenericArrayData(node.ordPath)
        })
    case _ => throw new UnsupportedOperationException(s"Cannot serialize ${obj.getClass}")
  }

  // scalastyle:off cyclomatic.complexity
  override def deserialize(datum: Any): Node = datum match {
    case row: InternalRow => {
      val stringArray = row.getArray(0).toArray[UTF8String](StringType).map {
        case null => null
        case somethingElse => somethingElse.toString
      }
      val readDataTypeString: String = row.getString(1)
      val readDataType: DataType = DataType.fromJson(readDataTypeString)
      val path: Seq[Any] = readDataType match {
        case StringType => stringArray
        case LongType => stringArray.map(v => if (v != null) v.toLong else null)
        case IntegerType => stringArray.map(v => if (v != null) v.toInt else null)
        case DoubleType => stringArray.map(v => if (v != null) v.toDouble else null)
        case FloatType => stringArray.map(v => if (v != null) v.toFloat else null)
        case ByteType => stringArray.map(v => if (v != null) v.toByte else null)
        case BooleanType => stringArray.map(v => if (v != null) v.toBoolean else null)
        case TimestampType => stringArray.map(v => if (v != null) v.toLong else null)
        case dt: DataType => sys.error(s"Type $dt not supported for hierarchy path")
      }
      val preRank: Integer = if (row.isNullAt(2)) null else row.getInt(2)
      val postRank: Integer = if (row.isNullAt(3)) null else row.getInt(3)
      // scalastyle:off magic.number
      val isLeaf: java.lang.Boolean = if (row.isNullAt(4)) null else row.getBoolean(4)
      val ordPath: Seq[Long] = if (row.isNullAt(5)) null else row.getArray(5).toLongArray()
      // scalastyle:on magic.number
      Node(
        path,
        readDataTypeString,
        preRank,
        postRank,
        isLeaf,
        ordPath
      )
    }
    case node: Node => node
    case _ => throw new UnsupportedOperationException(s"Cannot deserialize ${datum.getClass}")
  }
  // scalastyle:on

  override def userClass: java.lang.Class[Node] = classOf[Node]
}

case object NodeType extends NodeType
